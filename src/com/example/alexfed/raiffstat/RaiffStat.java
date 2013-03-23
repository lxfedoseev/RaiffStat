package com.example.alexfed.raiffstat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


/* TODO: 
 * - Save application state (screen rotation, going to background)
 * - Save report to a file (share report)
 * - Radio buttons for interval (1 week, 1 month, period)
 * - Make a good design (application icon as well)  
 * - Make demo mode in case user has no data but wants to try the application
 * - Check out all licenses
 * - Implement help
 * 
 * - ? Make failed parsing messages table in DB and ask the user to send them by email to me ?
 * - ? Widget (Spent for a place, spent entirely, remainder) ?
 * - ? Remove particular items from report (long touch -> remove) ?
 * 
 */

public class RaiffStat extends SherlockFragment { 

	private final String LOG = "RaiffStat";
	static final int IMPORT_EXPORT_ID = Menu.FIRST;
    static final int CLEAR_ID = Menu.FIRST+1;
	private DatePicker dpFrom;
	private DatePicker dpTo;
	private Spinner spPlace;
	private String placeName; 
	private Button btnApply;
	private ProgressDialog progressBar;
	private int progressBarStatus = 0;
	private Handler progressBarHandler = new Handler();
	private int impExpItemIndex = 0;
	
	private int year;
	private int month;
	private int day; 
	
	private String dirName = "RaiffStat";
	
	private int itemIndex = 0;
	OnDBChangedListener mCallback;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_raiff_stat, container, false);
        return v;
    }
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume() 
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		addItemsOnSpinnerPlace();
		addListenerOnSpinnerItemSelection();
		setCurrentDateOnView();
		addListenerOnButton();
	}

	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnDBChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem populateItem = menu.add(Menu.NONE, IMPORT_EXPORT_ID, 0, R.string.menu_import_export);
        populateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItem clearItem = menu.add(Menu.NONE, CLEAR_ID, 0, R.string.menu_clear_db);
        clearItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }
	
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final ContentResolver cr = getActivity().getContentResolver();

        switch (item.getItemId()) {
            case IMPORT_EXPORT_ID:
            	importExport();
                return true;
            case CLEAR_ID:
            	clearDB();
            	mCallback.onDBChanged();
    			setCurrentDateOnView();
    			addItemsOnSpinnerPlace();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	
	public void addItemsOnSpinnerPlace() {
		spPlace = (Spinner) getActivity().findViewById(R.id.spinnerPlace);
		List<String> list = new ArrayList<String>();
		List<String> distPlaces = new ArrayList<String>();
		list.add(getResources().getString(R.string.spinner_all));
		distPlaces = queryDistinctPlaces();
		for(String s : distPlaces){
			list.add(s);
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
			android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spPlace.setAdapter(dataAdapter);
	  }
	
	// display current date
	public void setCurrentDateOnView() {
		dpFrom = (DatePicker) getActivity().findViewById(R.id.datePickerFrom);
		dpTo = (DatePicker) getActivity().findViewById(R.id.datePickerTo);
		
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		
		DatabaseHandler db = new DatabaseHandler(getActivity());
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
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_db_empty), Toast.LENGTH_LONG).show(); 
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
			db.close();
		}
	}
	
	public void addListenerOnButton() {
		btnApply = (Button) getActivity().findViewById(R.id.btnApply);
		btnApply.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) { 
		    	//Toast.makeText(getApplicationContext(), "Button click", Toast.LENGTH_LONG).show();               
		    	Intent myIntent;
		    	if(placeName.equalsIgnoreCase(getResources().getString(R.string.spinner_all))){
		    		myIntent = new Intent(getActivity(), ReportListAll.class);
		    	}else{
		    		myIntent = new Intent(getActivity(), ReportListPlace.class);
		    		myIntent.putExtra("place", placeName);
		    	}
		    	int month = dpFrom.getMonth() + 1;
		    	myIntent.putExtra("day_from", dpFrom.getDayOfMonth()+"/"+month+"/"+dpFrom.getYear());
		    	month = dpTo.getMonth() + 1;
		    	myIntent.putExtra("day_to", dpTo.getDayOfMonth()+"/"+month+"/"+dpTo.getYear());
		    	RaiffStat.this.startActivity(myIntent);
		    }
		});
	}
	
	public void addListenerOnSpinnerItemSelection() {
		spPlace = (Spinner) getActivity().findViewById(R.id.spinnerPlace);
		spPlace.setOnItemSelectedListener(new CustomOnItemSelectedListener());
	  }
	
	public class CustomOnItemSelectedListener implements OnItemSelectedListener {
		 
		  public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			  placeName = parent.getItemAtPosition(pos).toString();
		  }
		 
		  @Override
		  public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		  }
	}
	
	private void importExport(){		  
		  AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		  final CharSequence[] choiceList;
		  CharSequence[] choiceListLocal = {getResources().getString(R.string.dialog_sms_import),
					  							getResources().getString(R.string.dialog_csv_export),
					  							getResources().getString(R.string.dialog_csv_import)};
		  choiceList = choiceListLocal;

		  alert.setTitle(getResources().getString(R.string.menu_import_export));		  
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
						default:
							//do nothing
							break;
					}	
				}else{
					Toast.makeText(getActivity(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
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
					  getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setCurrentDateOnView();
					    		 addItemsOnSpinnerPlace();
					    		 mCallback.onDBChanged();
							}
						});
					  
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
	        cur = getActivity().getContentResolver().query(uri, projection, "address" + "='" + StaticValues.RAIFF_ADDRESS + "'", null, "date");
	    }catch (SQLiteException ex) {  
	    	myLog.LOGD(LOG, ex.getMessage()); 
	    	return false;
	    } 
	    if(cur.getCount()<1){
	    	Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_sms) + " " + StaticValues.RAIFF_ADDRESS, Toast.LENGTH_LONG).show(); 
	    	return false;
	    }
		progressBar = new ProgressDialog(getActivity());
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
	        Cursor cur = getActivity().getContentResolver().query(uri, projection, "address" + "='" + StaticValues.RAIFF_ADDRESS + "'", null, "date");
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
	                	parsedWell = prs.parseSmsBody(getActivity(), strbody.trim(), longDate);
	                    
	                if(parsedWell){
	                	myLog.LOGD(LOG, prs.getCard() + " & " +  prs.getPlace() + " & " + 
	                			prs.getAmount() + " & " + prs.getAmountCurr() + " & " 
	                			+ prs.getRemainder() + " & " + prs.getRemainderCurr() + " & " + prs.getPlace() + " & " + 
	                			prs.getInPlace() + " & " + prs.getType() + " & " + dateString);
	                    	
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
	
	private void doExportToCSV(){
		
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	
	    	DatabaseHandler db = new DatabaseHandler(getActivity());
	    	if(db.getTransactionsCount() < 1){
	    		Toast.makeText(getActivity(), getResources().getString(R.string.toast_db_empty), Toast.LENGTH_LONG).show();
	    		db.close();
	    		return;
	    	}
	    	
	    	if(!createDirIfNotExists(dirName)){
	    		Toast.makeText(getActivity(), getResources().getString(R.string.toast_dir_failed), Toast.LENGTH_LONG).show();
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
				    		t.getTerminal() + StaticValues.DELIMITER +
				    		t.getCard() + StaticValues.DELIMITER +
				    		t.getPlace() + StaticValues.DELIMITER +
				    		t.getInPlace() + StaticValues.DELIMITER + 
				    		t.getType() + "\r\n");
	    			out.flush();
	    		}
	    		out.close(); 
	    		Toast.makeText(getActivity(), 
	    				getResources().getString(R.string.str_file) + " " + fName  + " " +
	    						getResources().getString(R.string.str_created), Toast.LENGTH_LONG).show();
	    	}catch(Exception e){
	    		myLog.LOGD(LOG, e.getMessage());
	    		Toast.makeText(getActivity(), getResources().getString(R.string.toast_exp_failed), Toast.LENGTH_LONG).show();
	    	}
	    }else {
	    	Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_storage), Toast.LENGTH_LONG).show();
	    }
		
	}
	
	private void doImportFromCSV(){
		
		List<String> fileList = getCSVFileList();
		if(fileList.size()<1){
			Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_files), Toast.LENGTH_LONG).show();
			return;
		}
		
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
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
						Toast.makeText(getActivity(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
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
	
	private void importSmsFromFileWithProgressBar(CharSequence fileName){
		final CharSequence localFileName = fileName;
		progressBar = new ProgressDialog(getActivity());
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_csv_scanning));
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.show();
		
		new Thread(new Runnable() {
			public void run() {
				importSmsFromCSV(localFileName);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
					setCurrentDateOnView();
					addItemsOnSpinnerPlace();
					mCallback.onDBChanged();
				}
			});
					  
		}
		}).start();
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
                	
                	myLog.LOGD(LOG, prs.getCard() + " & " +  prs.getPlace() + " & " + 
                			prs.getAmount() + " & " + prs.getAmountCurr() + " & " 
                			+ prs.getRemainder() + " & " + prs.getRemainderCurr() + " & " + prs.getPlace() + " & " + 
                			prs.getInPlace() + " & " + prs.getType() + " & " + dateString);
                    	
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
			  Toast.makeText(getActivity(), getResources().getString(R.string.toast_imp_failed), Toast.LENGTH_LONG).show();
		}

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
	
	private boolean isCSVFile(File file, Pattern pattern){
	 		
	 		if(file.isDirectory())
	 			return false;
	 		
	 		Matcher matcher = pattern.matcher(file.getName());
	 		if (matcher.matches())
	            return true;
	        else
	            return false;
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
	
	private void addTransactionToDB(RaiffParser prs){
		DatabaseHandler db = new DatabaseHandler(getActivity());
		db.addTransaction(new TransactionEntry(prs.getDateTime(), prs.getAmount(), prs.getAmountCurr(),
				prs.getRemainder(), prs.getRemainderCurr(), prs.getTerminal(), prs.getCard(), 
				prs.getPlace(), prs.getInPlace(), prs.getType(), prs.getExpCategory()));
		db.close();
	}

	private void mergeTransactionToDB(RaiffParser prs){
		DatabaseHandler db = new DatabaseHandler(getActivity());
		db.mergeTransaction(new TransactionEntry(prs.getDateTime(), prs.getAmount(), prs.getAmountCurr(),
				prs.getRemainder(), prs.getRemainderCurr(), prs.getTerminal(), prs.getCard(), 
				prs.getPlace(), prs.getInPlace(), prs.getType(), prs.getExpCategory()));
		db.close();
	}

	private void clearDB(){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		db.clearAll();
		db.close();
	}
	
	private void printTransactions(List<TransactionEntry> transactions){
		for (TransactionEntry t : transactions) {
			String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(t.getDateTime()));
			myLog.LOGD(LOG, t.getID() + " & " + t.getCard() + " & " +  t.getTerminal() + " & " + 
	    			t.getAmount() + " & " + t.getAmountCurr() + " & " 
	    			+ t.getRemainder() + " & " + t.getRemainderCurr() + " & " +  t.getPlace() +  " & " +  
	    			t.getInPlace() +  " & " + t.getType() + " & " + dateString);
		}
	}
	
	private void queryAmountFixed(double amount){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		List<TransactionEntry> transactions = db.getTransactionsAmountFixed(amount);
		printTransactions(transactions);
		db.close();
	}
	
	private void queryAmountInterval(double start, double end){
		DatabaseHandler db = new DatabaseHandler(getActivity());
		List<TransactionEntry> transactions = db.getTransactionsAmountInterval(start, end);
		printTransactions(transactions);
		db.close();
	}
	
	private void queryDateInterval(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		List<TransactionEntry> transactions = db.getTransactionsDateInterval(start, end);
		printTransactions(transactions);
		db.close();
	}
	
	private long convertStringDate(String strDate){
		try {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = (Date)formatter.parse(strDate);
			return date.getTime();
		}catch (ParseException ex){
			myLog.LOGD(LOG, ex.getMessage()); 
			return 0;
		}
	}
	
	private List<String> queryDistinctTerminals(){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		return db.getDistinctTerminals();
		
	}
	
	private List<String> queryDistinctPlaces(){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		List <String> ls = db.getDistinctPlaces();
		db.close();
		return ls;
		
	}
	
	private long queryMinDate(){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		long date = db.getMinDate();
		db.close();
		return date;
		
	}
	
	private void queryCategorizedPlaces(){	
		DatabaseHandler db = new DatabaseHandler(getActivity());
		List <CategorizedPlaceModel> cat = db.getCategorizedPlaces();
		db.close();
		
		for(CategorizedPlaceModel c : cat){
			myLog.LOGD(LOG, "Place: " + c.getPlaceName() + " Category: " + c.getCategoryName() +
					" Color: " + c.getColor());
		}
		
	}

	// Container Activity must implement this interface
    public interface OnDBChangedListener {
        public void onDBChanged();
    }
    
}
