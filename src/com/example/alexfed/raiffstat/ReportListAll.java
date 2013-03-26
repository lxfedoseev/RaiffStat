package com.example.alexfed.raiffstat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
	
	static final int SUMMARY_ID = Menu.FIRST;
    static final int SORT_ID = Menu.FIRST+1;
    static final int GRAPH_ID = Menu.FIRST+2;
    static final int PERIOD_ID = Menu.FIRST+3;
    
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
		return view;  
	}

	
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
		if(bundleEmpty){
			MenuItem periodItem = menu.add(Menu.NONE, PERIOD_ID, 0, R.string.menu_period);
			periodItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		MenuItem summaryItem = menu.add(Menu.NONE, SUMMARY_ID, 0, R.string.menu_summary);
		summaryItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    MenuItem sortItem = menu.add(Menu.NONE, SORT_ID, 0, R.string.menu_sort);
	    sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    MenuItem graphItem = menu.add(Menu.NONE, GRAPH_ID, 0, R.string.menu_graph);
	    graphItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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
            case GRAPH_ID:
            	doDrawGraph();
    			return true;
            case PERIOD_ID:
            	//TODO:
    			return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	private void doDrawGraph(){
		getActivity();
		LinearLayout graphLayout = new LinearLayout(activity);
		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		  
		DatabaseHandler db = new DatabaseHandler(activity);
		List<TransactionEntry> trs = db.getTransactionsForGraph(convertStringDate(dayFrom+ " 00:00:00"), 
				convertStringDate(dayTo+ " 23:59:59"), getResources().getString(R.string.spinner_all), StaticValues.CURR_RUB);
		if(trs.size()<1){
			  Toast.makeText(activity, getResources().getString(R.string.toast_no_values) + " " + StaticValues.CURR_RUB, 
					  Toast.LENGTH_LONG).show();
			  db.close();
			  return;
			  
		  }
		
		Map<Integer, Double> sum = new HashMap<Integer, Double>();
		Set<Integer> cats = new HashSet<Integer>();
		double totalSum = 0.0;
		for (TransactionEntry t : trs) {
			if(!cats.contains(t.getExpCategory())){
				cats.add(t.getExpCategory());
				sum.put(t.getExpCategory(), 0.0);
			}
			sum.put(t.getExpCategory(), sum.get(t.getExpCategory())+t.getAmount());
			totalSum += t.getAmount();
		}
		
		GraphViewSeries[] sers = new GraphViewSeries[cats.size()];
		int i = 0;
		String catName = "";
		int catColor = 0xff000000;
		int thickness = 50;
		double maxVal = Double.MIN_VALUE;
		double percents = 0.0;
		for(Integer c: cats){
			if(c == StaticValues.EXPENSE_CATEGORY_UNKNOWN){
				catName = getResources().getString(R.string.str_category_undefined);
				catColor = 0xff000000;
			}else{
				CategoryEntry cat = db.getCategory(c);
				catName = cat.getName();
				catColor = cat.getColor();
			}
			percents = (sum.get(c)/totalSum)*100;
			maxVal = Math.max(maxVal, percents);
			GraphViewData[] data = new GraphViewData[2];
			data[0] = new GraphViewData(i+1, 0);
			data[1] = new GraphViewData(i+1, percents);
			percents = Math.round(percents);
			if(percents < 0.1){
				catName = catName + " < 0.1%";
			}else{
				int per = (int) percents;
				catName = catName + " " + per + "%";
			}
			sers[i] = new GraphViewSeries(catName, 
					new GraphViewSeries.GraphViewSeriesStyle(catColor, thickness), data);
			i++;
		}
		db.close();

		LineGraphView graphView = new LineGraphView(activity, "%"){ 
			   @Override  
			   protected String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {
			    	  /*int i = (int) value;
			    	  long date = dateList.get(i<=0?0:(i>=count)?count-1:i);
			    	  String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(date));
			    	  return dateString;*/
			    	  return "";
			      } else {
			    	  /*double result = value * 10; 
			    	  result = Math.round(result);
			    	  result = result / 10;
			          return ""+result; */
			    	  
			          return super.formatLabel(value, isValueX);
			    	  //return "";
			      }
			   }  
		}; 
		
		 int height = (int)activity.getResources().getDisplayMetrics().heightPixels/2;
		 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		 params.rightMargin = (int) convertDpToPixel(5, activity);
		 params.leftMargin = (int) convertDpToPixel(5, activity);
		 graphView.setLayoutParams(params);
		  
		  graphView.setBackgroundColor(Color.WHITE);
			for(i=0; i<cats.size();i++){
				graphView.addSeries(sers[i]);
			}
		  
		  graphView.setViewPort(0,cats.size()+1);
		  graphView.setScrollable(false);   
		  graphView.setScalable(false);   
		  graphView.setManualYAxisBounds(maxVal, 0);
		  graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK); 
		  graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		  graphView.setShowLegend(true); 
		  graphView.setLegendAlign(GraphView.LegendAlign.TOP);  
		  graphView.setLegendWidth(200); 
			
			//setLabelParams(graphView);
			
			graphLayout.addView(graphView); 
		  
		  alert.setView(graphLayout);
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		  });

		  alert.show(); 
	}
	
	private float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}
	
	private void inflateList(){
		getListView().setDivider(null);
		queryDateIntervalPlace(convertStringDate(dayFrom+ " 00:00:00"), convertStringDate(dayTo+ " 23:59:59"), getResources().getString(R.string.spinner_all));
		setListAdapter(new ReportListAdapter(activity, transactions));
	}
	
	private void queryDateInterval(long start, long end){	
		DatabaseHandler db = new DatabaseHandler(activity);
		transactions = db.getTransactionsDateInterval(start, end);
		db.close();
	}
	
	private void queryDateIntervalPlace(long start, long end, String place){	
		DatabaseHandler db = new DatabaseHandler(activity);
		transactions = db.getTransactionsDateIntervalPlace(start, end, place, sortType, true);
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
		
		String message = "";
		String card = "";
		
		Map<String, Double> sum = new HashMap<String, Double>();
		Set<String> currs = new HashSet<String>();
		
		Map<String, Double> sumIncom = new HashMap<String, Double>();
		Set<String> currsIncom = new HashSet<String>();
		
		for (TransactionEntry t : transactions) {
			if(!currs.contains(t.getAmountCurr()) &&
					(t.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE)){
				currs.add(t.getAmountCurr());
				sum.put(t.getAmountCurr(), 0.0);
			}
			if(!currsIncom.contains(t.getAmountCurr()) && 
					(t.getType() == StaticValues.TRANSACTION_TYPE_INCOME)){
				currsIncom.add(t.getAmountCurr());
				sumIncom.put(t.getAmountCurr(), 0.0);
			}
			
			if(t.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE){
				sum.put(t.getAmountCurr(), sum.get(t.getAmountCurr())+t.getAmount());
				card = t.getCard();
			}else if(t.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
				sumIncom.put(t.getAmountCurr(), sumIncom.get(t.getAmountCurr())+t.getAmount());
				card = t.getCard();
			}
		}
		
		message += getResources().getString(R.string.str_period) + ": " + dayFrom + "~" + dayTo + "\r\n" + 
				getResources().getString(R.string.str_card) + ": " + card + "\r\n" + 
				getResources().getString(R.string.str_spent) + ": ";
		//round sum values
		for(String s: currs){
			double rndSum = sum.get(s) * 100;
			rndSum = Math.round(rndSum);
			rndSum /=100;
			message += ""+rndSum+ " " + s + ", "; 
		}
		message = message.substring(0, message.length()-2); // remove last comma[space] ", "
		
		if(currsIncom.size()>0){
			message += "\r\n" + getResources().getString(R.string.str_earned) + ": ";
			for(String s: currsIncom){
				double rndSum = sumIncom.get(s) * 100;
				rndSum = Math.round(rndSum);
				rndSum /=100;
				message += ""+rndSum+ " " + s + ", "; 
			}
			message = message.substring(0, message.length()-2); // remove last comma[space] ", "
		}
		
		message += "\r\n"+ getResources().getString(R.string.str_tr_number) + ": " + transactions.size();
		
		new AlertDialog.Builder(activity)
	    .setMessage(message)
	    .setPositiveButton(R.string.dialog_ok, new android.content.DialogInterface.OnClickListener() {                
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	                       
	        }
	    })
	    .show(); 
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
        		int myGreen = Color.argb(255, 0, 127, 14);
        		//holder.date_time.setTextColor(myGreen);
        		//holder.card.setTextColor(myGreen);
        		//holder.amount.setTextColor(myGreen);
        		//holder.type.setTextColor(myGreen);
        		holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.income_logo));
        	}else{//expense
        		//holder.date_time.setTextColor(Color.RED);
        		//holder.card.setTextColor(Color.RED);
        		//holder.amount.setTextColor(Color.RED);
        		//holder.type.setTextColor(Color.RED);
        		holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.outcome_logo));
        	}
        	
        	holder.date_time.setText(dayString+"\r\n"+timeString); 
        	holder.place.setText(entry.getPlace());
            holder.card.setText(context.getResources().getString(R.string.str_card) + ": " + entry.getCard());                
            holder.amount.setText(entry.getAmount() + " " + entry.getAmountCurr());
           /* holder.type.setText(""); 
            if(entry.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE){
            	holder.type.setText("-");
            }else if(entry.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
            	holder.type.setText("+");
            }*/
        }
    }

}
