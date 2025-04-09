package global.annotation;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageValidator implements ConstraintValidator<ImageConstraint, List<MultipartFile>> {

  @Override
  public boolean isValid(List<MultipartFile> images, ConstraintValidatorContext context) {
      if (images == null || images.isEmpty()) {
          return true; // 이미지는 optional이면 true로 통과시켜도 됨
      }

      if (images.size() > 10) {
          context.disableDefaultConstraintViolation();
          context.buildConstraintViolationWithTemplate("이미지는 최대 10장까지 업로드 가능합니다.")
                 .addConstraintViolation();
          return false;
      }

      for (MultipartFile file : images) {
          if (file.getSize() > 10 * 1024 * 1024) { // 10MB로 수정
              context.disableDefaultConstraintViolation();
              context.buildConstraintViolationWithTemplate("이미지 1장의 최대 크기는 10MB입니다.")
                     .addConstraintViolation();
              return false;
          }
      }

      return true;
  }
}
