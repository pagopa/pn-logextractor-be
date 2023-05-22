package it.gov.pagopa.logextractor.util.external.s3;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;
//import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
public class S3ClientService {

	@Value("${bucketName}")
	String bucketName;

	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;

	Regions clientRegion = Regions.EU_WEST_3;

	public void signBucket(String keyName) {
		S3Presigner presigner = S3Presigner.builder().region(software.amazon.awssdk.regions.Region.EU_WEST_3)
				.credentialsProvider(null).build();

		signBucket(presigner, keyName);
	}

	public URL signedUrlForUpload() {
		Regions bucketRegion = Regions.EU_WEST_3;// region where your bucket is located
		String objectKey = "doc/hello.pdf"; // file locaion in s3 Bucket

		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
					.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(bucketRegion).build();

			java.util.Date expiration = new java.util.Date();
			long expTimeMillis = expiration.getTime();
			expTimeMillis += 1000 * 60 * 5;// 5min
			expiration.setTime(expTimeMillis);

			// Generate the presigned URL.
			System.out.println("Generating pre-signed URL.");
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
					objectKey).withMethod(com.amazonaws.HttpMethod.PUT).withExpiration(expiration);
			URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

			System.out.println("Pre-Signed URL: " + url.toString());
			return url;
		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}
		return null;
	}

	public void upload(URL url, String keyName) {
		try {
			// Create the connection and use it to upload the new object by using the
			// presigned URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestMethod("PUT");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write("This text was uploaded as an object by using a presigned URL.");
			out.close();

			System.out.println("HTTP response code is " + connection.getResponseCode() + " msg: "
					+ connection.getResponseMessage());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public void signBucket(S3Presigner presigner, String keyName) {

		try {
			PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName)
					.contentType("text/plain").build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10)).putObjectRequest(objectRequest).build();

			PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
			String myURL = presignedRequest.url().toString();
			System.out.println("Presigned URL to upload a file to: " + myURL);
			System.out.println("Which HTTP method needs to be used when uploading a file: "
					+ presignedRequest.httpRequest().method());

			// Upload content to the Amazon S3 bucket by using this URL.
			URL url = presignedRequest.url();

			// Create the connection and use it to upload the new object by using the
			// presigned URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "text/plain");
			connection.setRequestMethod("PUT");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write("This text was uploaded as an object by using a presigned URL.");
			out.flush();
			downloadUrl(keyName);
			out.write("This text was uploaded as an object by using a presigned URL.");
			out.close();

			System.out.println("HTTP response code is " + connection.getResponseCode() + " msg: "
					+ connection.getResponseMessage());

		} catch (S3Exception | IOException e) {
			e.getStackTrace();
		}
	}

	public String downloadUrl(String objectKey) {

		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					.withCredentials(new ProfileCredentialsProvider()).build();

			// Set the presigned URL to expire after one hour.
			java.util.Date expiration = new java.util.Date();
			long expTimeMillis = Instant.now().toEpochMilli();
			expTimeMillis += 1000 * 60 * 60;
			expiration.setTime(expTimeMillis);

			// Generate the presigned URL.
			System.out.println("Generating pre-signed URL for download.");
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
					objectKey).withMethod(com.amazonaws.HttpMethod.GET).withExpiration(expiration);
			URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

			System.out.println("Pre-Signed URL for download: " + url.toString());
			return url.toString();
		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}
		return null;
	}

	public S3Object getObject(String key) {
		Regions bucketRegion = Regions.EU_WEST_3;
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(bucketRegion).build();

		log.info("Retrieving SAML assertion from s3 bucket... ");
		long performanceMillis = System.currentTimeMillis();
		S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		return object;
	}

}
