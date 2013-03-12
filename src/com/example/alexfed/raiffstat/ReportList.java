package com.example.alexfed.raiffstat;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportList extends ListActivity {

	private final String LOG = "ReportList";
	private List<TransactionEntry> transactions;
	private String dayFrom;
	private String dayTo;
	private String place;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_report);
		dayFrom = getIntent().getStringExtra("day_from");
		dayTo = getIntent().getStringExtra("day_to");
		place = getIntent().getStringExtra("place");
		if(!place.equalsIgnoreCase("All")){
			setTitle(place);
		}
				
		inflateList();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_report_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
    		case R.id.menu_summary:
    			String message = "";
    			String card = "";
    			Map<String, Double> sum = new HashMap<String, Double>();
    			Set<String> currs = new HashSet<String>();
    			for (TransactionEntry t : transactions) {
    				if(!currs.contains(t.getAmountCurr())){
    					currs.add(t.getAmountCurr());
    					sum.put(t.getAmountCurr(), 0.0);
    				}
    				sum.put(t.getAmountCurr(), sum.get(t.getAmountCurr())+t.getAmount());
    				card = t.getCard();
    			}
    			if(!place.equalsIgnoreCase("All"))
    				message += "Place: " + place + "\r\n";
    			message += "Period: " + dayFrom + "~" + dayTo + "\r\n" + 
    					"Card: " + card + "\r\n" + 
    					"Amount: ";
    			//round sum values
    			for(String s: currs){
    				double rndSum = sum.get(s) * 100;
    				rndSum = Math.round(rndSum);
    				rndSum /=100;
    				message += ""+rndSum+ " " + s + ", "; 
    			}
    			message = message.substring(0, message.length()-2); // remove last comma[space] ", "
    			message += "\r\nTransactions number: " + transactions.size();
    			new AlertDialog.Builder(this)
    		    .setMessage(message)
    		    .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {                
    		        @Override
    		        public void onClick(DialogInterface dialog, int which) {
    		                       
    		        }
    		    })
    		    .show(); 
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
		}
   
	}
	
	
	private void inflateList(){
		queryDateIntervalPlace(convertStringDate(dayFrom+ " 00:00:00"), convertStringDate(dayTo+ " 23:59:59"), place);
		setListAdapter(new ReportListAdapter(this, transactions, place));
	}
	
	private void queryDateInterval(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(this);
		transactions = db.getTransactionsDateInterval(start, end);
		db.close();
	}
	
	private void queryDateIntervalPlace(long start, long end, String place){	
		DatabaseHandler db = new DatabaseHandler(this);
		transactions = db.getTransactionsDateIntervalPlace(start, end, place);
		db.close();
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

    private static class ReportListAdapter extends BaseAdapter {
    	private LayoutInflater mInflater;
        private List<TransactionEntry> trs;
        private String place;
        
        public ReportListAdapter(Context context, List<TransactionEntry> trs, String place) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.trs = trs;
            this.place = place; 
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return this.trs.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return trs.get(position);
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
            	if(place.equalsIgnoreCase("All")){
            		convertView = mInflater.inflate(R.layout.row, null);
            	}else{
            		convertView = mInflater.inflate(R.layout.row_no_place, null);
            	}
               
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.card = (TextView) convertView.findViewById(R.id.card);
                holder.date_time = (TextView) convertView.findViewById(R.id.date_time);
                if(place.equalsIgnoreCase("All")){
                	holder.place = (TextView) convertView.findViewById(R.id.place);
                }
                holder.amount = (TextView) convertView.findViewById(R.id.amount);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            setListEntry(holder, this.trs.get(position));
           	    	  	    	
	    	return convertView;
        }
        
        static class ViewHolder {
            TextView place;
            TextView date_time;
            TextView amount;
            TextView card;
        }
        
        private void setListEntry(ViewHolder holder, TransactionEntry entry){
        	
        	long longDate = entry.getDateTime();
        	String dayString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(longDate));
        	String timeString = new SimpleDateFormat("HH:mm:ss").format(new Date(longDate));
        	
        	holder.date_time.setText(dayString+"\r\n"+timeString); 
        	if(place.equalsIgnoreCase("All")){
        		holder.place.setText(entry.getPlace());
        	}
            holder.card.setText("Card: " + entry.getCard());                
            holder.amount.setText(entry.getAmount() + " " + entry.getAmountCurr());
        }
    }
}
