package ru.almaunion.raiffstat.donationui.ui;

import ru.almaunion.raiffstat.donationui.base.PurchaseActivity;

import android.os.Bundle;

import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.example.alexfed.raiffstat.myLog;

public class StartUpActivity extends PurchaseActivity implements OnIabSetupFinishedListener {
   
	private final String LOG = "StartUpActivity";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myLog.LOGD(LOG, "App started");
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        if (result.isSuccess()) {
        	myLog.LOGD(LOG, "In-app Billing set up" + result);
            dealWithIabSetupSuccess();
        } else {
        	myLog.LOGD(LOG, "Problem setting up In-app Billing: " + result);
            dealWithIabSetupFailure();
        }
    }

    @Override
    protected void dealWithIabSetupSuccess() {
        navigate().toMainActivity();
        finish();
    }

    @Override
    protected void dealWithIabSetupFailure() {
        popBurntToast("Sorry In App Billing isn't available on your device");
    }
}
