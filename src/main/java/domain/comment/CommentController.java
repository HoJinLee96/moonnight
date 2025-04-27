package domain.comment;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import auth.sign.token.CustomUserDetails;
import domain.user.User.UserProvider;
import global.util.ApiResponse;
import global.validator.annotaion.ValidId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  
//  댓글 등록
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PostMapping("/private/register")
  public ResponseEntity<ApiResponse<CommentResponseDto>> registerComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @RequestBody CommentRequestDto commentRequestDto) {
    
    String email = userDetails.getEmail();
    UserProvider userProvider = userDetails.getUserProvider();
    
    CommentResponseDto commentResponseDto = commentService.registerComment(userProvider, email, commentRequestDto);
    
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 목록 조회 성공", commentResponseDto));
  }
  
//  특정 견적의 댓글 목록 조회 
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @GetMapping("/private/estimate/{estimateSeq}")
  public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getCommentList(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @PathVariable int estimateId) {
    List<CommentResponseDto> responseDtoList = commentService.getCommentList(estimateId, userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 목록 조회 성공", responseDtoList));
  }
  
//  댓글 수정
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PutMapping("/private/{commentSeq}")
  public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @ValidId @PathVariable int commentId, 
      @RequestBody CommentRequestDto commentRequestDto) {
    
    CommentResponseDto commentResponseDto = commentService.updateComment(userDetails.getUserProvider(), userDetails.getEmail(), commentRequestDto.estimateId(), commentId, commentRequestDto.commentText());
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 수정 성공", commentResponseDto));
  }
  
//  댓글 삭제
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @DeleteMapping("/private/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @PathVariable int commentId) {
    
    commentService.deleteComment(userDetails.getUserProvider(), userDetails.getEmail(), commentId);
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 삭제 성공", null));
  }
  
  
//  @PostMapping("/register")
//  public ResponseEntity<?> registerComment(HttpSession session, @RequestBody Comment commentDto) {
//    
//    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
//    
//    if(commentDto.getCommentText() == "" || commentDto.getUserSeq() == 0 || commentDto.getEstimateSeq() ==0)
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    
//    commentDto.setUserSeq(userSeq);
//    try {
//      commentService.registerComment(commentDto);
//      return ResponseEntity.status(HttpStatus.OK).build();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } catch (NotUpdateException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//  }
//  
//  @GetMapping("/getCommentListByEstimateSeq")
//  public ResponseEntity<?> getCommentListByEstimateSeq(@RequestParam("estimateSeq") int reqEstimateSeq) {
//
//    if (reqEstimateSeq <= 0)
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    
//    try {
//      List<Comment> list = commentService.getCommentList(reqEstimateSeq);
//      ObjectMapper objectMapper = new ObjectMapper();
//      objectMapper.registerModule(new JavaTimeModule());
//      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//      String listJson =objectMapper.writeValueAsString(list);   
//      HttpHeaders headers = new HttpHeaders();
//      headers.add("Content-Type", "application/json; charset=UTF-8");
//      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(listJson);
//      
//    } catch (SQLException | JsonProcessingException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } catch (NotFoundException e) {
//      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
//  }
//  
//  @PostMapping("/update")
//  public ResponseEntity<?> updateComment(HttpSession session, @RequestBody Comment commentDto) {
//    
//    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
//    
//    if(userSeq == 0)
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    
//    commentDto.setUserSeq(userSeq);
//    
//    try {
//      commentService.updateComment(userSeq,commentDto);
//      return ResponseEntity.status(HttpStatus.OK).build();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } 
//    
//  }
//  
//  @PostMapping("/delete")
//  public ResponseEntity<?> deleteComment(HttpSession session,@RequestBody int commentSeq) {
//    
//    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
//    
//    if(commentSeq == 0 || userSeq == 0)
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    
//    try {
//      commentService.deleteComment(userSeq,commentSeq);
//      return ResponseEntity.status(HttpStatus.OK).build();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } 
//    
  
}
