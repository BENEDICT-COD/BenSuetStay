package com.mansuetdev.bensuetstay

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Offline mode: a local SQLite "outbox" of actions taken without internet (viewing bookings
 * and room applications). SyncManager sends them to Firestore once the phone reconnects.
 */
class OfflineStore private constructor(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    data class Item(
        val id: String,
        val type: String,
        val ownerId: String,
        val listingId: String,
        val payload: String,
        val attempts: Int
    )

    companion object {
        private const val DB_NAME = "bensuet_offline.db"
        private const val DB_VERSION = 1
        private const val TABLE = "outbox"
        const val TYPE_BOOKING = "booking"
        const val TYPE_APPLICATION = "application"

        @Volatile private var instance: OfflineStore? = null

        fun get(context: Context): OfflineStore =
            instance ?: synchronized(this) {
                instance ?: OfflineStore(context.applicationContext).also { instance = it }
            }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE (
                id TEXT PRIMARY KEY,
                type TEXT NOT NULL,
                owner_id TEXT NOT NULL,
                listing_id TEXT NOT NULL,
                payload TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                attempts INTEGER NOT NULL DEFAULT 0,
                last_error TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE")
        onCreate(db)
    }

    fun add(type: String, ownerId: String, listingId: String, id: String, payload: String) {
        val values = ContentValues().apply {
            put("id", id)
            put("type", type)
            put("owner_id", ownerId)
            put("listing_id", listingId)
            put("payload", payload)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /** Queued items for one user, oldest first, optionally of one type. */
    fun pending(ownerId: String, type: String? = null): List<Item> {
        val selection = if (type == null) "owner_id = ?" else "owner_id = ? AND type = ?"
        val args = if (type == null) arrayOf(ownerId) else arrayOf(ownerId, type)
        return readableDatabase.query(TABLE, null, selection, args, null, null, "created_at ASC").use { c ->
            val items = mutableListOf<Item>()
            while (c.moveToNext()) {
                items.add(
                    Item(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        type = c.getString(c.getColumnIndexOrThrow("type")),
                        ownerId = c.getString(c.getColumnIndexOrThrow("owner_id")),
                        listingId = c.getString(c.getColumnIndexOrThrow("listing_id")),
                        payload = c.getString(c.getColumnIndexOrThrow("payload")),
                        attempts = c.getInt(c.getColumnIndexOrThrow("attempts"))
                    )
                )
            }
            items
        }
    }

    fun has(ownerId: String, type: String, listingId: String): Boolean =
        DatabaseUtils.queryNumEntries(
            readableDatabase, TABLE, "owner_id = ? AND type = ? AND listing_id = ?", arrayOf(ownerId, type, listingId)
        ) > 0

    fun clear(ownerId: String) {
        writableDatabase.delete(TABLE, "owner_id = ?", arrayOf(ownerId))
    }

    fun remove(id: String) {
        writableDatabase.delete(TABLE, "id = ?", arrayOf(id))
    }

    /** Records a failed send and returns how many times this item has now failed. */
    fun recordFailure(id: String, error: String?): Int {
        writableDatabase.execSQL("UPDATE $TABLE SET attempts = attempts + 1, last_error = ? WHERE id = ?", arrayOf(error, id))
        return readableDatabase.rawQuery("SELECT attempts FROM $TABLE WHERE id = ?", arrayOf(id)).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }
}
