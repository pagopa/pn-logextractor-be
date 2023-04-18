package it.gov.pagopa.logextractor;

import redis.clients.jedis.Jedis;

public class RedisTest {

	public static void main(String[] args) {
		Jedis j = new Jedis("redis://127.0.0.1:6379");
		j.connect();
	}

}
