package ru.almaunion.raiffstat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	private static Context mContext = null;
	
	public AboutDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about);
		TextView tv = (TextView)findViewById(R.id.info_text);
		String str = String.format(mContext.getResources().getString(R.string.dialog_about_info), 
				StaticValues.VERSION, StaticValues.EMAIL);
		tv.setText(Html.fromHtml(str));
		tv.setLinkTextColor(Color.BLUE);
		Linkify.addLinks(tv, Linkify.ALL);
	}
}
