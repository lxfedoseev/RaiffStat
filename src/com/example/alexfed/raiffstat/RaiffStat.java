package com.example.alexfed.raiffstat;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

public class RaiffStat extends Activity { 

	private final String LOG = "RaiffStat";
	
	private DatePicker dpFrom;
	private DatePicker dpTo;
	private Spinner spGroup;
	private String groupName; 
	private Button btnApply;
	
	private int year;
	private int month;
	private int day;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_stat);
		
		setCurrentDateOnView();
		addItemsOnSpinnerGroup();
		addListenerOnSpinnerItemSelection();
		addListenerOnButton();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_raiff_stat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
    		case R.id.menu_settings:
    			//TODO:
    			//Intent settingsActivity = new Intent(getBaseContext(), MyPreferences.class);
    			//startActivity(settingsActivity);
    			return true;
    		case R.id.menu_sms_import:
    			clearDB();
    			importSms();
    			setCurrentDateOnView();
    			addItemsOnSpinnerGroup();
    			return true;
    		case R.id.menu_clear_db:
    			clearDB();
    			setCurrentDateOnView();
    			addItemsOnSpinnerGroup();
    			return true; 
    		case R.id.menu_query:
    			queryDistinctGroups();
    			//queryDistinctPlaces();
    			//queryDateInterval(convertStringDate("02/03/2013 00:00:00"), convertStringDate("07/03/2013 23:59:59"));
    			//queryAmmountInterval(0, 500);
    			//queryAmmountFixed(2077.3);
    			return true;
    		case R.id.menu_manage_places:
    			//TODO:
    			return true; 
    		default:
    			return super.onOptionsItemSelected(item);
		}
   
	}
	
	public void addItemsOnSpinnerGroup() {
		spGroup = (Spinner) findViewById(R.id.spinnerGroup);
		List<String> list = new ArrayList<String>();
		List<String> distGroups = new ArrayList<String>();
		list.add("All");
		distGroups = queryDistinctGroups();
		for(String s : distGroups){
			list.add(s);
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spGroup.setAdapter(dataAdapter);
	  }
	
	// display current date
	public void setCurrentDateOnView() {
		dpFrom = (DatePicker) findViewById(R.id.datePickerFrom);
		dpTo = (DatePicker) findViewById(R.id.datePickerTo);
		
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		DatabaseHandler db = new DatabaseHandler(this);
		if(db.getTransactionsCount() > 0){// DB is not empty
		 
			if (currentapiVersion >= 11) {
			  try {
			    Method m = dpFrom.getClass().getMethod("setCalendarViewShown", boolean.class);
			    m.invoke(dpFrom, false);
			    m.invoke(dpTo, false);
			    
			    m = dpFrom.getClass().getMethod("setMinDate", long.class);
			    m.invoke(dpFrom, queryMinDate());
			    m.invoke(dpTo, queryMinDate());
			  }
			  catch (Exception e) {} // eat exception in our case
			}
			
			dpFrom.init(Integer.valueOf(new SimpleDateFormat("yyyy").format(new Date(queryMinDate()))), 
					Integer.valueOf(new SimpleDateFormat("MM").format(new Date(queryMinDate())))-1, 
					Integer.valueOf(new SimpleDateFormat("dd").format(new Date(queryMinDate()))), 
					null);
			dpTo.init(year, month, day, null);	
		}else{
			Toast.makeText(getApplicationContext(), "DB is empty", Toast.LENGTH_LONG).show(); 
			if (currentapiVersion >= 11) {
			  try {
			    Method m = dpFrom.getClass().getMethod("setCalendarViewShown", boolean.class);
			    m.invoke(dpFrom, false);
			    m.invoke(dpTo, false);
			  }
			  catch (Exception e) {} // eat exception in our case
			}
	
			dpFrom.init(year, month, day, null);
			dpTo.init(year, month, day, null);
		}
	}
	
	public void addListenerOnButton() {
		btnApply = (Button) findViewById(R.id.btnApply);
		btnApply.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	//Toast.makeText(getApplicationContext(), "Button click", Toast.LENGTH_LONG).show();               
		    	Intent myIntent;
		    	myIntent = new Intent(RaiffStat.this, ReportList.class);
		    	int month = dpFrom.getMonth() + 1;
		    	myIntent.putExtra("day_from", dpFrom.getDayOfMonth()+"/"+month+"/"+dpFrom.getYear());
		    	month = dpTo.getMonth() + 1;
		    	myIntent.putExtra("day_to", dpTo.getDayOfMonth()+"/"+month+"/"+dpTo.getYear());
		    	myIntent.putExtra("group", groupName);
		    	RaiffStat.this.startActivity(myIntent);
		    }
		});
	}
	
	public void addListenerOnSpinnerItemSelection() {
		spGroup = (Spinner) findViewById(R.id.spinnerGroup);
		spGroup.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	  }
	
	public class CustomOnItemSelectedListener implements OnItemSelectedListener {
		 
		  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			  groupName = parent.getItemAtPosition(pos).toString();
		  }
		 
		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		  }
		 
	}
	
	private void importSms(){
		Log.d(LOG, "importSms");
	    final String SMS_URI_INBOX = "content://sms/inbox"; 
	    boolean parsedWell = false;
	    try {  
	    	Uri uri = Uri.parse(SMS_URI_INBOX);  
	        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" }; 
	        String raiffAddress = "Raiffeisen";
	        Cursor cur = getContentResolver().query(uri, projection, "address" + "='" + raiffAddress + "'", null, "date");
	        if (cur.moveToFirst()) {  
	        	int index_Address = cur.getColumnIndex("address");  
	            int index_Person = cur.getColumnIndex("person");  
	            int index_Body = cur.getColumnIndex("body");  
	            int index_Date = cur.getColumnIndex("date");  
	            int index_Type = cur.getColumnIndex("type");      
	           
	            do {  
	            	String strAddress = cur.getString(index_Address);  
	                int intPerson = cur.getInt(index_Person);  
	                String strbody = cur.getString(index_Body);  
	                long longDate = cur.getLong(index_Date);  
	                int int_Type = cur.getInt(index_Type);  
	                    
	                //http://developer.android.com/reference/java/text/SimpleDateFormat.html
	                String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(longDate));
	                //Log.d("LOG", "Date is: " + dateString);
	                    
	                RaiffParser prs = new RaiffParser();
	                if(strbody!=null) 
	                	parsedWell = prs.parseSmsBody(strbody.trim());
	                    
	                    if(parsedWell){
	                    	Log.d(LOG, prs.getCard() + " & " +  prs.getPlace() + " & " + 
	                    			prs.getAmmount() + " & " + prs.getAmmountCurr() + " & " 
	                    			+ prs.getRemainder() + " & " + prs.getRemainderCurr() + " & " + prs.getGroup() + " & " + dateString);
	                    	
	                    	addTransactionToDB(longDate, prs);
	                    }
	                    
	                } while (cur.moveToNext());  

	                if (!cur.isClosed()) {  
	                    cur.close();  
	                    cur = null;  
	                }  
	        } else {  
	        	Log.d(LOG, "no result!");
	        } // end if  
	    } catch (SQLiteException ex) {  
	    	Log.d(LOG, ex.getMessage());  
	    }      
	}
	
	private void addTransactionToDB(long dateTime, RaiffParser prs){
		DatabaseHandler db = new DatabaseHandler(this);
		db.addTransaction(new TransactionEntry(dateTime, prs.getAmmount(), prs.getAmmountCurr(),
				prs.getRemainder(), prs.getRemainderCurr(), prs.getPlace(), prs.getCard(), prs.getGroup()));  
	}

	private void clearDB(){	
		DatabaseHandler db = new DatabaseHandler(this);
		db.clearAll();
	}
	
	private void printTransactions(List<TransactionEntry> transactions){
		for (TransactionEntry t : transactions) {
			String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(t.getDateTime()));
			Log.d(LOG, t.getID() + " & " + t.getCard() + " & " +  t.getPlace() + " & " + 
	    			t.getAmmount() + " & " + t.getAmmountCurr() + " & " 
	    			+ t.getRemainder() + " & " + t.getRemainderCurr() + " & " +  t.getGroup() +  " & " + dateString);
		}
	}
	
	private void queryAmmountFixed(double ammount){	
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> transactions = db.getTransactionsAmountFixed(ammount);
		printTransactions(transactions);
	}
	
	private void queryAmmountInterval(double start, double end){
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> transactions = db.getTransactionsAmountInterval(start, end);
		printTransactions(transactions);
	}
	
	private void queryDateInterval(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> transactions = db.getTransactionsDateInterval(start, end);
		printTransactions(transactions);
	}
	
	private long convertStringDate(String strDate){
		try {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = (Date)formatter.parse(strDate);
			return date.getTime();
		}catch (ParseException ex){
			Log.d(LOG, ex.getMessage()); 
			return 0;
		}
	}
	
	private List<String> queryDistinctPlaces(){	
		DatabaseHandler db = new DatabaseHandler(this);
		return db.getDistinctPlaces();
		
	}
	
	private List<String> queryDistinctGroups(){	
		DatabaseHandler db = new DatabaseHandler(this);
		return db.getDistinctGroups();
		
	}
	
	private long queryMinDate(){	
		DatabaseHandler db = new DatabaseHandler(this);
		return db.getMinDate();
		
	}
}
