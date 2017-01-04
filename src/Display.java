import java.io.IOException;

import ext.raspiMatrix.AdafruitLEDBackPack;

public class Display {
	
	/* Displaying variables */
	private int busStopDisp_idx = 0;
	private int upcomingBusDisp_idx = 0;
	private boolean cityCentreDisp = true;
	
	/* Adafruit LED Backpack I2C address */
	private int i2c_addr = 0x70;
	
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
	
	/* LED Backpack object */
	private AdafruitLEDBackPack matrix7;
	
	
	/*
	 * Constructor
	 */
	public Display(){
		// Initialize backpack object
		try {
			matrix7 = new AdafruitLEDBackPack(1, i2c_addr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*
	 * Write register into backpack register
	 */
	public void write(){
		// Write to register
		for (int i=0 ; i<this.reg.length ; i++)
			matrix7.setBufferRow(i,this.reg[i]);
		
		// Flush
		matrix7.writeDisplay();
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
	 * Show waiting time and the associated bus line
	 * @param busLine Bus number
	 * @param waitTime Waiting time
	 * @param waitTimeIndex 0 if it's the next upcoming bus, 1 if it's the following bus, etc.
	 */
	public void showWaitTime(int busLine, int waitTime, int waitTimeIndex){
	    // Show the index of the wait time in the semicolon on the left
	    reg[2] = (byte) ((reg[2] & 0xF3) | waitTimeIndex << 2);

	    // Show bus line
	    if( busLine == 0 ){
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
	        reg[4] = (byte) numDisplay[waitTime];
	    }else if( waitTime < 100 ){
	        reg[3] = (byte) numDisplay[(int)(waitTime/10)];
	        reg[4] = (byte) numDisplay[waitTime % 10];
	    }
	    
	    this.write();
	}
	
	
	/*
	 * Display the next bus stop
	 */
	public void dispNextBusStop(){
		this.busStopDisp_idx = (this.busStopDisp_idx + 1) % Main.bStop_names.length;
	}
	
	
	/*
	 * Display next upcoming bus waiting time
	 */
	public void dispNextUpcomingBus(){
		this.upcomingBusDisp_idx = (this.upcomingBusDisp_idx + 1) % Main.numOfBusesToShow;
	}
	
	
	/*
	 * Display the city centre direction if it was not being displayed already and vice versa
	 */
	public void dispOppositeDirection(){
		this.cityCentreDisp = !this.cityCentreDisp;
	}
}
