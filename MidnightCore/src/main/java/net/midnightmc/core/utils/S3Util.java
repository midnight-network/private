package net.midnightmc.core.utils;

import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public final class S3Util {

    public static final String S3_BUCKET = "midnight";
    private static final StaticCredentialsProvider awsCreds = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                    "49Z1I1FPZCHH98TI7YSN",
                    "AlEATB353b4JwAYQ3G55yZjaIWVPNEvXSdgrhu1v"));

    private S3Util() {}

    public static S3Client getS3Client() {
        return S3Client.builder()
                .credentialsProvider(awsCreds)
                .region(Region.AP_NORTHEAST_2)
                .endpointOverride(URI.create("https://ewr1.vultrobjects.com"))
                .build();
    }

    public static boolean download(File file, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(S3_BUCKET)
                .key(key)
                .build();

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
        }
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            IOUtils.copy(getS3Client().getObject(getObjectRequest), outputStream);
        } catch (IOException | S3Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean upload(File file, String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(S3_BUCKET)
                .key(key)
                .build();
        try {
            getS3Client().putObject(objectRequest, RequestBody.fromFile(file));
        } catch (S3Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
