package dto;

import java.time.LocalDateTime;

public class CommentDto {

  private int commentSeq;
  private int userSeq;
  private int estimateSeq;
  private String commentText;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  
  public CommentDto() {
  }

  public CommentDto(int commentSeq, int userSeq, int estimateSeq, String commentText, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.commentSeq = commentSeq;
    this.userSeq = userSeq;
    this.estimateSeq = estimateSeq;
    this.commentText = commentText;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public CommentDto(int userSeq, int estimateSeq, String commentText) {
    this.userSeq = userSeq;
    this.estimateSeq = estimateSeq;
    this.commentText = commentText;
  }
  
  public CommentDto(int commentSeq, int userSeq, int estimateSeq, String commentText) {
    this.userSeq = userSeq;
    this.estimateSeq = estimateSeq;
    this.commentText = commentText;
  }

  public int getCommentSeq() {
    return commentSeq;
  }

  public void setCommentSeq(int commentSeq) {
    this.commentSeq = commentSeq;
  }

  public int getEstimateSeq() {
    return estimateSeq;
  }

  public void setEstimateSeq(int estimateSeq) {
    this.estimateSeq = estimateSeq;
  }

  public String getCommentText() {
    return commentText;
  }

  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public int getUserSeq() {
    return userSeq;
  }

  public void setUserSeq(int userSeq) {
    this.userSeq = userSeq;
  }

  @Override
  public String toString() {
    return "CommentDto [commentSeq=" + commentSeq + ", userSeq=" + userSeq + ", estimateSeq="
        + estimateSeq + ", commentText=" + commentText + ", createdAt=" + createdAt + ", updatedAt="
        + updatedAt + "]";
  }
  
  
  

  
}
