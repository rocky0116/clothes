package com.zujihu.clothes.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.zujihu.clothes.util.Utils;

public class DatabaseHelper extends SDSQLiteOpenHelper {
	private static final String	TABLE_NAME_SUITS	= "suits";
	private static final String	TABLE_NAME_ITEMS	= "items";
	private static final String	DATABASE_NAME		= "clothes.db";
	private static final int	DATABASE_VERSION	= 1;

	private static final String	KEY_ID				= "_id";
	private static final String	KEY_URL				= "url";
	private static final String	KEY_DESC			= "desc";
	private static final String	KEY_DATA			= "data";
	private static final String	KEY_TYPE			= "type";
	private static final String	KEY_MASK			= "mask_path";	// type==0 --->upper
																	// outer,type==1
																	// --->pants,type==2
																	// --->skirt,type==3
																	// --->shoes,type==4
																	// --->bags,type==5
																	// --->decorations

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	public void createTableSuits() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_SUITS + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_URL + " TEXT," + KEY_DESC + " TEXT,"
				+ KEY_DATA + " VARBINARY" + ")";
		try {
			db.execSQL(sql);
			db.close();
		}
		catch (SQLException ex) {
			db.close();
		}
	}

	public void addSuit(PictureModel pictureModel) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_URL, pictureModel.url);
			db.insert(TABLE_NAME_SUITS, null, values);
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
	}

	public void delSuitById(PictureModel pictureModel) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.delete(TABLE_NAME_SUITS, KEY_ID + " = ?",
				new String[] { String.valueOf(pictureModel._id) });
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
	}

	public int updateSuit(PictureModel pictureModel) {

		SQLiteDatabase db = this.getWritableDatabase();
		int updateC = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_URL, pictureModel.url);

			updateC = db.update(TABLE_NAME_SUITS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(pictureModel._id) });
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
		return updateC;
	}

	public PictureModel searchSuitById(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		PictureModel pictureModel = null;
		try {
			Cursor cursor = db.query(TABLE_NAME_SUITS, new String[] { KEY_ID, KEY_URL, KEY_DESC,
					KEY_DATA }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null,
				null, null);
			if (cursor != null)
				cursor.moveToFirst();

			pictureModel = new PictureModel(cursor.getInt(0), cursor.getString(1));
			cursor.close();
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

		return pictureModel;
	}

	public List<PictureModel> getAllSuit() {
		List<PictureModel> pictureModelList = new ArrayList<PictureModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_NAME_SUITS + " ORDER BY " + KEY_ID + " DESC";

		SQLiteDatabase db = this.getReadableDatabase();
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					PictureModel model = new PictureModel();
					model._id = Integer.parseInt(cursor.getString(0));
					model.url = cursor.getString(1);
					model.bitmap = Utils.getSdcardImage(model.url.split("\\|")[0]);
					pictureModelList.add(model);
				}
			}
			cursor.close();
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

		return pictureModelList;
	}

	public void dropTableSuit() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_SUITS;
		try {
			db.execSQL(sql);
			db.close();
		}
		catch (SQLException ex) {
			db.close();
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////

	public void createTableItems() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ITEMS + "(" + KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TYPE + " INTEGER," + KEY_URL
				+ " TEXT," + KEY_DESC + " TEXT," + KEY_MASK + " TEXT)";
		try {
			db.execSQL(sql);
			db.close();
		}
		catch (SQLException ex) {
			db.close();
		}
	}

	public void addItem(PictureModel pictureModel) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_TYPE, pictureModel.type);
			values.put(KEY_URL, pictureModel.url);
			values.put(KEY_DESC, pictureModel.desc);
			values.put(KEY_MASK, pictureModel.mask_path);
			db.insert(TABLE_NAME_ITEMS, null, values);
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

	}

	public void delItemById(PictureModel pictureModel) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			db.delete(TABLE_NAME_ITEMS, KEY_ID + " = ?",
				new String[] { String.valueOf(pictureModel._id) });
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
	}

	public void deleteItemsByIds(String ids) {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "delete from items where " + KEY_ID + " in (" + ids + ")";
		try {
			db.rawQuery(sql, null);
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
	}

	public int updateItem(PictureModel pictureModel) {

		SQLiteDatabase db = this.getWritableDatabase();
		int updateC = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_URL, pictureModel.url);

			updateC = db.update(TABLE_NAME_ITEMS, values, KEY_ID + " = ?",
				new String[] { String.valueOf(pictureModel._id) });
			db.close();
		}
		catch (Exception e) {
			db.close();
		}
		return updateC;
	}

	public PictureModel searchItemById(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		PictureModel pictureModel = null;
		try {
			Cursor cursor = db.query(TABLE_NAME_ITEMS, new String[] { KEY_ID, KEY_TYPE, KEY_URL,
					KEY_DESC, KEY_MASK }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null,
				null, null, null);
			if (cursor != null)
				cursor.moveToFirst();

			pictureModel = new PictureModel(cursor.getInt(0), cursor.getInt(1),
				cursor.getString(2), null, cursor.getString(3), cursor.getString(4));
			cursor.close();
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

		return pictureModel;
	}

	public List<PictureModel> getItemsByType(int type) {
		List<PictureModel> pictureModelList = new ArrayList<PictureModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_NAME_ITEMS + " WHERE " + KEY_TYPE + "="
				+ type + " ORDER BY " + KEY_ID + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor != null) {
				while (cursor.moveToNext()) {
					PictureModel model = new PictureModel();
					model._id = cursor.getInt(0);
					model.type = cursor.getInt(1);
					model.url = cursor.getString(2);
					model.bitmap = Utils.getSdcardImage(model.url);
					model.desc = cursor.getString(3);
					model.mask_path = cursor.getString(4);
					pictureModelList.add(model);
				}
			}
			cursor.close();
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

		return pictureModelList;
	}

	public List<PictureModel> getAllItems() {
		List<PictureModel> pictureModelList = new ArrayList<PictureModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_NAME_ITEMS + " ORDER BY " + KEY_ID + " DESC";

		SQLiteDatabase db = this.getReadableDatabase();
		try {
			Cursor cursor = db.rawQuery(selectQuery, null);

			if (cursor != null) {
				while (cursor.moveToNext()) {
					PictureModel model = new PictureModel();
					model._id = Integer.parseInt(cursor.getString(0));
					model.url = cursor.getString(1);
					model.bitmap = Utils.getSdcardImage(model.url);
					pictureModelList.add(model);
				}
			}
			cursor.close();
			db.close();
		}
		catch (Exception e) {
			db.close();
		}

		return pictureModelList;
	}

	public void dropTableItem() {
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME_ITEMS;
		try {
			db.execSQL(sql);
			db.close();
		}
		catch (SQLException ex) {
			db.close();
		}
	}

}
