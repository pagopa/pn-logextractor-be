package it.gov.pagopa.logextractor.util;

import java.util.Date;

/**
 * Handle the number of requests per minute
 * @author ivanr
 *
 */
public class Throttle {
	private final long INTERVAL = 1000; //1 second
	private int size;

	// The counter for current interval
	private int counter;

	private long lastRequest;
	
	public Throttle(int size) {
		this.size = size;
	}

	/**
	 * Evaluate if the throttle is still in given range
	 * @return
	 */
	public synchronized boolean acceptRequest() {
		checkCounter();
		return counter ++ < size;
	}
	
	private synchronized void checkCounter() {
		long currentRequest = new Date().getTime();
		if (currentRequest - lastRequest > INTERVAL) {
			lastRequest = currentRequest;
			counter = 0;
		}
	}


}
