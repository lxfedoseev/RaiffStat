package com.example.alexfed.raiffstat;

import android.content.Context;

public class RaiffParser {
	
	private final String LOG = "RaiffParser";
	
	private double _amount;
	private String _amount_curr;
	private double _remainder;
	private String _remainder_curr;
    private String _terminal;
    private String _card;
    private String _place; 
    private int _in_place;
    private int _type;
    
    RaiffParser(){
    	this._amount = 0.0;
    	this._amount_curr = "unknown";
    	this._remainder = 0.0;
    	this._remainder_curr = "unknown";
    	this._terminal = "unknown";
    	this._card = "unknown";
    	this._place = "unknown";
    	this._in_place = 0;
    	this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
    }
    
    public boolean parseSmsBody(Context context, String body){
    	String delims;
    	String[] tokens;
    	if(body.toLowerCase().startsWith("balans")){//Income
    		delims = "[.]+";
    		tokens = body.split(delims);
    		if(tokens.length>2){
    			parseIncomeCardAndAmount(tokens[0]);
    			parseRemainder(tokens[1]);
    			this._type = StaticValues.TRANSACTION_TYPE_INCOME;
    			this._terminal = context.getResources().getString(R.string.str_earned);
    			this._place = this._terminal;
    			return true;
    		}else{
    			return false;
    		}
    	}
    	
    	delims = "[;]+"; 
		tokens = body.split(delims);
		if(tokens.length>4){	
			
			if(tokens[1].toLowerCase().startsWith("otkaz")){ // Denial
				parseCard(tokens[0]);
				parseAmount(tokens[2]);
				parseTerminal(tokens[4]);
				parseRemainder(tokens[5]);
				this._type = StaticValues.TRANSACTION_TYPE_DENIAL;
				return true;
			}else{//Expense
				parseCard(tokens[0]);
				parseAmount(tokens[1]);
				parseTerminal(tokens[3]);
				parseRemainder(tokens[4]);
				this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
				return true;
			}
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
    
    private void parseTerminal(String str){
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			this._terminal = tokens[1].trim().replace(StaticValues.DELIMITER, " ");//need to remove "," to support CSV
			this._place = this._terminal;
			this._in_place = 0;
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
    
    private void parseIncomeCardAndAmount(String str){
    	String strLocal = "";
    	
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			//TODO: what if it is neither RUB not USD not EUR (has not 3 letters)
			tokens[1] = tokens[1].trim();
			strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
			this._amount = Double.parseDouble(strLocal.replace(',', '.'));
			this._amount_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
			
			String cardDelims = "[\\s]+";
			String[] cardTokens = tokens[0].split(cardDelims);
			if(cardTokens.length>3){
				this._card = cardTokens[3].trim(); //card
			}
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
    
    public String getTerminal(){
    	return this._terminal;
    }
    
    public String getCard(){
    	return this._card;
    }
    
    public String getPlace(){
    	return this._place;
    }

    public int getInPlace(){
    	return this._in_place;
    }
    
    public int getType(){
    	return this._type;
    }
}
