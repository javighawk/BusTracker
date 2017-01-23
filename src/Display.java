import java.io.IOException;

import ext.raspiMatrix.AdafruitLEDBackPack;

public class Display extends AdafruitLEDBackPack{
	
	private int busStopDisp_idx = 0;
	private int upcomingBusDisp_idx = 0;
	private boolean cityCentreDisp = true;
	public static int numOfBusesToShow = 3;
	private static int i2c_addr = 0x70;
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
	private byte[] reg = new byte[5];
	
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
						Main.disp.showWaitingTime();
						Thread.sleep(1000);
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}


	public void write(){
		for (int i=0 ; i<this.reg.length ; i++)
			this.setBufferRow(i,this.reg[i]);
		this.writeDisplay();
	}
	
	
	public void clear(){
		this.reg = new byte[5];
	}
	
	
	public void clearAndWrite() {
		this.clear();
		this.write();
	}
	
	
	public void showWaitingTime(){
		Bus bus = Main.bStops.get(this.busStopDisp_idx).getUpcomingBus(this.upcomingBusDisp_idx, this.cityCentreDisp);
		try {
			writeBusLine(bus);
			writeRealTimeIndicator(bus);
			writeWaitingTime(bus);
		} catch(Exception e) {
			e.printStackTrace();
			this.clear();
		}
		reg[2] = (byte) ((reg[2] & 0xF3) | upcomingBusDisp_idx << 2);
	    this.write();
	}

	
	private void writeWaitingTime(Bus bus) {
		long waitTime = bus.getWaitingTime();
	    if( waitTime < 10 && waitTime >= 0){
	        reg[3] = 0;
	        reg[4] = (byte) numDisplay[(int) waitTime];
	    }else if( waitTime < 100 ){
	        reg[3] = (byte) numDisplay[(int)(waitTime/10)];
	        reg[4] = (byte) numDisplay[(int) (waitTime % 10)];
	    }else{
	    	this.clear();
	    }
	}

	
	private void writeRealTimeIndicator(Bus bus) {
		if (bus.isRealtime()) {
			reg[2] = (byte) (reg[2] | 0x02);
		} else {
			reg[2] = (byte) (reg[2] & (~0x02));
		}
	}
	

	private void writeBusLine(Bus bus) throws Exception {
		int busLine = Integer.parseInt(Main.gtfsdata.getBusNumberFromTrip(bus.getTripID()));
		if( busLine > 0 && busLine < 10 ){
		    reg[0] = (byte) numDisplay[busLine];
		    reg[1] = 0;
		}else if( busLine >= 10 ){
		    reg[0] = (byte) numDisplay[(int)(busLine/10)];
		    reg[1] = (byte) numDisplay[busLine % 10];
		} else {
			throw new Exception("Bus line <= 0");
		}
	}
	
	
	public void displayNextBusStop() throws Exception{
		this.busStopDisp_idx = (this.busStopDisp_idx + 1) % Main.bStop_names.length;
		this.showWaitingTime();
	}
	
	
	public void displayNextUpcomingBus() throws Exception{
		this.upcomingBusDisp_idx = (this.upcomingBusDisp_idx + 1) % numOfBusesToShow;
		this.showWaitingTime();
	}
	
	
	public void displayOppositeDirection() throws Exception{
		this.cityCentreDisp = !this.cityCentreDisp;
		this.showWaitingTime();
	}
}
