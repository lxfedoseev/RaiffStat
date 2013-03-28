package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PlacesList extends SherlockListFragment {


	private final String LOG = "PlacesList";
	private List<Model> modelList;
	private int itemIndex = 0;
	
	private ProgressDialog progressBar;
	private FragmentActivity activity;
	
	static final int MAKE_ID = Menu.FIRST;
    static final int ADD_ID = Menu.FIRST+1;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activity = getActivity();
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

		MenuItem makeItem = menu.add(Menu.NONE, MAKE_ID, 0, R.string.menu_add);
		makeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    
		MenuItem addItem = menu.add(Menu.NONE, ADD_ID, 0, R.string.menu_add);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	  
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			boolean hasSelection = false;
			switch (item.getItemId()) {
	    		case MAKE_ID:
	    			for (Model m: modelList){
	    		    	if(m.isSelected()){
	    		    		hasSelection = true;
	    		    		break;
	    		    	}
	    		    }
	    			if(hasSelection){
	    				makeNewPlace();
	    			}else{
	    				Toast.makeText(activity, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		case ADD_ID:
	    			DatabaseHandler db = new DatabaseHandler(activity);
	    			if(db.getDistinctPlacesForPlaceList().size()<1){
	    				Toast.makeText(activity, getResources().getString(R.string.toast_no_place), Toast.LENGTH_LONG).show();
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
	    				Toast.makeText(activity, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			db.close();
	    			return true;
	    		default:
	    			return super.onOptionsItemSelected(item);
			}
	   
	  }
		
	  private void inflateList(){
		  getListView().setDivider(null);
		  modelList = getModel();
		  ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(activity, modelList);
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
			DatabaseHandler db = new DatabaseHandler(activity);
			//TODO:
			//List<String> ls = db.getUnplacedDistinctTerminals();
			List<String> ls = new ArrayList<String>();
			db.close();
			return ls;
			
		}
	  
	  private void makeNewPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(activity);

		  alert.setTitle(getResources().getString(R.string.str_place));
		  alert.setMessage(getResources().getString(R.string.ctx_place_name));

		  // Set an EditText view to get user input 
		  final EditText input = new EditText(activity);
		  alert.setView(input);
		  input.setSingleLine();
		  //final Context context = getBaseContext();
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(!value.isEmpty()){
		    	if(value.equalsIgnoreCase(getResources().getString(R.string.spinner_all))){
		    		Toast.makeText(activity, getResources().getString(R.string.str_place) + " " + 
		    					value + " " + getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	if(value.contains(",")){
		    		Toast.makeText(activity, activity.getResources().getString(R.string.str_comma_usage) + " " + 
	    					getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	
		    	makeNewPlaceWithProgressBar(value);
		    }else{
		    	Toast.makeText(activity, getResources().getString(R.string.str_forbidden_empty_place), Toast.LENGTH_LONG).show(); 
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
	  
	  private void makeNewPlaceWithProgressBar(String placeName){
		  final String localPlaceName = placeName;
			progressBar = new ProgressDialog(activity);
			progressBar.setCancelable(false);
			progressBar.setMessage(getResources().getString(R.string.progress_working));
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setProgress(0);
			progressBar.show();
			
			new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(activity);
				    for (Model m: modelList){
				    	if(m.isSelected()){
				    		//TODO:
				    		//List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
				    		List<TransactionEntry> transactions = new ArrayList<TransactionEntry>();
				    		for(TransactionEntry t : transactions){
				    			//t.setPlace(localPlaceName);
				    			//t.setInPlace(1);
				    			t.setExpCategory(StaticValues.EXPENSE_CATEGORY_UNKNOWN);
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
				    db.close();
				    progressBar.dismiss();
				    activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
	  }

	  private void addToPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		  DatabaseHandler db = new DatabaseHandler(activity);
		  List<String> placeList = db.getDistinctPlacesForPlaceList();
		  db.close();
		  
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
		alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(itemIndex > -1){
					addToPlaceWithProgressBar(choiceList[itemIndex]);
				}else{
					Toast.makeText(activity, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
				}
			}
		});
		alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		  
		alert.show();
	  }
	  
	  private void addToPlaceWithProgressBar(CharSequence choice){
		  final CharSequence localChoice = choice;
		  
		  progressBar = new ProgressDialog(activity);
			progressBar.setCancelable(false);
			progressBar.setMessage(getResources().getString(R.string.progress_working));
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setProgress(0);
			progressBar.show();
			
			new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(activity);
					for (Model m: modelList){
				    	if(m.isSelected()){
				    		//TODO: 
				    		//List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
				    		//List<TransactionEntry> trForPlace = db.getTransactionsPlaceFixed(localChoice.toString());
				    		List<TransactionEntry> transactions = new ArrayList<TransactionEntry>();
				    		List<TransactionEntry> trForPlace = new ArrayList<TransactionEntry>();
				    		for(TransactionEntry t : transactions){
				    			//t.setPlace(localChoice.toString());
				    			//t.setInPlace(1);
				    			t.setExpCategory(trForPlace.get(0).getExpCategory());
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
				    db.close();
				    progressBar.dismiss();
				    activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
		  
	  }

}
