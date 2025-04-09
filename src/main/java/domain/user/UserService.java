package domain.user;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import auth.login.log.LoginLogService;
import auth.redis.TokenStore;
import auth.redis.TokenStore.TokenType;
import domain.address.Address;
import domain.address.AddressRepository;
import domain.user.User.UserProvider;
import domain.user.User.UserStatus;
import global.exception.DuplicationException;
import global.exception.StatusDeleteException;
import global.exception.StatusStayException;
import global.exception.StatusStopException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final AddressRepository addressRepository;
  private final LoginLogService loginLogService;
  private final TokenStore uuidProvider;
  private final PasswordEncoder passwordEncoder;
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  public String createJoinToken(String email, String password, String valificationEmailToken) {

    String emailRedis = uuidProvider.getVerificationEmail(valificationEmailToken);
    
    try {
      validateByReidsValue(email, emailRedis);
      
      isEmailExists(UserProvider.LOCAL, email);
      
      return uuidProvider.createMapToken(TokenType.ACCESS_JOIN, Map.of("email",email,"password",password));
    } finally {
        uuidProvider.removeToken(TokenType.VERIFICATION_EMAIL, valificationEmailToken);
    }
  }
  
  @Transactional
  public String joinLocalUser(UserCreateRequestDto userCreateRequestDto, String accessJoinToken, String verificationPhoneToken) {
    
    Map<String,String> mapValue = uuidProvider.getMapTokenData(TokenType.ACCESS_JOIN, accessJoinToken);
    String phoneRedis = uuidProvider.getVerificationPhone(verificationPhoneToken);
    
    try {
      validateByReidsValue(userCreateRequestDto.phone(), phoneRedis);
      
      isPhoneExists(UserProvider.LOCAL, phoneRedis);
      
      // 비밀번호 인코딩 후 저장
      String encodePassoword = passwordEncoder.encode(mapValue.get("password"));
      
      User user = userCreateRequestDto.toEntity();
      user.setEmail(mapValue.get("email"));
      user.setPassword(encodePassoword);
      userRepository.save(user);
      
      Address address = userCreateRequestDto.toAddressEntity();
      address.setUser(user);
      addressRepository.save(address);
      
      return userCreateRequestDto.name();
    } finally {
        uuidProvider.removeToken(TokenType.ACCESS_JOIN, accessJoinToken);
        uuidProvider.removeToken(TokenType.VERIFICATION_PHONE, verificationPhoneToken);
    }
  }
  
  public User getUserByUserId(int userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 유저."));
    validateStatus(user);
    return user;
  }
  public User getUserByUserProviderAndEmail(UserProvider userProvider, String email) {
    User user = userRepository.findByUserProviderAndEmail(userProvider, email)
        .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 유저입니다."));
    validateStatus(user);
    return user;
  }

  @Transactional
  public String verifyPhoneForEmail(UserProvider userProvider, String phone, String token) {
    
    String phoneRedis = uuidProvider.getVerificationPhone(token);
    try {
      validateByReidsValue(phone, phoneRedis);
      
      User user = getUserByUserProviderAndPhone(userProvider, phone);
      
      validateStatus(user);
      
      return user.getEmail();
    }finally {
      uuidProvider.removeToken(TokenType.VERIFICATION_PHONE, token);
    }
  }
  
  @Transactional
  public String verifyPhoneAndCreateFindPwToken(UserProvider userProvider, String email, String phone, String token) {
    
    String phoneRedis = uuidProvider.getVerificationPhone(token);
    try {
      validateByReidsValue(phone, phoneRedis);
      
      User user = userRepository.findByUserProviderAndEmailAndPhone(userProvider, email, phone)
          .orElseThrow(() -> new NoSuchElementException("일치하는 계정을 찾을 수 없습니다."));
      
      validateStatus(user);
      
      return uuidProvider.createAccessFindPwToken(email);
    }finally {
      uuidProvider.removeToken(TokenType.VERIFICATION_PHONE, token);
    }
  }
  
  @Transactional
  public String verifyEmailAndCreateFindPwToken(UserProvider userProvider, String email, String token) {
    
    String emailRedis = uuidProvider.getVerificationEmail(token);
    try {
      validateByReidsValue(email, emailRedis);
      
      User user = getUser(userProvider, email);
      
      validateStatus(user);
      
      return uuidProvider.createAccessFindPwToken(email);
      
    }finally {
      uuidProvider.removeToken(TokenType.VERIFICATION_EMAIL, token);
    }
  }
  
  // 비밀번호 검증 완료 토큰 반환 (예:회원탈퇴시 필요)
  @Transactional
  public String verifyPasswordAndCreatePasswordToken(UserProvider userProvider, String email, String password) {
    
    User user = getUser(userProvider, email);     
    
    validateStatus(user);
    
    if(!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 틀립니다.");
    }
    
    return uuidProvider.createAccessPaaswordToken(email);
  }
  
  @Transactional
  public UserResponseDto updatePasswordByFindPwToken(UserProvider userProvider, String newPassword, String token, String ip) {
  
    String email = uuidProvider.getAccessFindpwToken(token);
    
    try {
      
      User user = getUser(userProvider, email);
      
      validateStatus(user);
      
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setUserStatus(UserStatus.ACTIVE);
      userRepository.flush();
        
      loginLogService.loginFailLogResolveByUpdatePassword(userProvider, email, ip);
        
      return UserResponseDto.fromEntity(userRepository.getReferenceById(user.getUserSeq())); 
    } finally {
        uuidProvider.removeToken(TokenType.ACCESS_FINDPW , token);
    }
  }

  @Transactional
  public UserResponseDto updatePhoneByVerfication(UserProvider userProvider, String email, String phone, String token) {
    
    String phoneRedis = uuidProvider.getVerificationPhone(token);
    try {
      validateByReidsValue(phone, phoneRedis);
      
      User user = getUser(userProvider, email);
      
      validateStatus(user);
      
      user.setPhone(phone);
      userRepository.flush();
      
      uuidProvider.removeToken(TokenType.VERIFICATION_PHONE, token);
      return UserResponseDto.fromEntity(userRepository.getReferenceById(user.getUserSeq())); 
      
    } finally {
      uuidProvider.removeToken(TokenType.ACCESS_FINDPW , token);
    }
  }

  @Transactional
  public void deleteUser(UserProvider userProvider, String email, String token) {
    String emailRedis = uuidProvider.getAccessPasswordToken(token);
    try {
      validateByReidsValue(email, emailRedis);
      
      User user = getUser(userProvider, email);
      
      validateStatus(user);
      
      user.setUserStatus(UserStatus.DELETE);
      
    } finally {
      uuidProvider.removeToken(TokenType.ACCESS_PASSWORD, token);
    }
  }

  public void isEmailExists(UserProvider userProvider, String email) {
    userRepository.findByUserProviderAndEmail(UserProvider.LOCAL, email)
    .filter(e->e.getUserStatus()!=UserStatus.DELETE)
    .orElseThrow(()->new DuplicationException("해당 이메일의 계정이 이미 존재합니다."));
  }

  public void isPhoneExists(UserProvider userProvider, String phone) {
    userRepository.findByUserProviderAndPhone(UserProvider.LOCAL, phone)
    .filter(e->e.getUserStatus()!=UserStatus.DELETE)
    .orElseThrow(()->new DuplicationException("잘못된 요청입니다. 해당 휴대폰의 계정이 이미 존재합니다."));
  }
  
  private User getUser(UserProvider userProvider, String email) {
    return userRepository.findByUserProviderAndEmail(userProvider, email)
        .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 유저."));
  }
  
  protected User getUserByUserProviderAndPhone(UserProvider userProvider, String phone) {
    return userRepository.findByUserProviderAndPhone(userProvider, phone)
      .orElseThrow(() -> new NoSuchElementException("찾을 수 없는 유저."));
  }
  
  @SuppressWarnings("incomplete-switch")
  private void validateStatus(User user) {
    switch (user.getUserStatus()) {
      case STAY -> throw new StatusStayException("인증이 필요한 계정입니다.");
      case STOP -> throw new StatusStopException("정지된 계정입니다. 고객센터에 문의해주세요.");
      case DELETE -> throw new StatusDeleteException("탈퇴한 계정입니다.");
    }
  }
  
  private void validateByReidsValue(String a, String b) {
    if(!Objects.equals(a,b)) {
      logger.info("입력값과 Redis 값이 다름");
      throw new IllegalArgumentException("잘못된 요청입니다.");
    }
  }
  

//    public boolean isEmailPhoneExists(String email, String phone) {
//        return userRepository.existsByEmailAndPhone(email, phone);
//    }
  

//  @Transactional
//  public UserRequestDto registerUser(UserRequestDto userDto, AddressDto addressDto) {
//
//    // 이메일 중복 확인
//    if (userDao.isEmailExists(userDto.getEmail())) {
//      throw new DuplicateKeyException("회원가입 실패 : 해당 이메일의 유저가 이미 존재합니다.");
//    }
//
//    // 비밀번호 인코딩
//    String encodePassoword = passwordEncoder.encode(userDto.getPassword());
//    
//    // 유저 등록
//    int userSeq = userDao.registerNormalUser(userDto, encodePassoword);
//
//    // 주소 등록
//    addressDto.setUserSeq(userSeq);
//    addressDto.setPrimary(true);
//    addressDao.registerAddressDto(addressDto);
//
//    return userDao.getDtoBySequence(userSeq);
//  }
//  
////  public boolean comparePasswordByUserSeq(int userSeq, String reqPassword) throws SQLException {
////    String password = userDao.getPasswordBySeq(userSeq)
////        .orElseThrow(() -> new IllegalArgumentException("비교 실패 : 등록된 유저 정보가 없습니다."));
////    return passwordEncoder.matches(reqPassword, password);
////  }
//      
//  public UserRequestDto getUserBySeq(int userSeq) {
//    return userDao.getDtoBySequence(userSeq);
//  }
//
//  public Optional<UserRequestDto> getUserByEmail(String email) {
//    return userDao.getUserDtoByEmail(email);
//  }
//  
//  public Optional<String> getEmailByPhone(String phone) {
//    return userDao.getEmailByPhone(phone);
//  }
//
//  public List<UserRequestDto> getAllUsers() throws SQLException{
//    return userDao.getAllDto();
//  }
//    
////    public Status getUserStatusByEmail(String email) throws SQLException, NoSuchElementException{
////      Optional<String> statusOptional = userDao.getUserStatusByEmail(email);
////      if(statusOptional.isEmpty()) {
////        throw new NoSuchElementException("유저 상태 조회 실패 :  일치하는 유저가 없습니다. email = "+email);
////      }
////      try {
////        Status status = Status.valueOf(statusOptional.get());
////        return status;
////      }catch (IllegalArgumentException e) {
////        throw new IllegalStateException("유저 상태 조회 실패 : 데이터 베이스에 저장된 Status 값이 적합하지 않습니다.");
////      }
////    }
//
//  @Transactional
//  public void updatePhone(UserRequestDto userDto, int verificationSeq) {
//    VerificationDto verificationDto = verificationDao.getDtoBySequence(verificationSeq);
//    if(!verificationDto.isVerify()) {
//      throw new BadCredentialsException("휴대폰 인증이 완료되지 않았습니다.");
//    }
//    userDao.updatePhone(userDto.getUserSeq(),verificationDto.getTo());
//  }
//    
//  //기존 비밀번호를 모르는 상태
//  @Transactional
//  public UserRequestDto updatePasswordByEmail(String ip, int verificationSeq, String email, String newPassword) throws HttpSessionRequiredException {
//    VerificationDto verificationDto = verificationDao.getDtoBySequence(verificationSeq);
//    if(!verificationDto.isVerify()) {
//      throw new BadCredentialsException("인증이 완료되지 않았습니다.");
//    }
//    
//    // 유저 조회
//    UserRequestDto userDto = userDao.getUserDtoByEmail(verificationDto.getTo())
//        .or(() -> userDao.getUserDtoByPhone(verificationDto.getTo()))
//        .orElseThrow(() -> new NoSuchElementException("일치하는 유저가 없습니다."));
//    
//    if(userDto.getEmail().equals(email)) {
//      throw new HttpSessionRequiredException("세션 값과 DB값의 비교 이상.");
//    }
//
//    // 비밀번호 업데이트
//    String newEncodePassword = passwordEncoder.encode(newPassword);
//    userDao.updatePassword(userDto.getUserSeq(), newEncodePassword);
//
//    // 계정 상태 업데이트
//    userDao.updateStatus(userDto.getUserSeq(), Status.ACTIVE);
//    
//    loginLogService.loginFailLogResolveByUpdatePassword(Provider.NORMAL, email, ip);
//    
//    return userDao.getDtoBySequence(userDto.getUserSeq());
//  }
//    
//  //로그인 이후 비밀번호 변경
//  @Transactional
//  public UserRequestDto updatePassword(UserRequestDto userDto, String oldPassword, String newPassword) {
//    
//    // 기존 비밀번호 검증 확인
//    String password = userDao.getPasswordByUserSeq(userDto.getUserSeq());
//    if(passwordEncoder.matches(oldPassword, password)) {
//      throw new BadCredentialsException("기존 비밀번호 불일치.");
//    }
//    
//    // 비밀번호 업데이트
//    String newEncodePassword = passwordEncoder.encode(newPassword);
//    userDao.updatePassword(userDto.getUserSeq(), newEncodePassword);
//
//    return userDao.getDtoBySequence(userDto.getUserSeq());
//  }
//    
//
////  @Transactional
////  public UserDto updateAddressSeq(int userSeq, int addressSeq) throws SQLException{
////    
////    userDao.getUserBySeq(userSeq).orElseThrow(()->new IllegalArgumentException("대표 주소 업데이트 실패 : 등록된 유저가 없습니다."));
////    AddressDto addressDto = addressDao.getAddressDtoByAddressSeq(addressSeq).orElseThrow(()->new IllegalArgumentException("대표 주소 업데이트 실패 : 등록된 주소지가 없습니다."));
////    
////    if(userSeq!=addressDto.getUserSeq()) {
////      throw new AccessDeniedException("대표 주소 업데이트 실패 : 등록되어 있는 주소 유저와 인자로 받은 유저가 상이합니다.");
////    }
////    
////    int result = userDao.updateAddressSeq(userSeq, addressSeq);
////    if(result==0) throw new IllegalStateException("대표 주소 업데이트 실패 : 업데이트 결과 이상.");
////    
////    return userDao.getUserBySeq(userSeq).orElseThrow(()->new IllegalStateException("대표 주소 업데이트 실패 : 저장한 유저 정보를 불러올 수 없습니다."));
////  }
//
//  @Transactional
//  public void stopUser(UserRequestDto userDto) throws SQLException {
//    userDao.updateStatus(userDto.getUserSeq(), Status.STOP);
//  }
//  
//  @Transactional
//  public void delteUser(UserRequestDto userDto) throws SQLException {
//    userDao.updateStatus(userDto.getUserSeq(), Status.DELETE);
//  }
//    
//  public boolean isEmailExists(String email) {
//    return userDao.isEmailExists(email);
//  }
//  
//  public boolean isPhoneExists(String phone) {
//    return userDao.isPhoneExists(phone);
//  }
//  
//  public boolean isEmailPhoneExists(String email, String phone){
//    return userDao.isEmailPhoneExists(email, phone);
//  }


}