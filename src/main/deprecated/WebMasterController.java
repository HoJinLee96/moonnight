package controller;

//@Controller
//@RequestMapping("/master")
public class WebMasterController {
//  
//  @Autowired
//  private UserService userService;
//  
//  @GetMapping("/login")
//  public String showMasterLogin(HttpServletRequest req, HttpServletResponse ress) {
//    return "masterLogin";
//  }
//  
//  @GetMapping("/home")
//  public String showMasterHome(HttpServletRequest req, HttpServletResponse ress) {
//    return "masterHome";
//  }
//  
//  @GetMapping("/estimateView")
//  public String showEstimateView(HttpServletRequest req, HttpServletResponse res) {
//    return "masterEstimateView";
//  }
  
//  @PostMapping("/login")
//  public ResponseEntity<?> login(
//      @RequestParam("email") String reqEmail,
//      @RequestParam("password") String reqPassword,
//      HttpSession session, HttpServletRequest req){
//    
//    String ip = HttpUtil.getClientIp(req);
//    
//    try {
//      if (userService.comparePasswordByEmail(reqEmail, reqPassword, ip)) {
//        UserDto userDto = userService.getUserByEmail(reqEmail);
//        if("MASTER".equals(userDto.getStatus().name())) {
//          session.setAttribute("masterDto", userDto);
//          return ResponseEntity.status(HttpStatus.OK).build();
//        }
//      }
//      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    } 
//  }

}
