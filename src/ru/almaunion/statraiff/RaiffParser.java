package ru.almaunion.statraiff;

import android.content.Context;

public class RaiffParser {
	
	private final String LOG = "RaiffParser";
	
	private long _date_time;
	private double _amount;
	private String _amount_curr;
	private double _remainder;
	private String _remainder_curr;
    private String _place;
    private String _card;
    private int _type;
    private int _exp_category;
    
    RaiffParser(){
    	this._date_time = 0;
    	this._amount = 0.0;
    	this._amount_curr = "unknown";
    	this._remainder = 0.0;
    	this._remainder_curr = "unknown";
    	this._place = "unknown";
    	this._card = "unknown";
    	this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
    	this._exp_category = StaticValues.EXPENSE_CATEGORY_UNKNOWN;
    }
    
    public boolean parseSmsBody(Context context, String body, long date){
    	
    	this._date_time = date;
    	this._exp_category = StaticValues.EXPENSE_CATEGORY_UNKNOWN;
    	String delims;
    	String[] tokens;
    	if(body.toLowerCase().startsWith("balans")){//Income
    		//Examples:
    		//Balans vashey karty *7716 umenshilsya 23/09/2013 na:5934,37RUR. Dostupny Ostatok: 6481,05RUR. Raiffeisenbank
    		//Balans vashey karty *7716 popolnilsya 23/09/2013 na:4000,55RUR. Dostupny Ostatok: 7777,05RUR. Raiffeisenbank
    		boolean smthIsWrong = false;
    		delims = "[.;]+";
    		tokens = body.split(delims);
    		if(tokens.length>2){
    			try{
	    			parseIncomeCardAndAmount(tokens[0]);
	    			if(tokens.length>3){
	    				//Sometimes it comes as
	    				//Balans vashey karty *2113 popolnilsya 05/03/2014 na:3480,00RUB.Mesto: null;Dostupny Ostatok: 67491,94RUR. Raiffeisenbank
	    				parseRemainder(tokens[2]);
	    			}
	    			else{
	    				//Most commonly it comes as
	    				//Balans vashey karty *2113 popolnilsya 20/01/2014 na:87000,00RUB.Dostupny Ostatok: 102448,86RUR. Raiffeisenbank
	    				parseRemainder(tokens[1]);
	    			}
	    			
	    			if(body.contains("popolnilsya")){
	    				this._type = StaticValues.TRANSACTION_TYPE_INCOME;
	    				this._place = context.getResources().getString(R.string.str_earned);
	    			}else if(body.contains("umenshilsya")){
	    				this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
	    				this._place = context.getResources().getString(R.string.str_spent);
	    			}else{
	    				this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
	    				this._place = context.getResources().getString(R.string.str_unknown);
	    			}
	    			
	    			return true;
    			}catch (Exception e) {
    				smthIsWrong = true;
				}
    			
    			if(smthIsWrong){
    				//Try to process this kind of messages:
    				//Balans vashey karty *1527 izmenilsya 11/08/2012 Popolnenie:135000,00RUR Spisanie:200000,00RUR. Dostupny Ostatok: 42993,78RUR. Raiffeisenbank
    				try{
    					parseIncomeCardAndAmount2(tokens[0]);
    					parseRemainder(tokens[1]);
    					
    					if(this._amount<0){
    						this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
    						this._amount = Math.abs(this._amount);
    						this._place = context.getResources().getString(R.string.str_spent);
    						
    					}else{
    						this._type = StaticValues.TRANSACTION_TYPE_INCOME;
    						this._place = context.getResources().getString(R.string.str_earned);
    					}
    					return true;
    				}catch (Exception e) {  
					}try{
						//Try to process this kind of messages:
						//Balans vashego scheta #3936 popolnilsya 05/08/2014 na 3480.00 RUR. Dostupny ostatok 19821.18 RUR. Raiffeisenbank
						delims = "[\\s]+";
						tokens = body.split(delims);
						if(body.contains("popolnilsya")){
		    				this._type = StaticValues.TRANSACTION_TYPE_INCOME;
		    				this._place = context.getResources().getString(R.string.str_earned);
		    			}else if(body.contains("umenshilsya")){
		    				this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
		    				this._place = context.getResources().getString(R.string.str_spent);
		    			}else{
		    				this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
		    				this._place = context.getResources().getString(R.string.str_unknown);
		    			}
						if(tokens.length>12){
							this._card = tokens[3].trim();
							this._amount =  Double.parseDouble(tokens[7].trim().replace(',', '.'));
							this._amount_curr = tokens[8].substring(0, tokens[8].length()-1);
							if(this._amount_curr.equalsIgnoreCase("rur"))
								this._amount_curr = StaticValues.CURR_RUB;
							
							this._remainder =  Double.parseDouble(tokens[11].trim().replace(',', '.'));
							this._remainder_curr = tokens[12].substring(0, tokens[12].length()-1);
							if(this._remainder_curr.equalsIgnoreCase("rur"))
								this._remainder_curr = StaticValues.CURR_RUB;
						}else{
							throw new Exception("tokens.length<=12");
						}
						return true;
					}catch (Exception e) {
						//return false;
					}try{
						// Balans scheta karty *2113 na 08/07/2014: 22693.01 RUR. Provedeno po schety 07/07/2014: - 30.00 RUR. Raiffeisenbank
						delims = "[\\s]+";
						tokens = body.split(delims);
						if(tokens[12].trim().equalsIgnoreCase("+")){
		    				this._type = StaticValues.TRANSACTION_TYPE_INCOME;
		    				this._place = context.getResources().getString(R.string.str_earned);
		    			}else if(tokens[12].trim().equalsIgnoreCase("-")){
		    				this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
		    				this._place = context.getResources().getString(R.string.str_spent);
		    			}else{
		    				this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
		    				this._place = context.getResources().getString(R.string.str_unknown);
		    			}
						if(tokens.length>14){
							this._card = tokens[3].trim();
							this._amount =  Double.parseDouble(tokens[13].trim().replace(',', '.'));
							this._amount_curr = tokens[14].substring(0, tokens[14].length()-1);
							if(this._amount_curr.equalsIgnoreCase("rur"))
								this._amount_curr = StaticValues.CURR_RUB;
							
							this._remainder =  Double.parseDouble(tokens[6].trim().replace(',', '.'));
							this._remainder_curr = tokens[7].substring(0, tokens[7].length()-1);
							if(this._remainder_curr.equalsIgnoreCase("rur"))
								this._remainder_curr = StaticValues.CURR_RUB;
						}else{
							throw new Exception("tokens.length<=14");
						}
						return true;
					}catch (Exception e) {
						return false;
					}
    				
    			}
    		}else{
    			return false;
    		}
    	}
    	
    	if(body.toLowerCase().startsWith("provedeno")){//New format
    		// Provedeno po schety 06/08/2014: - 3025.00 RUR. Balans scheta karty *2113 na 07/08/2014: 15763.98 RUR. Raiffeisenbank
    		delims = "[\\s]+";
    		tokens = body.split(delims);
    		if(tokens.length>14){
    			try{
    				this._card = tokens[10].trim();
        			this._amount =  Double.parseDouble(tokens[5].trim().replace(',', '.'));
        			this._amount_curr = tokens[6].substring(0, tokens[6].length()-1);
					if(this._amount_curr.equalsIgnoreCase("rur"))
						this._amount_curr = StaticValues.CURR_RUB;
					
					this._remainder =  Double.parseDouble(tokens[13].trim().replace(',', '.'));
					this._remainder_curr = tokens[14].substring(0, tokens[14].length()-1);
					if(this._remainder_curr.equalsIgnoreCase("rur"))
						this._remainder_curr = StaticValues.CURR_RUB;
					
					if(tokens[4].trim().equalsIgnoreCase("-")){
						this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
	    				this._place = context.getResources().getString(R.string.str_spent);
					}else if(tokens[4].trim().equalsIgnoreCase("+")){
						this._type = StaticValues.TRANSACTION_TYPE_INCOME;
	    				this._place = context.getResources().getString(R.string.str_earned);
					}else{
						this._type = StaticValues.TRANSACTION_TYPE_UNKNOWN;
	    				this._place = context.getResources().getString(R.string.str_unknown);
					}
					return true;
    			}catch (Exception e) {
					return false;
				}
    			
    		}else{
    			return false;
    		}
    	}
    	
    	delims = "[;]+"; 
		tokens = body.split(delims);
		if(tokens.length>=4){	
			
			if(tokens[1].toLowerCase().startsWith("otkaz")){ // Denial
			//Karta *1527;Otkaz: otkaz v avtorizacii;Summa:343,00RUB;Data:19/10/2012;Mesto: APTEKA RADUGA ST. PETERSBUR;Dostupny Ostatok: 51978,87RUB.Raiffeisenbank
				try{
					parseCard(tokens[0]);
					parseAmount(tokens[2]);
					parsePlace(tokens[4]);
					parseRemainder(tokens[5]);
					this._type = StaticValues.TRANSACTION_TYPE_DENIAL;
					return true;
				}catch (Exception e) {
					return false;
				}
			}else{//Expense
				try{
					parseCard(tokens[0]);
					parseAmount(tokens[1]);
					parsePlace(tokens[3]);
					parseRemainder(tokens[4]);
					this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
					return true;
				}catch (Exception e) {
				}
				try{
					//Try to parse this format:
					//Karta *2113; Provedena tranzakcija:RU/ST-PETERSBURG/RBA ATM 14381; Summa:1900.00 RUR Data:26/06/2014; Dostupny Ostatok: 58236.74 RUR. Raiffeisenbank
					parseCard(tokens[0]);
					parsePlace(tokens[1]);
					parseAmount2(tokens[2]);
					parseRemainder(tokens[3]);
					this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
					return true;
				}catch (Exception e) {
				}
				try{
					//Try to parse this format:
					//Karta *2113; Pokupka: RU/SANKT-PETERBU/APTEKA FIALKA; 305.00 RUR; Data: 28/10/2014; Dostupny Ostatok: 57654.20 RUR. Raiffeisenbank
					//Karta *4151; Pokupka:SANKT-PETERBU/O KEY; 3730,90RUB; Data:21/11/2014;Dostupny Ostatok: 286455,32RUB. Raiffeisenbank
					parseCard(tokens[0]);
					parsePlace(tokens[1]);
					parseAmount3(tokens[2]);
					parseRemainder(tokens[4]);
					this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
					return true;
				}catch (Exception e) {
				}
				try{
					//Try to parse this format:
					//Karta *4151; Provedena tranzakcija: 1388.50 RUB; Data: 14/06/2015; OKEY Dostupny Ostatok: 273519.58 RUB. Raiffeisenbank
					parseCard(tokens[0]);
					parsePlace2(tokens[3]);
					parseAmount4(tokens[1]);
					parseRemainder2(tokens[3]);
					this._type = StaticValues.TRANSACTION_TYPE_EXPENSE;
					return true;
				}
				catch (Exception e) {
					return false;
				}
			}
		}else{
			return false;
		}

    }
    
    public boolean parseCSVLine(String line){
    	
    	String delims = "[,]+"; 
    	String[] tokens = line.split(delims);
    	if(tokens.length<9){
    		return false;
    	}
    	else{
    		try{
    			this._date_time = Long.parseLong(tokens[1].trim());
    			this._amount = Double.parseDouble(tokens[2].trim());
    			this._amount_curr = tokens[3].trim();
    			this._remainder = Double.parseDouble(tokens[4].trim());
    			this._remainder_curr = tokens[5].trim();
    			this._card = tokens[6].trim();
    			this._type = Integer.parseInt(tokens[7].trim());
    			
    			this._place = "";
    			for(int i=8; i<tokens.length; i++){
    				this._place += tokens[i] + ",";
    			}
    			this._place = this._place.substring(0, this._place.length()-1); // remove last comma ","
	
    			this._exp_category = StaticValues.EXPENSE_CATEGORY_UNKNOWN;
    			
    			return true;
    			
    		}catch (Exception e) {
    			return false;
			}
    	}
    	
    }
    
    private void parseCard(String str){
    	this._card = str.trim().substring(5).trim(); //Skip "Karta"
    }
    
    private void parseAmount(String str) throws Exception{
    	String strLocal = "";
    	
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			try{
				tokens[1] = tokens[1].trim();
				strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
				this._amount = Double.parseDouble(strLocal.replace(',', '.'));
				this._amount_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
				if(this._amount_curr.equalsIgnoreCase("rur"))
					this._amount_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseAmount2(String str) throws Exception{
    	//Summa:1900.00 RUR Data:26/06/2014
    	String delims = "[\\s:]+"; 
		String[] tokens = str.trim().split(delims);
		if(tokens.length>2){
			try{
				this._amount = Double.parseDouble(tokens[1].trim().replace(',', '.'));
				this._amount_curr = tokens[2].trim();
				if(this._amount_curr.equalsIgnoreCase("rur"))
					this._amount_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseAmount3(String str) throws Exception{
    	//305.00 RUR
    	//3730,90RUB
    	String strLocal = "";
    	try{
    		str = str.trim();
			strLocal = str.substring(0, str.length()-3); //cut "RUB", "USD", "EUR" in the end
			this._amount = Double.parseDouble(strLocal.replace(',', '.'));
			this._amount_curr = str.substring(str.length()-3, str.length());
			if(this._amount_curr.equalsIgnoreCase("rur"))
				this._amount_curr = StaticValues.CURR_RUB;
		}catch (Exception e) {
			throw e;
		}
    }
    
    private void parseAmount4(String str) throws Exception{
    	//Provedena tranzakcija: 1388.50 RUB
    	String delims = "[\\s:]+"; 
		String[] tokens = str.trim().split(delims);
		if(tokens.length>3){
			try{
				this._amount = Double.parseDouble(tokens[2].trim().replace(',', '.'));
				this._amount_curr = tokens[3].trim();
				if(this._amount_curr.equalsIgnoreCase("rur"))
					this._amount_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=3");
		}
    }
    
    private void parsePlace(String str) throws Exception{
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			this._place = "";
			for(int i=1; i<tokens.length; i++){
				this._place += tokens[i] + " ";
			}
			this._place = this._place.trim();
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parsePlace2(String str) throws Exception{
    	//OKEY Dostupny Ostatok: 273519.58 RUB. Raiffeisenbank
    	String delims = "Dostupny Ostatok:";
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			this._place = tokens[0].trim();
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseRemainder(String str) throws Exception{

    	String copy = str;
    	int count = copy.length() - copy.replace(".", "").length();
    	if(count > 1){
    		char[] chars = str.toCharArray();
    		chars[str.indexOf(".")] = ',';
    		str = String.valueOf(chars);
    	}
    	
    	String strLocal = "";
    	
    	String delims = "[:.]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			try{
				tokens[1] = tokens[1].trim();
				strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
				this._remainder = Double.parseDouble(strLocal.replace(',', '.'));
				this._remainder_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
				if(this._remainder_curr.equalsIgnoreCase("rur"))
					this._remainder_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseRemainder2(String str) throws Exception{
    	//OKEY Dostupny Ostatok: 273519.58 RUB. Raiffeisenbank
    	String delims = "Dostupny Ostatok:";
		String[] tokens = str.split(delims);
		String rem;
		if(tokens.length>1){
			rem = tokens[1].trim();
		}else{
			throw new Exception("tokens.length<=1");
		}
    	
		String copy = rem;
		int count = copy.length() - copy.replace(".", "").length();
    	if(count > 1){
	   		char[] chars = rem.toCharArray();
	    	chars[rem.indexOf(".")] = ',';
	    	rem = String.valueOf(chars);
    	}
    	
    	String strLocal = "";
    	
    	delims = "[.]+"; 
		tokens = rem.split(delims);
		if(tokens.length>1){
			try{
				tokens[0] = tokens[0].trim();
				strLocal = tokens[0].substring(0, tokens[0].length()-3); //cut "RUB", "USD", "EUR" in the end
				this._remainder = Double.parseDouble(strLocal.replace(',', '.'));
				this._remainder_curr = tokens[0].substring(tokens[0].length()-3, tokens[0].length());
				if(this._remainder_curr.equalsIgnoreCase("rur"))
					this._remainder_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseIncomeCardAndAmount(String str) throws Exception{
    	String strLocal = "";
    	
    	String delims = "[:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>1){
			try{
				tokens[1] = tokens[1].trim();
				strLocal = tokens[1].substring(0, tokens[1].length()-3); //cut "RUB", "USD", "EUR" in the end
				this._amount = Double.parseDouble(strLocal.replace(',', '.'));
				this._amount_curr = tokens[1].substring(tokens[1].length()-3, tokens[1].length());
				if(this._amount_curr.equalsIgnoreCase("rur"))
					this._amount_curr = StaticValues.CURR_RUB;
			}catch (Exception e) {
				throw e;
			}
			
			String cardDelims = "[\\s]+";
			String[] cardTokens = tokens[0].split(cardDelims);
			if(cardTokens.length>3){
				this._card = cardTokens[3].trim(); //card
			}else{
				throw new Exception("tokens.length<=3");
			}
		}else{
			throw new Exception("tokens.length<=1");
		}
    }
    
    private void parseIncomeCardAndAmount2(String str) throws Exception{
    	//Balans vashey karty *1527 izmenilsya 11/08/2012 Popolnenie:135000,00RUR Spisanie:200000,00RUR
    	double inc = 0;
    	double exp = 0;
    	String strLocal = "";
    	
    	String delims = "[\\s:]+"; 
		String[] tokens = str.split(delims);
		if(tokens.length>9){
			try{
				this._card = tokens[3].trim(); //card
				tokens[7] = tokens[7].trim();
				strLocal = tokens[7].substring(0, tokens[7].length()-3); //cut "RUB", "USD", "EUR" in the end
				inc = Double.parseDouble(strLocal.replace(',', '.'));
				
				tokens[9] = tokens[9].trim();
				strLocal = tokens[9].substring(0, tokens[9].length()-3); //cut "RUB", "USD", "EUR" in the end
				exp = Double.parseDouble(strLocal.replace(',', '.'));
				
				this._amount = inc - exp;
				
				this._amount_curr = tokens[7].substring(tokens[7].length()-3, tokens[7].length());
				if(this._amount_curr.equalsIgnoreCase("rur"))
					this._amount_curr = StaticValues.CURR_RUB;	
				
			}catch (Exception e) {
				throw e;
			}
		}else{
			throw new Exception("tokens.length<=9");
		}
    }
    
    public long getDateTime(){
    	return this._date_time;
    }
    
    public double getAmount(){
    	return this._amount;
    }
    
    public String getAmountCurr(){
    	return this._amount_curr;
    }
    
    public double getRemainder(){
    	return this._remainder;
    }
    
    public String getRemainderCurr(){
    	return this._remainder_curr;
    }
    
    public String getPlace(){
    	return this._place;
    }
    
    public String getCard(){
    	return this._card;
    }
    
    public int getType(){
    	return this._type;
    }
    
    public int getExpCategory(){
    	return this._exp_category;
    }
}
