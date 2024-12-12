<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<meta charset="UTF-8">
<title>로그인</title>
<style type="text/css">
.container {
	max-width: 1200px;
	margin: 0 auto;
	padding-top: 50px;
	min-height: 1080px;
}
</style>
</head>
<body>
<div class ="container">

	<div class ="loginform">
	    <h2 class = "title">로그인</h2>
		<form action="" id="loginForm">
			<div class = "emailDiv">
		        <label for="email">이메일</label>
		        <input type="email" id=email name="email" required autofocus>
			</div>
			<div class = "passwordDiv">
		        <label for="password">비밀번호</label>
		        <input type="password" id="password" name="password" required>
			</div>
	        <button type="submit">로그인</button>
	    </form>
    </div>
    
</div>
</body>
<script type="text/javascript">
$(document).ready(function() {
    $('#loginForm').on('submit', function(event) {
        event.preventDefault();
        var email = $('#email').val();
        var password = $('#password').val();
        
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/master/login', true); // 비동기식 요청
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
        
        var data = 'email=' + encodeURIComponent(email) +
        '&password=' + encodeURIComponent(password);
        
        xhr.onload = function() {
            if (xhr.status === 200) {
            	location.href="/master/home";
            } else if (xhr.status === 401 || xhr.status === 410) {
                alert("일치하는 회원 정보가 없습니다.");
                location.reload();
            } else if (xhr.status === 500) {
                alert("서버 오류가 발생했습니다. \n 다시 시도해주세요.");
                location.reload();
            } else {
                alert("알 수 없는 오류가 발생했습니다.");
                location.reload();
            }
        };
        xhr.send(data);
    });
});    
    	</script>
</html>