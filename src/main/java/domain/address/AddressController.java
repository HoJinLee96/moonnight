package domain.address;

import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import auth.sign.token.CustomUserDetails;
import global.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/private/address")
public class AddressController {

  private final AddressService addressService;

  @PreAuthorize("hasRole('OAUTH')")
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AddressResponseDto>> registerAddress(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AddressRequestDto addressDto){
    AddressResponseDto addressResponseDto = addressService.registerAddress(userDetails.getUserProvider(),userDetails.getEmail(),addressDto);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 등록 성공.",addressResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/{addressSeq}")
  public ResponseEntity<ApiResponse<AddressResponseDto>> getAddress(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam int addressSeq) throws AccessDeniedException {
    AddressResponseDto addressResponseDto = addressService.getAddress(userDetails.getUserProvider(),userDetails.getEmail(),addressSeq);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 조회 성공.",addressResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @GetMapping("/getList")
  public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAddressList(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<AddressResponseDto> list = 
        addressService.getAddressList(
            userDetails.getUserProvider(), 
            userDetails.getEmail());
    
    return ResponseEntity.ok(ApiResponse.of(200, "주소 조회 성공.",list));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @PutMapping("/{addressSeq}")
  public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @Valid @PathVariable int addressSeq,
      @Valid @RequestBody AddressRequestDto addressRequestDto) throws AccessDeniedException {
    AddressResponseDto addressResponseDto =addressService.updateAddress(userDetail.getUserProvider(),userDetail.getEmail(), addressSeq, addressRequestDto);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 업데이트 성공.",addressResponseDto));  
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @PutMapping("/primary/{addressSeq}")
  public ResponseEntity<ApiResponse<List<AddressResponseDto>>> updatePrimary(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @RequestParam int addressSeq) throws AccessDeniedException {
     
    List<AddressResponseDto> list = addressService.updatePrimary(
        userDetail.getUserProvider(), 
        userDetail.getEmail(), 
        addressSeq);
    
    return ResponseEntity.ok(ApiResponse.of(200, "대표 주소 변경 성공",  list));
  }
  
  @PreAuthorize("hasRole('OAUTH')")
  @DeleteMapping("/{addressSeq}")
  public ResponseEntity<ApiResponse<AddressRequestDto>> deleteAddress(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @Valid @PathVariable int addressSeq) throws AccessDeniedException {
    addressService.deleteAddress(userDetail.getUserProvider(),userDetail.getEmail(),addressSeq);
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "주소 삭제 성공.", null));  
  }
  
  
}
