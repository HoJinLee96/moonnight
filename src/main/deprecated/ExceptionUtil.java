package deprecated;

import org.springframework.security.access.AccessDeniedException;
@Deprecated
public class ExceptionUtil {
    public static IllegalArgumentException createIllegalArgument(String message, Object... args) {
      return new IllegalArgumentException(String.format(message, args));
  }
  
  public static IllegalStateException createIllegalState(String message, Object... args) {
      return new IllegalStateException(String.format(message, args));
  }
  
  public static AccessDeniedException createAccessDenied(String message, Object... args) {
    return new AccessDeniedException(String.format(message, args));
  }
}
