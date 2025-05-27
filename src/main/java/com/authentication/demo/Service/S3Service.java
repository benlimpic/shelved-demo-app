package com.authentication.demo.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service

public class S3Service {
  private final S3Client s3Client;

  public S3Service() {
    this.s3Client = S3Client.builder()
        .region(Region.US_WEST_1) // Change to your region
        .build();
  }

  public void createBucket(String bucketName) {
    if (!s3Client.listBuckets().buckets().stream()
        .anyMatch(bucket -> bucket.name().equals(bucketName))) {
      s3Client.createBucket(CreateBucketRequest.builder()
          .bucket(bucketName)
          .build());
    }
  }

  public void uploadFile(String bucketName, String key, InputStream inputStream, String contentType) {
    try {
      s3Client.putObject(PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType(contentType)
          .build(),
          software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream,
              inputStream.available()));
    } catch (java.io.IOException | software.amazon.awssdk.core.exception.SdkClientException e) {
      throw new RuntimeException("Failed to upload file to S3", e);
    }
  }

  public List<String> listFiles(String bucketName) {
    return s3Client.listObjectsV2(ListObjectsV2Request.builder()
        .bucket(bucketName)
        .build())
        .contents()
        .stream()
        .map(S3Object::key)
        .collect(Collectors.toList());
  }

  public String generatePresignedUrl(String bucketName, String key) {
    return "https://" + bucketName + ".s3." + Region.US_WEST_1.id() + ".amazonaws.com/" + key;
  }

  public void deleteFile(String bucketName, String key) {
    s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
  }

}
