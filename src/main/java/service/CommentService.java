package service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dao.CommentDao;
import dto.CommentDto;
import exception.AccessDeniedException;
import exception.NotFoundException;
import exception.NotUpdateException;

@Service
public class CommentService {

  CommentDao commentDao;

  @Autowired
  public CommentService(CommentDao commentDao) {
    this.commentDao = commentDao;
  }

  @Transactional
  public void registerComment(CommentDto commentDto) throws SQLException, NotUpdateException {
    int commentSeq = commentDao.registerComment(commentDto);
    commentDto.setCommentSeq(commentSeq);
  }
  
  public Optional<CommentDto> getComment(int commentSeq) throws SQLException {
    return commentDao.getComment(commentSeq);
  }
  
  public List<CommentDto> getCommentList(int estimateSeq) throws SQLException, NotFoundException {
    List<CommentDto> list = commentDao.getCommentList(estimateSeq);
    if(list.isEmpty()) throw new NotFoundException(); else return list;
  }
  
  @Transactional
  public boolean updateComment(int userSeq, CommentDto newCommentDto) throws SQLException {
    CommentDto oldCommentDto = commentDao.getComment(newCommentDto.getCommentSeq())
        .orElseThrow(() -> new NotFoundException());
    if (oldCommentDto.getUserSeq() != userSeq) {
      throw new AccessDeniedException();
    }
    int result = commentDao.updateComment(newCommentDto);
    if(result!=1) {
      throw new SQLException("comment 업데이트 실패 commentSeq : " + newCommentDto.getCommentSeq());
    }
    return true;
  }
  
  @Transactional
  public boolean deleteComment(int userSeq, int commentSeq) throws SQLException {
    CommentDto commentDto =
        commentDao.getComment(commentSeq).orElseThrow(() -> new NotFoundException());
    if (!(commentDto.getUserSeq() == userSeq))
      throw new AccessDeniedException();

    int result = commentDao.deleteComment(commentSeq);
    if (result != 1) {
      throw new SQLException("comment 삭제 실패 commentSeq : " + commentSeq);
    }
    return true;
  }
  
  
}
