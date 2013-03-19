package com.example.alexfed.raiffstat;

public class TransactionEntry {
	
    //private variables
    private int _id;
    private long _date_time;
    private double _amount;
    private String _amount_curr;
    private double _remainder;
    private String _remainder_curr;
    private String _terminal;
    private String _card;
    private String _place;
    private int _in_place;
    private int _type;
    private int _exp_category;
 
    // Empty constructor
    public TransactionEntry(){
    	this._id = -1;
        this._date_time = 0;
        this._amount = 0.0;
        this._amount_curr = "UNK";
        this._remainder = 0.0;
        this._remainder_curr = "UNK";
        this._terminal = "unknown";
        this._card = "unknown";
        this._place = "unknown";
        this._in_place = 0;
        this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
        this._exp_category = StaticValues.EXPENSE_CATEGORY_UNKNOWN;
    }
    // constructor
    public TransactionEntry(int id, long date_time, double amount, String amount_curr, 
    						double remainder, String remainder_curr, String terminal, String card, 
    						String place, int in_place, int _type, int exp_category){
        this._id = id;
        this._date_time = date_time;
        this._amount = amount;
        this._amount_curr = amount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._terminal = terminal;
        this._card = card;
        this._place = place;
        this._in_place = in_place;
        this._type = _type;
        this._exp_category = exp_category;
    }
 
    // constructor
    public TransactionEntry(long date_time, double amount, String amount_curr, 
    						double remainder, String remainder_curr, String terminal, String card, 
    						String place, int in_place, int type, int exp_category){
    	this._date_time = date_time;
        this._amount = amount;
        this._amount_curr = amount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._terminal = terminal;
        this._card = card;
        this._place = place;
        this._in_place = in_place;
        this._type = type;
        this._exp_category = exp_category;
    }
    // getting ID
    public int getID(){
        return this._id;
    }
 
    // setting id
    public void setID(int id){
        this._id = id;
    }
 
    // getting _date_time
    public long getDateTime(){
        return this._date_time;
    }
 
    // setting _date_time
    public void setDateTime(long date_time){
        this._date_time = date_time;
    }
 
    // getting _amount
    public double getAmount(){
        return this._amount;
    }
 
    // setting _amount
    public void setAmount(double _amount){
        this._amount = _amount;
    }
    
    // getting _amount_curr
    public String getAmountCurr(){
        return this._amount_curr;
    }
 
    // setting _amount_curr
    public void setAmountCurr(String _amount_curr){
        this._amount_curr = _amount_curr;
    }
    
 // getting _terminal
    public String getTerminal(){
        return this._terminal;
    }
 
    // setting _terminal
    public void setTerminal(String _terminal){
        this._terminal = _terminal;
    }
    
    // getting _remainder
    public double getRemainder(){
        return this._remainder;
    }
 
    // setting _remainder
    public void setRemainder(double remainder){
        this._remainder = remainder;
    }
    
    // getting _remainder_curr
    public String getRemainderCurr(){
        return this._remainder_curr;
    }
 
    // setting _remainder_curr
    public void setRemainderCurr(String _remainder_curr){
        this._remainder_curr = _remainder_curr;
    }

    // getting _card
    public String getCard(){
        return this._card;
    }
 
    // setting _card
    public void setCard(String _card){
        this._card = _card;
    }
    
 // getting _place
    public String getPlace(){
        return this._place; 
    }
 
    // setting _place
    public void setPlace(String _place){
        this._place = _place;
    }
    
 // getting _in_place
    public int getInPlace(){
        return this._in_place;
    }
 
    // setting _in_place
    public void setInPlace(int in_place){
        this._in_place = in_place;
    }
    
 // getting type
    public int getType(){
        return this._type;
    }
 
    // setting type
    public void setType(int type){
        this._type = type;
    }
    
    // getting exp_category
    public int getExpCategory(){
        return this._exp_category;
    }
 
    // setting exp_category
    public void setExpCategory(int exp_category){
        this._exp_category = exp_category;
    }
}
