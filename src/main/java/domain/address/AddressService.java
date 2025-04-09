package domain.address;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import auth.crypto.Obfuscator;
import domain.user.User;
import domain.user.User.UserProvider;
import domain.user.UserService;
import infra.kakao.DaumMapClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
  private final AddressRepository addressRepository;
  private final UserService userService;
  private final DaumMapClient daumMapClient;
  private final Obfuscator obfuscator;
  private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
  
  @Transactional
  public AddressResponseDto registerAddress(UserProvider userProvider, String email, AddressRequestDto addressRequestDto) {
    
    validateAddressWithDaumClient(addressRequestDto);
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    Address address = addressRequestDto.toEntity(user);
    
    return AddressResponseDto.fromEntity(addressRepository.save(address),obfuscator);
  }
  
  public AddressResponseDto getAddress(UserProvider userProvider, String email, int addressId) throws AccessDeniedException {
    
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    
    Address address = addressRepository.findById(obfuscator.decode(addressId))
        .orElseThrow(() -> new NoSuchElementException("일치하는 주소가 없습니다. addressId : " + addressId));
    
    if(address.getUser().getUserSeq()!=user.getUserSeq()) {
      throw new AccessDeniedException("주소를 조회할 권한이 없습니다.");
    }
    return AddressResponseDto.fromEntity(address, obfuscator);
  }
  
  public List<AddressResponseDto> getAddressList(UserProvider userProvider, String email) {
    
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    
    List<Address> list = addressRepository.findByUserOrderByPrimaryAndDate(user.getUserSeq());
    
    return list.stream()
        .filter(e->e.getUser().getUserSeq()==user.getUserSeq())
        .map(e->AddressResponseDto.fromEntity(e, obfuscator))
        .collect(Collectors.toList());
  }
  
  @Transactional
  public AddressResponseDto updateAddress(UserProvider userProvider, String email, int addressId, AddressRequestDto addressRequestDto) throws AccessDeniedException {
    
    validateAddressWithDaumClient(addressRequestDto);
    
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    
    Address address = getAuthorizedAddress(user.getUserSeq(), obfuscator.decode(addressId));
    address.update(addressRequestDto);
    addressRepository.flush();
    
    Address updatedAddress = addressRepository.getReferenceById(address.getAddressSeq());
    
    return AddressResponseDto.fromEntity(updatedAddress,obfuscator);
  }
  
  @Transactional
  public List<AddressResponseDto> updatePrimary(UserProvider userProvider, String email, int addressId) throws AccessDeniedException {
    
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    Address address = getAuthorizedAddress(user.getUserSeq(), obfuscator.decode(addressId));
    
    addressRepository.updatePrimaryAddress(address.getAddressSeq(), user.getUserSeq());
    addressRepository.flush();
    
    return addressRepository.findByUserOrderByPrimaryAndDate(user.getUserSeq())
        .stream().map(e->AddressResponseDto.fromEntity(e, obfuscator)).collect(Collectors.toList());
  }
  
  @Transactional
  public void deleteAddress(UserProvider userProvider, String email, int addressId) throws AccessDeniedException {
    User user = userService.getUserByUserProviderAndEmail(userProvider, email);
    Address address = getAuthorizedAddress(user.getUserSeq(), obfuscator.decode(addressId));
    
    if (address.isPrimary()) {
      throw new IllegalArgumentException("대표 주소는 삭제할 수 없습니다. 먼저 다른 주소를 대표 주소로 변경하세요.");
    }
    
    addressRepository.delete(address);
  }
  

  private void validateAddressWithDaumClient(AddressRequestDto addressRequestDto) {
    if (!daumMapClient.validateAddress(addressRequestDto.postcode(),
        addressRequestDto.mainAddress())) {
      throw new IllegalArgumentException("주소를 다시 확인해 주세요.");
    }
  }

  private Address getAuthorizedAddress(int userSeq, int addressId) throws AccessDeniedException {
    return addressRepository.findById(addressId)
        .filter(a -> a.getUser().getUserSeq() == userSeq)
        .orElseThrow(() -> {
          logger.info("주소를 수정할 권한이 없습니다. userSeq: {}, addressId: {}", userSeq, addressId);
          return new AccessDeniedException("주소를 수정할 권한이 없습니다.");
              });
  }
}
    
//  @Transactional
//  public AddressDto registerAddress(int userSeq,AddressDto addressDto) throws SQLException, IllegalArgumentException, IllegalStateException, EmptyResultDataAccessException{
//    addressDto.setUserSeq(userSeq);
//    validateAddress(addressDto.getPostcode(), addressDto.getMainAddress());
//    int addressSeq = addressDao.registerAddressDto(addressDto);
//    return addressDao.getAddressDtoByAddressSeq(addressSeq);
//  }
//   
//  public AddressDto getAddressDtoByAddressSeq(int addressSeq) throws SQLException {
//    return addressDao.getAddressDtoByAddressSeq(addressSeq);
//  }
//   
//  //첫번째 대표주소, updated_at 또는 created_at 기준 최신 순으로 정렬
//  public List<AddressDto> getSortedAddressListByUserSeq(UserRequestDto userDto) throws SQLException {
//   if (!userDao.isActive(userDto.getSequence())) {
//     throw new AccessDeniedException(
//         "조회 불가 : 조회할 수 없는 유저.(UserDto.getSequence : " + userDto.getSequence() + ")");
//   }
//   return addressDao.getAddressListByUserSeq(userDto.getSequence());
//  }
//  
//  @Transactional
//  public AddressDto updateAddress(int userSeq, AddressDto addressDto) throws SQLException, IllegalArgumentException, IllegalStateException, EmptyResultDataAccessException, AccessDeniedException{
//    AddressDto oldAddress = addressDao.getAddressDtoByAddressSeq(addressDto.getAddressSeq());
//    if(userSeq!=oldAddress.getUserSeq()) {
//      throw new AccessDeniedException("수정 불가 : 권한 요청이 부적합 합니다.(userSeq : " + userSeq + ", AddressDto.userSeq : " + addressDto.getUserSeq() + ", addressSeq : " + addressDto.getAddressSeq());
//    }
//    validateAddress(addressDto.getPostcode(), addressDto.getMainAddress());
//    addressDao.updateAddress(addressDto);
//    return addressDao.getAddressDtoByAddressSeq(addressDto.getAddressSeq());
//  }
//  
//  @Transactional
//  public List<AddressDto> updatePrimaryAddress(int userSeq, int addressSeq) throws SQLException{
//    addressDao.updatePrimaryAddress(userSeq, addressSeq);
//    return addressDao.getAddressListByUserSeq(userSeq);
//  }
//  
//  @Transactional
//  public AddressDto deleteAddressDto(int userSeq, int addressSeq) throws SQLException{
//    AddressDto addressDto = addressDao.getAddressDtoByAddressSeq(addressSeq);
//    if(userSeq!=addressDto.getUserSeq()) {
//      throw new AccessDeniedException("수정 불가 : 권한 요청이 부적합 합니다.(userSeq : " + userSeq + ", AddressDto.userSeq : " + addressDto.getUserSeq() + ", addressSeq : " + addressDto.getAddressSeq());
//    }
//    addressDao.deleteAddressDto(addressSeq);
//    return addressDao.getAddressDtoByAddressSeq(addressSeq);
//  }
