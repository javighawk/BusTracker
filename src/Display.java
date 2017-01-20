import java.io.IOException;

import ext.raspiMatrix.AdafruitLEDBackPack;

public class Display extends AdafruitLEDBackPack{
	
	/* Displaying variables */
	private int busStopDisp_idx = 0;
	private int upcomingBusDisp_idx = 0;
	private boolean cityCentreDisp = true;
	public static int numOfBusesToShow = 3;
	
	/* Adafruit LED Backpack I2C address */
	private static int i2c_addr = 0x70;
	
	/* Byte values to write for each number */
	private static int[] numDisplay = {0x3F,		// 0
									   0x06,		// 1
									   0x5B,    	// 2
									   0x4F,    	// 3
									   0x66,		// 4
									   0x6D, 		// 5
									   0x7D, 		// 6
									   0x07, 		// 7
									   0x7F, 		// 8
									   0x6F};		// 9
	
	/* Register to write on the backpack */
	private byte[] reg = new byte[5];
	
	/*
	 * Constructor
	 */
	public Display() throws IOException{
		super(1, i2c_addr);
		reg[0] = 0b00110000;
		reg[1] = 0b01010100;
		reg[2] = 0;
		reg[3] = 0b00010000;
		reg[4] = 0b01111000;
		write();
	}
	
	public void startDisplayBusStops() {
		Thread t = new Thread(){
			public void run(){
				while(true) {
					try {
						Main.disp.showWaitTime();
						Thread.sleep(1000);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}

	/*
	 * Write register into backpack register
	 */
	public void write(){
		// Write to register
		for (int i=0 ; i<this.reg.length ; i++)
			this.setBufferRow(i,this.reg[i]);
		
		// Flush
		this.writeDisplay();
	}
	
	
	/*
	 * Clear display
	 */
	public void clear(){
		// Clear register
		this.reg = new byte[5];
		
		// Write register
		this.write();
	}
	
	
	/*
	 * Update waiting time of the showing bus
	 * @throws NumberFormatException Exception when parsing bus line
	 * @throws Exception Thrown by WaitTime
	 */
	public void showWaitTime() throws Exception{
		// Retrieve the WaitTime object representing the displaying bus
		WaitTime wt = Main.bStops.get(this.busStopDisp_idx).getUpcomingBus(this.upcomingBusDisp_idx, this.cityCentreDisp);
		
		// Get the bus line number
		int busLine = 0;
		try{
			busLine = Integer.parseInt(Main.gtfsdata.getBusNumberFromTrip(wt.getTripID()));
		} catch(NumberFormatException e){
			e.printStackTrace();
		}
		
		// Get waiting time
		long waitTime = wt.getWaitingTime();
		
		// Get real time
		String realtime;
		if (wt.isRealtime()) {
			reg[2] = (byte) (reg[2] | 0x02);
			realtime = "*";
		} else {
			reg[2] = (byte) (reg[2] & (~0x02));
			realtime = "";
		}

	    // Show the index of the wait time in the semicolon on the left
	    reg[2] = (byte) ((reg[2] & 0xF3) | upcomingBusDisp_idx << 2);

	    // Show bus line
	    if( busLine == 0 || waitTime < 0 ){
	        this.clear();
	        return;
	    }
	    else if( busLine < 10 ){
	        reg[0] = (byte) numDisplay[busLine];
	        reg[1] = 0;
	    }else{
	        reg[0] = (byte) numDisplay[(int)(busLine/10)];
	        reg[1] = (byte) numDisplay[busLine % 10];
	    }

	    // Show wait time (only if less than 100 minutes)
	    if( waitTime < 10 ){
	        reg[3] = 0;
	        reg[4] = (byte) numDisplay[(int) waitTime];
	    }else if( waitTime < 100 ){
	        reg[3] = (byte) numDisplay[(int)(waitTime/10)];
	        reg[4] = (byte) numDisplay[(int) (waitTime % 10)];
	    }
	    
	    this.write();
	    
	    String dir;
	    if (this.cityCentreDisp)
	    	dir = "City Centre";
	    else
	    	dir = "Other";
	    System.out.println(busLine + " " + dir + ": " + waitTime + realtime);
	    this.upcomingBusDisp_idx = (this.upcomingBusDisp_idx + 1) % numOfBusesToShow;
	}
	
	
	/*
	 * Display the next bus stop
	 */
	public void dispNextBusStop() throws Exception{
		this.busStopDisp_idx = (this.busStopDisp_idx + 1) % Main.bStop_names.length;
		this.showWaitTime();
	}
	
	
	/*
	 * Display next upcoming bus waiting time
	 */
	public void dispNextUpcomingBus() throws Exception{
		this.upcomingBusDisp_idx = (this.upcomingBusDisp_idx + 1) % numOfBusesToShow;
		this.showWaitTime();
	}
	
	
	/*
	 * Display the city centre direction if it was not being displayed already and vice versa
	 */
	public void dispOppositeDirection() throws Exception{
		this.cityCentreDisp = !this.cityCentreDisp;
		this.showWaitTime();
	}
}
