package com.example.alexfed.raiffstat;

public class CategorizedPlaceModel {
	
	private String placeName;
	private String categoryName;
	private int color;

	public CategorizedPlaceModel(String placeName, String categoryName, int color) {
	  this.placeName = placeName;
	  this.categoryName = categoryName;
	  this.color = color;
	}

	public String getPlaceName() {
	  return this.placeName;
	}

	public void setPlaceName(String name) {
	  this.placeName = name;
	}

	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String name) {
		this.categoryName = name;
	}
		
	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		this.color = color;
	}

}
