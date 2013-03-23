package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class TerminalsList extends SherlockListFragment {

	private final String LOG = "TerminalsList";
	private List<Model> modelList;
	private int itemIndex = 0;
	static final int MAKE_ID = Menu.FIRST;
    static final int ADD_ID = Menu.FIRST+1;
	private ProgressDialog progressBar;

	
	/** Called when the activity is first created. */
	  public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    //getActivity().setContentView(R.layout.activity_raiff_report);
	  }
	  
	  @Override public void onActivityCreated(Bundle savedInstanceState) {
	        super.onActivityCreated(savedInstanceState);
	        // We have a menu item to show in action bar.
	        myLog.LOGD(LOG, "onActivityCreated");
	        setHasOptionsMenu(true);
	        inflateList();
	  }

	public void inflateDBChanged(){
		modelList = getModel();
	}
	
	public void setData() {
		myLog.LOGD(LOG, "setData 01");
        // The reload fragment code here !
        if (this.isDetached()) {
        	myLog.LOGD(LOG, "setData 02");
            getFragmentManager().beginTransaction()
               .detach(this)
               .attach(this)
               .commit();
        }
    }
	
		/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockListFragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		myLog.LOGD(LOG, "onAttach");
	}
	
	
	

		/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myLog.LOGD(LOG, "onCreateView");
		// TODO Auto-generated method stub 
		//return super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.activity_raiff_report, null);
		return view;  //You must return your view here
	}

	
	/* (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onDestroyView()
	 */
	@Override
	public void onDestroyView() {
		myLog.LOGD(LOG, "onDestroyView");
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		myLog.LOGD(LOG, "onResume");
	}

		@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
			MenuItem populateItem = menu.add(Menu.NONE, MAKE_ID, 0, R.string.menu_make_place);
	        populateItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	        MenuItem clearItem = menu.add(Menu.NONE, ADD_ID, 0, R.string.menu_add);
	        clearItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		super.onCreateOptionsMenu(menu, inflater);
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
	    				Toast.makeText(getActivity(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
	    			}
	                return true;
	            case ADD_ID:
	            	DatabaseHandler db = new DatabaseHandler(getActivity());
	    			if(db.getDistinctPlacesForPlaceList().size()<1){
	    				Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_place), Toast.LENGTH_LONG).show();
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
	    				Toast.makeText(getActivity(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show();
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
		  ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(getActivity(), modelList);
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
			DatabaseHandler db = new DatabaseHandler(getActivity());
			List<String> ls = db.getUnplacedDistinctTerminals();
			db.close();
			return ls;
			
		}
	  
	  private void makeNewPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		  alert.setTitle(getResources().getString(R.string.str_place));
		  alert.setMessage(getResources().getString(R.string.ctx_place_name));

		  // Set an EditText view to get user input 
		  final EditText input = new EditText(getActivity());
		  alert.setView(input);
		  input.setSingleLine();
		  //final Context context = getBaseContext();
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(!value.isEmpty()){
		    	if(value.equalsIgnoreCase(getResources().getString(R.string.spinner_all))){
		    		Toast.makeText(getActivity(), getResources().getString(R.string.str_place) + " " + 
		    					value + " " + getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	if(value.contains(",")){
		    		Toast.makeText(getActivity(), getResources().getString(R.string.str_comma_usage) + " " + 
	    					getResources().getString(R.string.str_forbidden), Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	
		    	makeNewPlaceWithProgressBar(value);
		    }else{
		    	Toast.makeText(getActivity(), getResources().getString(R.string.str_forbidden_empty_place), Toast.LENGTH_LONG).show(); 
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
			progressBar = new ProgressDialog(getActivity());
			progressBar.setCancelable(false);
			progressBar.setMessage(getResources().getString(R.string.progress_working));
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setProgress(0);
			progressBar.show();
			
			new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(getActivity());
				    for (Model m: modelList){
				    	if(m.isSelected()){
				    		List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
				    		for(TransactionEntry t : transactions){
				    			t.setPlace(localPlaceName);
				    			t.setInPlace(1);
				    			t.setExpCategory(StaticValues.EXPENSE_CATEGORY_UNKNOWN);
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
				    db.close();
				    progressBar.dismiss();
				    getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
	  }

	  private void addToPlace(){
		  AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		  DatabaseHandler db = new DatabaseHandler(getActivity());
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
					Toast.makeText(getActivity(), getResources().getString(R.string.toast_nothing_selected), Toast.LENGTH_LONG).show(); 
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
		  
		  progressBar = new ProgressDialog(getActivity());
			progressBar.setCancelable(false);
			progressBar.setMessage(getResources().getString(R.string.progress_working));
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setProgress(0);
			progressBar.show();
			
			new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(getActivity());
					for (Model m: modelList){
				    	if(m.isSelected()){
				    		List<TransactionEntry> transactions = db.getTransactionsTerminal(m.getName());
				    		List<TransactionEntry> trForPlace = db.getTransactionsPlaceFixed(localChoice.toString());
				    		for(TransactionEntry t : transactions){
				    			t.setPlace(localChoice.toString());
				    			t.setInPlace(1);
				    			t.setExpCategory(trForPlace.get(0).getExpCategory());
				    			db.updateTransaction(t);
				    		}
				    	}
				    }
				    db.close();
				    progressBar.dismiss();
				    getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
		  
	  }
}
