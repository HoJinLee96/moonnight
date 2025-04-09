package domain.comment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auth.crypto.Obfuscator;
import domain.comment.Comment.CommentStatus;
import domain.estimate.Estimate;
import domain.estimate.EstimateService;
import domain.user.User;
import domain.user.User.UserProvider;
import domain.user.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;
  private final EstimateService estimateService;
  private final Obfuscator obfuscator;
  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

  @Transactional
  public CommentResponseDto registerComment(UserProvider userProvider, String email, CommentRequestDto commentRequestDto) {

    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    
    Estimate estimate = estimateService.getEstimateById(commentRequestDto.estimateId());
    
    Comment comment = commentRequestDto.toEntity(user, estimate);
    
    return CommentResponseDto.fromEntity(commentRepository.save(comment), user.getUserSeq(), obfuscator);
  }

  public List<CommentResponseDto> getCommentList(int estimateId, int userId) {
    
    User user = userService.getUserByUserId(userId);
    
    List<Comment> comments = commentRepository.findByEstimate_EstimateSeq(obfuscator.decode(estimateId));
    
    return comments.stream()
        .filter(e->e.getCommentStatus()!=CommentStatus.DELETE)
        .map(comment -> CommentResponseDto.fromEntity(comment, user.getUserSeq(), obfuscator))
        .collect(Collectors.toList());
  }

  @Transactional
  public CommentResponseDto updateComment(UserProvider userProvider, String email, int commentId, String newText) {
    
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    
    Comment comment = getAuthorizedComment(userProvider, email, obfuscator.decode(commentId));
    comment.setCommentText(newText);
    commentRepository.flush();  
    
    Comment updatedComment = commentRepository.getReferenceById(comment.getCommentSeq());

    return CommentResponseDto.fromEntity(updatedComment,user.getUserSeq(),obfuscator);
  }

  @Transactional
  public void deleteComment(UserProvider userProvider, String email, int commentId) {
      Comment comment = getAuthorizedComment(userProvider, email, obfuscator.decode(commentId));
      comment.setCommentStatus(CommentStatus.DELETE);
  }
  
  private Comment getAuthorizedComment(UserProvider userProvider, String email, int commentId) {
      Comment comment = commentRepository.findById(commentId)
          .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 댓글. commentId: " + commentId));

      if (comment.getCommentStatus() == Comment.CommentStatus.DELETE) {
        logger.warn("삭제된 댓글을 수정 시도. commentId: {}", commentId);
        throw new AccessDeniedException("삭제된 댓글을 수정 시도.");
      }

      if (!comment.getUser().getEmail().equals(email) || !comment.getUser().getUserProvider().equals(userProvider)) {
        logger.warn("댓글 권한 없음. commentId: {} by userProvider: {}, email:{}", commentId, userProvider, email);
        throw new AccessDeniedException("댓글 권한 없음.");
      }
      return comment;
  }

  
//  @Transactional
//  public Comment registerCommentDto(Comment commentDto) throws SQLException, IllegalStateException, EmptyResultDataAccessException{
//      int commentSeq = commentDao.registerCommentDto(commentDto);
//      return commentDao.getCommentDtoByCommentSeq(commentSeq);
//  }
//  
//  public Comment getCommentDto(int commentSeq) throws SQLException, NoSuchElementException {
//    return commentDao.getCommentDtoByCommentSeq(commentSeq);
//  }
//  
//  public List<Comment> getCommentList(int estimateSeq) throws SQLException {
//    List<Comment> list = commentDao.getCommentListByEstimateSeq(estimateSeq);
//    return list;
//  }
//  
//  public List<Comment> getALLCommentList(int estimateSeq) throws SQLException {
//    List<Comment> list = commentDao.getCommentListByEstimateSeq(estimateSeq);
//    return list;
//  }
//  
//  @Transactional
//  public Comment updateComment(int userSeq, Comment newCommentDto) throws SQLException, NoSuchElementException, AccessDeniedException{
//    Comment oldCommentDto = commentDao.getCommentDtoByCommentSeq(newCommentDto.getCommentSeq());
//    if (oldCommentDto.getUserSeq() != userSeq) {
//      throw new AccessDeniedException("답글 수정 권한이 없습니다.");
//    }
//    commentDao.updateComment(newCommentDto);
//    return commentDao.getCommentDtoByCommentSeq(newCommentDto.getCommentSeq());
//  }
//  
//  @Transactional
//  public Comment deleteComment(int userSeq, int commentSeq) throws SQLException, AccessDeniedException{
//    Comment commentDto = commentDao.getCommentDtoByCommentSeq(commentSeq);
//    if (commentDto.getUserSeq() != userSeq) {
//      throw new AccessDeniedException("답글 삭제 권한이 없습니다.");
//    }
//    commentDao.updateCommentStatus(commentSeq, Status.DELETE);
//    return commentDao.getAllCommentDtoByCommentSeq(commentSeq);
//  }
  
  
}
