import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class PriceSettings {
	
//	public static void main(String[] args) throws Exception {
//		
//	}
	
	private static final int BOND_ID = 13190;
	
	private static final String CLOUDANT_URL = "https://kaisumaro.cloudant.com/bond/prices";
	private static final String OSRS_GE_API_URL = "http://services.runescape.com/m=itemdb_oldschool/api/catalogue/detail.json?item=" + BOND_ID;
	private static final String ODBUDDY_API_URL = "https://api.rsbuddy.com/grandExchange?a=guidePrice&i=" + BOND_ID;
	private static final String OSRS_WIKI_BOND_PAGE = "http://oldschoolrunescape.wikia.com/wiki/Exchange:Old_school_bond";
	
	private static int overallPrice;
	private static int conversionCost;

	private static boolean psychologySell;
	private static boolean psychologyBuy;
	private static boolean optimizeSell;
	private static boolean optimizeBuy;
	
	public static int getSellPrice() throws Exception{
		if(useCloudantPrices())
			return getCloudantSellPrice();
		else
			return getOSBuddySellPrice();
	}
	
	public static int getBuyPrice() throws Exception{
		if(useCloudantPrices())
			return getCloudantBuyPrice();
		else
			return getOSBuddyBuyPrice();
	}
	
	private static boolean useCloudantPrices() throws Exception {
		return Boolean.parseBoolean(getFromDoc("useCloudantPrices", CLOUDANT_URL));
	}

	private static int getOSBuddySellPrice() throws Exception {
		updateParameters();
		updateOverallPrice();
		updateConversioncost();
		int profitMargin = getProfitMarginFromCloudant();
		
		return getPerfectSellPrice(getNiceRounding((overallPrice + (conversionCost / 2)) + (profitMargin / 2)));
	}
	
	private static int getOSBuddyBuyPrice() throws Exception{
		updateParameters();
		updateOverallPrice();
		updateConversioncost();
		int profitMargin = getProfitMarginFromCloudant();
		
		return getPerfectBuyPrice(getNiceRounding((overallPrice - (conversionCost / 2)) - (profitMargin / 2)));
	}
	
	private static void updateConversioncost() throws Exception {
		conversionCost = getConversionCost();
	}

	private static void updateParameters() throws Exception {
		psychologyBuy = Boolean.parseBoolean(getFromDoc("psychologyBuy", CLOUDANT_URL));
		psychologySell = Boolean.parseBoolean(getFromDoc("psychologySell", CLOUDANT_URL));
		optimizeBuy = Boolean.parseBoolean(getFromDoc("optimizeBuy", CLOUDANT_URL));
		optimizeSell = Boolean.parseBoolean(getFromDoc("optimizeSell", CLOUDANT_URL));
	}

	private static int getPerfectSellPrice(int price) {
		
		int rest = price % 100000;
		
		if(psychologySell){
			if((rest % 10000) > 5000)
				price = price - 5000;
			
			if(rest < 10000)
				return price - 10000;
			else if(rest < 20000)
				return price - 20000;
	//		else if(rest < 30000)
	//			return price - 30000;
		}
		
		if(optimizeSell){
			if(rest > 70000 && rest < 80000)
				return price + 20000;
			else if(rest > 80000 && rest < 90000)
				return price + 10000;
		}
		
		return price;
	}
	
	private static int getPerfectBuyPrice(int price) {
		
		int rest = price % 100000;
		
		if(psychologyBuy){
			if((rest % 10000) < 5000)
				price = price + 5000;
			
			if(rest > 90000)
				return price + 10000;
			else if(rest > 80000)
				return price + 20000;
	//		else if(rest > 70000)
	//			return price + 30000;
		}
		
		if(optimizeBuy){
			if(rest > 20000 && rest < 30000)
				return price - 20000;
			else if(rest > 10000 && rest < 20000)
				return price - 10000;
		}
		
		return price;
	}

	private static int getNiceRounding(int i) {
		return ((i/1000) * 1000);
	}

	public static void updateOverallPrice() throws Exception {
		overallPrice = getOSBuddyOverall();
	}
	
	public static int getOverallPrice() {
		return overallPrice;
	}

	public static int getConversionCost() throws Exception {
		return (int) (getOSRSGEBondPrice() * 0.1);
	}

	public static long getPriceUpdateTimeSpan() throws Exception{
		int minutes = Integer.parseInt(getFromDoc("priceUpdateTimespan", CLOUDANT_URL));
		return minutes * 60000;
	}
	
	private static int getOSBuddyOverall() throws Exception{
		return Integer.parseInt(getFromDoc("overall", ODBUDDY_API_URL));
	}
	
	private static int getProfitMarginFromCloudant() throws Exception{
		return Integer.parseInt(getFromDoc("profitMargin", CLOUDANT_URL));
	}
	
	private static int getCloudantBuyPrice() throws Exception{
		return Integer.parseInt(getFromDoc("buy", CLOUDANT_URL));
	}
	
	private static int getCloudantSellPrice() throws Exception{
		return Integer.parseInt(getFromDoc("sell", CLOUDANT_URL));
	}
	
	private static String getFromDoc(String key, String url) throws Exception {
		String response = readUrl(url);
		response = response.substring(1, response.length()-2);
		
		List<String> splits = Arrays.asList(response.split(","));
		
		return splits
				.stream()
				.filter(
						s -> s.contains(key)
						)
				.findAny().get().split(":")[1];
	}

	private static int getOSRSGEBondPrice() throws Exception {
		String response = readUrl(OSRS_WIKI_BOND_PAGE);
		
        return Integer.parseInt(
        		Arrays.asList(response.split("<"))
        		.stream()
        		.filter(
        				str -> str.contains("GEPrice")
        				)
        		.findAny()
        		.get()
        		.split(">")[1].replace(",", "")
        		);
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
}
