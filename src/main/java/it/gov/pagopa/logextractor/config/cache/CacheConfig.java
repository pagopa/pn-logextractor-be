package it.gov.pagopa.logextractor.config.cache;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableScheduling
public class CacheConfig {
	
	@Autowired
	CacheManager cacheManager;
	
	public void evictAllCaches() {
	    cacheManager.getCacheNames().stream()
	      .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
	}
	
	@Scheduled(cron = "0 0 22 * * *")
	//@Scheduled(fixedRate = 6000)
	public void evictAllcachesAtIntervals() {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		log.info("Evicting caches at: " + formatter.format(System.currentTimeMillis()));
	    evictAllCaches();
	    log.info("Caches evicted at: " + formatter.format(System.currentTimeMillis()));
	}

}

