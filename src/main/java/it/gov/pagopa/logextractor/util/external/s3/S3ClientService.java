package it.gov.pagopa.logextractor.util.external.s3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class S3ClientService {

	@Value("${bucketName:logextractor-bucket}")
	String bucketName;

	@Value("${external.s3.saml.assertion.awsprofile:}")
	String awsProfile;

	
	private static String CONTENT_TYPE = "application/zip";
	
	Regions clientRegion = Regions.EU_WEST_3;
/*
	public void signBucket(String keyName) {
		S3Presigner presigner = S3Presigner.builder().region(software.amazon.awssdk.regions.Region.EU_WEST_3)
				.credentialsProvider(null).build();

		signBucket(presigner, keyName);
	}
*/
//	public URL signedUrlForUpload() {
//		Regions bucketRegion = Regions.EU_WEST_3;// region where your bucket is located
//		String objectKey = "doc/hello.zip"; // file locaion in s3 Bucket
//
//		try {
//			AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//					.withCredentials(new DefaultAWSCredentialsProviderChain()).withRegion(bucketRegion).build();
//
//			java.util.Date expiration = new java.util.Date();
//			long expTimeMillis = expiration.getTime();
//			expTimeMillis += 1000 * 60 * 5;// 5min
//			expiration.setTime(expTimeMillis);
//
//			// Generate the presigned URL.
//			System.out.println("Generating pre-signed URL.");
//			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
//					objectKey).withMethod(com.amazonaws.HttpMethod.PUT).withExpiration(expiration);
//			URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//
//			System.out.println("Pre-Signed URL: " + url.toString());
//			return url;
//		} catch (AmazonServiceException e) {
//			// The call was transmitted successfully, but Amazon S3 couldn't process
//			// it, so it returned an error response.
//			e.printStackTrace();
//		} catch (SdkClientException e) {
//			// Amazon S3 couldn't be contacted for a response, or the client
//			// couldn't parse the response from Amazon S3.
//			e.printStackTrace();
//		}
//		return null;
//	}

//	public void upload(URL url, String keyName) {
//		try {
//			// Create the connection and use it to upload the new object by using the
//			// presigned URL.
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//			connection.setDoOutput(true);
//			connection.setRequestProperty("Content-Type", "text/plain");
//			connection.setRequestMethod("PUT");
//			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//			out.write("This text was uploaded as an object by using a presigned URL.");
//			out.close();
//
//			System.out.println("HTTP response code is " + connection.getResponseCode() + " msg: "
//					+ connection.getResponseMessage());
//		} catch (Exception err) {
//			err.printStackTrace();
//		}
//	}
	
	/*
	public URL getUploadUrl(String keyName) {
		URL url = null;
		try {
			S3Presigner presigner = S3Presigner.builder().region(software.amazon.awssdk.regions.Region.EU_WEST_3)
					.credentialsProvider(null).build();
			
			PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName)
					.contentType(CONTENT_TYPE).build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10)).putObjectRequest(objectRequest).build();

			PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
			String myURL = presignedRequest.url().toString();
			log.info("Presigned URL to upload a file to: ({}) {}" , presignedRequest.httpRequest().method(), myURL);

			// Upload content to the Amazon S3 bucket by using this URL.
			url = presignedRequest.url();
		} catch (S3Exception  e) {
			log.error("Error building upload url to bucket", e);
		}
		return url;
	}
	
	public OutputStream openBucket(String keyName) {
		
		OutputStream ret=null;

		URL url = getUploadUrl(keyName);

		try {
			// Create the connection and use it to upload the new object by using the
			// presigned URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", CONTENT_TYPE);
			connection.setRequestMethod("PUT");
			ret = connection.getOutputStream();
			
//			byte[] buffer = new byte[221];
//			IOUtils.readFully(new FileInputStream("c:\\tmp\\signedreq.zip"), buffer);
//			ret.write(buffer);
//			ret.flush();
//			downloadUrl(keyName);

			log.info("HTTP response code is " + connection.getResponseCode() + " msg: "
					+ connection.getResponseMessage());
		}catch(Exception err) {
			log.error("Error opening upload connection to bucket ",err);
		}
		
		return ret;
	}
	*/
	public void uploadFile(String keyName, File file) {
		try {
			log.info("Starting upload to bucket .....");
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			if (StringUtils.isNotBlank(awsProfile)) {
				builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
			}
			
			AmazonS3 s3Client = builder.withRegion(clientRegion)
					.withCredentials(new ProfileCredentialsProvider()).build();

			PutObjectRequest por = new PutObjectRequest(bucketName,
	                keyName,
	                file);
			s3Client.putObject(por);
			
			log.info("File uploaded to bucket !");
		}catch(Exception err) {
			log.error("Error uploading file", err);
		}
        
//		try {
//			FileInputStream fis = new FileInputStream(file);
//			URL upUrl = getUploadUrl(keyName);
//			HttpURLConnection connection = (HttpURLConnection) upUrl.openConnection();
//			connection.setDoOutput(true);
//			connection.setRequestProperty("Content-Type", CONTENT_TYPE);
//			connection.setRequestMethod("PUT");
//			OutputStream out = connection.getOutputStream();
//			int len=0;
//			byte[] buffer = new byte[1024];
//			while ((len = IOUtils.read(fis, buffer))>0) {
//				out.write(buffer, 0, len);
//			}
//			out.flush();
//			out.close();
//			fis.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	
	public OutputStream uploadStream(String keyName) {
		try {
			log.info("Starting upload to bucket .....");
			PipedInputStream in = new PipedInputStream();
		    PipedOutputStream out = new PipedOutputStream(in);
		    
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			if (StringUtils.isNotBlank(awsProfile)) {
				builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
			}
		    
			AmazonS3 s3Client = builder.withRegion(clientRegion)
					.build();
			ObjectMetadata metadata = new ObjectMetadata();
			PutObjectRequest por = new PutObjectRequest(bucketName,
	                keyName,
	                in,
	                metadata);
			Thread thread1 = new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		            	s3Client.putObject(por);

		            	log.info("Upload to bucket completed!");
		            } catch(Exception err) {
		                log.error("Error in thread upload to bucket", err);
		            }                   
		        }
		    });
			thread1.start();
			
			log.info("Opened upload stream to bucket !");
			return out;
		}catch(Exception err) {
			log.error("Error uploading file", err);
		}
		return null;
	}
/*
	public void signBucket(S3Presigner presigner, String keyName) {

		try {
			PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName)
					.contentType(CONTENT_TYPE).build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10)).putObjectRequest(objectRequest).build();

			PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
			String myURL = presignedRequest.url().toString();
			log.info("Presigned URL to upload a file to: ({}) {}" , presignedRequest.httpRequest().method(), myURL);

			// Upload content to the Amazon S3 bucket by using this URL.
			URL url = presignedRequest.url();

			// Create the connection and use it to upload the new object by using the
			// presigned URL.
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/zip");
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
			log.error("Error uploading zip to bucket", e);
		}
	}
*/
	public String downloadUrl(String objectKey) {

		try {
			if (getObject(objectKey)!=null) {
				AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
				if (StringUtils.isNotBlank(awsProfile)) {
					builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
				}
				
				AmazonS3 s3Client = builder.withRegion(clientRegion).build();
	
				// Set the presigned URL to expire after one hour.
				java.util.Date expiration = new java.util.Date();
				long expTimeMillis = Instant.now().toEpochMilli();
				expTimeMillis += 1000 * 60 * 60;
				expiration.setTime(expTimeMillis);
	
				// Generate the presigned URL.
				log.info("Generating pre-signed URL for download.");
				GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName,
						objectKey).withMethod(com.amazonaws.HttpMethod.GET).withExpiration(expiration);
				URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
	
				log.info("Pre-Signed URL for download: " + url.toString());
				return url.toString();
			}
		} catch (Exception e) {
			log.error("Error getting downloadURL from S3", e);
		}
		return "notready";
	}

	public S3Object getObject(String key) {
		S3Object object = null;
		Regions bucketRegion = Regions.EU_WEST_3;
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
		if (StringUtils.isNotBlank(awsProfile)) {
			builder = builder.withCredentials(new ProfileCredentialsProvider(awsProfile));
		}
		
		AmazonS3 s3Client = builder.withRegion(clientRegion)
				.withCredentials(new ProfileCredentialsProvider()).build();
		
		log.info("Retrieving SAML assertion from s3 bucket... ");
		long performanceMillis = System.currentTimeMillis();
		try {
			object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		}catch(AmazonS3Exception err) {
			if (err.getErrorCode().equals("NoSuchKey")){
				//Do nothing
				log.debug("download url not ready for key {}",key);
			}else {
				throw err;
			}
			
		}
		return object;
	}

}
