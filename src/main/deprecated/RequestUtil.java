package deprecated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Deprecated
public class RequestUtil {

  private static final String IP_REGEX = 
      "^(?:(?:25[0-5]|2[0-4][0-9]|1?[0-9][0-9]?)\\." +
      "(?:25[0-5]|2[0-4][0-9]|1?[0-9][0-9]?)\\." +
      "(?:25[0-5]|2[0-4][0-9]|1?[0-9][0-9]?)\\." +
      "(?:25[0-5]|2[0-4][0-9]|1?[0-9][0-9]?))" + 
      "|" + 
      "([a-fA-F0-9:]+:+)+[a-fA-F0-9]+" +  
      "$";

  public static Optional<String> getString(Map<String, Object> requestBody, String key) {
    Object value = requestBody.get(key);
    if (value instanceof String) {
      return Optional.of((String) value);
    }
    return Optional.empty();
  }

  public static Optional<Integer> getInteger(Map<String, Object> requestBody, String key) {
    Object value = requestBody.get(key);
    if (value == null) {
      return Optional.empty();
    }
    if (value instanceof Integer) {
      return Optional.of((Integer) value);
    }

    if (value instanceof String) {
      try {
        return Optional.of(Integer.parseInt((String) value));
      } catch (NumberFormatException ignored) {
      }
    }

    return Optional.empty();
  }
  
  public static Optional<String> getString(HttpSession session, String key) {
    Object value = session.getAttribute(key);
    if (value instanceof String) {
      return Optional.of((String) value);
    }
    return Optional.empty();
  } 
  
  public static Optional<Integer> getInteger(HttpSession session, String key) {
    Object value = session.getAttribute(key);
    if (value == null) {
      return Optional.empty();
    }
    if (value instanceof Integer) {
      return Optional.of((Integer) value);
    }

    if (value instanceof String) {
      try {
        return Optional.of(Integer.parseInt((String) value));
      } catch (NumberFormatException ignored) {
      }
    }
    return Optional.empty();
  }
  public static <T> Optional<T> get(HttpSession session, String key, Class<T> type) {
    Object value = session.getAttribute(key);
    if (value == null) {
        return Optional.empty();
    }
    if (type.isInstance(value)) {
        return Optional.of(type.cast(value));
    }
    return Optional.empty();
  } 
  
  public static String getIp(HttpServletRequest request) {
    List<String> headers = List.of(
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    );

    for (String header : headers) {
        String ip = request.getHeader(header);
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim();
            if (Pattern.matches(IP_REGEX, ip)) {
                return ip;
            }
        }
    }

    String ip = request.getRemoteAddr();
    if (Pattern.matches(IP_REGEX, ip)) {
        return ip;
    }

    throw new IllegalArgumentException("데이터 형식 부적합 [IP] ip : " + ip);
}
  
}
