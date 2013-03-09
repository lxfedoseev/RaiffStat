package com.example.alexfed.raiffstat;

import android.util.Log;

public class RaiffParser {
	
	private final String LOG = "RaiffParser";
	
	private double _amount;
	private String _amount_curr;
	private double _remainder;
	private String _remainder_curr;
    private String _place;
    private String _card;
    private String _group; 
    private int _in_group;
    
    RaiffParser(){
    	this._amount = 0.0;
    	this._amount_curr = "unknown";
    	this._remainder = 0.0;
    	this._remainder_curr = "unknown";
    	this._place = "unknown";
    	this._card = "unknown";
    	this._group = "unknown";
    	this._in_group = 0;
    }
    
    public boolean parseSmsBody(String body){

    	String delims = "[;]+"; 
		String[] tokens = body.split(delims);
		if(tokens.length>4){	
			if(tokens[1].toLowerCase().startsWith("otkaz"))
				return false;
			
			parseCard(tokens[0]);
			parseAmount(tokens[1]);
			parsePlace(tokens[3]);
			parseRemainder(tokens[4]);			
			return true;
		}else{
			return false;
		}
		
    	
    }
    
    private void parseCard(String str){
    	this._card = str.trim().substring(5).trim(); //Skip "Karta"
    }
    
    private void parseAmount(String str){
    	String strLocal = "";
    	
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			//TODO: what if it is neither RUB not USD not EUR (has not 3 letters)
			tokens[1] = tokens[1].trim();
			strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
			this._amount = Double.parseDouble(strLocal.replace(',', '.'));
			this._amount_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
		}
    }
    
    private void parsePlace(String str){
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			this._place = tokens[1].trim();
			this._group = this._place;
			this._in_group = 0;
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
    
    public double getAmount(){
    	return this._amount;
    }
    
    public String getAmountCurr(){
    	return this._amount_curr;
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
    
    public String getGroup(){
    	return this._group;
    }

    public int getInGroup(){
    	return this._in_group;
    }
}
