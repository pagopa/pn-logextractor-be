package it.gov.pagopa.logextractor.config;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URISyntaxException;
import java.time.Instant;

/**
 * A class to generate an IAM auth token. This implementation is based on the AWS User Guide: <a href="https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth-iam.html">...</a>
 */
public class IAMAuthTokenRequest {
    private static final String REQUEST_PROTOCOL = "http://";
    private static final String PARAM_ACTION = "Action";
    private static final String PARAM_USER = "User";
    private static final String PARAM_RESOURCE_TYPE = "ResourceType";
    private static final String RESOURCE_TYPE_SERVERLESS_CACHE = "ServerlessCache";
    private static final String ACTION_NAME = "connect";
    private static final String SERVICE_NAME = "elasticache";
    private static final long TOKEN_EXPIRY_SECONDS = 900;

    private final String userId;
    private final String cacheName;
    private final String region;
    private final boolean isServerless;

    /**
     * Instantiates a new Iam auth token request.
     *
     * @param userId       the user id
     * @param cacheName    the cache name
     * @param region       the region
     * @param isServerless the is serverless
     */
    public IAMAuthTokenRequest(String userId, String cacheName, String region, boolean isServerless) {
        this.userId = userId;
        this.cacheName = cacheName;
        this.region = region;
        this.isServerless = isServerless;
    }

    /**
     * To signed request uri string.
     *
     * @param credentialsProvider the credentials provider
     * @return the string
     * @throws URISyntaxException the uri syntax exception
     */
    public String toSignedRequestUri(AwsCredentialsProvider credentialsProvider) throws URISyntaxException {
        SdkHttpFullRequest request = buildSdkHttpRequest();
        SdkHttpFullRequest signed = presign(request, credentialsProvider);
        return signed.getUri().toString().replace(REQUEST_PROTOCOL, "");
    }

    private SdkHttpFullRequest buildSdkHttpRequest() {
        SdkHttpFullRequest.Builder builder = SdkHttpFullRequest.builder()
                .method(SdkHttpMethod.GET)
                .protocol("http")
                .host(cacheName)
                .encodedPath("/")
                .putRawQueryParameter(PARAM_ACTION, ACTION_NAME)
                .putRawQueryParameter(PARAM_USER, userId);
        if (isServerless) {
            builder.putRawQueryParameter(PARAM_RESOURCE_TYPE, RESOURCE_TYPE_SERVERLESS_CACHE);
        }
        return builder.build();
    }

    private SdkHttpFullRequest presign(SdkHttpFullRequest request, AwsCredentialsProvider credentialsProvider) {
        Aws4PresignerParams params = Aws4PresignerParams.builder()
                .awsCredentials(credentialsProvider.resolveCredentials())
                .signingName(SERVICE_NAME)
                .signingRegion(Region.of(region))
                .expirationTime(Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS))
                .build();
        return Aws4Signer.create().presign(request, params);
    }
}
