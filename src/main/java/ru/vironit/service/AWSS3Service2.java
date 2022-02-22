package ru.vironit.service;

import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AWSS3Service2 {

    private final S3Client s3Client;

    public AWSS3Service2(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String createBucket(String bucketName) {
        S3Waiter s3Waiter = s3Client.waiter();
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        s3Client.createBucket(bucketRequest);
        HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
        waiterResponse.matched().response().ifPresent(System.out::println);
        return bucketName + " is ready";
    }

    public List<Bucket> listBuckets() {
        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
        return new ArrayList<>(listBucketsResponse.buckets());
    }

    public void deleteBucket(String bucketName) {
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response listObjectsV2Response;

        do {
            listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
            for (S3Object s3Object : listObjectsV2Response.contents()) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Object.key())
                        .build());
            }

            listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName)
                    .continuationToken(listObjectsV2Response.nextContinuationToken())
                    .build();

        } while(listObjectsV2Response.isTruncated());

        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
        s3Client.deleteBucket(deleteBucketRequest);
    }

    public PutObjectResponse putObject(String bucketName, String key, Path path, Map<String, String> metadata) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .metadata(metadata)
                .build();

        return s3Client.putObject(objectRequest, path);
    }

    public List<S3Object> listObjects(String bucketName) {
        ListObjectsRequest listObjects = ListObjectsRequest
                .builder()
                .bucket(bucketName)
                .build();

        ListObjectsResponse response = s3Client.listObjects(listObjects);
        return response.contents();
    }

    public ResponseInputStream<GetObjectResponse> getObject(String bucketName, String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        return s3Client.getObject(getObjectRequest);
    }

    public HeadObjectResponse getHeadObjectResponse(String bucketName, String objectKey) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        return s3Client.headObject(headObjectRequest);
    }

    public String copyObject(
            String fromBucket,
            String sourceKey,
            String toBucket,
            String destinationKey
    ) {
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(fromBucket + "/" + sourceKey, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return ("URL could not be encoded: " + e.getMessage());
        }
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .copySource(encodedUrl)
                .destinationBucket(toBucket)
                .destinationKey(destinationKey)
                .build();

        try {
            CopyObjectResponse copyRes = s3Client.copyObject(copyReq);
            return copyRes.copyObjectResult().toString();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "Copied successful";
    }

    public void deleteObject(String bucketName, String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
