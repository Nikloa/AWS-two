package ru.vironit.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

import java.io.File;
import java.util.List;

public class AWSS3Service {

    private final AmazonS3 s3client;

    public AWSS3Service(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public  boolean doesBucketExist(String bucketName) {
        return s3client.doesBucketExistV2(bucketName);
    }

    public Bucket createBucket(String bucketName) {
        return s3client.createBucket(bucketName);
    }

    public List<Bucket> listBuckets() {
        return s3client.listBuckets();
    }

    public void deleteBucket(String bucketName) {
        s3client.deleteBucket(bucketName);
    }

    public PutObjectResult putObject(String bucketName, String key, File file) {
        return s3client.putObject(bucketName, key, file);
    }

    public PutObjectResult putObject(PutObjectRequest request) {
        return s3client.putObject(request);
    }

    public ObjectListing listObjects(String bucketName) {
        return s3client.listObjects(bucketName);
    }

    public S3Object getObject(String bucketName, String objectKey) {
        return s3client.getObject(bucketName, objectKey);
    }

    public CopyObjectResult copyObject(
            String sourceBucketName,
            String sourceKey,
            String destinationBucketName,
            String destinationKey
    ) {
        return s3client.copyObject(
                sourceBucketName,
                sourceKey,
                destinationBucketName,
                destinationKey
        );
    }

    public void deleteObject(String bucketName, String objectKey) {
        s3client.deleteObject(bucketName, objectKey);
    }

    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest delObjReq) {
        return s3client.deleteObjects(delObjReq);
    }
}
