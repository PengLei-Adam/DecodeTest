package com.example.decodetest.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.decodetest.Constants;

/**
 * Created by leip on 2016/7/1.
 */
public class MediaProvider extends ContentProvider {

    private static final String TAG = "MediaProvider";

    private static final String CONTENT_URI_HOST =
            "content://" + Constants.COMPANY_PATH + ".mediaprovider/";
    public static final Uri CONTENT_URI_PHOTOS = Uri.parse(CONTENT_URI_HOST + "photos");
    public static final Uri CONTENT_URI_VIDEOS = Uri.parse(CONTENT_URI_HOST + "videos");

    //raw names
    public static final String KEY_ID = "_id";
    public static final String KEY_CREATED_TIME = "created_time";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_SIZE = "size";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";


    MediaDatabaseHelper dbHelper;
    @Override
    public boolean onCreate() {
        dbHelper = new MediaDatabaseHelper(getContext(), MediaDatabaseHelper.DATABASE_NAME,
                null, MediaDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    //UriMatcher deal with different requests
    private static final int PHOTOS = 1;
    private static final int PHOTO_ID = 2;
    private static final int SEARCH = 3;
    private static final int VIDEOS = 4;
    private static final int VIDEO_ID = 5;

    private static final UriMatcher matcher;
    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Constants.COMPANY_PATH + ".mediaprovider", "photos", PHOTOS);
        matcher.addURI(Constants.COMPANY_PATH + ".mediaprovider", "photos/#", PHOTO_ID);
        matcher.addURI(Constants.COMPANY_PATH + ".mediaprovider", "videos", VIDEOS);
        matcher.addURI(Constants.COMPANY_PATH + ".mediaprovider", "videos/#", VIDEO_ID);
        //search

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = null;
        try{
            database = dbHelper.getWritableDatabase();
        } catch (SQLiteException e){

        }
        if(database == null) return null;

        long rowID = 0;
        switch(matcher.match(uri)){
            case PHOTOS:
                rowID = database.insert(MediaDatabaseHelper.PHOTO_TABLE, "photo", values);
                if(rowID > 0){
                    Uri uriRe = ContentUris.withAppendedId(CONTENT_URI_PHOTOS, rowID);
                    getContext().getContentResolver().notifyChange(uriRe, null);
                    return uriRe;
                }
                break;
            case VIDEOS:
                rowID = database.insert(MediaDatabaseHelper.VIDEO_TABLE, "video", values);
                if(rowID > 0){
                    Uri uriRe = ContentUris.withAppendedId(CONTENT_URI_VIDEOS, rowID);
                    getContext().getContentResolver().notifyChange(uriRe, null);
                    return uriRe;
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        throw new SQLiteException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = null;
        try{
            database = dbHelper.getWritableDatabase();
        } catch (SQLiteException e){

        }
        if(database == null) return 0;

        int count;
        switch (matcher.match(uri)){
            case PHOTOS:
                count = database.delete(MediaDatabaseHelper.PHOTO_TABLE, selection, selectionArgs);
                break;
            case PHOTO_ID:
                String segmentP = uri.getPathSegments().get(1);
                count = database.delete(MediaDatabaseHelper.PHOTO_TABLE,
                        KEY_ID + "=" + segmentP
                        + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case VIDEOS:
                count = database.delete(MediaDatabaseHelper.VIDEO_TABLE, selection, selectionArgs);
                break;
            case VIDEO_ID:
                String segmentV = uri.getPathSegments().get(1);
                count = database.delete(MediaDatabaseHelper.VIDEO_TABLE,
                        KEY_ID + "=" + segmentV
                                + (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = null;
        try{
            database = dbHelper.getWritableDatabase();
        } catch (SQLiteException e){

        }
        if(database == null) return 0;

        int count;
        switch (matcher.match(uri)){
            case PHOTOS:
                count = database.update(MediaDatabaseHelper.PHOTO_TABLE, values, selection, selectionArgs);
                break;
            case PHOTO_ID:
                String segmentP = uri.getPathSegments().get(1);
                count = database.update(MediaDatabaseHelper.PHOTO_TABLE, values,
                        KEY_ID + "=" + segmentP
                        + (!TextUtils.isEmpty(selection)? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            case VIDEOS:
                count = database.update(MediaDatabaseHelper.PHOTO_TABLE, values, selection, selectionArgs);
                break;
            case VIDEO_ID:
                String segmentV = uri.getPathSegments().get(1);
                count = database.update(MediaDatabaseHelper.PHOTO_TABLE, values,
                        KEY_ID + "=" + segmentV
                                + (!TextUtils.isEmpty(selection)? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)){
            case PHOTOS: return "vnd.android.cursor.dir/vnd." + Constants.COMPANY_NAME + ".photo";
            case PHOTO_ID:  return "vnd.android.cursor.item/vnd." + Constants.COMPANY_NAME + ".photo";
            case VIDEOS: return "vnd.android.cursor.dir/vnd." + Constants.COMPANY_NAME + ".video";
            case VIDEO_ID: return "vnd.android.cursor.item/vnd." + Constants.COMPANY_NAME + ".video";
            //TODO:other cases

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = null;
        try{
            database = dbHelper.getWritableDatabase();
        }catch (SQLiteException e){
            database = dbHelper.getReadableDatabase();
        }

        if(database == null) return null;

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int matchId = matcher.match(uri);
        Log.d(TAG, "matches " + matchId);
        switch(matchId){
            case PHOTO_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
            case PHOTOS:
                qb.setTables(MediaDatabaseHelper.PHOTO_TABLE);
                Log.d(TAG, "set table : photos");
                break;
            case VIDEO_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
            case VIDEOS:
                qb.setTables(MediaDatabaseHelper.VIDEO_TABLE);
                Log.d(TAG, "set table : videos");
                break;
            //other cases

            default:
                return null;
        }

        String orderBy = TextUtils.isEmpty(sortOrder)? KEY_CREATED_TIME : sortOrder;

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    //SQLite Database on basis
    protected static class MediaDatabaseHelper extends SQLiteOpenHelper{
        private static final String TAG = "MediaDBHelper";

        private static final String DATABASE_NAME = "scanmedia.db";
        private static final int DATABASE_VERSION = 1;
        //tables to create for photos and videos
        private static final String PHOTO_TABLE = "photos";
        private static final String VIDEO_TABLE = "videos";

        private static final String CREATE_TABLE_PHOTO =
                "create table " + PHOTO_TABLE + " ("
                + KEY_ID + " integer primary key autoincrement, "
                + KEY_CREATED_TIME + " LONG, "    //TODO:use date type
                + KEY_FILENAME + " TEXT, "
                + KEY_SIZE + " LONG, "      //size in byte
                + KEY_WIDTH + " INTEGER, "
                + KEY_HEIGHT + " INTEGER);";
        private static final String CREATE_TABLE_VIDEO =
                "create table " + VIDEO_TABLE + " ("
                        + KEY_ID + " integer primary key autoincrement, "
                        + KEY_CREATED_TIME + " LONG, "    //TODO:use date type
                        + KEY_FILENAME + " TEXT, "
                        + KEY_SIZE + " LONG, "      //size in byte
                        + KEY_WIDTH + " INTEGER, "
                        + KEY_HEIGHT + " INTEGER);";

        //Database
        private SQLiteDatabase mediaDB;

        public MediaDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_PHOTO);
            db.execSQL(CREATE_TABLE_VIDEO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + PHOTO_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + VIDEO_TABLE);
            onCreate(db);
        }
    }
}
