package api;

import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.AddressDto;
import exception.NotFoundException;
import jakarta.servlet.http.HttpSession;
import service.AddressService;

@RestController
@RequestMapping("/address")
public class AddressController {

  @Autowired
  AddressService addressService;
  
  @PostMapping("/register")
  public ResponseEntity<?> registerAddress(@RequestBody AddressDto addressDto,HttpSession session) {
    String postcode = addressDto.getPostcode();
    String mainAddress = addressDto.getMainAddress();
    try {
      if(postcode.equals("")|| mainAddress.equals("") ||!addressService.validateAddress(postcode, mainAddress)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
      addressService.registerAddress(addressDto);
      List<AddressDto> list = (List)session.getAttribute("addressList");
      list.add(addressDto);
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      session.setAttribute("addressList", list);
      session.setAttribute("addressListJson", mapper.writeValueAsString(list));
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/get")
  public ResponseEntity<AddressDto> getAddress(int addressSeq) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json; charset=UTF-8");
    
    try {
      AddressDto addressDto = addressService.getAddressDtoByAddressSeq(addressSeq);
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(addressDto);
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/getList")
  public ResponseEntity<List<AddressDto>> getListAddress(int userSeq) {
    
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json; charset=UTF-8");
    
    try {
      List<AddressDto> list = addressService.getListByUserSeq(userSeq);
      
      return ResponseEntity.status(HttpStatus.OK).headers(headers).body(list);
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  @PostMapping("/update")
  public ResponseEntity<?> updateAddress(@RequestBody AddressDto addressDto,HttpSession session) {
    
    try {
      String postcode = addressDto.getPostcode()+"";
      String mainAddress = addressDto.getMainAddress();
      
      if(postcode.equals("0")|| mainAddress.equals("") ||!addressService.validateAddress(postcode, mainAddress))
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
          
      addressService.updateAddress(addressDto);
      List<AddressDto> list = (List)session.getAttribute("addressList");
      for(int i = 0 ; i<list.size() ; i++) {
        AddressDto l = list.get(i);
        if(l.getAddressSeq()==addressDto.getAddressSeq()) {
          list.add(i, addressDto);
          list.remove(l);
        }
      }
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      session.setAttribute("addressList", list);
      session.setAttribute("addressListJson", mapper.writeValueAsString(list));
      
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (NotFoundException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
  }
  
  @PostMapping("/delete")
  public ResponseEntity<?> deleteAddress(@RequestBody int addressSeq, HttpSession session) {
    System.out.println("deleteAddress()");
    try {
      addressService.deleteAddress(addressSeq);
      List<AddressDto> list = (List)session.getAttribute("addressList");
      for(int i = 0 ; i<list.size() ; i++) {
        AddressDto l = list.get(i);
        if(l.getAddressSeq()==addressSeq) {
          list.remove(l);
        }
      }
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      session.setAttribute("addressList", list);
      session.setAttribute("addressListJson", mapper.writeValueAsString(list));
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (SQLException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }catch (JsonProcessingException e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
  
  
}
