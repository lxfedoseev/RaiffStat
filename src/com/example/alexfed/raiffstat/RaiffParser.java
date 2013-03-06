package com.example.alexfed.raiffstat;

import android.util.Log;

public class RaiffParser {
	
	private final String LOG = "RaiffParser";
	
	private double _ammount;
	private String _ammount_curr;
	private double _remainder;
	private String _remainder_curr;
    private String _place;
    private String _card; 
    
    RaiffParser(){
    	this._ammount = 0.0;
    	this._ammount_curr = "unknown";
    	this._remainder = 0.0;
    	this._remainder_curr = "unknown";
    	this._place = "unknown";
    	this._card = "unknown";
    }
    
    public boolean parseSmsBody(String body){

    	String delims = "[;]+"; 
		String[] tokens = body.split(delims);
		if(tokens.length>4){
			
			Log.d(LOG, tokens[0] + " & " + tokens[1] + " & " + tokens[2] + " & " + tokens[3] + " & " + tokens[4]);
			
			if(tokens[1].toLowerCase().startsWith("otkaz"))
				return false;
			
			parseCard(tokens[0]);
			Log.d(LOG, "%"+this._card+"%");
			parseAmmount(tokens[1]);
			Log.d(LOG, "%"+this._ammount+"%" + "   " + "%"+this._ammount_curr+"%");
			parsePlace(tokens[3]);
			Log.d(LOG, "%"+this._place+"%");
			parseRemainder(tokens[4]);
			Log.d(LOG, "%"+this._remainder+"%" + "   " + "%"+this._remainder_curr+"%");
			
			return true;
		}else{
			return false;
		}
		
    	
    }
    
    private void parseCard(String str){
    	this._card = str.trim().substring(5).trim(); //Skip "Karta"
    }
    
    private void parseAmmount(String str){
    	String strLocal = "";
    	
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			//TODO: what if it is neither RUB not USD not EUR (has not 3 letters)
			tokens[1] = tokens[1].trim();
			strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
			this._ammount = Double.parseDouble(strLocal.replace(',', '.'));
			this._ammount_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
		}
    }
    
    private void parsePlace(String str){
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			this._place = tokens[1].trim();
		}
    }
    
    private void parseRemainder(String str){

    	String strLocal = "";
    	
    	String delims = "[:.]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			//TODO: what if it is neither RUB not USD not EUR (has not 3 letters)
			tokens[1] = tokens[1].trim();
			strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
			this._remainder = Double.parseDouble(strLocal.replace(',', '.'));
			this._remainder_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
		}
    }
    
    public double getAmmount(){
    	return this._ammount;
    }
    
    public String getAmmountCurr(){
    	return this._ammount_curr;
    }
    
    public double getRemainder(){
    	return this._remainder;
    }
    
    public String getRemainderCurr(){
    	return this._remainder_curr;
    }
    
    public String getPlace(){
    	return this._place;
    }
    
    public String getCard(){
    	return this._card;
    }

}
