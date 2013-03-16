package com.example.alexfed.raiffstat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class ReportListPlace extends ListActivity {


	private final String LOG = "ReportListPlace";
	private List<TransactionEntry> transactions;
	private String dayFrom;
	private String dayTo;
	private String place;
	private int sortItemIndex = 0;
	private int sortType;
	
	private static final int BASIC_BORDER = 20;
	private static final int BASIC_FONT_SIZE = 14;
	private static final int BASIC_VLABEL_WIDTH = 100;
	
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
		setTitle(place);
		
		sortType = StaticValues.SORT_BY_DATE;
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
    			doSummary();
    			return true;
    		case R.id.menu_sort:
    			doSort();
    			return true;
    		case R.id.menu_graph:
    			doDrawGraph();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
		}
   
	}
	
	
	private void inflateList(){
		getListView().setDivider(null);
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
		
		for (TransactionEntry t : transactions) {
			if(!currs.contains(t.getAmountCurr()) &&
					(t.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE)){
				currs.add(t.getAmountCurr());
				sum.put(t.getAmountCurr(), 0.0);
			}
			
			if(t.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE){
				sum.put(t.getAmountCurr(), sum.get(t.getAmountCurr())+t.getAmount());
				card = t.getCard();
			}
		}
		

		message += getResources().getString(R.string.str_place) + ": " + place + "\r\n";
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
		message += "\r\n"+ getResources().getString(R.string.str_tr_number) + ": " + transactions.size();
		
		new AlertDialog.Builder(this)
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
			  Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_one_value), 
					  Toast.LENGTH_LONG).show();
			  return;
		  }
		  
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  final CharSequence[] choiceList;

		  CharSequence[] choiceListLocal = {getResources().getString(R.string.dialog_sort_by_date),
						getResources().getString(R.string.dialog_sort_by_amount)};
			  
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

	  private void doDrawGraph(){
		  
		LinearLayout graphLayout = new LinearLayout(getBaseContext());
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		  
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> trs = db.getTransactionsDateIntervalPlace(convertStringDate(dayFrom+ " 00:00:00"), 
				convertStringDate(dayTo+ " 23:59:59"), place, StaticValues.SORT_BY_DATE, false);
		db.close();
		  if(trs.size()<2){
			  Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_one_value), 
					  Toast.LENGTH_LONG).show();
			  return;
		  }
		  GraphView graphView;
		  GraphViewData[] data = new GraphViewData[transactions.size()];
		  final ArrayList<Long> dateList = new ArrayList<Long>();
		  double maxV = Double.MIN_VALUE;
		  double minV = Double.MAX_VALUE;
		  final int count = trs.size();
		  int i = 0;
		  
		  for (TransactionEntry t : trs) {
			  data[i] = new GraphViewData(/*t.getDateTime()*/i, t.getAmount());
			  maxV = Math.max(data[i].valueY, maxV);
			  minV = Math.min(data[i].valueY, minV);
			  dateList.add(t.getDateTime());
			  i++;
		  }
		  
		  graphView = new LineGraphView(this, place){ 
			   @Override  
			   protected String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {
			    	  int i = (int) value;
			    	  long date = dateList.get(i<=0?0:(i>count)?count-1:i-1);
			    	  String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(date));
			    	  return dateString;
			      } else {
			    	  /*double result = value * 10; 
			    	  result = Math.round(result);
			    	  result = result / 10;
			          return ""+result; */
			    	  
			          //return super.formatLabel(value, isValueX);
			    	  return "";
			      }
			   }  
		};
		  
		  int height = (int)getBaseContext().getResources().getDisplayMetrics().heightPixels/2;
		  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		  params.rightMargin = (int) convertDpToPixel(5, getBaseContext());
		  params.leftMargin = (int) convertDpToPixel(5, getBaseContext());
		  graphView.setLayoutParams(params);
		  
		  graphView.setBackgroundColor(Color.WHITE);
		// add data  
			graphView.addSeries(new GraphViewSeries(data)); 
			
			if(data.length >= 50)
				graphView.setViewPort(data.length-50, 50);   

			
			graphView.setScrollable(true);   
			// optional - activate scaling / zooming  
			graphView.setScalable(true);   
			graphView.setManualYAxisBounds(maxV+10, minV-10);
			//graphView.setHorizontalLabels(new String[] {"a", "b", "c"});
			graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK); 
			graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
			
			setLabelParams(graphView);
			
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
	  
	  private float convertPixelsToDp(float px,Context context){
		    Resources resources = context.getResources();
		    DisplayMetrics metrics = resources.getDisplayMetrics();
		    float dp = px / (metrics.densityDpi / 160f);
		    return dp;
		}
	  
	  private float screenHeightDP(Context context){
		  Resources resources = context.getResources();
		  DisplayMetrics metrics = resources.getDisplayMetrics();
		  float dp = metrics.heightPixels / (metrics.densityDpi / 160f);
		  return dp;
	  }
	  
	  private void setLabelParams(GraphView graphView){
			DisplayMetrics metrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(metrics);
	        int density = metrics.densityDpi;

	        if (density==DisplayMetrics.DENSITY_HIGH) {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER + 5);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE + 4);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH+50);
	        }
	        else if (density==DisplayMetrics.DENSITY_MEDIUM) {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH);
	        }
	        else if (density==DisplayMetrics.DENSITY_LOW) {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER - 5);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE - 4);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH-50);
	        }
	        else if (density==DisplayMetrics.DENSITY_XHIGH) {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER*2);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE*2);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH*2);
	        }
	        else if (density==DisplayMetrics.DENSITY_XXHIGH) {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER*2 + 5);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE*2 + 4);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH*2+50);
	        }
	        else {
	        	graphView.getGraphViewStyle().setLabelBorder(BASIC_BORDER);
	    		graphView.getGraphViewStyle().setLabelFontSize(BASIC_FONT_SIZE);
	    		graphView.getGraphViewStyle().setVerticalLabelWidth(BASIC_VLABEL_WIDTH);
	        }
		}

	  
    private static class ReportListAdapter extends BaseAdapter {
    	private final String LOG = "ReportListAdapter";
    	private LayoutInflater mInflater;
        private List<TransactionEntry> trs;
        private String place;
        private Context context;
        
        public ReportListAdapter(Context context, List<TransactionEntry> trs, String place) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.trs = trs;
            this.place = place; 
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
            	convertView = mInflater.inflate(R.layout.row_no_place, null);
               
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.card = (TextView) convertView.findViewById(R.id.card);
                holder.date_time = (TextView) convertView.findViewById(R.id.date_time);
                holder.amount = (TextView) convertView.findViewById(R.id.amount);
                holder.type = (TextView) convertView.findViewById(R.id.type);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            setListEntry(holder, this.trs.get(position));
           	    	  	    	
	    	return convertView;
        }
        
        static class ViewHolder {
            //TextView place;
            TextView date_time;
            TextView amount;
            TextView card;
            TextView type;
        }
        
        private void setListEntry(ViewHolder holder, TransactionEntry entry){
        	long longDate = entry.getDateTime();
        	String dayString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(longDate));
        	String timeString = new SimpleDateFormat("HH:mm:ss").format(new Date(longDate));
        	
        	if(entry.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
        		holder.date_time.setTextColor(Color.GREEN);
        		holder.card.setTextColor(Color.GREEN);
        		holder.amount.setTextColor(Color.GREEN);
        		holder.type.setTextColor(Color.GREEN);
        	}else{//expense
        		holder.date_time.setTextColor(Color.RED);
        		holder.card.setTextColor(Color.RED);
        		holder.amount.setTextColor(Color.RED);  
        		holder.type.setTextColor(Color.RED);
        	}
        	
        	holder.date_time.setText(dayString+"\r\n"+timeString); 

            holder.card.setText(context.getResources().getString(R.string.str_card) + ": " + entry.getCard());                
            holder.amount.setText(entry.getAmount() + " " + entry.getAmountCurr());
            holder.type.setText(""); 
            if(entry.getType() == StaticValues.TRANSACTION_TYPE_EXPENSE){
            	holder.type.setText("-");
            }else if(entry.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
            	holder.type.setText("+");
            }
        }
    }

}
