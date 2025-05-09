package infra.s3;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class AwsS3Cilent {

  @Value("${aws.s3.bucket}")
  private String bucket;
  private S3Client s3Client;
  
  public List<String> uploadFiles(List<MultipartFile> files, String keyPrefix) {
    return files.stream()
            .map(file->uploadFile(file, keyPrefix))
            .collect(Collectors.toList());
  }

  private String uploadFile(MultipartFile file, String keyPrefix) {
      try {
          String key = keyPrefix + UUID.randomUUID() + "_" + file.getOriginalFilename(); // 고유한 파일명 생성

          // S3 업로드 요청 생성
          PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                  .bucket(bucket)
                  .key(key)
                  .contentType(file.getContentType())
                  .acl("public-read")
                  .build();

          // S3에 파일 업로드
          s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

          // 업로드된 파일의 S3 URL 반환
          return "https://" + bucket + ".s3.amazonaws.com/" + key;

      } catch (Exception e) {
          throw new RuntimeException("S3 업로드 실패", e);
      }
  }

//  public Optional<String> uploadImagesToS3(List<File> images, String keyPrefix) throws IOException {
//    
//    StringBuilder imagePaths = new StringBuilder();
//    
//    for (File image : images) {
//      String key = keyPrefix + image.getName();
//      try (InputStream inputStream = new FileInputStream(image)) {
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//            .bucket(bucket)
//            .key(key)
//            .acl("public-read")
//            .build();
//        
//        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, image.length()));
//        imagePaths.append(key).append(",");
//      }
//    }
//    
//    // 콤마 삭제
//    if (imagePaths.length() > 0) {
//      imagePaths.setLength(imagePaths.length() - 1);
//    }
//    return Optional.of(imagePaths.toString());
//  }
  
//  private final String S3_BUCKET_URL = "s3://chamman/estimateImages/";

//  public List<File> getFiles(String[] paths) throws IOException {
//    List<File> imagesFile = new ArrayList<>();
//
//    for (String path : paths) {
//      try (ResponseInputStream<GetObjectResponse> inputStream =
//          s3Client.getObject(GetObjectRequest.builder()
//              .bucket(bucket)
//              .key(path)
//              .build())) {
//        
//        File tempFile = File.createTempFile("s3image-", ".tmp");
//
//        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
//          byte[] buffer = new byte[1024];
//          int bytesRead;
//          while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//          }
//        }
//        imagesFile.add(tempFile);
//      }
//    }
//    return imagesFile;
//  }

  public void deleteFiles(String[] paths) {
      if (paths == null || paths.length == 0) {
          System.out.println("No files to delete.");
          return;
      }

      // S3 Object Key 리스트 변환
      List<ObjectIdentifier> objectIdentifiers = List.of(paths).stream()
              .map(path -> ObjectIdentifier.builder().key(path).build())
              .collect(Collectors.toList());

      // DeleteObjectsRequest 생성
      DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
              .bucket(bucket)
              .delete(builder -> builder.objects(objectIdentifiers))
              .build();
      
      s3Client.deleteObjects(deleteRequest);
      System.out.println("AWS S3 파일 삭제 완료. ");
  }

}
