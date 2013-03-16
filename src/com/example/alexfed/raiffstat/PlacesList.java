package com.example.alexfed.raiffstat;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class PlacesList extends ListActivity {

	private final String LOG = "PlacesList";
	private List<String> places;
	private final int CTX_MENU_ITEM_DELETE = 0; 
	private final int CTX_MENU_ITEM_RENAME = 1;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_report);
		
		inflateList();
		setClickListeners();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		inflateList();
	}

	private void inflateList(){
		getListView().setDivider(null);
		queryDistinctPlaces();
		setListAdapter(new PlacesListAdapter(this, places));
	}

	private void queryDistinctPlaces(){	
		DatabaseHandler db = new DatabaseHandler(this);
		places = db.getDistinctPlacesForPlaceList();
		db.close();
	}
	
	private void queryDeletePlace(String place){	
		DatabaseHandler db = new DatabaseHandler(this);
		List<TransactionEntry> trs = db.getTransactionsPlaceFixed(place);
		
		for (TransactionEntry t:trs){
			t.setPlace(t.getTerminal());
			t.setInPlace(0);
			db.updateTransaction(t);
		}
		db.close();
	}
	
	void setClickListeners(){
		ListView lv = getListView();
	     lv.setOnItemLongClickListener( 
	    		 new AdapterView.OnItemLongClickListener(){ 
	    			 @Override 
	    		     public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) { 
	    				 onLongListItemClick(v,pos,id); 
	    		         return false; 
	    		     } 
	      }); 
	     
	     lv.setOnItemClickListener(
	    		 new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int pos, long id) {
						Intent myIntent;
				    	myIntent = new Intent(PlacesList.this, PlaceDetailedList.class);
				    	myIntent.putExtra("place", places.get(pos));
				    	PlacesList.this.startActivity(myIntent);
					} 
		});
	}
	
	protected void onLongListItemClick(View v, int pos, long id) { 
		final int localPos = pos;
		final String[] items = new String [] {
        		getResources().getString(R.string.long_click_place_delete),
        		getResources().getString(R.string.long_click_place_rename)
        };
        
        ArrayAdapter<String> stringAdapter  = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder     = new AlertDialog.Builder(this);
        
        //builder.setTitle(getResources().getString(R.string.long_click_ctx_dialog_title));
        builder.setTitle(places.get(localPos));
        builder.setAdapter( stringAdapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
            	
            	if(item == CTX_MENU_ITEM_DELETE){
            		//Delete the place here
            		queryDeletePlace(places.get(localPos));
            		inflateList();
            	}else if(item == CTX_MENU_ITEM_RENAME){
            		//Handle item rename here
            		renamePlace(places.get(localPos));
            	}else{
            		//Should not enter here
            	}
            }
        } );
 
        final AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	  private void renamePlace(String place){
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  alert.setTitle(R.string.long_click_place_rename);
		  alert.setMessage("Input new place name");
		  alert.setMessage(R.string.ctx_new_place_name);
		  final String localPlace = place;
		  // Set an EditText view to get user input 
		  final EditText input = new EditText(this);
		  alert.setView(input);
		  final Context context = getBaseContext();
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(!value.isEmpty()){
		    	if(value.equalsIgnoreCase(getResources().getString(R.string.spinner_all))){
		    		//Toast.makeText(getApplicationContext(), "Place " + value + " is not allowed", Toast.LENGTH_LONG).show();
		    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_place) + " " + value + " " + 
		    				getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	
			    DatabaseHandler db = new DatabaseHandler(context);
			    List<TransactionEntry> transactions = db.getTransactionsPlaceFixed(localPlace);
			    for(TransactionEntry t : transactions){
			    	t.setPlace(value);
			    	t.setInPlace(1);
			    	db.updateTransaction(t);
			    }
			    db.close();
			    inflateList();
			}else{
		    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_forbidden_empty_place), Toast.LENGTH_LONG).show(); 
		    }
		  }
		  });

		  alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		      // Canceled.
		    }
		  });

		  alert.show();
	  }

    private static class PlacesListAdapter extends BaseAdapter {
    	private LayoutInflater mInflater;
        private List<String> places;
        
        public PlacesListAdapter(Context context, List<String> places) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.places = places;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return this.places.size();
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
            return places.get(position);
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
            	convertView = mInflater.inflate(R.layout.row_place, null); 
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.place = (TextView) convertView.findViewById(R.id.place);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            setListEntry(holder, this.places.get(position));
           	    	  	    	
	    	return convertView;
        }
        
        static class ViewHolder {
            TextView place;
        }
        
        private void setListEntry(ViewHolder holder, String place){
        		holder.place.setText(place);
        }
    }
}
	  
