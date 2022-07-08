package it.gov.pagopa.logextractor.config.cache;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Utility class to setup the active caches
 * */
public class SimpleCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {
	 @Override
	    public void customize(ConcurrentMapCacheManager cacheManager) {
		 List<String> list = Arrays.asList("Cluster");
	        cacheManager.setCacheNames(list);
	    }
}
