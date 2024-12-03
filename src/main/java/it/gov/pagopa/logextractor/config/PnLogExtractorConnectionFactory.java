package it.gov.pagopa.logextractor.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import it.gov.pagopa.logextractor.enums.RedisMode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static it.gov.pagopa.logextractor.enums.RedisMode.SERVERLESS;

/**
 * A custom connection factory extending the JedisConnectionFactory. This connection factory implements an IAM based authentication for Redis and a scheduled IAM token refresh.
 */
@Slf4j
public class PnLogExtractorConnectionFactory extends JedisConnectionFactory {

    // Token refresh interval is set to 14 minutes because the token expires after 15 minutes. Reference: https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth-iam.html#:~:text=The%20IAM%20authentication%20token%20is%20valid%20for%2015%20minutes.%20For%20long%2Dlived%20connections%2C%20we%20recommend%20using%20a%20Redis%20OSS%20client%20that%20supports%20a%20credentials%20provider%20interface.
    private static final Long TOKEN_REFRESH_MINUTES = 14L;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    private final IAMAuthTokenRequest iamAuthTokenRequest;
    @Getter
    private final RedisMode redisMode;
    @Getter
    private final String userId;
    @Getter
    private final String cacheName;
    @Getter
    private final String region;

    /**
     * Instantiates a new Pn log extractor connection factory.
     *
     * @param standaloneConfiguration  the standalone configuration
     * @param jedisClientConfiguration the jedis client configuration
     * @param redisMode                the redis mode
     * @param userId                   the user id
     * @param cacheName                the cache name
     * @param region                   the region of the cache
     * @throws URISyntaxException the uri syntax exception
     */
    public PnLogExtractorConnectionFactory(RedisStandaloneConfiguration standaloneConfiguration, JedisClientConfiguration jedisClientConfiguration, RedisMode redisMode, String userId, String cacheName, String region) throws URISyntaxException {
        super(standaloneConfiguration, jedisClientConfiguration);
        this.redisMode = redisMode;
        this.userId = userId;
        this.cacheName = cacheName;
        this.region = region;
        this.iamAuthTokenRequest = new IAMAuthTokenRequest(this.userId, this.cacheName, this.region, redisMode == SERVERLESS);
        initializeConnectionFactory();
        scheduleTokenRefresh();
    }

    /**
     * A method to generate the IAM token for Redis, using the IAMAuthTokenRequest class.
     *
     * @return String the generated IAM token
     * @throws URISyntaxException the uri syntax exception
     */
    private String generateAuthToken() throws URISyntaxException {
        return this.iamAuthTokenRequest.toSignedRequestUri(this.credentialsProvider.getCredentials());
    }

    /**
     * A scheduled task to refresh the IAM token.
     */
    private void scheduleTokenRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                refreshAuthToken();
            } catch (URISyntaxException e) {
                log.error("Error while refreshing IAM token.", e);
            }
        }, TOKEN_REFRESH_MINUTES, TOKEN_REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * A method to generate a new IAM token and refresh it.
     * The afterPropertiesSet() method is called to refresh the connection.
     *
     * @throws URISyntaxException the uri syntax exception
     */
    private void refreshAuthToken() throws URISyntaxException {
        String authToken = generateAuthToken();
        Objects.requireNonNull(this.getStandaloneConfiguration()).setPassword(authToken);
        this.afterPropertiesSet();
        log.debug("Auth token refreshed for Redis connection.");
    }

    /**
     * An initialization method to set the username and password for the Redis connection.
     *
     * @throws URISyntaxException the uri syntax exception
     */
    private void initializeConnectionFactory() throws URISyntaxException {
        String authToken = generateAuthToken();
        Objects.requireNonNull(this.getStandaloneConfiguration()).setUsername(this.userId);
        Objects.requireNonNull(this.getStandaloneConfiguration()).setPassword(authToken);
    }

}
