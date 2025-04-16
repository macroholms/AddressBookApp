package com.example.adressbook;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.adressbook.DatabaseDescription.Contact;

public class AddressBookContentProvider extends ContentProvider {
    private AddressBookDatabaseHelper dbHelper;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ONE_CONTACT = 1;
    private static final int CONTACTS = 2;

    static {
        uriMatcher.addURI(DatabaseDescription.AUTHORITY,
                DatabaseDescription.Contact.TABLE_NAME, CONTACTS);
        uriMatcher.addURI(DatabaseDescription.AUTHORITY,
                DatabaseDescription.Contact.TABLE_NAME + "/#", ONE_CONTACT);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new AddressBookDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {return null;}

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Contact.TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                break;
            case ONE_CONTACT:
                queryBuilder.appendWhere(Contact._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_query_uri) + uri
                );
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection,selection,selectionArgs,null,null,sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newContactUri = null;
        switch (uriMatcher.match(uri)) {
            case CONTACTS:
                long rowId = dbHelper.getWritableDatabase().insert(
                        Contact.TABLE_NAME,
                        null,
                        values);
                if (rowId > 0) {
                    newContactUri = Contact.buildContactUri(rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                } else {
                    throw new SQLException(
                            getContext().getString(R.string.insert_failed) + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_insert_uri) + uri);
        }
        return newContactUri;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int numberOfRowsUpdated;
        switch (uriMatcher.match(uri)) {
            case ONE_CONTACT:
                String id = uri.getLastPathSegment();
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
                        Contact.TABLE_NAME,
                        values,
                        Contact._ID + "=" + id,
                    selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_update_uri) + uri);
        }
        if (numberOfRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;
        switch (uriMatcher.match(uri)) {
            case ONE_CONTACT:
                String id = uri.getLastPathSegment();
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
                        Contact.TABLE_NAME,
                        Contact._ID + "=" + id,
                    selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.invalid_delete_uri) + uri);
        }
        if (numberOfRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsDeleted;
    }
}
