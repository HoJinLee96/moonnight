package domain.address;

import java.nio.file.AccessDeniedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import auth.sign.token.CustomUserDetails;
import global.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {

  private final AddressService addressService;

  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PostMapping("/private/register")
  public ResponseEntity<ApiResponse<AddressResponseDto>> registerAddress(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AddressRequestDto addressDto){
    AddressResponseDto addressResponseDto = addressService.registerAddress(userDetails.getUserProvider(),userDetails.getEmail(),addressDto);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 등록 성공.",addressResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @GetMapping("/private/{addressSeq}")
  public ResponseEntity<ApiResponse<AddressResponseDto>> getAddress(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable int addressSeq) throws AccessDeniedException {
    AddressResponseDto addressResponseDto = addressService.getAddress(userDetails.getUserProvider(),userDetails.getEmail(),addressSeq);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 조회 성공.",addressResponseDto));
  }
  
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @GetMapping("/private/getList")
  public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAddressList(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    
    List<AddressResponseDto> list = 
        addressService.getAddressList(
            userDetails.getUserProvider(), 
            userDetails.getEmail());
    
    return ResponseEntity.ok(ApiResponse.of(200, "주소 조회 성공.",list));
  }
  
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PatchMapping("/private/{addressId}")
  public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @Valid @PathVariable int addressId,
      @Valid @RequestBody AddressRequestDto addressRequestDto) throws AccessDeniedException {
    AddressResponseDto addressResponseDto =addressService.updateAddress(userDetail.getUserProvider(),userDetail.getEmail(), addressId, addressRequestDto);
    return ResponseEntity.ok(ApiResponse.of(200, "주소 업데이트 성공.",addressResponseDto));  
  }
  
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @PatchMapping("/private/primary/{addressId}")
  public ResponseEntity<ApiResponse<Void>> updatePrimary(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @PathVariable int addressId) throws AccessDeniedException {
     
    addressService.updatePrimary(
        userDetail.getUserProvider(), 
        userDetail.getEmail(), 
        addressId);
    
    return ResponseEntity.ok(ApiResponse.of(200, "대표 주소 변경 성공",  null));
  }
  
  @PreAuthorize("hasRole('OAUTH') or hasRole('LOCAL')")
  @DeleteMapping("/private/{addressSeq}")
  public ResponseEntity<ApiResponse<AddressRequestDto>> deleteAddress(
      @AuthenticationPrincipal CustomUserDetails userDetail,
      @Valid @PathVariable int addressSeq) throws AccessDeniedException {
    addressService.deleteAddress(userDetail.getUserProvider(),userDetail.getEmail(),addressSeq);
    return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "주소 삭제 성공.", null));  
  }
  
}
