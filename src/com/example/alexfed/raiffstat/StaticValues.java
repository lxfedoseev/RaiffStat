package com.example.alexfed.raiffstat;

public class StaticValues {
	public static String RAIFF_ADDRESS = "Raiffeisen";
	
	//Transaction types
	public static int TRANSACTION_TYPE_UNKNOWN = 0;
	public static int TRANSACTION_TYPE_EXPENSE = 1;
	public static int TRANSACTION_TYPE_INCOME = 2;
	public static int TRANSACTION_TYPE_DENIAL = 3;
	
	public static int EXPENSE_CATEGORY_UNKNOWN = -1;
	
	//Sort types: order is essential!!!
	public static int SORT_BY_DATE = 0;
	public static int SORT_BY_AMMOUNT = 1;
	public static int SORT_BY_PLACE = 2;
	
	public static String DELIMITER = ",";
	
	public static String CURR_RUB = "RUB";
}
