package dto;

public class RegisterUserDto {
  private String email;
  private String password;


  public RegisterUserDto() {
  }

  public RegisterUserDto(String email, String password) {
    super();
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  
}
