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
    private static final int DATABASE_VERSION = 8;
 
    // Database Name
    private static final String DATABASE_NAME = "raiffDB";
 
    // Transactions table name
    private static final String TABLE_TRANSACTIONS = "transactions";
 
    // Transactions Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DATE_TIME = "date_time";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_AMOUNT_CURR = "amount_curr";
    private static final String KEY_REMAINDER = "remainder";
    private static final String KEY_REMAINDER_CURR = "remainder_curr";
    private static final String KEY_TERMINAL = "terminal";
    private static final String KEY_CARD = "card";
    private static final String KEY_PLACE = "place";
    private static final String KEY_IN_PLACE = "in_place";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE_TIME + " TEXT,"
                + KEY_AMOUNT + " REAL," 
                + KEY_AMOUNT_CURR + " TEXT," 
                + KEY_REMAINDER + " REAL," 
                + KEY_REMAINDER_CURR + " TEXT," 
                + KEY_TERMINAL + " TEXT," 
                + KEY_CARD + " TEXT," 
                + KEY_PLACE + " TEXT," 
                + KEY_IN_PLACE + "  INTEGER" + ")";
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
        values.put(KEY_AMOUNT, t.getAmount()); 
        values.put(KEY_AMOUNT_CURR, t.getAmountCurr());
        values.put(KEY_REMAINDER, t.getRemainder()); 
        values.put(KEY_REMAINDER_CURR, t.getRemainderCurr());
        values.put(KEY_CARD, t.getCard());
        values.put(KEY_TERMINAL, t.getTerminal());
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_IN_PLACE, t.getInPlace());
 
        // Inserting Row
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single transaction entry
    TransactionEntry getTransaction(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_TRANSACTIONS, new String[] { KEY_ID, KEY_DATE_TIME, 
        		KEY_AMOUNT, KEY_AMOUNT_CURR, KEY_REMAINDER, KEY_REMAINDER_CURR, KEY_TERMINAL, KEY_CARD, KEY_PLACE, KEY_IN_PLACE }, 
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        TransactionEntry t = new TransactionEntry(cursor.getInt(0), Long.valueOf(cursor.getString(1)), 
        		cursor.getDouble(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5),
        		cursor.getString(6), cursor.getString(7), cursor.getString(8), cursor.getInt(9));
        
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
                t.setAmount(cursor.getDouble(2));
                t.setAmountCurr(cursor.getString(3));
                t.setRemainder(cursor.getDouble(4));
                t.setRemainderCurr(cursor.getString(5));
                t.setTerminal(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setPlace(cursor.getString(8));
                t.setInPlace(cursor.getInt(9));
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
        values.put(KEY_AMOUNT, t.getAmount());
        values.put(KEY_AMOUNT_CURR, t.getAmountCurr());
        values.put(KEY_REMAINDER, t.getRemainder());
        values.put(KEY_REMAINDER_CURR, t.getRemainderCurr());
        values.put(KEY_TERMINAL, t.getTerminal());
        values.put(KEY_CARD, t.getCard());
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_IN_PLACE, t.getInPlace());
 
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
                t.setAmount(cursor.getDouble(2));
                t.setAmountCurr(cursor.getString(3));
                t.setRemainder(cursor.getDouble(4));
                t.setRemainderCurr(cursor.getString(5));
                t.setTerminal(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setPlace(cursor.getString(8));
                t.setInPlace(cursor.getInt(9));
                // Adding transaction to list
                transactionList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return transaction list
        return transactionList;
    }

    public  List<TransactionEntry> getTransactionsAmountFixed(double amount){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " WHERE " + KEY_AMOUNT + " = ?";
    	return queryDB(countQuery, new String[] {String.valueOf(amount)});
    }
    
    public List<TransactionEntry> getTransactionsAmountInterval(double amountStar, double amountEnd) {
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_AMOUNT + " >= ? )" + " AND " + " ( " + KEY_AMOUNT + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME + " DESC";
    	return queryDB(countQuery, new String[] {String.valueOf(amountStar), String.valueOf(amountEnd)});
    }
    
    public List<TransactionEntry> getTransactionsDateInterval(long dateStart, long dateEnd){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME + " DESC";
    	return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    }
    
    public List<TransactionEntry> getTransactionsDateIntervalTerminal(long dateStart, long dateEnd, String terminal){
    	String countQuery;
    	if(terminal.equalsIgnoreCase("All")){
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME + " DESC";
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    	}else{
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
        			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" + 
    				" AND " + " ( " + KEY_TERMINAL + " = ? )" +
        			" ORDER BY " + KEY_DATE_TIME + " DESC";
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd), terminal});
    	}
    }
    
    public List<TransactionEntry> getTransactionsDateIntervalPlace(long dateStart, long dateEnd, String place){
    	String countQuery;
    	if(place.equalsIgnoreCase("All")){
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME + " DESC";
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    	}else{
    		countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
        			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" + 
    				" AND " + " ( " + KEY_PLACE + " = ? )" +
        			" ORDER BY " + KEY_DATE_TIME + " DESC";
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd), place});
    	}
    }
    
    public List<String> getDistinctTerminals(){
    	List<String> distVals = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	String countQuery = "SELECT DISTINCT " + KEY_TERMINAL + " FROM " + TABLE_TRANSACTIONS +
    			" ORDER BY " + KEY_TERMINAL; 
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
            do {
            	distVals.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	return distVals;
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
    
    public List<String> getUnplacedDistinctTerminals(){
    	List<String> distVals = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	String countQuery = "SELECT DISTINCT " + KEY_TERMINAL + " FROM " + TABLE_TRANSACTIONS +
    			" WHERE " + KEY_IN_PLACE + " = ? " +
    			" ORDER BY " + KEY_TERMINAL; 
    	Cursor cursor = db.rawQuery(countQuery, new String[] {String.valueOf(0)});
    	
    	if (cursor.moveToFirst()) {
            do {
            	distVals.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	return distVals;
    }
    
    public  List<TransactionEntry> getTransactionsTerminal(String terminal){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " WHERE " + KEY_TERMINAL + " = ?";
    	return queryDB(countQuery, new String[] {terminal});
    }
}
