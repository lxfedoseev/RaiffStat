package ru.almaunion.statraiff.donationui.ui;

import ru.almaunion.statraiff.AppProperties;
import ru.almaunion.statraiff.R;
import ru.almaunion.statraiff.myLog;
import ru.almaunion.statraiff.donationui.base.AlmaUnionActivity;
import ru.almaunion.statraiff.donationui.utils.Navigator;
import ru.almaunion.statraiff.donationui.xml.MainMenu;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class MainDonationActivity extends AlmaUnionActivity implements MainMenu {

	private final String LOG = "MainDonationActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);
    }

    @Override
    public void onPurchaseItemClick01(View v) {
        navigate().toPurchasePassportActivityForResult(AppProperties.DONATION_AMOUNT_01);
    }
    
    @Override
    public void onPurchaseItemClick02(View v) {
        navigate().toPurchasePassportActivityForResult(AppProperties.DONATION_AMOUNT_02);
    }
    
    @Override
    public void onPurchaseItemClick03(View v) {
        navigate().toPurchasePassportActivityForResult(AppProperties.DONATION_AMOUNT_03);
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
        popToast(getBaseContext().getResources().getString(R.string.toast_thanks));
        //passportImage.setVisibility(View.VISIBLE);
    }

    private void dealWithFailedPurchase() {
    	myLog.LOGD(LOG, "Passport purchase failed");
        popToast(getBaseContext().getResources().getString(R.string.toast_donation_failed));
    }
    
}
