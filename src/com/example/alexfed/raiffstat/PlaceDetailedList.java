package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class PlaceDetailedList extends ListActivity {
	
	private final String LOG = "PlaceDetailedList";
	private List<Model> modelList;
	private String place;
	
	/** Called when the activity is first created. */
	  public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    setContentView(R.layout.activity_raiff_report);
	    place = getIntent().getStringExtra("place");
	    setTitle(place);
	    
	    inflateList();
	  }
	  
	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_place_detailed_list, menu);
			return true;
		}

	  @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	    		case R.id.menu_exclude_from_place:
	    			boolean hasSelection = false;
	    			for (Model m: modelList){
	    		    	if(m.isSelected()){
	    		    		hasSelection = true;
	    		    		break;
	    		    	}
	    		    }
	    			if(hasSelection){
	    				excludeFromPlace();
	    			}else{ 
	    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		default:
	    			return super.onOptionsItemSelected(item);
			}
	   
	  }
	  
	  private void inflateList(){
		  modelList = getModel();
		  ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(this, modelList);
		  setListAdapter(adapter);
	  }
	  
	  private List<Model> getModel() {
			List<Model> list = new ArrayList<Model>();
		    List <String> distTerminals = new ArrayList<String>();
		    distTerminals = queryTerminalsOfPlace();
		    
		    for (String s: distTerminals){
		    	list.add(get(s));
		    }
		    return list;
	  }
	  
	  private Model get(String s) {
		    return new Model(s);
	  }
	  
	  private List<String> queryTerminalsOfPlace(){	
			DatabaseHandler db = new DatabaseHandler(this);
			List<String> ls = db.getTerminalsOfPlace(place);
			db.close();
			return ls;		
	  }
	  
	  private void excludeFromPlace(){
		  
		  DatabaseHandler db = new DatabaseHandler(getBaseContext());
		    for (Model m: modelList){
		    	if(m.isSelected()){
		    		List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
		    		for(TransactionEntry t : transactions){
		    			t.setPlace(t.getTerminal());
		    			t.setInPlace(0);
		    			db.updateTransaction(t);
		    		}
		    	}
		    }
		    db.close();
		    inflateList();
	  }
	  
}
