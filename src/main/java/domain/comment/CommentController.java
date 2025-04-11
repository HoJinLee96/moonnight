package domain.comment;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
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
import lombok.RequiredArgsConstructor;

@Component
@RestController
@RequestMapping("/api/private/comment")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  
  @PreAuthorize("hasRole('GUEST')")
  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponseDto>> registerComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @RequestBody CommentRequestDto commentRequestDto) {
    
    String email = userDetails.getEmail();
    UserProvider userProvider = userDetails.getUserProvider();
    
    CommentResponseDto commentResponseDto = commentService.registerComment(userProvider, email, commentRequestDto);
    
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 목록 조회 성공", commentResponseDto));
  }
  
  @PreAuthorize("hasRole('GUEST')")
  @GetMapping("/estimate/{estimateSeq}")
  public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getCommentList(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @PathVariable int estimateSeq) {
    List<CommentResponseDto> responseDtoList = commentService.getCommentList(estimateSeq, userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 목록 조회 성공", responseDtoList));
  }
  
  @PreAuthorize("hasRole('GUEST')")
  @PutMapping("/{commentSeq}")
  public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @PathVariable int commentSeq, 
      @RequestBody CommentRequestDto commentRequestDto) {
    
    CommentResponseDto commentResponseDto = commentService.updateComment(userDetails.getUserProvider(), userDetails.getEmail(), commentSeq, commentRequestDto.commentText());
    return ResponseEntity.ok(ApiResponse.of(200, "댓글 수정 성공", commentResponseDto));
  }
  
  @PreAuthorize("hasRole('GUEST')")
  @DeleteMapping("/{commentSeq}")
  public ResponseEntity<Void> deleteComment(
      @AuthenticationPrincipal CustomUserDetails userDetails, 
      @PathVariable int commentSeq) {
    
    commentService.deleteComment(userDetails.getUserProvider(), userDetails.getEmail(), commentSeq);
    return ResponseEntity.noContent().build();
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
