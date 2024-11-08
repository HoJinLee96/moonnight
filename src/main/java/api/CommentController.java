package api;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.CommentDto;
import dto.UserDto;
import exception.NotFoundException;
import exception.NotUpdateException;
import jakarta.servlet.http.HttpSession;
import service.CommentService;

@RestController
@RequestMapping("/comment")
public class CommentController {

  CommentService commentService;

  @Autowired
  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }
  
  
  @PostMapping("/register")
  public ResponseEntity<?> registerComment(HttpSession session, @RequestBody CommentDto commentDto) {
    
    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
    
    if(commentDto.getCommentText() == "" || commentDto.getUserSeq() == 0 || commentDto.getEstimateSeq() ==0)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    
    commentDto.setUserSeq(userSeq);
    try {
      commentService.registerComment(commentDto);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotUpdateException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/getList")
  public ResponseEntity<?> getCommentList(@RequestBody int estimateSeq) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json; charset=UTF-8");
    if (estimateSeq == 0)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    
    try {
      List<CommentDto> list = commentService.getCommentList(estimateSeq);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      String listJson =objectMapper.writeValueAsString(list);   
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(listJson);
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/update")
  public ResponseEntity<?> updateComment(HttpSession session, @RequestBody CommentDto commentDto) {
    
    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
    
    if(userSeq == 0)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    
    commentDto.setUserSeq(userSeq);
    
    try {
      commentService.updateComment(userSeq,commentDto);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } 
    
  }
  
  @PostMapping("/delete")
  public ResponseEntity<?> deleteComment(HttpSession session,@RequestBody int commentSeq) {
    
    int userSeq = ((UserDto) session.getAttribute("UserDto")).getUserSeq();
    
    if(commentSeq == 0 || userSeq == 0)
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    
    try {
      commentService.deleteComment(userSeq,commentSeq);
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } 
    
  }
  
}
