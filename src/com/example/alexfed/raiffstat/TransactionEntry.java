package com.example.alexfed.raiffstat;

public class TransactionEntry {
	
    //private variables
    private int _id;
    private long _date_time;
    private double _amount;
    private String _amount_curr;
    private double _remainder;
    private String _remainder_curr;
    private String _place;
    private String _card;
    private String _group;
    private int _in_group;
 
    // Empty constructor
    public TransactionEntry(){
    	this._id = -1;
        this._date_time = 0;
        this._amount = 0.0;
        this._amount_curr = "UNK";
        this._remainder = 0.0;
        this._remainder_curr = "UNK";
        this._place = "unknown";
        this._card = "unknown";
        this._group = "unknown";
        this._in_group = 0;
    }
    // constructor
    public TransactionEntry(int id, long date_time, double amount, String amount_curr, 
    						double remainder, String remainder_curr, String place, String card, 
    						String group, int in_group){
        this._id = id;
        this._date_time = date_time;
        this._amount = amount;
        this._amount_curr = amount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._place = place;
        this._card = card;
        this._group = group;
        this._in_group = in_group;
    }
 
    // constructor
    public TransactionEntry(long date_time, double amount, String amount_curr, 
    						double remainder, String remainder_curr, String place, String card, 
    						String group, int in_group){
    	this._date_time = date_time;
        this._amount = amount;
        this._amount_curr = amount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._place = place;
        this._card = card;
        this._group = group;
        this._in_group = in_group;
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
    
 // getting _place
    public String getPlace(){
        return this._place;
    }
 
    // setting _place
    public void setPlace(String _place){
        this._place = _place;
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
    
 // getting _group
    public String getGroup(){
        return this._group;
    }
 
    // setting _group
    public void setGroup(String _group){
        this._group = _group;
    }
    
 // getting in_group
    public int getInGroup(){
        return this._in_group;
    }
 
    // setting in_group
    public void setInGroup(int in_group){
        this._in_group = in_group;
    }
}
