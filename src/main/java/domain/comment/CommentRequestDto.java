package domain.comment;

import domain.comment.Comment.CommentStatus;
import domain.estimate.Estimate;
import domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CommentRequestDto(
    
    @NotNull(message = "{error.sequnce.not_found}")
    int estimateId,
    
    @NotBlank(message = "{validation.comment.text.required}")
    @Size(max = 250, message = "{validation.comment.text.length}")
    String commentText
    
) {
    
    public Comment toEntity(User user, Estimate estiamte) {
      return Comment.builder()
              .user(user)
              .estimate(estiamte)
              .commentText(commentText)
              .commentStatus(CommentStatus.ACTIVE)
              .build();
    }

}
