import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.osbot.rs07.api.GrandExchange.Box;
import org.osbot.rs07.api.GrandExchange.Status;

@ScriptManifest(name = "BondsFlipper", author = "dokato", version = 3.5, info = "", logo = "") 
public class Main extends Script {
	
	private static final Color standardTxtColor = new Color(255, 255, 255);
	private static final Color standardTxtColor2 = new Color(255, 255, 255);
	
	
	//defaults
	private int buyingPrice;
	private int sellingPrice;
	private int conversionCost;
	
	private static final int BOND = 13190;
	private static final int BOND_UNTRADEBALE = 13192;
	
	private boolean startb = true;

	private long lastPriceSetTime;
	private boolean boxUpdateCheck = false;
	private long priceUpdateTimeSpan;
	
	private long timeSineclastPriceSet;
	
	private long timeBegan;
	private long timeRan;
	private long timeReset;
	private long timeSinceReset;
	private long timeBotted;
	private long timeOffline;
	
	private static final long TIME_BEFORE_LOGOUT = 5 * 60000;
	private static final long TIME_BEFORE_CLICK = 2 * 60000;
	private long timeToDoNothing;
	private long timeSinceLastClick;
	
	private String status;
	
	@Override
    public void onStart() throws InterruptedException{
		resetTime();
		setTimeToDoNothing();
		updatePriceSettings();
		
		
		timeSinceLastClick = System.currentTimeMillis();
//		mouseListenerStuff();
    }
	
    private void setTimeToDoNothing() {
    	status="setting TimeToDoNothing";
    	Random random = new Random();
    	timeToDoNothing =  TIME_BEFORE_CLICK + 
                (long)(random.nextDouble()*(TIME_BEFORE_LOGOUT - TIME_BEFORE_CLICK));
	}

	private void updatePriceSettings() throws InterruptedException {
		while(true){
	    	try {
	    		status = "getting buy price";
				buyingPrice = PriceSettings.getBuyPrice();
				status = "getting sell price";
				sellingPrice = PriceSettings.getSellPrice();
				status = "updating overall price";
				PriceSettings.updateOverallPrice();
				status = "getting conversion cost";
				conversionCost = PriceSettings.getConversionCost();
				status = "getting price update time span";
				priceUpdateTimeSpan = PriceSettings.getPriceUpdateTimeSpan();
				
				
				updatePriceSetTime(true);
				
				break;
			} catch (Exception e) {
				sleep(random(4000,10000));
			}
		}
	}

	private void updatePriceSetTime(boolean boxUpdate) {
		lastPriceSetTime = System.currentTimeMillis();
		boxUpdateCheck = boxUpdate;
	}

	public int onLoop() throws InterruptedException{
    	status="loop started";
    	
    	preventLogout();
    	
    	if(isConvertingBondsNeeded()){
    		convertBonds();
    	}else{
    		if(isGEInterfaceOpen()){
    			if(isSomeThingBoughtOrSoldOrCancelled()){
    				collectStuff();
    			}else if(isSellingBondNeeded()){
    				sell();
    			}else if(hasToBuyMore()){
        			buy();
        		}else if(timeToCheckPrices()){
					updatePriceSettings();
				}else if(boxUpdateCheck){
					abortBoxPricesIfNeeded();
				}else{
					status="Waiting ... ";
				}
        	}else{
        		openGEInterface();
        	}
    	}
    	
    	return random(0,1000);
    }

	private void preventLogout() {
		if((System.currentTimeMillis() - timeSinceLastClick) > timeToDoNothing){
			clickSomewhereToStayLoggedIn();
			timeSinceLastClick = System.currentTimeMillis();
			setTimeToDoNothing();
		}
	}

	private void clickSomewhereToStayLoggedIn() {
		status="clicking somewhere to stay logged in";
		getMouse().click(new RectangleDestination(bot, new Rectangle(532, 260, 200, 190)),false);
	}

	private boolean isGEInterfaceOpen() {
		return getGrandExchange().isOpen();
	}

	private void abortBox(RS2Widget rs2Widget, String box) throws InterruptedException {
		status="aborting box " + box;
		rs2Widget.interact("Abort offer");
		sleepForRamdomTime(1890, 2941);
	}
	
	private void abortBoxPricesIfNeeded() throws InterruptedException {
		
		status="getting pertical box that needs abort";
		
		if(
				!getGrandExchange().getStatus(Box.BOX_1).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_1) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_1) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 7, 2), "1");
		} if(
				!getGrandExchange().getStatus(Box.BOX_2).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_2) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_2) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 8, 2), "2");
		} if(
				!getGrandExchange().getStatus(Box.BOX_3).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_3) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_3) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 9, 2), "3");
		} if(
				!getGrandExchange().getStatus(Box.BOX_4).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_4) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_4) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 10, 2), "4");
		} if(
				!getGrandExchange().getStatus(Box.BOX_5).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_5) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_5) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 11, 2), "5");
		} if(
				!getGrandExchange().getStatus(Box.BOX_6).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_6) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_6) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 12, 2), "6");
		} if(
				!getGrandExchange().getStatus(Box.BOX_7).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_7) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_7) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 13, 2), "7");
		} if(
				!getGrandExchange().getStatus(Box.BOX_8).equals(Status.EMPTY) 
				&& getGrandExchange().getItemPrice(Box.BOX_8) != buyingPrice 
				&& getGrandExchange().getItemPrice(Box.BOX_8) != sellingPrice 
			){
			abortBox(getWidgets().get(465, 14, 2), "8");
		}{
			boxUpdateCheck = false;
		}
		
	}
	
	private boolean timeToCheckPrices() {
		status="checking if it's time to update the prices";
		return (System.currentTimeMillis() - lastPriceSetTime) >= priceUpdateTimeSpan;
	}

	private void sell() throws InterruptedException {
		status = "selling";
		getGrandExchange().sellItem(BOND, sellingPrice, 1);
		sleepForRamdomTime(1540, 2300);
	}

	private boolean isSellingBondNeeded() {
		status="checking if bonds need to be sold";
		return getInventory().contains(BOND) && isOpenBox();
	}

	private boolean isConvertingBondsNeeded() {
		status="checking if bonds need to be converted";
		return !hasAnyBoxStatus(Status.FINISHED_BUY) && getInventory().contains(BOND_UNTRADEBALE) && hasEnoughToConvert();
	}

	private boolean hasEnoughToConvert() {
		return getInventory().contains("Coins") &&
				getInventory().getItem("Coins").getAmount() >= conversionCost;
	}

	private void convertBonds() throws InterruptedException {
		if(isBondsInterfaceOpen()){
			status="Bonds interface is open";
			clickMouse(getWidgets().get(64, 41, 0));
			sleepForRamdomTime(940, 1740);
		}else{
			status="Need to open bonds interface";
			if(getWidgets().closeOpenInterface()){
				sleepForRamdomTime(750, 1284);
				getInventory().getItem(BOND_UNTRADEBALE).interact("Convert");
				sleepForRamdomTime(9, 1741);
			}
		}
	}

	private void clickMouse(RS2Widget rs2Widget) {
		status="Clicking confirm button to convert";
		getMouse().click(
				random(
						(int)rs2Widget.getRectangle().getMinX(),
						(int)rs2Widget.getRectangle().getMaxX()
						), 
				random(
						(int)rs2Widget.getRectangle().getMinY(),
						(int)rs2Widget.getRectangle().getMaxY()
						), 
				false);
		
	}

	private boolean isBondsInterfaceOpen() {
		status="checking if bonds interface is open";
		return getWidgets().isVisible(64, 3);
	}

	private void closeGEInterface() throws InterruptedException {
		status="closing GE interface";
		getGrandExchange().close();
		sleepForRamdomTime(950, 2100-4);
	}

	private void collectStuff() throws InterruptedException {
		status="collecting stuff";
		if(getGrandExchange().collect()){
			updatePriceSetTime(false);
			sleepForRamdomTime(1912, 2901);
		}
	}

	private boolean isSomeThingBoughtOrSoldOrCancelled() {
		status="Checking if any box is finished buying";
		return hasAnyBoxStatus(Status.FINISHED_BUY) 
				|| hasAnyBoxStatus(Status.FINISHED_SALE)
				|| hasAnyBoxStatus(Status.CANCELLING_BUY)
				|| hasAnyBoxStatus(Status.CANCELLING_SALE);
	}

	private boolean hasToBuyMore() {
		status="Checking if has to buy more";
		return !getInventory().contains(BOND_UNTRADEBALE) && hasEnoughMoney() && isOpenBox();
	}

	private boolean isOpenBox() {
		status="Checking if any available box is open";
		return hasAnyBoxStatus(Status.EMPTY);		
	}

	private boolean hasAnyBoxStatus(Status status) {
		
		boolean isInMemWorld = getWorlds().isMembersWorld();
		
		return getGrandExchange().getStatus(Box.BOX_1).equals(status)
				|| getGrandExchange().getStatus(Box.BOX_2).equals(status)
				|| getGrandExchange().getStatus(Box.BOX_3).equals(status)
				|| (isInMemWorld && getGrandExchange().getStatus(Box.BOX_4).equals(status))
				|| (isInMemWorld && getGrandExchange().getStatus(Box.BOX_5).equals(status))
				|| (isInMemWorld && getGrandExchange().getStatus(Box.BOX_6).equals(status))
				|| (isInMemWorld && getGrandExchange().getStatus(Box.BOX_7).equals(status))
				|| (isInMemWorld && getGrandExchange().getStatus(Box.BOX_8).equals(status));
	}

	private boolean hasEnoughMoney() {
		status="Checking if has enough money to buy more";
		int minimalCashNeeded = (getAmountOfPendingBuys() * conversionCost) + (buyingPrice + conversionCost);
		return getInventory().contains("Coins")
				&& getInventory().getItem("Coins").getAmount() > minimalCashNeeded;
	}

	private int getAmountOfPendingBuys() {
		int amount = 0;
		Box[] boxes = new Box[]{Box.BOX_1,Box.BOX_2,Box.BOX_3,Box.BOX_4,Box.BOX_5,Box.BOX_6,Box.BOX_7,Box.BOX_8};
		for(Box box : boxes)
			if(getGrandExchange().getStatus(box).equals(Status.PENDING_BUY))
				amount++;
		
		return amount;
	}

	private void buy() throws InterruptedException {
		status = "Buying";
		getGrandExchange().buyItem(BOND, "bond", buyingPrice, 1);
		sleepForRamdomTime(1189, 4300);
	}

	private void openGEInterface() throws InterruptedException {
		status="GE interface isnt openend, opening GE";
		getObjects().closest(10061,10060).interact("Exchange");
		
		sleepForRamdomTime(1741,2156);
	}

	private void sleepForRamdomTime(int i, int j) throws InterruptedException {
		sleep(random(i,j));
	}

	@Override
    public void onPaint(Graphics2D g1){
		
    	if(this.startb){
    		this.startb=false;
    		this.timeBegan = System.currentTimeMillis();
    		this.timeReset = timeBegan;
    	}
    	this.timeRan = (System.currentTimeMillis() - this.timeBegan);
    	this.timeSinceReset = (System.currentTimeMillis() - this.timeReset);
		if (getClient().isLoggedIn()) {
			this.timeBotted = (this.timeSinceReset - this.timeOffline);
		} else {
			this.timeOffline = (this.timeSinceReset - this.timeBotted);
		}
		
		this.timeSineclastPriceSet = (System.currentTimeMillis() - this.lastPriceSetTime);
		
		Graphics2D g = g1;

		int startY = 65;
		int increment = 15;
		int value = (-increment);
		int x = 20;
		
		g.setFont(new Font("Arial", 0, 13));
		g.setColor(standardTxtColor);
		g.drawString("Acc: " + getBot().getUsername().substring(0, getBot().getUsername().indexOf('@')), x,getY(startY, value+=increment));
		g.drawString("World: " + getWorlds().getCurrentWorld(),x,getY(startY, value+=increment));
		value+=increment;
		g.drawString("Version: " + getVersion(), x, getY(startY, value+=increment));
		g.drawString("Runtime: " + ft(this.timeRan), x, getY(startY, value+=increment));
		g.drawString("Time botted: " + ft(this.timeBotted), x, getY(startY, value+=increment));
		value+=increment;
		value+=increment;
		value+=increment;
		g.drawString("Buy : " + formatPrice(this.buyingPrice), x, getY(startY, value+=increment));
		g.drawString("Sell : " + formatPrice(this.sellingPrice), x, getY(startY, value+=increment));
		g.drawString("Overall : " + formatPrice(PriceSettings.getOverallPrice()), x, getY(startY, value+=increment));
		g.drawString("Time since last update: " + ft(this.timeSineclastPriceSet), x, getY(startY, value+=increment));
		value+=increment;
		g.drawString("Status: " + status, x, getY(startY, value+=increment));
    }
    
    private String formatPrice(int price) {
    	return price/1000 + "K";
	}

	public void onMessage(Message message) throws InterruptedException {
		
	}

	public void onExit() {
		
	}
    
    private int getY(int startY, int value){
		return startY + value;
	}
	
	private void fillRect(Graphics2D g, Rectangle rect){
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
    
	 
	
    private void mouseListenerStuff(){
    	getBot().addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    private void resetTime(){
		this.timeReset = System.currentTimeMillis();
		this.timeBotted = 0;
		this.timeOffline = 0;
	}
    
	private String ft(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(duration));
		if (days == 0L) {
			res = hours + ":" + minutes + ":" + seconds;
		} else {
			res = days + ":" + hours + ":" + minutes + ":" + seconds;
		}
		return res;
	}
}