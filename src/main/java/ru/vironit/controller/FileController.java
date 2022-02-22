package ru.vironit.controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import ru.vironit.model.MetadataEntity;
import ru.vironit.other.FileData;
import ru.vironit.service.AWSS3Service;
import ru.vironit.service.MetadataService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

@RestController
@RequestMapping("/")
public class FileController {

    private static final AWSCredentials credentials;

    private static final AWSS3Service awsService;

    static {
        credentials = new BasicAWSCredentials(
                "AKIA4HKNEZGSIHXEJIO7",
                "2PVE6Z3RovsiGQFvqG87FBqBUqY95KvbsZto7wX7"
        );

        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.EU_WEST_1)
                .build();

        awsService = new AWSS3Service(s3client);
    }

    @RequestMapping(value = "/buckets/create", method = RequestMethod.POST)
    public ResponseEntity<String> createBucket(@RequestParam String name) {
        if (awsService.doesBucketExist(name)) {
            return new ResponseEntity<>("Bucket name is not available."
                    + " Try again with a different Bucket name.", HttpStatus.BAD_REQUEST);
        } else {
            awsService.createBucket(name);
            return new ResponseEntity<>("Bucket with name " + name + " created", HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/buckets/list", method = RequestMethod.GET)
    public ResponseEntity<List<Bucket>> getBucketList() {
        List<Bucket> bucketList = awsService.listBuckets();
        return new ResponseEntity<>(bucketList, HttpStatus.OK);
    }

    @RequestMapping(value = "/buckets/delete", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteBucket(@RequestParam String name) {
        try {
            awsService.deleteBucket(name);
            return new ResponseEntity<>("Bucket " + name + " deleted", HttpStatus.OK);
        } catch (AmazonServiceException e) {
            return new ResponseEntity<>(e.getErrorMessage(), HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "/objects/upload", method = RequestMethod.POST)
    public ResponseEntity<String> uploadObject(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("bucket") String bucket,
            @RequestParam("key") String key) throws IOException {

        File file = File.createTempFile("temp", "tmp");
        multipartFile.transferTo(file);

        ObjectMetadata objectMetadata = FileData.generateMetadata(file);

        awsService.putObject(
                new PutObjectRequest(bucket, key, file)
                .withMetadata(objectMetadata));

        return new ResponseEntity<>("Object with name " + key + " added", HttpStatus.OK);
    }

    @RequestMapping(value = "/objects/list", method = RequestMethod.GET)
    public ResponseEntity<List<S3ObjectSummary>> getObjectsList(@RequestParam String bucket) {
        ObjectListing objectListing = awsService.listObjects(bucket);
        return new ResponseEntity<>(objectListing.getObjectSummaries(), HttpStatus.OK);
    }

    @RequestMapping(value = "/objects/download", method = RequestMethod.GET)
    public ResponseEntity<String> downloadObject(@RequestParam String bucket, String key) throws IOException {
        S3Object s3Object = awsService.getObject(bucket, key);

        MetadataEntity metadata = FileData.writeMetadata(s3Object.getObjectMetadata().getUserMetadata());
        MetadataService.save(metadata);

        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        FileUtils.copyInputStreamToFile(inputStream, new File("C:\\Users\\User\\Desktop\\AWS\\" + metadata.getFileName()));

        return new ResponseEntity<>("File " + key + " downloaded", HttpStatus.OK);
    }

    @RequestMapping(value = "/objects/delete", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteObjects(@RequestParam String bucket, String... keys) {
        try {
            DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucket)
                    .withKeys(keys);
            awsService.deleteObjects(delObjReq);
            return new ResponseEntity<>("Objects " + String.join(", ", keys) + " deleted", HttpStatus.OK);
        } catch (AmazonServiceException e) {
            return new ResponseEntity<>(e.getErrorMessage(), HttpStatus.NOT_MODIFIED);
        }
    }

    @RequestMapping(value = "/objects/copy", method = RequestMethod.POST)
    public ResponseEntity<String> copyObject(@RequestParam String source, String destination, String key) {
        try {
            awsService.copyObject(source, key, destination, key);
            return new ResponseEntity<>("File " + key + " copied to " + destination, HttpStatus.OK);
        } catch (AmazonServiceException e) {
            return new ResponseEntity<>(e.getErrorMessage(), HttpStatus.NOT_MODIFIED);
        }
    }
}
