package com.zujihu.clothes.data;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.os.Environment;

import com.zujihu.clothes.common.Constant;

public abstract class SDSQLiteOpenHelper {

	private final String		mName;
	private final CursorFactory	mFactory;
	private final int			mNewVersion;

	private SQLiteDatabase		mDatabase		= null;
	private boolean				mIsInitializing	= false;

	public SDSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		if (version < 1)
			throw new IllegalArgumentException("Version must be >= 1, was " + version);

		mName = name;
		mFactory = factory;
		mNewVersion = version;
	}

	public synchronized SQLiteDatabase getWritableDatabase() {
		if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
			return mDatabase; // The database is already open
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getWritableDatabase");
		}

		boolean success = false;
		SQLiteDatabase db = null;
		try {
			mIsInitializing = true;
			if (mName == null) {
				db = SQLiteDatabase.create(null);
			}
			else {
				String path = getDatabasePath(mName).getPath();
				db = SQLiteDatabase.openOrCreateDatabase(path, mFactory);
			}

			int version = db.getVersion();
			if (version != mNewVersion) {
				db.beginTransaction();
				try {
					if (version == 0) {
						onCreate(db);
					}
					else {
						onUpgrade(db, version, mNewVersion);
					}
					db.setVersion(mNewVersion);
					db.setTransactionSuccessful();
				}
				finally {
					db.endTransaction();
				}
			}

			onOpen(db);
			success = true;
			return db;
		}
		finally {
			mIsInitializing = false;
			if (success) {
				if (mDatabase != null) {
					try {
						mDatabase.close();
					}
					catch (Exception e) {
					}
				}
				mDatabase = db;
			}
			else {
				if (db != null)
					db.close();
			}
		}
	}

	public synchronized SQLiteDatabase getReadableDatabase() {
		if (mDatabase != null && mDatabase.isOpen()) {
			return mDatabase;
		}

		if (mIsInitializing) {
			throw new IllegalStateException("getReadableDatabase");
		}

		try {
			return getWritableDatabase();
		}
		catch (SQLiteException e) {
			if (mName == null)
				throw e;
		}

		SQLiteDatabase db = null;
		try {
			mIsInitializing = true;
			String path = getDatabasePath(mName).getPath();
			db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READWRITE);
			if (db.getVersion() != mNewVersion) {
				throw new SQLiteException("upgrade error or exception version" + db.getVersion()
						+ " to " + mNewVersion + ": " + path);
			}
			onOpen(db);
			mDatabase = db;
			return mDatabase;
		}
		finally {
			mIsInitializing = false;
			if (db != null && db != mDatabase)
				db.close();
		}
	}

	public synchronized void close() {
		if (mIsInitializing)
			throw new IllegalStateException("Closed");

		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	public File getDatabasePath(String name) {
		String EXTERN_PATH = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == true) {
			EXTERN_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
					+ Constant.DB_PATH;
			File file = new File(EXTERN_PATH);
			if (!file.exists()) {
				file.mkdirs();
			}
		}
		return new File(EXTERN_PATH + name);
	}

	public abstract void onCreate(SQLiteDatabase db);

	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

	public void onOpen(SQLiteDatabase db) {
	}
}
