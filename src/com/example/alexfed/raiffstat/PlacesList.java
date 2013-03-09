package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class PlacesList extends ListActivity {
	
	private final String LOG = "PlacesList";
	private List<Model> modelList;
	
	/** Called when the activity is first created. */
	  public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    setContentView(R.layout.activity_raiff_report);
	    
	    inflateList();
	  }
	  
	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_places_list, menu);
			return true;
		}
	  
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	    		case R.id.menu_make_group:
	    			boolean hasSelection = false;
	    			for (Model m: modelList){
	    		    	if(m.isSelected()){
	    		    		hasSelection = true;
	    		    		break;
	    		    	}
	    		    }
	    			if(hasSelection){
	    				makeNewGroup();
	    			}else{
	    				Toast.makeText(getApplicationContext(), "Nothing selected", Toast.LENGTH_LONG).show();
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
	    List <String> distPlaces = new ArrayList<String>();
	    distPlaces = queryUngroupedDistinctPlaces();
	    
	    for (String s: distPlaces){
	    	list.add(get(s));
	    }
	    return list;
	  }

	  private Model get(String s) {
	    return new Model(s);
	  }
	  
	  private List<String> queryUngroupedDistinctPlaces(){	
			DatabaseHandler db = new DatabaseHandler(this);
			return db.getUngroupedDistinctPlaces();
			
		}
	  
	  private void makeNewGroup(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  alert.setTitle("Group");
		  alert.setMessage("Input group name");

		  // Set an EditText view to get user input 
		  final EditText input = new EditText(this);
		  alert.setView(input);
		  final Context context = getBaseContext();
		  alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(!value.isEmpty()){
			    DatabaseHandler db = new DatabaseHandler(context);
			    for (Model m: modelList){
			    	if(m.isSelected()){
			    		List<TransactionEntry> transactions = db.getTransactionsPlace(m.getName());
			    		for(TransactionEntry t : transactions){
			    			t.setGroup(value);
			    			t.setInGroup(1);
			    			db.updateTransaction(t);
			    		}
			    	}
			    }
			    inflateList();
		    }else{
		    	Toast.makeText(getApplicationContext(), "Group name can't be empty", Toast.LENGTH_LONG).show(); 
		    }
		  }
		  });

		  alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		      // Canceled.
		    }
		  });

		  alert.show();
	  }
}
