package ru.almaunion.raiffstat;

public class Model {
	
	private String place;
	private String category;
	private int color;
	private boolean selected;

	public Model(String place, String category, int color) {
	  this.place = place;
	  this.category = category;
	  this.color = color;
	  selected = false;
	}

	public String getPlace() {
	  return place;
	}

	public void setPlace(String place) {
	  this.place = place;
	}

	public String getCategory() {
		  return category;
	}

	public void setCategory(String category) {
	  this.category = category;
	}
	
	public int getColor() {
		  return color;
	}

	public void setColor(int color) {
	  this.color = color;
	}
		
	public boolean isSelected() {
	  return selected;
	}

	public void setSelected(boolean selected) {
	  this.selected = selected;
	}

}
