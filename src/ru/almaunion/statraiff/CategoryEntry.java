package ru.almaunion.statraiff;

public class CategoryEntry {

    //private variables
    private int _id;
    private String _name;
    private int _color;
 
    // Empty constructor
    public CategoryEntry(){
    	this._id = -1;
        this._name = "unknown";
        this._color = 0xffffffff;
    }
    // constructor
    public CategoryEntry(int id, String name, int color){
        this._id = id;
        this._name = name;
        this._color = color;
    }
 
    // constructor
    public CategoryEntry(String name, int color){
    	this._name = name;
        this._color = color;
    }
    // getting ID
    public int getID(){
        return this._id;
    }
 
    // setting id
    public void setID(int id){
        this._id = id;
    }
 
    // getting _name
    public String getName(){
        return this._name;
    }
 
    // setting _name
    public void setName(String name){
        this._name = name;
    }
 
    // getting _color
    public int getColor(){
        return this._color;
    }
 
    // setting _color
    public void setColor(int color){
        this._color = color;
    }

}
