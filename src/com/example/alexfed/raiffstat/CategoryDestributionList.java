package com.example.alexfed.raiffstat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CategoryDestributionList extends ListActivity {

	private final String LOG = "CategoryDestributionList";
	private List<CategorizedPlaceModel> catPlaces;
	private ProgressDialog progressBar;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_report);
		
		setClickListeners();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		inflateList(); 
	}
	
	void setClickListeners(){
		ListView lv = getListView();
	     lv.setOnItemClickListener(
	    		 new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int pos, long id) {
						onListItemClick(arg1,pos,id); 
					} 
		});
	}
	
	protected void onListItemClick(View v, int pos, long id) {
		
		final int localPos = pos;
		final String[] items = new String [] {
        		getResources().getString(R.string.click_assign_category),
        		getResources().getString(R.string.click_remove_category)
        };
        
        ArrayAdapter<String> stringAdapter  = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder     = new AlertDialog.Builder(this);
        
        builder.setTitle(catPlaces.get(localPos).getPlaceName());
        builder.setAdapter( stringAdapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
            	
            	if(item == 0){
            		Intent myIntent;
                	myIntent = new Intent(CategoryDestributionList.this, CategoryList.class);
                	myIntent.putExtra("place", catPlaces.get(localPos).getPlaceName());
                	CategoryDestributionList.this.startActivity(myIntent);
            	}else if(item == 1){
            		removeCategoryWithProgressBar(localPos);
            	}else{
            		//Should not enter here
            	}
            }
        } );
 
        final AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void removeCategoryWithProgressBar(int pos){
		final int localPos = pos;
		
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_working));
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.show();
		
		new Thread(new Runnable() {
			public void run() {
				DatabaseHandler db = new DatabaseHandler(getBaseContext());
        		List<TransactionEntry> trs = db.getTransactionsPlace(catPlaces.get(localPos).getPlaceName());
        		for(TransactionEntry t : trs){
        			t.setExpCategory(StaticValues.EXPENSE_CATEGORY_UNKNOWN);
        			db.updateTransaction(t);
        		}
        		db.close();
			    progressBar.dismiss();
				CategoryDestributionList.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						inflateList();
					}
				});
					  
		}
		}).start();
	}
	
	private void inflateList(){
		getListView().setDivider(null);
		DatabaseHandler db = new DatabaseHandler(this);
		catPlaces = db.getCategorizedPlaces();
		db.close();
		setListAdapter(new CategoryDestributionListAdapter(this, catPlaces));
	}
	
	private static class CategoryDestributionListAdapter extends BaseAdapter {
    	private final String LOG = "CategoryDestributionListAdapter";
    	private LayoutInflater mInflater;
        private List<CategorizedPlaceModel> catPlaces;
        private Context context;
        
        public CategoryDestributionListAdapter(Context context, List<CategorizedPlaceModel> catPlaces) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.catPlaces = catPlaces;
            this.context = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return this.catPlaces.size();
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
            return catPlaces.get(position);
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
            	convertView = mInflater.inflate(R.layout.row_categorized_places, null);
                 
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();

                holder.place = (TextView) convertView.findViewById(R.id.place);
                holder.category = (TextView) convertView.findViewById(R.id.category);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            setListEntry(holder, this.catPlaces.get(position));
           	    	  	    	
	    	return convertView;
        }
        
        static class ViewHolder {
            TextView place;
            TextView category;
        }
        
        private void setListEntry(ViewHolder holder, CategorizedPlaceModel entry){
        	holder.place.setText(entry.getPlaceName());
        	if(entry.getCategoryName() != null){
        		holder.category.setTextColor(entry.getColor());
        		holder.category.setText(entry.getCategoryName());
        	}else{
        		holder.category.setTextColor(Color.BLACK);
        		holder.category.setText(context.getResources().getString(R.string.str_category_undefined));
        	}
        }
    }
}
