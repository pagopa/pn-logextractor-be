import { S3Client, PutObjectCommand, HeadObjectCommand } from "@aws-sdk/client-s3";
import md5 from 'crypto-js';

export const handler = async(event) => {
    if(event) {
        try {
            var s3Client = new S3Client();
            const bucket_name = process.env.BucketName;
            const userAttributeJson =  event.request.userAttributes;
            const userName = event.request.userAttributes.sub;
            const fileName = userName+'.json';
            console.log(userAttributeJson);
            var buf = Buffer.from(JSON.stringify(userAttributeJson));
            var md5File = buf.toString();
            var md5Hash = await getMD5HashFromFile(md5File);
            if(await checkIfUserexists(s3Client ,bucket_name, fileName) === false) {
                await putObjectToS3(s3Client, bucket_name, fileName, buf, md5Hash);
            }
        }
        catch(err) {
            console.log(err);
        }
    }
    
    return event;
};

const putObjectToS3 = async (s3Client ,bucket, key, data, md5Hash) => {
    var params = {
        Bucket : bucket,
        Key : key,
        Body : data,
        ContentMD5: md5Hash
    };
    
    try {
        await s3Client.send(new PutObjectCommand(params));
        console.log("Successfully written to S3");
    }
    catch(err) {
        console.log("Error occured in put object: ", err);
    }
};

const checkIfUserexists = async (s3Client, bucket, key) => {
    var params = {
        Bucket: bucket,
        Key: key
    };
    var returnValue;
    await s3Client.send(new HeadObjectCommand(params))
        .then((data) => { //file exists - don't write
            returnValue = true;
        })
        .catch((err) => {
            if(err.name === 'NotFound') { //File does not exist
                returnValue = false;
            }
            else if(err.name === 403|| err.message === 'UnknownError') { 
                returnValue = true;
            }
        });
    return returnValue;
};

const getMD5HashFromFile = async (file) => {
    var hash = md5.MD5(file);
    var base64Hash = hash.toString(md5.enc.Base64);
    return base64Hash;
};