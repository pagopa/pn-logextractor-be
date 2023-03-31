package it.gov.pagopa.logextractor.service;

import java.io.OutputStream;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalOutputStreamService {

	private static ThreadLocal<OutputStream> local = new ThreadLocal<>();
	
	public void set(OutputStream out) {
		local.set(out);
	}
	
	public OutputStream get() {
		return local.get();
	}
	
	public void remove() {
		local.remove();
	}
}
