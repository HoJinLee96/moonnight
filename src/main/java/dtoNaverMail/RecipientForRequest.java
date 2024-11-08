package dtoNaverMail;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipientForRequest {
  private String address = null;
  private String name = null;
  private String type = "R";
  private Object parameters = null;
  
  public RecipientForRequest() {
  }

  public RecipientForRequest(String address, String name, String type, Object parameters) {
    super();
    this.address = address;
    this.name = name;
    this.type = type;
    this.parameters = parameters;
  }

  protected RecipientForRequest(String address, String name) {
    this.address = address;
    this.name = name;
  }

  public static RecipientForRequest of(String address, String name) {
    return new RecipientForRequest(address, name);
  }

  public String getAddress() {
    return address;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Object getParameters() {
    return parameters;
  }
  
  

}
