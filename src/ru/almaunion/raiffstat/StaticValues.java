package ru.almaunion.raiffstat;

public class StaticValues {
	public static String VERSION = "1.0";
	public static String EMAIL = "info@almaunion.ru";
	
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
	
	public static int PLACES_ALL = 0;
	public static int PLACES_CATEGORY_IN = 1;
	public static int PLACES_CATEGORY_OUT = 2;
	
	public static String DIR_NAME = "RaiffStat";
	public static String HTML_REPORT_PREFIX = "RaiffStat_Report_";
}
