package ru.almaunion.statraiff;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class StaticValues {
	public static String VERSION = "1.2";
	public static String EMAIL = "almaunion@gmail.com";
	
	public static String RAIFF_ADDRESS = "Raiffeisen";
	//public static String RAIFF_ADDRESS = "5555";
	
	//Transaction types
	public static int TRANSACTION_TYPE_UNKNOWN = 0;
	public static int TRANSACTION_TYPE_EXPENSE = 1;
	public static int TRANSACTION_TYPE_INCOME = 2;
	public static int TRANSACTION_TYPE_DENIAL = 3;
	
	public static int EXPENSE_CATEGORY_UNKNOWN = -1;
	
	//Sort types: order is essential!!!
	public static int SORT_BY_DATE = 0;
	public static int SORT_BY_AMMOUNT = 1;
	public static int SORT_BY_PLACE = 2;
	
	public static String DELIMITER = ",";
	
	public static String CURR_RUB = "RUB";
	
	public static int PLACES_ALL = 0;
	public static int PLACES_CATEGORY_IN = 1;
	public static int PLACES_CATEGORY_OUT = 2;
	
	public static String DIR_NAME = "RaiffStat";
	public static String HTML_REPORT_PREFIX = "RaiffStat_Report_";
	
	public static int TIME_GAP = 10;//10 seconds gap for time difference
	
	public static void lockScreenRotation(Activity activity){
		
		switch (activity.getResources().getConfiguration().orientation){
        case Configuration.ORIENTATION_PORTRAIT:
            if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	            if(rotation == android.view.Surface.ROTATION_90|| rotation == android.view.Surface.ROTATION_180){
	            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	            } else {
	                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	            }
            }   
        break;

        case Configuration.ORIENTATION_LANDSCAPE:
            if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO){
            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                if(rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90){
                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }
        break;
		}
	}
	
	public static void unlockScreenRotation(Activity activity){
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
}
