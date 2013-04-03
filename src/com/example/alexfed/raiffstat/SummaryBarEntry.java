package com.example.alexfed.raiffstat;

public class SummaryBarEntry {
	//private variables
	private int _color;
    private String _name;
    private Double _percent;
    private Double _amount;
    
 // Empty constructor
    public SummaryBarEntry(){
    	this._color = 0xffffffff;
        this._name = "unknown";
        this._percent = 0.0;
        this._amount = 0.0;
    }
    // constructor
    public SummaryBarEntry(int color, String name, Double percent, Double amount){
    	this._color = color;
        this._name = name;
        this._percent = percent;
        this._amount = amount;
    }
    
 // getting _color
    public int getColor(){
        return this._color;
    }
 
    // setting _color
    public void setColor(int color){
        this._color = color;
    }
    
 // getting _name
    public String getName(){
        return this._name;
    }
 
    // setting _name
    public void setName(String name){
        this._name = name;
    }
    
 // getting _percent
    public Double getPercent(){
        return this._percent;
    }
 
    // setting _percent
    public void setPercent(Double percent){
        this._percent = percent;
    }
    
    // getting _amount
    public Double getAmount(){
        return this._amount;
    }
 
    // setting _amount
    public void setAmount(Double amount){
        this._amount = amount;
    }
}
