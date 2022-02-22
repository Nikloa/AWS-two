package ru.vironit;

import ru.vironit.service.AWSS3Service2;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import ru.vironit.service.AWSS3Service;
import ru.vironit.implementation.SimpleConsole;
import ru.vironit.implementation.SimpleFrame;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Scanner;

public class S3Application {

    private static final AWSCredentials credentials;

    private static final AwsBasicCredentials awsCreds;

    static {
        credentials = new BasicAWSCredentials(
                "AKIA4HKNEZGSIHXEJIO7",
                "2PVE6Z3RovsiGQFvqG87FBqBUqY95KvbsZto7wX7"
        );

        awsCreds = AwsBasicCredentials.create(
                "AKIA4HKNEZGSIHXEJIO7",
                "2PVE6Z3RovsiGQFvqG87FBqBUqY95KvbsZto7wX7");
    }

    public static void main(String[] args) {

        Scanner keyboard = new Scanner(System.in);
        AmazonS3 s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_2)
                .withRegion(Regions.EU_WEST_1)
                .build();

        Region region = Region.EU_WEST_1;
        S3Client s3Client2 = S3Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();

        AWSS3Service awsService = new AWSS3Service(s3client);
        AWSS3Service2 awsService2 = new AWSS3Service2(s3Client2);

        while (true) {
            System.out.print("Do you want use console? [y/n]: ");
            switch (keyboard.next()) {
                case "y":
                case "Y": new SimpleConsole(awsService, keyboard); break;
                case "n":
                case "N": new SimpleFrame(awsService2); break;
                default:
                    System.out.println("--- Unknown command ---");
                    continue;
            }
            return;
        }
    }
}
