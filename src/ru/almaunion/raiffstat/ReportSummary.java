package ru.almaunion.raiffstat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class ReportSummary extends SherlockListActivity {
	private final String LOG = "ReportSummary";
	
	private List<SummaryHeadEntry> mHead;
	private List<SummaryBarEntry> mBar;
	private GraphView mGraph;
	private String dayFrom;
	private String dayTo;
	private ProgressDialog progressBar;
	static final int SHARE_ID = Menu.FIRST;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		dayFrom = getIntent().getStringExtra("day_from");
		dayTo = getIntent().getStringExtra("day_to");
	}

	@Override
	protected void onResume() {
		super.onResume();
		inflateList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	  MenuItem shareItem = menu.add(Menu.NONE, SHARE_ID, 0, R.string.click_share);
	  shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	  return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
    		case SHARE_ID:
    			doShareWithProgressBar();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
		}
   
  }
	
	private void inflateList(){
		mHead = new ArrayList<SummaryHeadEntry>();
		mBar = new ArrayList<SummaryBarEntry>();
		doInitHead();
		doInitGraph();
		setListAdapter(new MyCustomAdapter(this, mHead, mBar, mGraph));
	}
	
	private void doShareWithProgressBar(){
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_working));
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.show();
		
		new Thread(new Runnable() {
			public void run() {
				final ReportHtml rep = new ReportHtml(getBaseContext(), dayFrom, dayTo, mHead, mBar);
    			if(rep.saveHtml()){
    				progressBar.dismiss();
    				ReportSummary.this.runOnUiThread(new Runnable() {
    					@Override
    					public void run() {
    						Intent shareIntent = new Intent();
    	    				shareIntent.setAction(Intent.ACTION_SEND);
    	    				shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(rep.getFile()));
    	    				shareIntent.setType("text/html");
    	    				startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.click_share)));
    					}
    				});
    			}else{
    				progressBar.dismiss();
    				ReportSummary.this.runOnUiThread(new Runnable() {
    					@Override
    					public void run() {
    						Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.toast_exp_failed), 
    									Toast.LENGTH_LONG).show();
    					}
    				});
    			}	  
		}
		}).start();
	}
	
	private void doInitHead(){
		String message = "";
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> transactions = db.getTransactionsDateInterval(convertStringDate(dayFrom+ " 00:00:00"), 
				convertStringDate(dayTo+ " 23:59:59"), StaticValues.SORT_BY_DATE, true);
		db.close();
		
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
			}else if(t.getType() == StaticValues.TRANSACTION_TYPE_INCOME){
				sumIncom.put(t.getAmountCurr(), sumIncom.get(t.getAmountCurr())+t.getAmount());
			}
		}
		
		mHead.add(new SummaryHeadEntry(getResources().getString(R.string.str_period), dayFrom + "~" + dayTo));
		for(String s: currs){
			double rndSum = sum.get(s) * 100;
			rndSum = Math.round(rndSum);
			rndSum /=100;
			message += rndSum+ " " + s + "\n"; 
		}
		message = message.substring(0, message.length()-1); // remove last "\n"
		
		mHead.add(new SummaryHeadEntry(getResources().getString(R.string.str_spent), message));
		
		message = "";
		if(currsIncom.size()>0){
			for(String s: currsIncom){
				double rndSum = sumIncom.get(s) * 100;
				rndSum = Math.round(rndSum);
				rndSum /=100;
				message += rndSum+ " " + s + "\n"; 
			}
			message = message.substring(0, message.length()-1); // remove last "\n"
		}else{
			message = "0";
		}
		mHead.add(new SummaryHeadEntry(getResources().getString(R.string.str_earned), message));
		mHead.add(new SummaryHeadEntry(getResources().getString(R.string.str_tr_number), String.valueOf(transactions.size())));
	}
	
	private void doInitGraph(){
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> trs = db.getTransactionsForGraph(convertStringDate(dayFrom+ " 00:00:00"), 
				convertStringDate(dayTo+ " 23:59:59"), StaticValues.CURR_RUB);
		
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
		Map<Integer, Double> sortedSum = sortByComparator(sum);
		
		GraphViewSeries[] sers = new GraphViewSeries[cats.size()];
		int i = 0;
		String catName = "";
		int catColor = 0xff000000;
		int thickness = (int)((this.getResources().getDisplayMetrics().widthPixels)*0.07);//7% of the screen's width
		double maxVal = Double.MIN_VALUE;
		double percents = 0.0;
		
		for (Map.Entry<Integer, Double> entry : sortedSum.entrySet()) {
			
			if(((Integer)entry.getKey()) == StaticValues.EXPENSE_CATEGORY_UNKNOWN){
				catName = getResources().getString(R.string.str_category_undefined);
				catColor = 0xff000000;
			}else{
				CategoryEntry cat = db.getCategory(((Integer)entry.getKey()));
				catName = cat.getName();
				catColor = cat.getColor();
			}
			percents = ((Double)entry.getValue()/totalSum)*100;
			maxVal = Math.max(maxVal, percents);
			GraphViewData[] data = new GraphViewData[2];
			data[0] = new GraphViewData(i+1, 0);
			data[1] = new GraphViewData(i+1, percents);
			
			percents = percents*10;
			percents = Math.round(percents);
			percents /=10;
			
			double rndSum = (Double)entry.getValue() * 100;
			rndSum = Math.round(rndSum);
			rndSum /=100;
			
			if(percents < 1){
				mBar.add(new SummaryBarEntry(catColor, catName, 0.0, rndSum));
				catName = catName + " < 1%";
			}else{
//				/int per = (int) percents;
				mBar.add(new SummaryBarEntry(catColor, catName, percents, rndSum));
				catName = catName + " " + percents + "%";
			}
			sers[i] = new GraphViewSeries(catName, 
					new GraphViewSeries.GraphViewSeriesStyle(catColor, thickness), data);
			i++;
			
		}
		db.close();
		
		mGraph = new LineGraphView(this, ""){  
			   @Override  
			   protected String formatLabel(double value, boolean isValueX) {  
			      if (isValueX) {
			    	  /*int i = (int) value;
			    	  long date = dateList.get(i<=0?0:(i>=count)?count-1:i);
			    	  String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(date));
			    	  return dateString;*/
			    	  return "";
			      } else {
			    	  double result = value * 10; 
			    	  result = Math.round(result);
			    	  result = result / 10;
			          return ""+result+"%"; 
			    	  
			          //return super.formatLabel(value, isValueX);
			    	  //return "";
			      }
			   }  
		}; 
		
		int height = (int)this.getResources().getDisplayMetrics().heightPixels/2;
		 LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
		 params.rightMargin = (int) convertDpToPixel(5, this);
		 params.leftMargin = (int) convertDpToPixel(5, this);
		 mGraph.setLayoutParams(params);
		  
		 mGraph.setBackgroundColor(Color.WHITE);
			for(i=0; i<cats.size();i++){
				mGraph.addSeries(sers[i]);
			}
		  
		 mGraph.setViewPort(0,cats.size()+1);
		 mGraph.setScrollable(false);   
		 mGraph.setScalable(false);   
		 mGraph.setManualYAxisBounds(maxVal, 0);
		 mGraph.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK); 
		 mGraph.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		 mGraph.getGraphViewStyle().setDrawVerticalLines(false);
	}
	
	private float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}
	
	//http://www.mkyong.com/java/how-to-sort-a-map-in-java/
	private static Map sortByComparator(Map unsortMap) {
		 
		List list = new LinkedList(unsortMap.entrySet());
 
		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue())*(-1);
			}
		});
 
		// put sorted list into map again
                //LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
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
	
	private static class MyCustomAdapter extends BaseAdapter {
		private final String LOG = "MyCustomAdapter";
		private static final int TYPE_HEAD = 0;
        private static final int TYPE_GRAPH = 1;
        private static final int TYPE_BAR_INFO = 2;
        private static final int TYPE_MAX_COUNT = TYPE_BAR_INFO + 1;
        
        private List<SummaryHeadEntry> mHeadData = new ArrayList();
        private List<SummaryBarEntry> mBarData = new ArrayList();
        private GraphView mGraphView;
        private LayoutInflater mInflater;
        
        public MyCustomAdapter(Context context, List<SummaryHeadEntry> head, List<SummaryBarEntry> bar, GraphView graph) {
        	this.mHeadData = head;
        	this.mBarData = bar;
        	this.mGraphView = graph;
        	mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getItemViewType(int position) {
        	if(position < 4){
        		return TYPE_HEAD;
        	}else if(position == 4){
        		return TYPE_GRAPH;
        	}else{
        		return TYPE_BAR_INFO;
        	}
        }
        
        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
        
        @Override
        public int getCount() {
            return mHeadData.size() + mBarData.size() + 1;
        }
        
        @Override
        public Object getItem(int position) {
        	if(position < 4){
        		return mHeadData.get(position);
        	}else if(position == 4){
        		return mGraphView;
        	}else{
        		return mBarData.get(position - 5);
        	}
        }
 
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	HeadViewHolder holderHead = null;
        	GraphViewHolder holderGraph = null;
        	BarViewHolder holderBar = null;
        	
        	int type = getItemViewType(position);
        	if (convertView == null) {
        		
        		switch (type) {
	                case TYPE_HEAD:
	                    convertView = mInflater.inflate(R.layout.row_summary_head, null);
	                    holderHead = new HeadViewHolder();
	                    holderHead.title = (TextView)convertView.findViewById(R.id.title);
	                    holderHead.info = (TextView)convertView.findViewById(R.id.info);
	                    convertView.setTag(holderHead);
	                    break;
	                case TYPE_GRAPH:
	                    convertView = mInflater.inflate(R.layout.row_summary_graph, null);
	                    holderGraph = new GraphViewHolder();
	                    holderGraph.layout = (LinearLayout)convertView.findViewById(R.id.graph);
	                    convertView.setTag(holderGraph);
	                    break;
	                case TYPE_BAR_INFO:
	                    convertView = mInflater.inflate(R.layout.row_summary_bar, null);
	                    holderBar = new BarViewHolder();
	                    holderBar.name = (TextView)convertView.findViewById(R.id.name);
	                    holderBar.percent = (TextView)convertView.findViewById(R.id.percent);
	                    holderBar.amount = (TextView)convertView.findViewById(R.id.amount);
	                    holderBar.color = (SurfaceView) convertView.findViewById(R.id.color);
	                    convertView.setTag(holderBar);
	                    break;
        		}
        	}else {
        		switch (type) {
	                case TYPE_HEAD:
	                	holderHead = (HeadViewHolder)convertView.getTag();
	                    break;
	                case TYPE_GRAPH:
	                	holderGraph = (GraphViewHolder)convertView.getTag();
	                    break;
	                case TYPE_BAR_INFO:
	                	holderBar = (BarViewHolder)convertView.getTag();
	                    break;
        		}
        	}
        	
        	switch (type) {
	            case TYPE_HEAD:
	            	holderHead.title.setText(mHeadData.get(position).getTitle());
	            	holderHead.info.setText(mHeadData.get(position).getInfo());
	                break;
	            case TYPE_GRAPH:
	            	holderGraph.layout.removeAllViews();
	            	holderGraph.layout.addView(mGraphView);
	                break;
	            case TYPE_BAR_INFO:
	            	holderBar.name.setText(mBarData.get(position-5).getName());
	            	if(mBarData.get(position-5).getPercent() == 0.0){
	            		holderBar.percent.setText("<1%");
	            	}else{
	            		holderBar.percent.setText(mBarData.get(position-5).getPercent().toString()+"%");
	            	}
	            	holderBar.amount.setText(mBarData.get(position-5).getAmount().toString() + " " + StaticValues.CURR_RUB);
	            	holderBar.color.setBackgroundColor(mBarData.get(position-5).getColor());
	                break;
    		}
        	
        	return convertView;
        }
        
        public static class HeadViewHolder {
            public TextView title;
            public TextView info;
        }

        public static class GraphViewHolder {
        	public LinearLayout layout;
        }
        
        public static class BarViewHolder {
        	public SurfaceView color;
            public TextView name;
            public TextView percent;
            public TextView amount;
        }
       
	}
	
}
