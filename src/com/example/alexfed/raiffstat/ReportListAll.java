package com.example.alexfed.raiffstat;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;


public class ReportListAll extends SherlockListFragment {


	private final String LOG = "ReportListAll";
	private List<TransactionEntry> transactions;
	private String dayFrom;
	private String dayTo;
	private int sortItemIndex = 0;
	private int sortType;
	private boolean bundleEmpty = true;
	private FragmentActivity activity;
	private DatePicker datePicker;
	private TextView mTextEmpty;
	
	static final int SUMMARY_ID = Menu.FIRST;
    static final int SORT_ID = Menu.FIRST+1;
    static final int PERIOD_ID = Menu.FIRST+2;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activity = getActivity();	
		sortType = StaticValues.SORT_BY_DATE;
        Bundle bundle = getArguments();
		bundleEmpty = bundle ==null;
		if(!bundleEmpty){
			dayFrom = bundle.getString("day_from");
			dayTo = bundle.getString("day_to");
		}
	}
	
	@Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);
        inflateList();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_raiff_report, null);
		mTextEmpty = (TextView) view.findViewById(android.R.id.empty);
		return view;  
	}
	/*
	 * https://github.com/JakeWharton/ActionBarSherlock/issues/272#issuecomment-4004069
	 * 
	 * Modifying com.actionbarsherlock.internal.view.menu.ActionMenuView to inherit from 
	 * LinearLayout instead of IcsLinearLayout as proposed earlier in this thread seems 
	 * to be a valid workaround for this issue in 4.1.0, side effect being that dividers 
	 * will not display between action items.
	 */
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
		MenuItem summaryItem = menu.add(Menu.NONE, SUMMARY_ID, 0, R.string.menu_summary);
		//summaryItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		summaryItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		summaryItem.setIcon(R.drawable.ic_action_line_chart);
		
	    MenuItem sortItem = menu.add(Menu.NONE, SORT_ID, 0, R.string.menu_sort);
	    //sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		if(bundleEmpty){
			MenuItem periodItem = menu.add(Menu.NONE, PERIOD_ID, 0, R.string.menu_period);
			//periodItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			periodItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
		
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SUMMARY_ID:
            	doSummary();
                return true;
            case SORT_ID:
            	doSort();
    			return true;
            case PERIOD_ID:
            	showDatePickerDialog(true);
    			return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
    private void showDatePickerDialog(boolean isFrom) {
    	final boolean localIsFrom = isFrom;
    	AlertDialog.Builder alert = new AlertDialog.Builder(activity);
    	datePicker = new DatePicker(activity);
    	final Calendar c = Calendar.getInstance();
    	if(isFrom)
    		c.add(Calendar.DAY_OF_YEAR, -7);
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
        
    	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 11) {
			 try {
			    Method m = datePicker.getClass().getMethod("setCalendarViewShown", boolean.class);
			    m.invoke(datePicker, false);
			  }
			  catch (Exception e) {} // eat exception in our case
		}
		
		if(isFrom){
			alert.setTitle(R.string.dialog_period_start);
		}else{
			alert.setTitle(R.string.dialog_period_end);
		}
		alert.setPositiveButton(R.string.dialog_ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int actualMonth = datePicker.getMonth() + 1;
				if(localIsFrom){
					dayFrom = datePicker.getDayOfMonth() + "/" + actualMonth + "/" + datePicker.getYear();
					showDatePickerDialog(false);
				}else{
					dayTo = datePicker.getDayOfMonth() + "/" + actualMonth + "/" + datePicker.getYear();
					inflateList();
				}
			}
		});
		alert.setNegativeButton(R.string.dialog_cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
				
			}
		});
    	alert.setView(datePicker);
    	alert.show();
    }
    	
	private void inflateList(){
		getListView().setDivider(null);
		queryDateIntervalPlace(convertStringDate(dayFrom+ " 00:00:00"), convertStringDate(dayTo+ " 23:59:59"));
		setListAdapter(new ReportListAdapter(activity, transactions));
		if(transactions.isEmpty() && !bundleEmpty){
			mTextEmpty.setText(R.string.str_period_no_data);
		}else if(bundleEmpty){
			mTextEmpty.setText(R.string.str_period_use_menu); 
		}
	}
	
	private void queryDateIntervalPlace(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(activity);
		transactions = db.getTransactionsDateInterval(start, end, sortType, true);
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
	
	private void doSummary(){
    	if(dayFrom == null || dayTo == null){
    		//TODO: toast something
    		return;
    	}
    	if(transactions.isEmpty()){
    		//TODO: toast something
    		return;
    	}
    	DatabaseHandler db = new DatabaseHandler(activity);
		List<TransactionEntry> trs = db.getTransactionsForGraph(convertStringDate(dayFrom+ " 00:00:00"), 
				convertStringDate(dayTo+ " 23:59:59"), StaticValues.CURR_RUB);
		db.close();
		if(trs.size()<1){
			  Toast.makeText(activity, getResources().getString(R.string.toast_no_values) + " " + StaticValues.CURR_RUB, 
					  Toast.LENGTH_LONG).show();
		}
    	Intent myIntent = new Intent(activity, ReportSummary.class);
    	myIntent.putExtra("day_from", dayFrom);
    	myIntent.putExtra("day_to", dayTo);
    	activity.startActivity(myIntent);
	}
	
	  private void doSort(){
		  if(transactions.size()<2){
			  Toast.makeText(activity, getResources().getString(R.string.toast_one_value), 
					  Toast.LENGTH_LONG).show();
			  return;
		  }
		  
		  AlertDialog.Builder alert = new AlertDialog.Builder(activity);

		  final CharSequence[] choiceList;
		  CharSequence[] choiceListLocal = {getResources().getString(R.string.dialog_sort_by_date),
					  							getResources().getString(R.string.dialog_sort_by_amount),
					  							getResources().getString(R.string.dialog_sort_by_place)};
		  choiceList = choiceListLocal;

		  alert.setTitle(getResources().getString(R.string.dialog_sort_type));		  
		  sortItemIndex = -1;
		  alert.setSingleChoiceItems(choiceList, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sortItemIndex = which; 
			}
		});
		alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(sortItemIndex > -1){
					sortType = sortItemIndex;
					inflateList();
				}else{
					Toast.makeText(activity, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
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
	  

    private static class ReportListAdapter extends BaseAdapter {
    	private final String LOG = "ReportListAdapter";
    	private LayoutInflater mInflater;
        private List<TransactionEntry> trs;
        private Context context;
        
        public ReportListAdapter(Context context, List<TransactionEntry> trs) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.trs = trs;
            this.context = context;
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
                holder.amount = (TextView) convertView.findViewById(R.id.amount);
                //holder.type = (TextView) convertView.findViewById(R.id.type);
                holder.type = (ImageView) convertView.findViewById(R.id.type);

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
            //TextView type;
            ImageView type;
        }
        
        private void setListEntry(ViewHolder holder, TransactionEntry entry){
        	long longDate = entry.getDateTime();
        	String dayString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(longDate));
        	String timeString = new SimpleDateFormat("HH:mm:ss").format(new Date(longDate));
        	
        	if(entry.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
        		holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.income_logo));
        	}else{//expense
        		holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.outcome_logo));
        	}
        	
        	holder.date_time.setText(dayString+"\r\n"+timeString); 
        	holder.place.setText(entry.getPlace());
            holder.card.setText(context.getResources().getString(R.string.str_card) + ": " + entry.getCard());                
            holder.amount.setText(entry.getAmount() + " " + entry.getAmountCurr());
        }
    }

}
