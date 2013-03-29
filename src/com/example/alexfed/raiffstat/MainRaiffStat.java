package com.example.alexfed.raiffstat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

public class MainRaiffStat extends SherlockActivity {
	
	private final String LOG = "MainRaiffStat";
	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandler = new Handler();
	private int impExpItemIndex = 0;
	private String dirName = "RaiffStat";
	private int itemIndex = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setButtons();
	}


	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	private void setButtons(){
		
		Button btnReport = (Button) findViewById(R.id.button_report);
		Button btnData = (Button) findViewById(R.id.button_data);
		Button btnPlaces = (Button) findViewById(R.id.button_places);
		Button btnAbout = (Button) findViewById(R.id.button_about);
		
		btnReport.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent tabsActivity = new Intent(getBaseContext(), ReportTabs.class);
    			startActivity(tabsActivity);
			}
		});
		
		btnData.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				importExport();
			}
		});
		
		btnPlaces.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent placesActivity = new Intent(getBaseContext(), PlacesList.class);
    			startActivity(placesActivity);
			}
		});

		btnAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}
	
	private void importExport(){		  
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  final CharSequence[] choiceList;
		  CharSequence[] choiceListLocal = {getResources().getString(R.string.dialog_sms_import),
					  							getResources().getString(R.string.dialog_csv_export),
					  							getResources().getString(R.string.dialog_csv_import),
					  							getResources().getString(R.string.menu_clear_db)};
		  choiceList = choiceListLocal;

		  alert.setTitle(getResources().getString(R.string.str_data));		  
		  impExpItemIndex = -1;
		  alert.setSingleChoiceItems(choiceList, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				impExpItemIndex = which; 
			}
		});
		alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(impExpItemIndex > -1){
					switch (impExpItemIndex){
						case 0:
							importSmsWithProgressBar();
							break;
						case 1:
							doExportToCSV();
							break;
						case 2:
							doImportFromCSV();
							break;
						case 3:
							clearDB();
							break;
						default:
							//do nothing
							break;
					}	
				}else{
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
				}
			}
		});
		alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		  
		alert.show();
	}
	
	private void importSmsWithProgressBar(){
		if(initProgressBar()){
			new Thread(new Runnable() {
				  public void run() {
					  importSms(); 
				  }
			}).start();
		}
	}
	
	private boolean initProgressBar(){
	    final String SMS_URI_INBOX = "content://sms/inbox"; 
	    Cursor cur;
	    try {  
	    	Uri uri = Uri.parse(SMS_URI_INBOX);  
	        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" }; 
	        cur = getContentResolver().query(uri, projection, "address" + "='" + StaticValues.RAIFF_ADDRESS + "'", null, "date");
	    }catch (SQLiteException ex) {  
	    	myLog.LOGD(LOG, ex.getMessage()); 
	    	return false;
	    } 
	    if(cur.getCount()<1){
	    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_sms) + " " + StaticValues.RAIFF_ADDRESS, Toast.LENGTH_LONG).show(); 
	    	return false;
	    }
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_sms_scanning));
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setProgress(0);
		progressBar.setMax(cur.getCount());
		progressBar.show();
		if (!cur.isClosed()) {  
            cur.close();  
            cur = null;  
        } 
		return true;
	}
	
	private void importSms(){
		myLog.LOGD(LOG, "importSms");
	    final String SMS_URI_INBOX = "content://sms/inbox"; 
	    boolean parsedWell = false;
	    try {  
	    	Uri uri = Uri.parse(SMS_URI_INBOX);  
	        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" }; 
	        Cursor cur = getContentResolver().query(uri, projection, "address" + "='" + StaticValues.RAIFF_ADDRESS + "'", null, "date");
	        if (cur.moveToFirst()) {  
	        	int index_Address = cur.getColumnIndex("address");  
	            int index_Person = cur.getColumnIndex("person");  
	            int index_Body = cur.getColumnIndex("body");  
	            int index_Date = cur.getColumnIndex("date");  
	            int index_Type = cur.getColumnIndex("type");    
				//reset progress bar status
				progressBarStatus = 0;
	           
	            do {  
	            	String strAddress = cur.getString(index_Address);  
	                int intPerson = cur.getInt(index_Person);  
	                String strbody = cur.getString(index_Body);  
	                long longDate = cur.getLong(index_Date);  
	                int int_Type = cur.getInt(index_Type);  
	                    
	                //http://developer.android.com/reference/java/text/SimpleDateFormat.html
	                String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(longDate));
	                //myLog.LOGD("LOG", "Date is: " + dateString);
	                    
	                RaiffParser prs = new RaiffParser();
	                if(strbody!=null) 
	                	parsedWell = prs.parseSmsBody(getBaseContext(), strbody.trim(), longDate);
	                    
	                if(parsedWell){
	                	myLog.LOGD(LOG, prs.getCard() + " & " + 
	                			prs.getAmount() + " & " + prs.getAmountCurr() + " & " 
	                			+ prs.getRemainder() + " & " + prs.getRemainderCurr() + 
	                			" & " + prs.getType() + " & " + dateString);
	                    	
	                	//addTransactionToDB(longDate, prs);
	                	mergeTransactionToDB(prs);
	                }else{
	                	//Something went wrong
	                	//TODO: add this strbody to problem SMS table of DB to be sent later to the developer
	                	myLog.LOGE(LOG, "Problem message: " + strbody);
	                	//TODO: remove Toast, using now only for test
	                	/*RaiffStat.this.runOnUiThread(new Runnable() {
	    					@Override
	    					public void run() {
	    						Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_pars_failed), Toast.LENGTH_SHORT).show();
	    				}
	    			});*/
	                }
	                
	                progressBarStatus++;
	             // Update the progress bar
	                progressBarHandler.post(new Runnable() {
	  					public void run() {
	  					  progressBar.setProgress(progressBarStatus);
	  					}
	  				});
	                
	            } while (cur.moveToNext());  

	         // close the progress bar dialog
				progressBar.dismiss();
	            if (!cur.isClosed()) {  
	            	cur.close();  
	                cur = null;  
	            }  
	        } else {  
	        	myLog.LOGD(LOG, "no result!");
	        } // end if  
	    } catch (SQLiteException ex) {  
	    	myLog.LOGD(LOG, ex.getMessage());  
	    }      

	}
	
	private void mergeTransactionToDB(RaiffParser prs){
		DatabaseHandler db = new DatabaseHandler(this);
		db.mergeTransaction(new TransactionEntry(prs.getDateTime(), prs.getAmount(), prs.getAmountCurr(),
				prs.getRemainder(), prs.getRemainderCurr(), prs.getPlace(), prs.getCard(), 
				prs.getType(), prs.getExpCategory()));
		db.close();
	}
	
	private void doExportToCSV(){
		
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	
	    	DatabaseHandler db = new DatabaseHandler(this);
	    	if(db.getTransactionsCount() < 1){
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_db_empty), Toast.LENGTH_LONG).show();
	    		db.close();
	    		return;
	    	}
	    	
	    	if(!createDirIfNotExists(dirName)){
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_dir_failed), Toast.LENGTH_LONG).show();
	    		db.close();
	    		return;
	    	}
	    	
	    	List<TransactionEntry> trs = db.getAllTransactions(); 
	    	db.close();
	    	try{
	    		String fName = makeExportFileName();
	    		FileWriter ostream = new FileWriter(fName);
	    		BufferedWriter out = new BufferedWriter(ostream);
	    		
	    		for(TransactionEntry t : trs){
	    			out.write(t.getID() + StaticValues.DELIMITER +
	    					t.getDateTime() + StaticValues.DELIMITER +
				    		t.getAmount() + StaticValues.DELIMITER +
				    		t.getAmountCurr() + StaticValues.DELIMITER +
				    		t.getRemainder() + StaticValues.DELIMITER +
				    		t.getRemainderCurr() + StaticValues.DELIMITER +
				    		t.getPlace() + StaticValues.DELIMITER +
				    		t.getCard() + StaticValues.DELIMITER +
				    		t.getType() + "\r\n");
	    			out.flush();
	    		}
	    		out.close(); 
	    		Toast.makeText(getApplicationContext(), 
	    				getResources().getString(R.string.str_file) + " " + fName  + " " +
	    						getResources().getString(R.string.str_created), Toast.LENGTH_LONG).show();
	    	}catch(Exception e){
	    		myLog.LOGD(LOG, e.getMessage());
	    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_exp_failed), Toast.LENGTH_LONG).show();
	    	}
	    }else {
	    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_storage), Toast.LENGTH_LONG).show();
	    }
		
	}
	
	private boolean createDirIfNotExists(String path) {
	    boolean ret = true;

	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            ret = false;
	        }
	    }
	    return ret;
	}
	
	private String makeExportFileName(){
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy__HH_mm_ss");	
		return Environment.getExternalStorageDirectory()+ "/" + dirName + "/" + df.format(c.getTime())+".csv";
	}
	
	private void doImportFromCSV(){
		
		List<String> fileList = getCSVFileList();
		if(fileList.size()<1){
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_files), Toast.LENGTH_LONG).show();
			return;
		}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final CharSequence[] choiceList = fileList.toArray(new CharSequence[fileList.size()]);
		
		alert.setTitle(getResources().getString(R.string.str_files));		  
		  itemIndex = -1;
		  alert.setSingleChoiceItems(choiceList, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				itemIndex = which;
			}
		});
		alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(itemIndex > -1){
						importSmsFromFileWithProgressBar(choiceList[itemIndex]);
					}else{
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
					}
				}
			});
		alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});
			  
		alert.show();
	}
	
	private List<String> getCSVFileList(){
		List<String> fileList = new ArrayList<String>();
		String patternString = "\\d{2}_\\d{2}_\\d{4}__\\d{2}_\\d{2}_\\d{2}.csv";
		Pattern pattern = Pattern.compile(patternString);
		
		File f = new File(Environment.getExternalStorageDirectory()+"/"+dirName);
		if(!f.exists())
			return fileList;
		
	    File[] files = f.listFiles();
	    for(int i=0; i < files.length; i++){
	    	File file = files[i];
	       
	    	if(!isCSVFile(file, pattern))
	    	   continue;
	    	fileList.add(file.getName());
	    }
	    return fileList;
		
	}
	
	private void importSmsFromFileWithProgressBar(CharSequence fileName){
		final CharSequence localFileName = fileName;
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_csv_scanning));
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.show();
		
		new Thread(new Runnable() {
			public void run() {
				importSmsFromCSV(localFileName);		  
		}
		}).start();
	}
	
	private boolean isCSVFile(File file, Pattern pattern){
 		
 		if(file.isDirectory())
 			return false;
 		
 		Matcher matcher = pattern.matcher(file.getName());
 		if (matcher.matches())
            return true;
        else
            return false;
 }
	
	private void importSmsFromCSV(CharSequence fileName){
	    
	    try{
			FileInputStream fstream = new FileInputStream(Environment.getExternalStorageDirectory()+"/"+dirName+"/"+fileName.toString());
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {	  
				
				RaiffParser prs = new RaiffParser();
                    
                if(prs.parseCSVLine(strLine.trim())){
                	String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(prs.getDateTime()));
                	
                	myLog.LOGD(LOG, prs.getCard() + " & " + 
                			prs.getAmount() + " & " + prs.getAmountCurr() + " & " 
                			+ prs.getRemainder() + " & " + prs.getRemainderCurr() + " & " + prs.getPlace() +  
                			" & " + prs.getType() + " & " + dateString);
                    	
                	mergeTransactionToDB(prs);
                }else{
                	//Something went wrong.
                	myLog.LOGE(LOG, "Problem message: " + strLine);
                	//TODO: remove Toast, using now only for test
                	/*RaiffStat.this.runOnUiThread(new Runnable() {
    					@Override
    					public void run() {
    						Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_pars_failed), Toast.LENGTH_SHORT).show();
    				}
    			});*/
                }
                
                progressBarStatus++;
             // Update the progress bar
                progressBarHandler.post(new Runnable() {
  					public void run() {
  					  progressBar.setProgress(progressBarStatus);
  					}
  				});
			}
			//Close the input stream
			in.close();
			// close the progress bar dialog
			progressBar.dismiss();
		}catch (Exception e){//Catch exception if any
			myLog.LOGE(LOG, "Error: " + e.getMessage());
			  Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_imp_failed), Toast.LENGTH_LONG).show();
		}

	}
	
	private void clearDB(){	
		DatabaseHandler db = new DatabaseHandler(this);
		db.clearAll();
		db.close();
	}
}
