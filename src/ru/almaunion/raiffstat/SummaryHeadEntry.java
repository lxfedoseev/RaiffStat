package ru.almaunion.raiffstat;

public class SummaryHeadEntry {
	//private variables
    private String _title;
    private String _info;
    
 // Empty constructor
    public SummaryHeadEntry(){
    	this._title = "unknown";
        this._info = "unknown";
    }
    // constructor
    public SummaryHeadEntry(String title, String info){
        this._title = title;
        this._info = info;
    }
    
 // getting _title
    public String getTitle(){
        return this._title;
    }
 
    // setting _title
    public void setTitle(String title){
        this._title = title;
    }
    
 // getting _info
    public String getInfo(){
        return this._info;
    }
 
    // setting _info
    public void setInfo(String info){
        this._info = info;
    }
}
