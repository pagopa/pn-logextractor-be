package it.gov.pagopa.logextractor.util.external.s3;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

import it.gov.pagopa.logextractor.util.FileUtilities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3DocumentUploader {
	@Autowired
	AmazonS3 s3Client;
	
	@Autowired
	FileUtilities fileutils;
	
	@Async
	public void upload(PutObjectRequest por) {
		try {
			TransferManager tm = TransferManagerBuilder.standard()
                    .withS3Client(s3Client)
                    .build();
        	Upload upload = tm.upload(por);
        	UploadResult result = upload.waitForUploadResult();
        	log.info("Upload to bucket completed! Version: {}", result.getVersionId());
        } catch(Exception err) {
            log.error("Error in thread upload to bucket", err);
        }
	}
	
	@Async
	public void uploadV2(InputStream is, String bucketName, String key) {
		
		final int BUFFER_SIZE = 1024*1024*5;//5MB size minima (https://docs.aws.amazon.com/AmazonS3/latest/userguide/qfacts.html)
		try {
			List<PartETag> partETags = new ArrayList<PartETag>();
			InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, key);
			InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
			
			TransferManager tm = TransferManagerBuilder.standard()
					.withS3Client(s3Client)
					.build();
			boolean chunkFilled=false;
			boolean finished=false;
			tm.getConfiguration().setMultipartCopyPartSize(BUFFER_SIZE);
			BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
			
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			long totalSize=0;
			int partNumber = 1;
			int partSize = 0;
			
			while(!finished) {
				partSize = 0;
				chunkFilled = false;
				File tmpFilename = fileutils.getFileWithRandomName("tmp", ".bin");
				log.info("Created file tmp {}",tmpFilename.getPath());
				FileOutputStream fos = new FileOutputStream(tmpFilename);
				//Riempiamo il chunk con i 5MB
				while (!chunkFilled) {
					byte[] buffer = new byte[BUFFER_SIZE];
					int readSize = bis.read(buffer);
					if (readSize < 0) {
						finished = true;
					}else {
						fos.write(buffer,0, readSize);
						partSize += readSize;
					}
					log.trace("read {} bytes to tmp file; total fileSize is now {}",readSize,partSize);
					chunkFilled = (finished || partSize >= BUFFER_SIZE);
				}
				fos.flush();
				fos.close();
				if (partSize>0) {
//					ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(tmpFilename.toPath()));
					FileInputStream bais = new FileInputStream(tmpFilename);
	
					UploadPartRequest uploadRequest = new UploadPartRequest()
							.withBucketName(bucketName).withKey(key)
							.withUploadId(initResponse.getUploadId())
							.withPartNumber(partNumber)
							.withInputStream(bais)
							.withPartSize(partSize);
					UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
					partETags.add(uploadResult.getPartETag());
	
					bais.close();
					tmpFilename.delete();
					log.info("Uploaded part {}, size {} to bucket {} for key {}",partNumber,partSize,bucketName,key);
					partNumber++;
					totalSize += partSize;
				}
			}
			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, key,
                    initResponse.getUploadId(), partETags);
            CompleteMultipartUploadResult completeResult = s3Client.completeMultipartUpload(compRequest);
			log.info("Upload to bucket completed with {} bytes ! result={}", totalSize, completeResult.toString());
		} catch(Exception err) {
			log.error("Error in thread upload to bucket", err);
		}
	}
}
