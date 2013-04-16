package com.example.alexfed.raiffstat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String LOG = "DatabaseHandler";
	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 13;
 
    // Database Name
    private static final String DATABASE_NAME = "raiffDB";
 
    // Transactions table name
    private static final String TABLE_TRANSACTIONS = "transactions";
 
    // Categories table name
    private static final String TABLE_CATEGORIES = "categories";
    
    private Context context;
    
    // Transactions Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DATE_TIME = "date_time";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_AMOUNT_CURR = "amount_curr";
    private static final String KEY_REMAINDER = "remainder";
    private static final String KEY_REMAINDER_CURR = "remainder_curr";
    private static final String KEY_PLACE = "place";
    private static final String KEY_CARD = "card";
    private static final String KEY_TYPE = "type";
    private static final String KEY_EXP_CATEGORY = "exp_category";
    
    // Categories Table Columns names
    private static final String CAT_KEY_ID = "id";
    private static final String CAT_KEY_NAME = "name";
    private static final String CAT_KEY_COLOR = "color";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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
                + KEY_PLACE + " TEXT," 
                + KEY_CARD + " TEXT," 
                + KEY_TYPE + "  INTEGER," 
                + KEY_EXP_CATEGORY + "  INTEGER" +")";
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        
        createDefaultCategoriesTable(db);   
    }
 
    private void createDefaultCategoriesTable(SQLiteDatabase db){
    	String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                + CAT_KEY_ID + " INTEGER PRIMARY KEY," + CAT_KEY_NAME + " TEXT,"
                + CAT_KEY_COLOR + " INTEGER" +")";
        db.execSQL(CREATE_CATEGORIES_TABLE);
        
        //Add some default values
        ContentValues values = new ContentValues();
        values.put(CAT_KEY_NAME, context.getResources().getString(R.string.category_food));
        values.put(CAT_KEY_COLOR, Color.GREEN); 
        db.insert(TABLE_CATEGORIES, null, values);
        
        values.clear(); 
        values.put(CAT_KEY_NAME, context.getResources().getString(R.string.category_health));
        values.put(CAT_KEY_COLOR, Color.BLUE); 
        db.insert(TABLE_CATEGORIES, null, values);
        
        values.clear();
        values.put(CAT_KEY_NAME, context.getResources().getString(R.string.category_clothes));
        values.put(CAT_KEY_COLOR, Color.RED); 
        db.insert(TABLE_CATEGORIES, null, values);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
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
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_TYPE, t.getType());
        values.put(KEY_EXP_CATEGORY, t.getExpCategory());
 
        // Inserting Row
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close(); // Closing database connection
    }
    
    // Adding new category
    void addCategory(CategoryEntry c) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(CAT_KEY_NAME, c.getName());
        values.put(CAT_KEY_COLOR, c.getColor()); 
 
        // Inserting Row
        db.insert(TABLE_CATEGORIES, null, values);
        db.close(); // Closing database connection
    }
 
 // Merging new transaction
    void mergeTransaction(TransactionEntry t) {
        
        if(transactionExistsInDB(t)){
        	//Do nothing
        	return;
        }else{
        	List<TransactionEntry> trs = this.getCatigorizedTransactionsPlaceFixed(t.getPlace());
        	if(trs.size()>0){
        		t.setExpCategory(trs.get(0).getExpCategory());
        	}
        	
        	//Create new transaction
        	SQLiteDatabase db = this.getWritableDatabase();
	        ContentValues values = new ContentValues();
	        values.put(KEY_DATE_TIME, String.valueOf(t.getDateTime()) );
	        values.put(KEY_AMOUNT, t.getAmount()); 
	        values.put(KEY_AMOUNT_CURR, t.getAmountCurr());
	        values.put(KEY_REMAINDER, t.getRemainder()); 
	        values.put(KEY_REMAINDER_CURR, t.getRemainderCurr());
	        values.put(KEY_CARD, t.getCard());
	        values.put(KEY_PLACE, t.getPlace());
	        values.put(KEY_TYPE, t.getType());
	        values.put(KEY_EXP_CATEGORY, t.getExpCategory());
	        // Inserting Row
	        db.insert(TABLE_TRANSACTIONS, null, values);
	        db.close(); // Closing database connection
	        return;
        }
    }
    
    private boolean transactionExistsInDB(TransactionEntry t){
    	
    	SQLiteDatabase db = this.getReadableDatabase();
    	boolean ret = false;
    	
        Cursor cursor = db.query(TABLE_TRANSACTIONS, new String[] { KEY_ID, KEY_DATE_TIME, 
        		KEY_AMOUNT, KEY_AMOUNT_CURR, KEY_REMAINDER, KEY_REMAINDER_CURR, KEY_PLACE, 
        		KEY_CARD, KEY_TYPE, KEY_EXP_CATEGORY }, 
                "( " + KEY_DATE_TIME + "=? ) " + " AND " +
                "( " + KEY_AMOUNT + "=? ) " + " AND " +
                "( " + KEY_AMOUNT_CURR + "=? ) " + " AND " +
                "( " + KEY_REMAINDER + "=? ) " + " AND " +
                "( " + KEY_REMAINDER_CURR + "=? ) " + " AND " +
                "( " + KEY_PLACE + "=? ) " + " AND " +
                "( " + KEY_CARD + "=? ) " + " AND " +
                "( " + KEY_TYPE + "=? ) ",
                new String[] { 
        		String.valueOf(t.getDateTime()),
        		String.valueOf(t.getAmount()),
        		t.getAmountCurr(),
        		String.valueOf(t.getRemainder()),
        		t.getRemainderCurr(),
        		t.getPlace(),
        		t.getCard(),
        		String.valueOf(t.getType())}, null, null, null, null);

        if(cursor.getCount() > 0)
        	ret = true;
        else
        	ret = false;
        
        cursor.close();
        db.close();
        return ret;
    }
    
    // Getting single transaction entry
    TransactionEntry getTransaction(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_TRANSACTIONS, new String[] { KEY_ID, KEY_DATE_TIME, 
        		KEY_AMOUNT, KEY_AMOUNT_CURR, KEY_REMAINDER, KEY_REMAINDER_CURR, KEY_PLACE, 
        		KEY_CARD, KEY_TYPE, KEY_EXP_CATEGORY }, 
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        TransactionEntry t = new TransactionEntry(cursor.getInt(0), Long.valueOf(cursor.getString(1)), 
        		cursor.getDouble(2), cursor.getString(3), cursor.getDouble(4), cursor.getString(5),
        		cursor.getString(6), cursor.getString(7), 
        		cursor.getInt(8), cursor.getInt(9));
        
        cursor.close();
        db.close();
        // return transaction
        return t;
    }
    
    // Getting single category entry
    CategoryEntry getCategory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[] { CAT_KEY_ID, CAT_KEY_NAME, CAT_KEY_COLOR }, 
                KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        
        CategoryEntry c;
        if (cursor.moveToFirst())
            c = new CategoryEntry(cursor.getInt(0), cursor.getString(1), cursor.getInt(2));
        else
        	c = new CategoryEntry(StaticValues.EXPENSE_CATEGORY_UNKNOWN, "-", 0xffffffff);
            
        cursor.close();
        db.close();
        // return category
        return c;
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
                t.setPlace(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setType(cursor.getInt(8));
                t.setExpCategory(cursor.getInt(9));
                // Adding transaction to list
                transactionList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return transaction list
        return transactionList;
    }
    
    // Getting All categories
    public List<CategoryEntry> getAllCategories() {
        List<CategoryEntry> categoryList = new ArrayList<CategoryEntry>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	CategoryEntry c = new CategoryEntry(); 
                c.setID(cursor.getInt(0));
                c.setName(cursor.getString(1));
                c.setColor(cursor.getInt(2));
                // Adding category to list
                categoryList.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return transaction list
        return categoryList;
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
        values.put(KEY_PLACE, t.getPlace());
        values.put(KEY_CARD, t.getCard());
        values.put(KEY_TYPE, t.getType());
        values.put(KEY_EXP_CATEGORY, t.getExpCategory());
        
     // updating row
        int ret = db.update(TABLE_TRANSACTIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(t.getID()) });
        db.close();
        return ret;
    }
    
    // Updating single category entry
    public int updateCategory(CategoryEntry c) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(CAT_KEY_NAME, c.getName()); 
        values.put(CAT_KEY_COLOR, c.getColor());
        
     // updating row
        int ret = db.update(TABLE_CATEGORIES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(c.getID()) });
        db.close();
        return ret;
    }
 
    // Deleting single transaction entry
    public void deleteTransaction(TransactionEntry t) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(t.getID()) });
        db.close();
    }
    
    // Deleting single category entry
    public void deleteCategory(CategoryEntry c) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, KEY_ID + " = ?",
                new String[] { String.valueOf(c.getID()) });
        db.close();
    }
 
    // Getting transactions Count
    public int getTransactionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return ret;
    }
 
    // Getting categories Count
    public int getCategoriesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return ret;
    }
    
    public void clearAll(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.execSQL("DELETE FROM " + TABLE_TRANSACTIONS + ";");
    	db.close();
    }
    
    public void clearAllCategories(){
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.execSQL("DELETE FROM " + TABLE_CATEGORIES + ";");
    	db.close();
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
                t.setPlace(cursor.getString(6));
                t.setCard(cursor.getString(7));
                t.setType(cursor.getInt(8));
                t.setExpCategory(cursor.getInt(9));
                // Adding transaction to list
                transactionList.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return transaction list
        return transactionList;
    }

    // Getting transactions Count
    public int getUncategorizedTransactionsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
        		" WHERE " + " ( " + KEY_EXP_CATEGORY + "=" + StaticValues.EXPENSE_CATEGORY_UNKNOWN + " ) " +
        		" AND " + " ( " + KEY_TYPE +"=" + StaticValues.TRANSACTION_TYPE_EXPENSE+ " ) ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int ret = cursor.getCount();
        cursor.close();
        db.close();
        // return count
        return ret;
    }
    
    public List<TransactionEntry> getTransactionsDateInterval(long dateStart, long dateEnd){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" ORDER BY " + KEY_DATE_TIME + " DESC";
    	return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd)});
    }
    
    public List<TransactionEntry> getTransactionsDateInterval(long dateStart, long dateEnd, 
    																	int sortType, boolean isDesc){
    	
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" AND " + " ( " + KEY_TYPE + " =?" + " OR " + KEY_TYPE + " =?" + " ) ";
    		
    		if(sortType == StaticValues.SORT_BY_AMMOUNT)
    			countQuery += " ORDER BY " + KEY_AMOUNT;
    		else if(sortType == StaticValues.SORT_BY_PLACE)
    			countQuery += " ORDER BY " + KEY_PLACE;
    		else
    			countQuery += " ORDER BY " + KEY_DATE_TIME;
    		
    		if(isDesc && sortType != StaticValues.SORT_BY_PLACE)
    			countQuery += " DESC";
    			
    		
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd),
    														String.valueOf(StaticValues.TRANSACTION_TYPE_EXPENSE),
    														String.valueOf(StaticValues.TRANSACTION_TYPE_INCOME)});

    }
    
    public List<TransactionEntry> getTransactionsForGraph(long dateStart, long dateEnd, String currency){
    	String countQuery  = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + " ( " + KEY_DATE_TIME + " >= ? )" + " AND " + " ( " + KEY_DATE_TIME + " <= ? )" +
    			" AND " + " ( " + KEY_TYPE + " =?" + " ) " + 
    			" AND " + " ( " + KEY_AMOUNT_CURR + " =?" + " ) "+
    			" ORDER BY " + KEY_DATE_TIME;    			
    		
    		return queryDB(countQuery, new String[] {String.valueOf(dateStart), String.valueOf(dateEnd),
    														String.valueOf(StaticValues.TRANSACTION_TYPE_EXPENSE),
    														currency});
    }
    
    public List<TransactionEntry>  getTransactionsWithCategory(CategoryEntry cat){
    	String countQuery  = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + KEY_EXP_CATEGORY + "=?";
    	
    	return queryDB(countQuery, new String[] {String.valueOf(cat.getID())});
    }
    
    public List<String> getDistinctPlaces(){
    	List<String> distVals = new ArrayList<String>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	String countQuery = "SELECT DISTINCT " + KEY_PLACE + " FROM " + TABLE_TRANSACTIONS +
    			" WHERE " + KEY_TYPE + " =" + StaticValues.TRANSACTION_TYPE_EXPENSE +
    			" ORDER BY " + KEY_PLACE; 
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
            do {
            	distVals.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	db.close();
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
    	db.close();
    	return date;
    }
    
    public  List<TransactionEntry> getTransactionsPlaceFixed(String place){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " WHERE " + "( " + KEY_PLACE + " = ?" + " ) ";
    	return queryDB(countQuery, new String[] {place});
    }
       
    public  List<TransactionEntry> getCatigorizedTransactionsPlaceFixed(String place){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + 
    			" WHERE " + "( " + KEY_PLACE + " = ?" + " ) " + " AND "+
    			" ( " + KEY_EXP_CATEGORY + " > ? )";
    	return queryDB(countQuery, new String[] {place, String.valueOf(StaticValues.EXPENSE_CATEGORY_UNKNOWN)});
    }
 
    public  List<TransactionEntry> getTransactionsPlace(String place){
    	String countQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS + " WHERE " + "( " + KEY_PLACE + " = ?" + " ) ";
    	return queryDB(countQuery, new String[] {place});
    }
    
    public List<String> getDistinctPlacesForPlaceList(){
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
    	db.close();
    	return distVals;
    }
    
    public List<Model> getAllPlacesAndCategories(int filter){
    	List<Model> vals = new ArrayList<Model>();
    	SQLiteDatabase db = this.getWritableDatabase();	
    	//http://zetcode.com/db/sqlite/joins/
    	String countQuery = "SELECT DISTINCT " + KEY_PLACE + ", "  + CAT_KEY_NAME + ", " + CAT_KEY_COLOR +
    			" FROM " + TABLE_TRANSACTIONS + " LEFT JOIN " + TABLE_CATEGORIES + 
    			" ON " + TABLE_TRANSACTIONS + "." + KEY_EXP_CATEGORY + " = " + TABLE_CATEGORIES + "." + CAT_KEY_ID +
    			" WHERE " + TABLE_TRANSACTIONS + "." + KEY_TYPE + "=" + StaticValues.TRANSACTION_TYPE_EXPENSE;
    	
    	if(filter == StaticValues.PLACES_CATEGORY_IN){
    		countQuery += " AND " + " ( " + TABLE_TRANSACTIONS + "." + KEY_EXP_CATEGORY + ">" + 
    							StaticValues.EXPENSE_CATEGORY_UNKNOWN + " ) ";
    	}else if(filter == StaticValues.PLACES_CATEGORY_OUT){
    		countQuery += " AND " + " ( " + TABLE_TRANSACTIONS + "." + KEY_EXP_CATEGORY + "=" + 
    							StaticValues.EXPENSE_CATEGORY_UNKNOWN + " ) ";
    	}
    	countQuery += " ORDER BY " + KEY_PLACE;
    			
    	Cursor cursor = db.rawQuery(countQuery, null);
    	
    	if (cursor.moveToFirst()) {
            do {
            	vals.add(new Model(cursor.getString(0), cursor.getString(1), cursor.getInt(2)));
            } while (cursor.moveToNext());
        }
    	cursor.close();
    	db.close();
    	return vals;
    }
    
        
    // Getting All categories
    public List<CategoryEntry> getAllCategoriesOrdered() {
        List<CategoryEntry> categoryList = new ArrayList<CategoryEntry>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES + " ORDER BY " + CAT_KEY_NAME;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	CategoryEntry c = new CategoryEntry(); 
                c.setID(cursor.getInt(0));
                c.setName(cursor.getString(1));
                c.setColor(cursor.getInt(2));
                // Adding category to list
                categoryList.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return transaction list
        return categoryList;
    }
    
}
