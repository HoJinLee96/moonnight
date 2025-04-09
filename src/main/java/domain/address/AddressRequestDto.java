package domain.address;

import domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequestDto(
    
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "{validation.address.name.invalid}")
    @Size(max = 20, message = "{validation.address.name.length}")
    String name,
    
    @NotBlank(message = "{validation.address.postcode.required}")
    @Size(max = 10, message = "{validation.address.invalid}")
    String postcode,
    
    @NotBlank(message = "validation.address.main_address.required")
    @Size(max = 255, message = "{validation.address.invalid}")
    String mainAddress,
    
    @NotBlank(message = "validation.address.detail_address.required")
    @Size(max = 255, message = "{validation.address.invalid}")
    String detailAddress,
    
    boolean isPrimary
) {
  
    public Address toEntity(User user) {
        return Address.builder()
            .user(user)
            .name(name)
            .postcode(postcode)
            .mainAddress(mainAddress)
            .detailAddress(detailAddress)
            .isPrimary(isPrimary)
            .build();
    }
}
