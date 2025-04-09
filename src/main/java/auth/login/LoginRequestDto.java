package auth.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
    
    @NotBlank(message = "{validation.user.email.required}")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "{validation.user.email.invalid}")
    @Size(min = 5, max = 50, message = "{validation.user.email.length}")
    String email,
    
    @NotBlank(message = "{validation.user.password.required}")
    @Pattern( regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W_])$", message = "{validation.user.password.invalid}")
    @Size(min = 8, max = 50, message = "{validation.user.password.length}")
    String password
    
    ) {

}
