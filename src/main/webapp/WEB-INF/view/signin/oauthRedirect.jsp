<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로그인 처리 중...</title>
</head>
<body>
  <p>로그인 처리 중입니다. 잠시만 기다려 주세요...</p>

  <script>
    // URL에서 'token' 파라미터 값 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('token');

    if (accessToken) {
      // localStorage에 Access Token 저장
      localStorage.setItem('accessToken', accessToken);
      console.log('Access Token 저장 완료.');

      // (보안 강화) 브라우저 히스토리에서 토큰 파라미터 제거
      // 현재 페이지 URL에서 쿼리 스트링 제거 (예: /oauth-redirect)
      window.history.replaceState({}, document.title, window.location.pathname);

      // 최종 목적지 페이지로 리다이렉션
      console.log('최종 목적지로 이동합니다...');
      window.location.href = '/home'; // 홈 또는 다른 원하는 경로로 변경 가능
    } else {
      // 토큰이 없는 경우 에러 처리 또는 로그인 페이지로 리다이렉션
      console.error('OAuth Access Token을 URL 파라미터에서 찾을 수 없습니다.');
      alert('로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
      window.location.href = '/signin'; // 로그인 페이지로 이동
    }
  </script>
</body>
</html>