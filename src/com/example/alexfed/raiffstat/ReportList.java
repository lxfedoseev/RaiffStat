package com.example.alexfed.raiffstat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ReportList extends ListActivity {

	private final String LOG = "ReportList";
	private List<TransactionEntry> transactions;
	private String dayFrom;
	private String dayTo;
	
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
    			double sum = 0.0;
    			String card = "";
    			for (TransactionEntry t : transactions) {
    				sum += t.getAmmount();
    				card = t.getCard();
    			}
    			//TODO: correct ammount for different currencies
    			message = "Card: " + card + "\r\n" +
    						"Ammount: " + sum + "\r\n" + 
    					"Transactions number: " + transactions.size();
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
		queryDateInterval(convertStringDate(dayFrom+ " 00:00:00"), convertStringDate(dayTo+ " 23:59:59"));
		setListAdapter(new ReportListAdapter(this, transactions));
	}
	
	private void queryDateInterval(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(this);
		transactions = db.getTransactionsDateInterval(start, end);
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
        
        public ReportListAdapter(Context context, List<TransactionEntry> trs) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.trs = trs;
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
                convertView = mInflater.inflate(R.layout.row, null);
               
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.card = (TextView) convertView.findViewById(R.id.card);
                holder.date_time = (TextView) convertView.findViewById(R.id.date_time);
                holder.place = (TextView) convertView.findViewById(R.id.place);
                holder.ammount = (TextView) convertView.findViewById(R.id.ammount);

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
            TextView ammount;
            TextView card;
        }
        
        private void setListEntry(ViewHolder holder, TransactionEntry entry){
        	
        	long longDate = entry.getDateTime();
        	String dayString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(longDate));
        	String timeString = new SimpleDateFormat("HH:mm:ss").format(new Date(longDate));
        	
        	holder.date_time.setText(dayString+"\r\n"+timeString); 
            holder.place.setText(entry.getPlace()); 
            holder.card.setText("Card: " + entry.getCard());                
            holder.ammount.setText(entry.getAmmount() + " " + entry.getAmmountCurr());
        }
    }
}
