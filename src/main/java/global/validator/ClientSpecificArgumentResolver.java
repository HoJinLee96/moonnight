package global.validator;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.util.WebUtils; // Cookie 찾는 데 유용
import global.exception.IllegalUuidException;
import global.validator.annotaion.ClientSpecific;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClientSpecificArgumentResolver implements HandlerMethodArgumentResolver {

    // 1. 이 Resolver가 어떤 파라미터를 지원(처리)할 것인가?
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @ClientSpecific 어노테이션이 붙어있는지 확인
        return parameter.hasParameterAnnotation(ClientSpecific.class);
    }

    // 2. 실제 파라미터 값을 어떻게 만들어낼 것인가? (핵심 로직)
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        // @ClientSpecific("여기에_넣은_값") 가져오기
        ClientSpecific clientSpecificAnnotation = parameter.getParameterAnnotation(ClientSpecific.class);
        String requiredValueName = clientSpecificAnnotation.value();

        // HttpServletRequest 객체 가져오기 (헤더, 쿠키 등에 접근하기 위해)
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            log.warn("HttpServletRequest를 가져올 수 없습니다.");
            // 혹은 예외를 던질 수도 있음
            throw new IllegalUuidException("요청 정보를 가져올 수 없습니다.");
        }

        String clientType = request.getHeader("X-Client-Type");
        boolean isMobileApp = clientType != null && clientType.contains("mobile");

        String token = null;
        String clientTypeKr = isMobileApp ? "모바일 앱" : "웹";

        if (isMobileApp) {
            token = request.getHeader(requiredValueName);
            log.debug("[{}] 모바일 헤더 토큰 탐색: {}", requiredValueName, token != null ? "찾음" : "못찾음");
        } else {
            Cookie cookie = WebUtils.getCookie(request, requiredValueName);
            if (cookie != null) {
                token = cookie.getValue();
            }
            log.debug("[{}] 웹 쿠키 토큰 탐색: {}", requiredValueName, token != null ? "찾음" : "못찾음");
        }

        // 토큰 유효성 검사 (null 또는 빈 값/공백 체크)
        if (token == null || token.isBlank()) {
            log.warn("[{}] {} 요청에 필요한 토큰이 없습니다.", requiredValueName, clientTypeKr);
            // Spring에서 파라미터 누락 시 주로 사용하는 예외를 던져주면 400 Bad Request로 처리될 수 있음
            throw new IllegalUuidException(clientTypeKr + " 요청에 필요한 '" + requiredValueName + "' 토큰이 없습니다.");
            // 또는 throw new IllegalArgumentException(...) 등 사용 가능
        }

        log.info("[{}] {} 토큰 '{}' 확인 완료", requiredValueName, clientTypeKr, requiredValueName.toLowerCase().contains("token") ? "****" : token); // 로그에는 토큰값 직접 노출 주의
        return token;
    }

}