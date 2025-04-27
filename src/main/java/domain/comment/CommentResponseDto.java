package domain.comment;

import java.time.LocalDateTime;
import auth.crypto.Obfuscator;
import lombok.Builder;

@Builder
public record CommentResponseDto(
    
    int estimateId,
    int commentId, 
    String commentText,
    LocalDateTime createdAt, 
    LocalDateTime updatedAt,
    boolean isMine

) {
  public static CommentResponseDto fromEntity(int estimateId, Comment comment, int userSeq, Obfuscator obfuscator) {
    boolean isMine = comment.getUser().getUserSeq()==userSeq;
    return CommentResponseDto.builder()
    .estimateId(obfuscator.encode(estimateId))
    .commentId(obfuscator.encode(comment.getCommentSeq()))
    .commentText(comment.getCommentText())
    .createdAt(comment.getCreatedAt())
    .updatedAt(comment.getUpdatedAt())
    .isMine(isMine)
    .build();
  }
}
