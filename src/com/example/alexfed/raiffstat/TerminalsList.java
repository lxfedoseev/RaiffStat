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

public class TerminalsList extends ListActivity {


	
	private final String LOG = "TerminalsList";
	private List<Model> modelList;
	private int itemIndex = 0;
	
	/** Called when the activity is first created. */
	  public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    setContentView(R.layout.activity_raiff_report);
	    
	    inflateList();
	  }
	  
	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.menu_terminals_list, menu);
			return true;
		}
	  
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			boolean hasSelection = false;
			switch (item.getItemId()) {
	    		case R.id.menu_make_place:
	    			for (Model m: modelList){
	    		    	if(m.isSelected()){
	    		    		hasSelection = true;
	    		    		break;
	    		    	}
	    		    }
	    			if(hasSelection){
	    				makeNewPlace();
	    			}else{
	    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		case R.id.menu_add_to_place:
	    			DatabaseHandler db = new DatabaseHandler(getBaseContext());
	    			if(db.getDistinctPlacesForPlaceList().size()<1){
	    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_place), Toast.LENGTH_LONG).show();
	    				db.close();
	    				return true;
	    			}
	    			for (Model m: modelList){
	    		    	if(m.isSelected()){
	    		    		hasSelection = true;
	    		    		break;
	    		    	}
	    		    }
	    			if(hasSelection){
	    				addToPlace();
	    			}else{
	    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			db.close();
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
	    distTerminals = queryUnplacedDistinctTerminals();
	    
	    for (String s: distTerminals){
	    	list.add(get(s));
	    }
	    return list;
	  }

	  private Model get(String s) {
	    return new Model(s);
	  }
	  
	  private List<String> queryUnplacedDistinctTerminals(){	
			DatabaseHandler db = new DatabaseHandler(this);
			List<String> ls = db.getUnplacedDistinctTerminals();
			db.close();
			return ls;
			
		}
	  
	  private void makeNewPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  alert.setTitle(getResources().getString(R.string.str_place));
		  alert.setMessage(getResources().getString(R.string.ctx_place_name));

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
		    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_place) + " " + 
		    					value + " " + getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	
			    DatabaseHandler db = new DatabaseHandler(context);
			    for (Model m: modelList){
			    	if(m.isSelected()){
			    		List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
			    		for(TransactionEntry t : transactions){
			    			t.setPlace(value);
			    			t.setInPlace(1);
			    			db.updateTransaction(t);
			    		}
			    	}
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

	  private void addToPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);
		  final Context context = getBaseContext();
		  final DatabaseHandler db = new DatabaseHandler(context);
		  List<String> placeList = db.getDistinctPlacesForPlaceList();

		  final CharSequence[] choiceList = placeList.toArray(new CharSequence[placeList.size()]);
		  alert.setTitle(getResources().getString(R.string.str_place));		  
		  itemIndex = -1;
		  alert.setSingleChoiceItems(choiceList, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				itemIndex = which;
			}
		});
		alert.setCancelable(false);
		alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(itemIndex > -1){
					for (Model m: modelList){
				    	if(m.isSelected()){
				    		List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
				    		for(TransactionEntry t : transactions){
				    			t.setPlace(choiceList[itemIndex].toString());
				    			t.setInPlace(1);
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
					db.close();
					inflateList();
				}else{
					db.close();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
				}
			}
		});
		alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				db.close();
			}
		});
		  
		alert.show();
	  }
}
