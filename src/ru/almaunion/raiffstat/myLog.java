package ru.almaunion.raiffstat;

import android.util.Log;

public class myLog {

	private static boolean ENABLE_LOG = false;
	
	public static void LOGD(String tag, String message){
		if(ENABLE_LOG){
			Log.d(tag, message);
		}
	}
	
	public static void LOGE(String tag, String message){
		if(ENABLE_LOG){
			Log.e(tag, message);
		}
	}
	
}
