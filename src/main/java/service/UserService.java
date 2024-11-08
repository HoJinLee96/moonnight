package service;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dao.AddressDao;
import dao.LoginLogDao;
import dao.UserDao;
import dto.AddressDto;
import dto.User;
import dto.UserDto;
import dto.User.Status;
import exception.FailReason;
import exception.NotFoundException;
import exception.NotUpdateException;

@Service
public class UserService {
    private UserDao userDao;
    private AddressDao addressDao;
    private LoginLogDao loginLogDao;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDao userDao,AddressDao addressDao, LoginLogDao loginLogDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.addressDao = addressDao;
        this.loginLogDao = loginLogDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public int registerUser(UserDto userDto,AddressDto addressDto) throws SQLException {
      String encodePassoword = passwordEncoder.encode(userDto.getPassword());
      int userSeq = userDao.registerUser(userDto, encodePassoword);
      addressDto.setUserSeq(userSeq);
      int addressSeq = addressDao.registerAddress(addressDto);
      userDao.updateAddressSeq(userSeq, addressSeq);
      return userSeq;
    }
    
    public boolean comparePasswordByUserSeq(int userSeq, String reqPassword) throws NotFoundException, SQLException{
      String password = userDao.getPasswordBySeq(userSeq).orElseThrow(()->new NotFoundException());
      return passwordEncoder.matches(reqPassword, password);
    }
      
    
    public boolean comparePasswordByEmail(String email, String reqPassword, String ip) throws SQLException, NotFoundException {
      String password = userDao.getPasswordByEmail(email).orElseThrow(()->new NotFoundException());
      
      UserDto userDto = getUserByEmail(email);
      
      if(passwordEncoder.matches(reqPassword, password)) {
        loginLogDao.loginSuccess(userDto, ip);
        return true;
      }else {
        loginLogDao.loginFail(userDto, ip, FailReason.INVALID_PASSWORD.name());
        
        if(countLoginFail(email)>4) {
          userDto.setStatus(Status.STAY);
          updateStatus(userDto);
        }
        return false;
      }
    }
    
    public UserDto getUserBySeq(int userSeq) throws NotFoundException, SQLException {
      return userDao.getUserBySeq(userSeq).orElseThrow(()-> new NotFoundException("일치하는 회원이 없습니다."));
    }

    public UserDto getUserByEmail(String email) throws NotFoundException, SQLException {
      return userDao.getUserByEmail(email).orElseThrow(()-> new NotFoundException("일치하는 회원이 없습니다."));
    }
    
    public String getEmailByPhone(String phone) throws NotFoundException, SQLException {
      return userDao.getEmailByPhone(phone).orElseThrow(()-> new NotFoundException("일치하는 회원이 없습니다."));
    }

    public List<UserDto> getAllUsers() throws SQLException{
      return userDao.getAllUsers();
    }
    
    public String getUserStatusByEmail(String email) throws NotFoundException {
      return userDao.getUserStatusByEmail(email);
    }

    @Transactional
    public void updateInfo(UserDto userDto) throws SQLException{
      userDao.updateInfo(userDto);
    }

    @Transactional
    public void updatePassword(int userSeq,String newPassword) throws SQLException{
      String newEncodePassword = passwordEncoder.encode(newPassword);
      userDao.updatePassword(userSeq,newEncodePassword);
    }
    
    @Transactional
    public void updateStatus(User reqUser) throws SQLException{
      userDao.updateStatus(reqUser.getEmail(), reqUser.getStatus().name());
    }
    
    @Transactional
    public void updateAddressSeq(int userSeq, int addressSeq) throws SQLException, NotUpdateException{
      int result = userDao.updateAddressSeq(userSeq, addressSeq);
      if(result==0)
        throw new NotUpdateException();
    }

    @Transactional
    public void stopUser(String email, String password, String ip) throws SQLException, NotFoundException {
      if(comparePasswordByEmail(email, password, ip)) {
        userDao.stopUser(email);
      }
    }
    
    public boolean isEmailExists(String email) {
      return userDao.isEmailExists(email);
    }
    
    public boolean isPhoneExists(String phone) {
      return userDao.isPhoneExists(phone);
    }
    
    public boolean isEmailPhoneExists(String email, String phone){
      return userDao.isEmailPhoneExists(email, phone);
    }
    
    public int countLoginFail(String id) {
      return userDao.countLoginFail(id);
    }

}