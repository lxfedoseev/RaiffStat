package com.example.alexfed.raiffstat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	private final String LOG = "SMSReceiver";
	@Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();        
        SmsMessage[] msgs = null;
        String message = ""; 
        String sender = "";
        long date = 0;
        String raiffAddress = "Raiffeisen";
        //String raiffAddress = "12345"; //test with emulator
        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];            
            for (int i=0; i<msgs.length; i++){
                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);                
                if(sender.equals(""))
                	sender = msgs[i].getOriginatingAddress();  
                if(date == 0)
                	date = msgs[i].getTimestampMillis();
                message += msgs[i].getMessageBody().toString();        
            }
            String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(date));
            Log.d(LOG, "Sender: " + sender + " Message: " + message + " Date: " + dateString);
            
            if(sender.trim().equals(raiffAddress))
            	parseSmsAddToDb(context, date, message);
        }
	}
	
	private void parseSmsAddToDb(Context context, long date, String body){
		boolean parsedWell = false;
		RaiffParser prs = new RaiffParser();
		String dateString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(date));
        if(body!=null) 
        	parsedWell = prs.parseSmsBody(body.trim()); 
            
        if(parsedWell){
        	Log.d(LOG, prs.getCard() + " & " +  prs.getTerminal() + " & " + 
        			 prs.getAmount() + " & " + prs.getAmountCurr() + " & " 
        			 + prs.getRemainder() + " & " + prs.getRemainderCurr() + " & " + prs.getPlace() + " & " + prs.getInPlace() + " & " + dateString);
            	
        	addTransactionToDB(context, date, prs);
        }
	}
	
	private void addTransactionToDB(Context context, long dateTime, RaiffParser prs){
		DatabaseHandler db = new DatabaseHandler(context);
		
		List<TransactionEntry> transactions = db.getTransactionsTerminal(prs.getTerminal());
		if(!transactions.isEmpty()){
			db.addTransaction(new TransactionEntry(dateTime, prs.getAmount(), prs.getAmountCurr(),
					prs.getRemainder(), prs.getRemainderCurr(), prs.getTerminal(), prs.getCard(), 
					transactions.get(0).getPlace(), transactions.get(0).getInPlace(), prs.getType()));
		}else{
			db.addTransaction(new TransactionEntry(dateTime, prs.getAmount(), prs.getAmountCurr(),
				prs.getRemainder(), prs.getRemainderCurr(), prs.getTerminal(), prs.getCard(), 
				prs.getPlace(), prs.getInPlace(), prs.getType()));  
		}
		db.close();
	}

}
