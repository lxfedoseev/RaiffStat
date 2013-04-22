package ru.almaunion.raiffstat.donationui.base;

import ru.almaunion.raiffstat.donationui.utils.Navigator;
import ru.almaunion.raiffstat.donationui.utils.Toaster;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.alexfed.raiffstat.R;

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
