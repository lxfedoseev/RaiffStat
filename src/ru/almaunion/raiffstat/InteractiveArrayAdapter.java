package ru.almaunion.raiffstat;

import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class InteractiveArrayAdapter extends ArrayAdapter<Model> {

	private final List<Model> list;
	  private final Activity context;

	  public InteractiveArrayAdapter(Activity context, List<Model> list) {
	    super(context, R.layout.rowbuttonlayout, list);
	    this.context = context;
	    this.list = list;
	  }

	  static class ViewHolder {
	    protected TextView place;
	    protected TextView category;
	    protected CheckBox checkbox;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    View view = null;
	    if (convertView == null) {
	      LayoutInflater inflator = context.getLayoutInflater();
	      view = inflator.inflate(R.layout.rowbuttonlayout, null);
	      final ViewHolder viewHolder = new ViewHolder();
	      viewHolder.place = (TextView) view.findViewById(R.id.place);
	      viewHolder.category = (TextView) view.findViewById(R.id.category);
	      viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
	      viewHolder.checkbox
	          .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                boolean isChecked) {
	              Model element = (Model) viewHolder.checkbox
	                  .getTag();
	              element.setSelected(buttonView.isChecked());

	            }
	          });
	      view.setTag(viewHolder);
	      viewHolder.checkbox.setTag(list.get(position));
	    } else {
	      view = convertView;
	      ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
	    }
	    ViewHolder holder = (ViewHolder) view.getTag();
	    holder.place.setText(list.get(position).getPlace());
	    holder.checkbox.setChecked(list.get(position).isSelected());
	    
	    if(list.get(position).getCategory() != null){
    		holder.category.setTextColor(list.get(position).getColor());
    		holder.category.setText(list.get(position).getCategory());
    	}else{
    		holder.category.setTextColor(Color.BLACK);
    		holder.category.setText(context.getResources().getString(R.string.str_category_undefined));
    	}
	    
	    return view;
	  }
	  
}
