package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class PlacesList extends SherlockListActivity{


	private final String LOG = "PlacesList";
	private List<Model> modelList;
	private int itemIndex = 0;
	
	private ProgressDialog progressBar;
	private Context context;
	
	static final int ASSIGN_ID = Menu.FIRST;
    static final int REMOVE_ID = Menu.FIRST+1;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		setContentView(R.layout.activity_raiff_report);
		context = getBaseContext();
	}
	
	  @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		inflateList();
	}

	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
		  MenuItem assignItem = menu.add(Menu.NONE, ASSIGN_ID, 0, R.string.click_assign_category);
		  assignItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		  
		  MenuItem removeItem = menu.add(Menu.NONE, REMOVE_ID, 0, R.string.click_remove_category);
		  removeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		  return true;
		}
	  
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			boolean hasSelection = false;
			for (Model m: modelList){
		    	if(m.isSelected()){
		    		hasSelection = true;
		    		break;
		    	}
		    }
			switch (item.getItemId()) {
	    		case ASSIGN_ID:
	    			if(hasSelection){
	    				List<String> places = new ArrayList<String>();
	    				for (Model m: modelList){
	    			    	if(m.isSelected()){
	    			    		places.add(m.getPlace());
	    			    	}
	    			    }
	    				Intent categoriesActivity = new Intent(getBaseContext(), CategoryList.class);
	    				categoriesActivity.putStringArrayListExtra("places", (ArrayList<String>) places);
	        			startActivity(categoriesActivity);
	    			}else{
	    				Toast.makeText(context, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		case REMOVE_ID:
	    			if(hasSelection){
	    				removeCategoryWithProgressBar();
	    			}else{
	    				Toast.makeText(context, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		default:
	    			return super.onOptionsItemSelected(item);
			}
	   
	  }

	private void inflateList(){
		  getListView().setDivider(null);
		  modelList = getModel();
		  ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(this, modelList);
		  setListAdapter(adapter);
	  }

	  private List<Model> getModel() {
		DatabaseHandler db = new DatabaseHandler(context);
		List<Model> list = db.getPlacesWithCategories();
		db.close();
		return list;
	  }
	  
	  private void removeCategoryWithProgressBar(){
		  progressBar = new ProgressDialog(this);
		  progressBar.setCancelable(false);
		  progressBar.setMessage(getResources().getString(R.string.progress_working));
		  progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		  progressBar.setProgress(0);
		  progressBar.show();
		  
		  new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(context);
					for (Model m: modelList){
				    	if(m.isSelected()){
				    		List<TransactionEntry> trForPlace = db.getTransactionsPlaceFixed(m.getPlace());
				    		for(TransactionEntry t : trForPlace){
				    			t.setExpCategory(StaticValues.EXPENSE_CATEGORY_UNKNOWN);
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
				    db.close();
				    progressBar.dismiss();
				    PlacesList.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
	  }

}
