package ru.almaunion.raiffstat.donationui.ui;

import ru.almaunion.raiffstat.AppProperties;
import ru.almaunion.raiffstat.R;
import ru.almaunion.raiffstat.donationui.base.PurchaseActivity;
import ru.almaunion.raiffstat.donationui.domain.items.Donation;
import android.os.Bundle;

import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;

public class PurchaseDonationActivity extends PurchaseActivity {

	private int donationAmount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the result as cancelled in case anything fails before we purchase the item
        setResult(RESULT_CANCELED);
        // Then wait for the callback if we have successfully setup in app billing or not (because we extend PurchaseActivity)
        donationAmount = getIntent().getExtras().getInt("donation_amount", 0);
    }

    @Override
    protected void dealWithIabSetupFailure() {
        popBurntToast(getBaseContext().getResources().getString(R.string.toast_donation_failed));
        finish();
    }

    @Override
    protected void dealWithIabSetupSuccess() {
    	// TODO:
    	//purchaseItem(Donation.SKU);
    	if(donationAmount == AppProperties.DONATION_AMOUNT_01){
    		purchaseItem(Donation.SKU_DONATION_01);
    	}else if(donationAmount == AppProperties.DONATION_AMOUNT_02){
    		purchaseItem(Donation.SKU_DONATION_02);
    	}else  if(donationAmount == AppProperties.DONATION_AMOUNT_03){
    		purchaseItem(Donation.SKU_DONATION_03);
    	}
        
    }

    @Override
    protected void dealWithPurchaseSuccess(IabResult result, Purchase info) {
        super.dealWithPurchaseSuccess(result, info);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void dealWithPurchaseFailed(IabResult result) {
        super.dealWithPurchaseFailed(result);
        setResult(RESULT_CANCELED);
        finish();
    }
    
}
