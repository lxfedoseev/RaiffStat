package ru.almaunion.raiffstat.donationui.utils;

import ru.almaunion.raiffstat.donationui.ui.MainDonationActivity;
import ru.almaunion.raiffstat.donationui.ui.PurchaseDonationActivity;
import android.app.Activity;
import android.content.Intent;

public class Navigator {
	
    public static final int REQUEST_PASSPORT_PURCHASE = 2012;

    private final Activity activity;

    public Navigator(Activity activity) {
        this.activity = activity;
    }

    public void toMainActivity() {
        Intent intent = new Intent(activity, MainDonationActivity.class);
        activity.startActivity(intent);
    }

    public void toPurchasePassportActivityForResult(int amount) {
        Intent intent = new Intent(activity, PurchaseDonationActivity.class);
        intent.putExtra("donation_amount", amount);
        activity.startActivityForResult(intent, REQUEST_PASSPORT_PURCHASE);
    }
}
