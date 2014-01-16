package de.gymwst.util.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class KeyValueDataSource {

	private final static String[] allColumns = { KeyValueDBHelper.COLUMN_STORE,
			KeyValueDBHelper.COLUMN_KEY, KeyValueDBHelper.COLUMN_VALUE };

	// Database fields
	private SQLiteDatabase database;
	private KeyValueDBHelper dbHelper;
	private String storeName;

	public KeyValueDataSource(Context context, String storeName) {
		this.storeName = storeName;
		this.dbHelper = new KeyValueDBHelper(context);
	}

	public synchronized void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public synchronized void close() {
		dbHelper.close();
	}

	public synchronized void createKeyValue(KeyValue keyValue) {
		ContentValues values = new ContentValues();
		values.put(KeyValueDBHelper.COLUMN_KEY, keyValue.getKey());
		values.put(KeyValueDBHelper.COLUMN_STORE, storeName);
		values.put(KeyValueDBHelper.COLUMN_VALUE, keyValue.getValue());
		int cnt = database.update(KeyValueDBHelper.TABLE_NAME, values,
				"store=? AND key=?",
				new String[] { storeName, keyValue.getKey() });
		if (cnt == 0) {
			database.insert(KeyValueDBHelper.TABLE_NAME, null, values);
		}
	}

	public synchronized void deleteKeyValue(KeyValue keyValue) {
		String key = keyValue.getKey();
		System.out.println("Comment deleted with key: " + key);
		database.delete(KeyValueDBHelper.TABLE_NAME, "store=? AND key=?",
				new String[] { storeName, key });
	}

	public synchronized KeyValue findKeyValueByKey(String key) {
		KeyValue result = null;
		Cursor cursor = database.query(KeyValueDBHelper.TABLE_NAME, allColumns,
				"store=? AND key=?", new String[] { storeName, key }, null,
				null, null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			result = cursorToKeyValue(cursor);
		}
		// Make sure to close the cursor
		cursor.close();
		return result;
	}

	public synchronized Map<String, KeyValue> getAllKeyValues() {
		Map<String, KeyValue> keyValues = new HashMap<String, KeyValue>();
		Cursor cursor = database.query(KeyValueDBHelper.TABLE_NAME, allColumns,
				"store=?", new String[] { storeName }, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			KeyValue keyValue = cursorToKeyValue(cursor);
			keyValues.put(keyValue.getKey(), keyValue);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return keyValues;
	}

	private KeyValue cursorToKeyValue(Cursor cursor) {
		KeyValue keyValue = new KeyValue();
		keyValue.setKey(cursor.getString(1));
		keyValue.setValue(cursor.getString(2));
		return keyValue;
	}

}
