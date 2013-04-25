package ru.almaunion.raiffstat;

import java.util.List;

import yuku.ambilwarna.AmbilWarnaDialog;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/*
 * Possible categories:
 * - Food
 * - Health
 * - Clothes
 * - Travel
 * - Cash
 * - Software
 * - House keeping
 * - For kid
 * - Furniture
 * - Electronics
 * - Public utilities
 * - Communication (telephone)
 * - Jewelry
 * - Entertainment
 * - Education
 */
public class CategoryList extends SherlockListActivity {
	private final String LOG = "CategoryList";
	
	private List<CategoryEntry> categories;
	private String catName;
	private Context context;
	
	private final int COLOR_DIALOG_NEW = 0;
	private final int COLOR_DIALOG_MODIFY = 1;
	private int colorDlgType;
	private int position;
	private ProgressDialog progressBar;
	private TextView mTextEmpty;
	
	static final int ADD_ID = Menu.FIRST;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		context = this;
		setContentView(R.layout.activity_raiff_report);
		mTextEmpty = (TextView) findViewById(android.R.id.empty);
		
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
		
		lv.setOnItemLongClickListener( 
	    		new AdapterView.OnItemLongClickListener(){ 
	    		@Override 
	    		public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) { 
	    			onLongListItemClick(v,pos,id); 
	    		    return false; 
	    		} 
	    }); 
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem addItem = menu.add(Menu.NONE, ADD_ID, 0, R.string.menu_add);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		addItem.setIcon(R.drawable.icn_assign);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
    		case ADD_ID:
    			doAddCategory();
    			return true;

    		default:
    			return super.onOptionsItemSelected(item);
		}
	}
	
	protected void onLongListItemClick(View v, int pos, long id) { 
		final int localPos = pos;
		final String[] items = new String [] {
        		getResources().getString(R.string.click_edit_name),
        		getResources().getString(R.string.click_edit_color),
        		getResources().getString(R.string.click_delete)
        };
        
        ArrayAdapter<String> stringAdapter  = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder     = new AlertDialog.Builder(this);
        
        builder.setTitle(getResources().getString(R.string.dialog_edit_category));
        builder.setAdapter( stringAdapter, new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int item ) {
            	
            	switch (item){
				case 0:
					doEditCategoryName(localPos);
					break;
				case 1:
					doEditCategoryColor(localPos);
					break;
				case 2:
					deleteCategoryWithProgressBar(localPos);
					break;
				default:
					//do nothing
					break;
			}	
            }
        } );
 
        final AlertDialog dialog = builder.create();
		dialog.show();
	} 
	
	private void deleteCategoryWithProgressBar(int pos){
		final int localPos = pos;
		progressBar = new ProgressDialog(this);
		progressBar.setCancelable(false);
		progressBar.setMessage(getResources().getString(R.string.progress_working));
		progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressBar.setProgress(0);
		progressBar.show();
		  
		  new Thread(new Runnable() {
				public void run() {
					DatabaseHandler db = new DatabaseHandler(context);
					db.deleteCategory(categories.get(localPos));
					List<TransactionEntry> trs = db.getTransactionsWithCategory(categories.get(localPos));
					for(TransactionEntry t:trs){
						t.setExpCategory(StaticValues.EXPENSE_CATEGORY_UNKNOWN);
						db.updateTransaction(t);
					}
					
				    db.close();
				    progressBar.dismiss();
				    CategoryList.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							inflateList();
						}
					});
						  
			}
			}).start();
	}
	private void doEditCategoryColor(int pos){
		colorDlgType = COLOR_DIALOG_MODIFY;
		position = pos;
		AmbilWarnaDialog dialogColor = new AmbilWarnaDialog(context, categories.get(pos).getColor(), 
				new ColorChangedListener());
		dialogColor.show();
	}
	
	private void doEditCategoryName(int pos){

		final int localPos = pos;
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  alert.setTitle(categories.get(pos).getName());
		  alert.setMessage(getResources().getString(R.string.ctx_category_name));

		  // Set an EditText view to get user input 
		  final EditText input = new EditText(this);
		  input.setSingleLine();
		  alert.setView(input);
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(/*!value.isEmpty()*/ value.length() != 0){	
		    	DatabaseHandler db = new DatabaseHandler(context);
		    	CategoryEntry c = db.getCategory(categories.get(localPos).getID());
		    	c.setName(value);
		    	db.updateCategory(c);
				db.close();
				inflateList();
		    }else{
		    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_forbidden_empty_category), Toast.LENGTH_LONG).show(); 
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
	private void doAddCategory(){
		
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		  alert.setTitle(getResources().getString(R.string.str_category));
		  alert.setMessage(getResources().getString(R.string.ctx_category_name));

		  // Set an EditText view to get user input 
		  final EditText input = new EditText(this);
		  input.setSingleLine();
		  alert.setView(input);
		  alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    String value = input.getText().toString();
		    value = value.trim();
		    if(/*!value.isEmpty()*/ value.length() != 0){	
		    	catName = value;
		    	colorDlgType = COLOR_DIALOG_NEW;
				AmbilWarnaDialog dialogColor = new AmbilWarnaDialog(context, 0xff00ff00 ,new ColorChangedListener());
				dialogColor.show();
		    }else{
		    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.str_forbidden_empty_category), Toast.LENGTH_LONG).show(); 
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
	
	private final class ColorChangedListener implements  AmbilWarnaDialog.OnAmbilWarnaListener {
		public void onCancel(AmbilWarnaDialog dialog){
			
		}
		public void onOk(AmbilWarnaDialog dialog, int color){
			DatabaseHandler db = new DatabaseHandler(context);
			if(colorDlgType == COLOR_DIALOG_NEW){
				db.addCategory(new CategoryEntry(catName, color));
			}else if(colorDlgType == COLOR_DIALOG_MODIFY){
				CategoryEntry c = db.getCategory(categories.get(position).getID());
				c.setColor(color);
				db.updateCategory(c);
			}
			db.close();
			inflateList();
		}
	}
	
	private void inflateList(){
		//getListView().setDivider(null);
		DatabaseHandler db = new DatabaseHandler(this);
		categories = db.getAllCategories();
		db.close();
		setListAdapter(new CategoryListAdapter(this, categories));
		mTextEmpty.setText(R.string.str_no_categories);
	}
	
    private static class CategoryListAdapter extends BaseAdapter {
    	private final String LOG = "CategoryListAdapter";
    	private LayoutInflater mInflater;
        private List<CategoryEntry> cats;
        private Context context;
        
        public CategoryListAdapter(Context context, List<CategoryEntry> cats) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            this.cats = cats;
            this.context = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return this.cats.size();
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
            return cats.get(position);
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
            	convertView = mInflater.inflate(R.layout.row_category, null);
               
                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.color = (SurfaceView) convertView.findViewById(R.id.color);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // Bind the data efficiently with the holder.
            setListEntry(holder, this.cats.get(position));
           	    	  	    	
	    	return convertView;
        }
        
        static class ViewHolder {
            TextView name;
            SurfaceView color;
        }
        
        private void setListEntry(ViewHolder holder, CategoryEntry entry){
        	holder.name.setText(entry.getName());
        	holder.color.setBackgroundColor(entry.getColor());
        }
    }
}
