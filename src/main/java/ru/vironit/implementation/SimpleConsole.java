package ru.vironit.implementation;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import ru.vironit.model.MetadataEntity;
import ru.vironit.other.FileData;
import ru.vironit.service.AWSS3Service;
import ru.vironit.service.MetadataService;

//import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SimpleConsole {

    public SimpleConsole(AWSS3Service awsService, Scanner keyboard) {
        init(awsService, keyboard);
    }

    private void init(AWSS3Service awsService, Scanner keyboard) {
        String command;

        while (true) {

            System.out.println("Enter \"help\" to view commands");
            System.out.print("Enter command: ");
            command = keyboard.next();
            try {
                switch (command) {
                    case "help": viewCommands(); break;
                    case "createBucket": createBucket(awsService, keyboard); break;
                    case "listBuckets": listBuckets(awsService); break;
                    case "deleteBucket": deleteBucket(awsService, keyboard); break;
                    case "uploadObject": uploadObject(awsService, keyboard); break;
                    case "listObjects": listObject(awsService, keyboard); break;
                    case "downloadObject": downloadObject(awsService, keyboard); break;
                    case "copyObject": copyObject(awsService, keyboard); break;
                    case "deleteObject": deleteObject(awsService, keyboard); break;
                    case "deleteObjects": deleteObjects(awsService, keyboard); break;
                    case "exit": return;
                    default:
                        System.out.println("--- Unknown command ---");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createBucket(AWSS3Service awsService, Scanner keyboard) {

        System.out.print("Enter bucket name:");
        String bucketName = keyboard.next();
        if (awsService.doesBucketExist(bucketName)) {
            System.out.println("Bucket name is not available."
                    + " Try again with a different Bucket name.");
        } else {
            awsService.createBucket(bucketName);
            System.out.println("Bucket with name " + bucketName + " created");
        }
    }

    private void listBuckets(AWSS3Service awsService) {
        List<Bucket> buckets = awsService.listBuckets();
        System.out.println("Buckets:");
        for (Bucket s : buckets) {
            System.out.println(s.getName());
        }
    }

    private void deleteBucket(AWSS3Service awsService, Scanner keyboard) {
        System.out.print("Enter bucket name for deleting: ");
        String bucketName = keyboard.next();
        try {
            awsService.deleteBucket(bucketName);
            System.out.println("Bucket " + bucketName + " deleted");
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    private void uploadObject(AWSS3Service awsService, Scanner keyboard) throws IOException {
        System.out.print("Enter bucket name: ");
        String bucketName = keyboard.next();
        System.out.print("Enter Amazon directory: ");
        String directory = keyboard.next();
        System.out.print("Enter path to file: ");
        String path = keyboard.next();

        File file = new File(path);
        ObjectMetadata objectMetadata = FileData.generateMetadata(file);

        awsService.putObject(
                new PutObjectRequest(bucketName, directory, file)
                .withMetadata(objectMetadata));

        System.out.println("Object with name " + directory + " added");
    }

    private void listObject(AWSS3Service awsService, Scanner keyboard) {
        System.out.print("Enter bucket name: ");
        String bucketName = keyboard.next();
        System.out.println("Objects: ");
        ObjectListing objectListing = awsService.listObjects(bucketName);
        for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
            System.out.println(os.getKey());
        }
    }

    private void downloadObject(AWSS3Service awsService, Scanner keyboard) throws IOException {

        System.out.print("Enter bucketName: ");
        String bucketName = keyboard.next();
        System.out.print("Enter Amazon directory: ");
        String directory = keyboard.next();
        System.out.print("Enter path for file: ");
        String path = keyboard.next();

        S3Object s3Object = awsService.getObject(bucketName, directory);
        MetadataEntity metadata = FileData.writeMetadata(s3Object.getObjectMetadata().getUserMetadata());

        MetadataService.save(metadata);

        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        FileUtils.copyInputStreamToFile(inputStream, new File(path));
        System.out.println("File " + directory + " downloaded to " + path);
    }

    private void copyObject(AWSS3Service awsService, Scanner keyboard) {
        System.out.print("Enter source bucket name: ");
        String sourceBucket = keyboard.next();
        System.out.print("Enter source key: ");
        String sourceKey = keyboard.next();
        System.out.print("Enter destination bucket name: ");
        String destinationBucket = keyboard.next();
        System.out.print("Enter destination key: ");
        String destinationKey = keyboard.next();
        awsService.copyObject(
                sourceBucket,
                sourceKey,
                destinationBucket,
                destinationKey
        );
        System.out.println("File " + sourceKey + " copied to " + destinationKey);
    }

    private void deleteObject(AWSS3Service awsService, Scanner keyboard) {
        System.out.print("Enter bucket name: ");
        String bucketName = keyboard.next();
        System.out.print("Enter object directory: ");
        String directory = keyboard.next();
        awsService.deleteObject(bucketName, directory);
        System.out.println("Object " + directory + " deleted");
    }

    private void deleteObjects(AWSS3Service awsService, Scanner keyboard) {
        System.out.print("Enter bucket name: ");
        String bucketName = keyboard.next();
        ArrayList<String> files = new ArrayList<>();
        System.out.println("Enter \"/out\" for exit");
        String string;
        while (true) {
            System.out.print("Enter another one object directory: ");
            string = keyboard.next();
            if (string.equals("/out")) {
                break;
            }
            files.add(string);
        }
        String[] objKeyArr = files.toArray(new String[0]);
        DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucketName)
                .withKeys(objKeyArr);
        awsService.deleteObjects(delObjReq);
        System.out.println("Objects " + String.join(", ", objKeyArr) + " deleted");
    }

    private void viewCommands() {
        System.out.println("List of commands: ");
        System.out.println("--> createBucket");
        System.out.println("--> listBuckets");
        System.out.println("--> deleteBucket");
        System.out.println("--> uploadObject");
        System.out.println("--> listObjects");
        System.out.println("--> downloadObjects");
        System.out.println("--> copyObject");
        System.out.println("--> deleteObject");
        System.out.println("--> deleteObjects");
        System.out.println("--> exit");
    }
}
