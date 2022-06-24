package it.gov.pagopa.logextractor.config.cache;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

public class SimpleCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {
	 @Override
	    public void customize(ConcurrentMapCacheManager cacheManager) {
		 List<String> list = Arrays.asList("services");
	        cacheManager.setCacheNames(list);
	    }
}
