package com.example.alexfed.raiffstat;

public class TransactionEntry {
	
    //private variables
    private int _id;
    private int _date_time;
    private double _ammount;
    private String _ammount_curr;
    private double _remainder;
    private String _remainder_curr;
    private String _place;
    private String _card;
 
    // Empty constructor
    public TransactionEntry(){
 
    }
    // constructor
    public TransactionEntry(int id, int date_time, double ammount, String ammount_curr, 
    						double remainder, String remainder_curr, String place, String card){
        this._id = id;
        this._date_time = date_time;
        this._ammount = ammount;
        this._ammount_curr = ammount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._place = place;
        this._card = card;
    }
 
    // constructor
    public TransactionEntry(int date_time, double ammount, String ammount_curr, 
    						double remainder, String remainder_curr, String place, String card){
    	this._date_time = date_time;
        this._ammount = ammount;
        this._ammount_curr = ammount_curr;
        this._remainder = remainder;
        this._remainder_curr = remainder_curr;
        this._place = place;
        this._card = card;
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
    public int getDateTime(){
        return this._date_time;
    }
 
    // setting _date_time
    public void setDateTime(int date_time){
        this._date_time = date_time;
    }
 
    // getting _ammount
    public double getAmmount(){
        return this._ammount;
    }
 
    // setting _ammount
    public void setAmmount(double _ammount){
        this._ammount = _ammount;
    }
    
    // getting _ammount_curr
    public String getAmmountCurr(){
        return this._ammount_curr;
    }
 
    // setting _ammount_curr
    public void setAmmountCurr(String _ammount_curr){
        this._ammount_curr = _ammount_curr;
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
}
