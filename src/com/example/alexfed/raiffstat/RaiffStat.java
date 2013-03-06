package com.example.alexfed.raiffstat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class RaiffStat extends Activity { 

	private final String LOG = "RaiffStat";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_stat);
		
		ReadSms();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_raiff_stat, menu);
		return true;
	}
	
	private void ReadSms(){
		
		Log.d(LOG, "ReadSms");
		StringBuilder smsBuilder = new StringBuilder();
	       final String SMS_URI_INBOX = "content://sms/inbox"; 
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
	                    
	                    Log.d(LOG, "strAddress: " + strAddress + " strbody: " + strbody);
	                    
	                    //http://developer.android.com/reference/java/text/SimpleDateFormat.html
	                    String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(longDate));
	                    Log.d("LOG", "Date is: " + dateString);

	                    smsBuilder.append("[ ");  
	                    smsBuilder.append(strAddress + ", ");  
	                    smsBuilder.append(intPerson + ", ");  
	                    smsBuilder.append(strbody + ", ");  
	                    smsBuilder.append(longDate + ", ");  
	                    smsBuilder.append(int_Type);  
	                    smsBuilder.append(" ]\n\n");  
	                    
	                } while (cur.moveToNext());  

	                if (!cur.isClosed()) {  
	                    cur.close();  
	                    cur = null;  
	                }  
	            } else {  
	                smsBuilder.append("no result!");
	                Log.d(LOG, "no result!");
	            } // end if  
	        } catch (SQLiteException ex) {  
	            Log.d(LOG, ex.getMessage());  
	        }  
		
	        
	}

}
