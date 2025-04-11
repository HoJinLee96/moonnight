<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="domain.estimate.EstimateRequestDto"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>회원가입 정보 입력</title>
<style>
/* Hide the spinner (arrow buttons) */
input[type="number"]::-webkit-outer-spin-button, input[type="number"]::-webkit-inner-spin-button
	{
	-webkit-appearance: none;
	margin: 0;
}

input[type="number"] {
	-moz-appearance: textfield;
	appearance: textfield;
}

.container {
	max-width: 1200px;
	margin: 0 auto;
	padding-top: 70px;
	min-height: 1600px;
}

/* 폼 스타일 */
#detailForm {
	max-width: 400px;
	margin: 0 auto;
	padding: 60px 200px;
	border: 1px solid #efefef;
	border-radius: 10px;
}

#detailForm label:not([for="marketingReceivedStatus"]):not([for="agreeToTerms"])
	{
	text-align: left;
	display: block;
	margin: 0px;
	padding: 0px;
	margin-top: 10px; /* input과의 간격을 조정 */
	font-size: 15px;
	font-weight: bold;
}

#verificationSmsCode, #phone {
	width: 192px !important;
}

#postcode {
	width: 108px !important;
}

#name, #birth {
	width: 170px !important;
}

#subStep1_1 {
	margin-right: 50px;
}

#subStep1_1, #subStep1_2 {
	display: inline-block;
	width: 170px !important;
}

#subStep1_1 label, #subStep1_2 label {
	width: 150px !important;
	margin-top: 0px !important;
}

#detailForm input:not([type="checkbox"]) {
	width: 400px;
	height: 40px;
	border: none;
	border-bottom: 2px solid #ccc;
	outline: none;
	transition: border-bottom-color 0.3s;
	margin-bottom: 10px;
}

#detailForm input:focus {
	border-bottom: 2px solid black;
}

#detailForm button:not(#postcode):not(#mainAddress) {
	border: none;
	background-color: #20367a;
	color: white;
	width: 190px;
	height: 30px;
	margin: 0px 3px;
	border-radius: 8px;
}

#detailForm button:hover:not(#postcode):not(#mainAddress) {
	border: 1px solid #20367a;
	background-color: white;
	color: black;
	cursor: pointer;
}

#buttonContainer {
	text-align: center;
	margin-top: 40px;
}

#detailForm span {
	display: block; /* block 요소처럼 동작하도록 설정 */
	height: 14px;
	margin: 0px;
	font-size: 14px;
}

#detailForm h2 {
	margin-top: 0px;
	margin-bottom: 50px;
}

#submitButton {
	height: 40px !important;
	width: 150px !important;
	cursor: pointer;
}

#marketingReceivedStatus, #agreeToTerms {
	height: 20px;
	width: 20px;
}

#input-wrapper {
	position: relative;
	display: inline-block;
}

#verificationTimeMessage {
	position: absolute;
	top: 40%;
	right: 10px;
	transform: translateY(-50%);
	font-size: 12px;
	color: red;
	pointer-events: none; /* 타이머가 클릭되지 않도록 설정 */
}

#marketingReceivedStatus, #agreeToTerms {
	position: relative;
	top: 4px;
}
</style>
</head>
<body>
	<%@ include file="/WEB-INF/view/main/main_header.jsp"%>


	<div class="container">

	<form id="detailForm">
<%-- 			<%
        // 세션에서 userDto 가져오기
        RegisterUserDto registerUserDto = (RegisterUserDto) session.getAttribute("registerUserDto");
        if (registerUserDto != null) {
    %>
			<input type="hidden" id="email"
				value="<%= registerUserDto.getEmail() %>"> <input
				type="hidden" id="password"
				value="<%= registerUserDto.getPassword() %>">
			<script type="text/javascript">
        document.addEventListener("DOMContentLoaded", function() {
            var userEmail = document.getElementById("email").value;
            var userPasswrod = document.getElementById("password").value;
        });
    </script>
			<%
        } else {
    %>
			<script type="text/javascript">
        console.log("정상적이지 않은 접근.");
        window.location.href = "/join";
    </script>
			<%
        }
    %> --%>
			<h2>2 단계 : 정보 입력</h2>
			<div class="step active" id="step1">
				<div id="subStep1_1">
					<label for="name">이름</label>
					<input type="text" id="name"name="name" maxlength="20" required onblur="formatName()">
				</div>
				<div id="subStep1_2">
					<label for="birth">생년월일</label> <input type="number" id="birth"
						name="birth" placeholder="19990101" maxlength="8"
						oninput="if(this.value.length > 8) this.value = this.value.slice(0, 8);"
						onblur="formatBirth()" required>
				</div>
				<span id="nameBirthMessage"></span>
				<label for="phone">휴대폰</label>
				<input type="text" id="phone" name="phone" required oninput="formatPhoneNumber(this)" maxlength="13" value="010-">
				<button class="sendSmsButton" id="sendSmsButton" type="button" onclick="sendSms()">인증번호 발송</button>
				
				<span id="sendSmsMessage"></span> <label for="verificationCode">인증번호</label>
				<div id="input-wrapper">
					<input type="text" id="verificationSmsCode"
						name="verificationSmsCode" required oninput="formatCode(this)"
						maxlength="5" readonly disabled>
					<div id="verificationTimeMessage"></div>
				</div>
				<input type="hidden" id="smsSeq" value="" />
				<button class="verifySmsCodeButton" id="verifySmsCodeButton"
					type="button" onclick="verifySmsCode()" disabled>인증번호 확인</button>
				<span id="verificationSmsMessage"></span> <label for="mainAddress">주소</label>
				<input type="text" id="postcode" name="postcode"
					onclick="searchAddress()" placeholder="우편번호"> <input
					type="text" id="mainAddress" name="mainAddress"
					onclick="searchAddress()" placeholder="주소"> <input
					type="text" id="detailAddress" name="detailAddress"
					autocomplete="off" required placeholder="상세주소"> <span
					id="addressMessage"></span> <input type="checkbox"
					id="marketingReceivedStatus" name="marketingReceivedStatus">
				<label for="marketingReceivedStatus">마케팅 정보 사용 동의 (선택)</label> <br>
				<input type="checkbox" id="agreeToTerms" name="agreeToTerms"
					required> <label for="agreeToTerms">개인정보 저장 동의 (필수)</label>
			</div>
			<div id="buttonContainer">
				<button type="submit" id="submitButton">저장하기</button>
			</div>
		</form>

	</div>

	<%@ include file="/WEB-INF/view/main/main_footer.jsp"%>
</body>

<!-- 주소 검색 api -->
<script
	src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="/static/js/daumAddressSearch4.js"></script>



</html>