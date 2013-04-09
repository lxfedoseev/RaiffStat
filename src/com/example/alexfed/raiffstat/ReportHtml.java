package com.example.alexfed.raiffstat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class ReportHtml {
	private final String LOG = "ReportHtml";
	
	private String dayFrom;
	private String dayTo;
	private List<SummaryHeadEntry> mHead;
	private List<SummaryBarEntry> mBar;
	private Context mContext;
	
	public ReportHtml(Context context, String from, String to, 
						List<SummaryHeadEntry> head, List<SummaryBarEntry> bar){
		this.dayFrom = from;
		this.dayTo = to;
		this.mHead = head;
		this.mBar = bar;
		this.mContext = context;
	}
	
	public boolean saveHtml(){
		
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	
	    	if(!createDirIfNotExists(StaticValues.DIR_NAME)){
	    		Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_dir_failed), Toast.LENGTH_LONG).show();
	    		return false;
	    	}
			
			try{
	    		String fName = makeHtmlFileName();
	    		FileWriter ostream = new FileWriter(fName);
	    		BufferedWriter out = new BufferedWriter(ostream);
	    		
	    		doWriteHtmlBegin(out);
	    		doWriteHead(out);
	    		doWriteMainTable(out); 
	    		doWriteHtmlEnd(out);
	    		
	    		out.close(); 
	    		Toast.makeText(mContext, 
	    				mContext.getResources().getString(R.string.str_file) + " " + fName  + " " +
	    						mContext.getResources().getString(R.string.str_created), Toast.LENGTH_LONG).show();
	    	}catch(Exception e){
	    		myLog.LOGD(LOG, "Error 01");
	    		myLog.LOGD(LOG, e.getMessage());
	    		Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_exp_failed), Toast.LENGTH_LONG).show();
	    		return false;
	    	}
	    	
	    }else {
	    	Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_no_storage), Toast.LENGTH_LONG).show();
	    	return false;
	    }
		
		return false;
	}
	
	private void doWriteHtmlBegin(BufferedWriter out) throws IOException{
		try{
			out.write(mContext.getResources().getString(R.string.html_begin));
		}catch(IOException e){
			throw e;
		}
	}
	
	private void doWriteHtmlEnd(BufferedWriter out) throws IOException{
		try{
			out.write(mContext.getResources().getString(R.string.html_end));
		}catch(IOException e){
			throw e;
		}
	}
	
	private void doWriteHead(BufferedWriter out) throws IOException{
		try{
			out.write(mContext.getResources().getString(R.string.html_head_table_begin));
			for(SummaryHeadEntry head:mHead){
				out.write(String.format(mContext.getResources().getString(R.string.html_head_row), 
						head.getTitle(), head.getInfo()));
			}
			out.write(mContext.getResources().getString(R.string.html_head_table_end));
		}catch(IOException e){
			throw e;
		}
	}
	
	private void doWriteMainTable(BufferedWriter out) throws IOException{
		try{
			DatabaseHandler db = new DatabaseHandler(mContext);
			List<TransactionEntry> trs = db.getTransactionsDateInterval(convertStringDate(dayFrom+ " 00:00:00"), 
																				convertStringDate(dayTo+ " 23:59:59"), 
																				StaticValues.SORT_BY_DATE, true);
			db.close();
			out.write(mContext.getResources().getString(R.string.html_main_table_begin));
			
			out.write(String.format(mContext.getResources().getString(R.string.html_main_table_title_row), 
					mContext.getResources().getString(R.string.str_number),
					mContext.getResources().getString(R.string.str_card),
					mContext.getResources().getString(R.string.str_date_time),
					mContext.getResources().getString(R.string.str_place),
					mContext.getResources().getString(R.string.str_type),
					mContext.getResources().getString(R.string.str_category),
					mContext.getResources().getString(R.string.str_amount),
					mContext.getResources().getString(R.string.str_remainder) ));
			
			int i=1;
			String rowColor;
			String dayString;
			String type;
			for(TransactionEntry t:trs){
				if((i & 1) == 0){ //even
					rowColor = "#DBFFF7";
				}else{//odd
					rowColor = "#FFFFFF";
				}
				
				if(t.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE){
					type = mContext.getResources().getString(R.string.str_spent);
				}else{
					type = mContext.getResources().getString(R.string.str_earned);
				}
				dayString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(t.getDateTime()));
				
				DatabaseHandler dbs = new DatabaseHandler(mContext);
				CategoryEntry cat = dbs.getCategory(t.getExpCategory());
				dbs.close();
				
				out.write(String.format(mContext.getResources().getString(R.string.html_main_table_row),
						rowColor,
						String.valueOf(i),
						t.getCard(),
						dayString,
						t.getPlace(),
						type,
						cat.getName(),
						String.valueOf(t.getAmount()) + " " + t.getAmountCurr(),
						String.valueOf(t.getRemainder()) + " " + t.getRemainderCurr() ));
				i++;
			}
			out.write(mContext.getResources().getString(R.string.html_main_table_end));
		}catch(IOException e){
			myLog.LOGD(LOG, "Error 03");
			myLog.LOGD(LOG, e.getMessage());
			throw e;
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
	
	private long convertStringDate(String strDate){
		try {
			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = (Date)formatter.parse(strDate);
			return date.getTime();
		}catch (ParseException ex){
			myLog.LOGD(LOG, "Error 02");
			myLog.LOGD(LOG, ex.getMessage()); 
			return 0;
		}
	}

	private String makeHtmlFileName(){	
		return Environment.getExternalStorageDirectory()+ "/" + StaticValues.DIR_NAME + "/" + 
					StaticValues.HTML_REPORT_PREFIX + dayFrom.replace("/", "_") + "--" + 
					dayTo.replace("/", "_") + ".html";
	}
}
