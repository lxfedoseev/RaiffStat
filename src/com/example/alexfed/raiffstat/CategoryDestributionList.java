package com.example.alexfed.raiffstat;

import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;

public class CategoryDestributionList extends ListActivity {

	private final String LOG = "CategoryDestributionList";
	private List<TransactionEntry> transactions;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raiff_report);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//inflateList(); 
	}
	
	/*private void inflateList(){
		getListView().setDivider(null);
		DatabaseHandler db = new DatabaseHandler(this);
		transactions = db.getTransactionsDateIntervalPlace(start, end, place, sortType, true);
		db.close();
		setListAdapter(new ReportListAdapter(this, transactions, place));
	}*/
	
}
