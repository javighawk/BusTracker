package com.bustracker.userio.display;


import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import net.slintes.raspiMatrix.AdafruitLEDBackPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOG =
			LoggerFactory.getLogger( BusDisplay.class );

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

	void drawOnDisplay(
			int busNumber, Duration waitingTime, boolean realTime ) {
		if( busNumber <= 0 && busNumber > 99 ) {
			throw new IllegalArgumentException(
					"BusNumber > 99: " + busNumber );
		}

		if( waitingTime.toMinutes() > 99 ) {
			LOG.info( "Waiting time for this trip is more than 99 minutes" );
			clear();
			return;
		}

		drawBusNumber( busNumber );
		drawWaitingTime( waitingTime );
		if( realTime ) {
			drawRealTimeIndicator( );
		} else {
			clearRealTimeIndicator();
		}
	}

    private void drawBusNumber( int busNumber ) {
		LOG.info( "Display bus number" );
        if( busNumber < 10 ){
            reg[0] = (byte) numDisplay[busNumber];
            reg[1] = 0;
        } else {
            reg[0] = (byte) numDisplay[busNumber/10];
            reg[1] = (byte) numDisplay[busNumber % 10];
        }
        write();
    }

	private void drawWaitingTime( Duration time ) {
		LOG.info( "Display waiting time" );
		int waitingTime = (int) Math.max( time.toMinutes(), 0 );
		if( waitingTime < 10 ){
	        reg[3] = 0;
	        reg[4] = (byte) numDisplay[waitingTime];
	    } else {
	        reg[3] = (byte) numDisplay[waitingTime/10];
	        reg[4] = (byte) numDisplay[waitingTime % 10];
	    }
	    write();
	}

	private void drawRealTimeIndicator( ) {
		LOG.info( "Display real time" );
		drawColon();
		write();
	}

	void drawBusIndexIndicator( int busIndex ) {
		LOG.info( "Display trip index" );
		reg[2] = (byte) ((reg[2] & 0xF3) | busIndex << 2);
		write();
	}

	private void clearRealTimeIndicator( ) {
		LOG.info( "Clear real time" );
		clearColon();
		write();
	}

	void drawError() {
		LOG.error( "Display error" );
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
