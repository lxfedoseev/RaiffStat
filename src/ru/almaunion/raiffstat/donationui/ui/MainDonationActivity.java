package ru.almaunion.raiffstat.donationui.ui;

import ru.almaunion.raiffstat.donationui.base.AlmaUnionActivity;
import ru.almaunion.raiffstat.donationui.utils.Navigator;
import ru.almaunion.raiffstat.donationui.xml.MainMenu;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.alexfed.raiffstat.R;
import com.example.alexfed.raiffstat.myLog;


public class MainDonationActivity extends AlmaUnionActivity implements MainMenu {

	private final String LOG = "MainDonationActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
    }

    @Override
    public void onPurchaseItemClick(View v) {
        navigate().toPurchasePassportActivityForResult();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Navigator.REQUEST_PASSPORT_PURCHASE == requestCode) {
            if (RESULT_OK == resultCode) {
                dealWithSuccessfulPurchase();
            } else {
                dealWithFailedPurchase();
            }
        }
    }

    private void dealWithSuccessfulPurchase() {
        myLog.LOGD(LOG, "Passport purchased");
        popToast("Passport purchased");
        //passportImage.setVisibility(View.VISIBLE);
    }

    private void dealWithFailedPurchase() {
    	myLog.LOGD(LOG, "Passport purchase failed");
        popToast("Failed to purchase passport");
    }
    
}
