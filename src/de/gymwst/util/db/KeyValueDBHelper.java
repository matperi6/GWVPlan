package de.gymwst.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KeyValueDBHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "keyvalue";
	public static final String COLUMN_STORE = "store";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_VALUE = "value";

	private static final String DATABASE_NAME = "keyvalue.db";
	private static final int DATABASE_VERSION = 3;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ " (" + COLUMN_STORE + " text NOT NULL, " + COLUMN_KEY
			+ " text NOT NULL, " + COLUMN_VALUE + " text not null, "
			+ "PRIMARY KEY(" + COLUMN_STORE + "," + COLUMN_KEY + "));";

	public KeyValueDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(KeyValueDBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}