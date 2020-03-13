package com.example.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "Stock_DatabaseHandler";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    // DB Name
    private static final String DATABASE_NAME = "StockAppDB";
    // DB Table Name
    private static final String TABLE_NAME = "StockTable";
    ///DB Columns
    private static final String NAME = "name";
    private static final String SYMBOL = "symbol";
    private static final String PRICE = "price";
    private static final String PRICE_CHANGE = "priceChange";
    private static final String PERCENT_CHANGE = "changePercentage";

    // DB Table Create Code
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    NAME + " TEXT not null unique," +
                    SYMBOL + " TEXT not null, " +
                    PRICE + " DOUBLE not null, " +
                    PRICE_CHANGE + " DOUBLE not null, " +
                    PERCENT_CHANGE + " DOUBLE not null)";

    private SQLiteDatabase database;


    DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase(); // Inherited from SQLiteOpenHelper
        Log.d(TAG, "constructor:");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // onCreate is only called if the DB does not exist
        Log.d(TAG, "onCreate: Making New DB");
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    ArrayList<Stock> loadStocks()
    {
        // Load countries - return ArrayList of loaded countries
        Log.d(TAG, "loadCountries: START");
        ArrayList<Stock> stockList = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,  // The table to query
                new String[]{NAME, SYMBOL, PRICE, PRICE_CHANGE, PERCENT_CHANGE}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                SYMBOL +" ASC"); // The sort order

        if (cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++)
            {
                String name = cursor.getString(0);
                String symbol = cursor.getString(1);
                Double price = 0.0;
                Double priceChange = 0.0;
                Double changePercentage = 0.0;
                Stock newStock = new Stock(name,symbol,price,priceChange,changePercentage);
                stockList.add(newStock);

                cursor.moveToNext();
            }
            cursor.close();
        }
        Log.d(TAG, "loadCountries: DONE");

        return stockList;
    }

    void addStock(Stock stock) {
        ContentValues values = new ContentValues();

        values.put(NAME, stock.getName());
        values.put(SYMBOL, stock.getSymbol());
        values.put(PRICE, stock.getPrice());
        values.put(PRICE_CHANGE, stock.getPriceChange());
        values.put(PERCENT_CHANGE, stock.getChangePercentage());

        long key = database.insert(TABLE_NAME, null, values);
        Log.d(TAG, "addStock: " + key);
    }

    void updateStock(Stock stock)
    {
        ContentValues values = new ContentValues();

        values.put(NAME, stock.getName());
        values.put(SYMBOL, stock.getSymbol());
        values.put(PRICE, stock.getPrice());
        values.put(PRICE_CHANGE, stock.getPriceChange());
        values.put(PERCENT_CHANGE, stock.getChangePercentage());

        long numRows = database.update(
                TABLE_NAME, values, NAME + " = ?", new String[]{stock.getName()});
        Log.d(TAG, "updateStock: " + numRows);
    }

    void deleteStock(String name)
    {
        Log.d(TAG, "deleteStock: " + name);
        int cnt = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{name});
        Log.d(TAG, "deleteStock: " + cnt);
    }

    void shutDown() {
        database.close();
    }
}
