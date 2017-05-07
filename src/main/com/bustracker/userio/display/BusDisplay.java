package com.bustracker.userio.display;


import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import net.slintes.raspiMatrix.AdafruitLEDBackPack;

import java.io.IOException;
import java.time.Duration;

class BusDisplay extends AdafruitLEDBackPack {

	private static int i2c_addr = 0x70;
	private static int[] numDisplay = {
	        0x3F,		// 0
            0x06,		// 1
            0x5B,    	// 2
            0x4F,    	// 3
            0x66,		// 4
            0x6D, 		// 5
            0x7D, 		// 6
            0x07, 		// 7
            0x7F, 		// 8
            0x6F };		// 9
	private byte[] reg = new byte[5];

	BusDisplay() throws IOException, UnsupportedBusNumberException {
		super(1, i2c_addr);
		reg[0] = 0b00110000;
		reg[1] = 0b01010100;
		reg[2] = 0;
		reg[3] = 0b00010000;
		reg[4] = 0b01111000;
		write();
	}

	private void drawColon() {
        reg[2] = (byte) (reg[2] | 0x02);
    }

    private void clearColon() {
        reg[2] = (byte) (reg[2] & ~0x02);
    }

	private void write(){
		for (int i=0 ; i<this.reg.length ; i++)
			this.setBufferRow(i,this.reg[i]);
		this.writeDisplay();
	}

    void drawBusNumber( int busNumber ) {
        if( busNumber > 0 && busNumber < 10 ){
            reg[0] = (byte) numDisplay[busNumber];
            reg[1] = 0;
        }else if( busNumber >= 10 ){
            reg[0] = (byte) numDisplay[busNumber/10];
            reg[1] = (byte) numDisplay[busNumber % 10];
        } else {
            throw new IllegalArgumentException(
                    "BusNumber > 99: " + busNumber );
        }
        write();
    }

	void drawWaitingTime( Duration time ) {
		int waitingTime = (int) time.toMinutes();
		if( waitingTime < 0 || waitingTime >= 100 ){
			this.clear();
		} else if( waitingTime < 10 ){
	        reg[3] = 0;
	        reg[4] = (byte) numDisplay[waitingTime];
	    } else {
	        reg[3] = (byte) numDisplay[waitingTime/10];
	        reg[4] = (byte) numDisplay[waitingTime % 10];
	    }
	    write();
	}

	void drawRealTimeIndicator() {
		drawColon();
		write();
	}

	void drawError() {
        reg[0] = 0b01111001;     // E
        reg[1] = 0b01010000;     // r
        reg[3] = 0b01010000;     // r
        reg[4] = 0;
        write();
    }

    void clear(){
        this.reg = new byte[5];
        write();
    }
}