package dtoNaverLogin;

public class Callback {
  private String code;
  private String state;
  private String error;
  private String error_description;
  public Callback(String code, String state, String error, String error_description) {
    super();
    this.code = code;
    this.state = state;
    this.error = error;
    this.error_description = error_description;
  }
  public Callback() {
    super();
    // TODO Auto-generated constructor stub
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  public String getError() {
    return error;
  }
  public void setError(String error) {
    this.error = error;
  }
  public String getError_description() {
    return error_description;
  }
  public void setError_description(String error_description) {
    this.error_description = error_description;
  }
  
  
}
