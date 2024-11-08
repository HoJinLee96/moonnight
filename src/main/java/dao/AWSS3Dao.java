package dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@PropertySource("classpath:application.properties")
public class AWSS3Dao {
  @Value("${aws.credentials.access-key}")
  private String accessKey;
  @Value("${aws.credentials.secret-key}")
  private String secretKey;
  @Value("${aws.region.static}")
  private String region;
  @Value("${aws.s3.bucket}")
  private String bucket;
  
  
  private final String S3_BUCKET_URL =
      "s3://chamman/estimateImages/";

  public List<File> getImageFileList(String imagesPath) throws IOException {
    S3Client s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();

    String[] paths = imagesPath.split(",");
    List<File> imagesFile = new ArrayList<>();

    for (String path : paths) {

      try (ResponseInputStream<GetObjectResponse> inputStream =
          s3Client.getObject(GetObjectRequest.builder()
              .bucket(bucket)
              .key(path)
              .build())) {
        
        File tempFile = File.createTempFile("s3image-", ".tmp");

        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
          byte[] buffer = new byte[1024];
          int bytesRead;
          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
          }
        }
        imagesFile.add(tempFile);

      }

    }
    return imagesFile;
  }

  public String uploadImagesToS3(List<File> images, String phone) throws IOException {
    
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String keyPrefix = "estimateImages/" + timestamp + "_" + phone.replace("-", "") + "/";
    StringBuilder imagePaths = new StringBuilder();

    S3Client s3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
        .build();

    for (File image : images) {
      String key = keyPrefix + image.getName();
        try (InputStream inputStream = new FileInputStream(image)) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .acl("public-read")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, image.length()));
            imagePaths.append(key).append(",");
        }
    }
    
    // Remove trailing comma
    if (imagePaths.length() > 0) {
        imagePaths.setLength(imagePaths.length() - 1);
    }

    return imagePaths.toString();
  }
  
  
  
  
}
