package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class PlacesList extends SherlockListActivity{


	private final String LOG = "PlacesList";
	private List<Model> modelList;
	private int itemIndex = 0;
	
	private ProgressDialog progressBar;
	private Context context;
	private int displayFilter;
	OnNavigationListener mOnNavigationListener;
	
	static final int ASSIGN_ID = Menu.FIRST;

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
		displayFilter = StaticValues.PLACES_ALL;
		setDropDownActionBar();
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
	    				assignCategory();
	    			}else{
	    				Toast.makeText(context, getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	    			return true;
	    		default:
	    			return super.onOptionsItemSelected(item);
			}
	   
	  }

	private void inflateList(){
		  //getListView().setDivider(null);
		  modelList = getModel();
		  ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(this, modelList);
		  setListAdapter(adapter);
	  }

	private void setDropDownActionBar(){
		
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(getSupportActionBar().NAVIGATION_MODE_LIST);
		//SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.places_display_array, android.R.layout.simple_spinner_dropdown_item);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.places_display_array, R.layout.spinner_selector_text_view);
		mOnNavigationListener = new OnNavigationListener() {
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch (itemPosition) {
                case 0:
                	displayFilter = StaticValues.PLACES_ALL;
                	inflateList();
                    break;
                case 1:
                	displayFilter = StaticValues.PLACES_CATEGORY_IN;
                	inflateList();
                    break;
                case 2:
                	displayFilter = StaticValues.PLACES_CATEGORY_OUT;
                	inflateList();
                    break;
                }

                return true;
            }
        };
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
	}
	
	  private List<Model> getModel() {
		DatabaseHandler db = new DatabaseHandler(context);
		List<Model> list = db.getAllPlacesAndCategories(displayFilter);
		db.close();
		return list;
	  }
	  
	  private void assignCategory(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(this);
		  DatabaseHandler db = new DatabaseHandler(context);
		  List<CategoryEntry> allCategories = db.getAllCategoriesOrdered();
		  db.close();
		  
		  final List<String> categoryList = new ArrayList<String>();
		  final List<Integer> idList = new ArrayList<Integer>();
		  for(CategoryEntry c:allCategories){
			  categoryList.add(c.getName());
			  idList.add(c.getID());
		  }
		  categoryList.add(0, context.getResources().getString(R.string.dialog_no_category));
		  idList.add(0, StaticValues.EXPENSE_CATEGORY_UNKNOWN);
		  
		  final CharSequence[] choiceList = categoryList.toArray(new CharSequence[categoryList.size()]);
		  alert.setTitle(getResources().getString(R.string.str_select_category));
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
					if(itemIndex > -1){
						assignCategoryWithProgressBar(idList.get(itemIndex));
					}else{
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
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
	  
	  private void assignCategoryWithProgressBar(Integer choice){
		  final Integer localChoice = choice;
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
				    		
				    		List<TransactionEntry> trs = db.getTransactionsPlace(m.getPlace());
							for(TransactionEntry t : trs){
								t.setExpCategory(localChoice);
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
