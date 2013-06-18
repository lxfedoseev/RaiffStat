package ru.almaunion.statraiff.donationui.base;

import ru.almaunion.statraiff.R;
import ru.almaunion.statraiff.donationui.utils.Navigator;
import ru.almaunion.statraiff.donationui.utils.Toaster;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

public class AlmaUnionActivity extends SherlockActivity {
   
	private Navigator navigator;
    private Toaster toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);

        navigator = new Navigator(this);
        toaster = new Toaster(this);
    }

    protected Navigator navigate() {
        return navigator;
    }

    protected void popBurntToast(String msg) {
        toaster.popBurntToast(msg);
    }

    protected void popToast(String msg) {
        toaster.popToast(msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigator = null;
        toaster = null;
    }
}
