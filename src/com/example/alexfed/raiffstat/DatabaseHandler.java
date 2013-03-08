package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 5;
 
    // Database Name
    private static final String DATABASE_NAME = "raiffDB";
 
    // Transactions table name
    private static final String TABLE_TRANSACTIONS = "transactions";
 
    // Transactions Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DATE_TIME = "date_time";
    private static final String KEY_AMMOUNT = "ammount";
    private static final String KEY_AMMOUNT_CURR = "ammount_curr";
    private static final String KEY_REMAINDER = "remainder";
    private static final String KEY_REMAINDER_CURR = "remainder_curr";
    private static final String KEY_PLACE = "place";
    private static final String KEY_CARD = "card";
    private static final String KEY_GROUP = "grp";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE_TIME + " TEXT,"
                + KEY_AMMOUNT + " REAL," 
                + KEY_AMMOUNT_CURR + " TEXT," 
                + KEY_REMAINDER + " REAL," 
                + KEY_REMAINDER_CURR + " TEXT," 
                + KEY_PLACE + " TEXT," 
                + KEY_CARD + " TEXT," 
                + KEY_GROUP + " TEXT" + ")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new transaction
    void addTransaction(TransactionEntry t) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_TIME, String.valueOf(t.getDateTime()) );
        values.put(KEY_AMMOUNT, t.getAmmount()); 
        values.put(KEY_AMMOUNT_CURR, t.getAmmountCurr());
        values.put(KEY_REMAINDER, t.getRemainder()); 
        values.put(KEY_REMAINDER_CURR, t.getRemainderCurr());
        values.put(KEY_CARD, t.getCard());
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_GROUP, t.getGroup());
 
        // Inserting Row
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single transaction entry
    TransactionEntry getTransaction(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_TRANSACTIONS, new String[] { KEY_ID, KEY_DATE_TIME, 
        		KEY_AMMOUNT, KEY_AMMOUNT_CURR, KEY_REMAINDER, KEY_REMAINDER_CURR, KEY_PLACE, KEY_CARD, KEY_GROUP }, 
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        TransactionEntry t = new TransactionEntry(cursor.getInt(0), Long.valueOf(cursor.getString(1)), 
        		cursor.getDouble(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5),
        		cursor.getString(6), cursor.getString(7), cursor.getString(8));
        
        cursor.close();
        // return transaction
        return t;
    }
 
    // Getting All transactions
    public List<TransactionEntry> getAllTransactions() {
        List<TransactionEntry> transactionList = new ArrayList<TransactionEntry>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	TransactionEntry t = new TransactionEntry(); 
                t.setID(cursor.getInt(0));
                t.setDateTime(Long.valueOf(cursor.getString(1)) );
                t.setAmmount(cursor.getDouble(2));
                t.setAmmountCurr(cursor.getString(3));
                t.setRemainder(cursor.getDouble(4));
                t.setRemainderCurr(cursor.getString(5));
                t.setPlace(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setGroup(cursor.getString(8));
                // Adding transaction to list
                transactionList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return transaction list
        return transactionList;
    }
 
    // Updating single transaction entry
    public int updateTransaction(TransactionEntry t) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_TIME, String.valueOf(t.getDateTime())); 
        values.put(KEY_AMMOUNT, t.getAmmount());
        values.put(KEY_AMMOUNT_CURR, t.getAmmountCurr());
        values.put(KEY_REMAINDER, t.getRemainder());
        values.put(KEY_REMAINDER_CURR, t.getRemainderCurr());
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_CARD, t.getCard());
        values.put(KEY_GROUP, t.getGroup());
 
        // updating row
        return db.update(TABLE_TRANSACTIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(t.getID()) });
    }
 
    // Deleting single transaction entry
    public void deleteTransaction(TransactionEntry t) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(t.getID()) });
        db.close();
    }
 
    // Getting transactions Count
    public int getTransactionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();
 
        // return count
        return ret;
    }
 
    public void clearAll(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.execSQL("DELETE FROM " + TABLE_TRANSACTIONS + ";");
    }
    
    private List<TransactionEntry> queryDB(String countQuery, String[] args){
    	List<TransactionEntry> transactionList = new ArrayList<TransactionEntry>();
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.rawQuery(countQuery, args);
    	
    	// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	TransactionEntry t = new TransactionEntry(); 
                t.setID(cursor.getInt(0));
                t.setDateTime(Long.valueOf(cursor.getString(1)) );
                t.setAmmount(cursor.getDouble(2));
                t.setAmmountCurr(cursor.getString(3));
                t.setRemainder(cursor.getDouble(4));
                t.setRemainderCurr(cursor.getString(5));
                t.setPlace(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setGroup(cursor.getString(8));
                // Adding transaction to list
                transactionList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return transaction list
        return transactionList;
    }

    public  List<TransactionEntry> getTransactionsAmountFixed(double ammount){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " WHERE " + KEY_AMMOUNT + " = ?";
    	return queryDB(countQuery, new String[] {String.valueOf(ammount)});
    }
    
    public List<TransactionEntry> getTransactionsAmountInterval(double ammountStar, double ammountEnd) {
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_AMMOUNT + " >= ? )" + " AND " + " ( " + KEY_AMMOUNT + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME;
    	return queryDB(countQuery, new String[] {String.valueOf(ammountStar), String.valueOf(ammountEnd)});
    }
    
    public List<TransactionEntry> getTransactionsDateInterval(long dateStart, long dateEnd){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME;
    	return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    }
    
    public List<TransactionEntry> getTransactionsDateIntervalPlace(long dateStart, long dateEnd, String place){
    	String countQuery;
    	if(place.equalsIgnoreCase("All")){
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME;
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    	}else{
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
        			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" + 
    				" AND " + " ( " + KEY_PLACE + " = ? )" +
        			" ORDER BY " + KEY_DATE_TIME;
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd), place});
    	}
    }
    
    public List<TransactionEntry> getTransactionsDateIntervalGroup(long dateStart, long dateEnd, String group){
    	String countQuery;
    	if(group.equalsIgnoreCase("All")){
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME;
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    	}else{
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
        			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" + 
    				" AND " + " ( " + KEY_GROUP + " = ? )" +
        			" ORDER BY " + KEY_DATE_TIME;
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd), group});
    	}
    }
    
    public List<String> getDistinctPlaces(){
    	List<String> distVals = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	String countQuery = "SELECT DISTINCT " + KEY_PLACE + " FROM " + TABLE_TRANSACTIONS +
    			" ORDER BY " + KEY_PLACE; 
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
            do {
            	distVals.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	return distVals;
    }
    
    public List<String> getDistinctGroups(){
    	List<String> distVals = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	String countQuery = "SELECT DISTINCT " + KEY_GROUP + " FROM " + TABLE_TRANSACTIONS +
    			" ORDER BY " + KEY_GROUP; 
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
            do {
            	distVals.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	return distVals;
    }
    
    public long getMinDate(){
    	long date = 0;
    	SQLiteDatabase db = this.getWritableDatabase();
    	String countQuery = "SELECT  MIN(" + KEY_DATE_TIME + ") FROM " + TABLE_TRANSACTIONS;
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
    		date = Long.valueOf(cursor.getString(0));
        }
    	cursor.close();
    	return date;
    }
}
