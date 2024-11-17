<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="dto.RegisterUserDto"%>

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
	<%@ include file="main_header.jsp"%>


	<div class="container">

		<form id="detailForm">
			<%
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
    %>
			<h2>2 단계 : 정보 입력</h2>
			<div class="step active" id="step1">
				<div id="subStep1_1">
					<label for="name">이름</label> <input type="text" id="name"
						name="name" required onblur="formatName()">
				</div>
				<div id="subStep1_2">
					<label for="birth">생년월일</label> <input type="number" id="birth"
						name="birth" placeholder="19990101" maxlength="8"
						oninput="if(this.value.length > 8) this.value = this.value.slice(0, 8);"
						onblur="formatBirth()" required>
				</div>
				<span id="nameBirthMessage"></span> <label for="phone">휴대폰</label> <input
					type="text" id="phone" name="phone" required
					oninput="formatPhoneNumber(this)" maxlength="13" value="010-">
				<button class="sendSmsButton" id="sendSmsButton" type="button"
					onclick="sendSms()">인증번호 발송</button>
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

	<%@ include file="main_footer.jsp"%>
</body>

<!-- 주소 검색 api -->
<script
	src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="/static/js/daumAddressSearch4.js"></script>

<!-- sms 인증 api -->
<script type="text/javascript">
	var timerInterval; // 타이머 인터벌을 저장할 변수
	var requestPhone;
	
	function sendSms() {
		var message = document.getElementById("sendSmsMessage");
		var verMessage = document.getElementById("verificationSmsMessage");
		var reqPhone = document.getElementById("phone").value.replace(/[^0-9]/g, '');
		if (formatName()&&formatBirth() && validatePhone()) {
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/api/verify/sendsms', false); // 동기식 요청으로 변경
			xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
			xhr.send('reqPhone=' + encodeURIComponent(reqPhone));
			if (xhr.status === 200) {
				alert("인증번호 발송 완료");
				message.style.color = 'green';
				message.innerText = "인증번호 발송 완료";
				verMessage.innerText = "";
				console.log("휴대폰 인증 코드 발송 성공.(성공)");
				document.getElementById("phone").setAttribute("readonly", true);
				document.getElementById("phone").setAttribute("disabled", true);
				document.getElementById("sendSmsButton").innerText = "인증번호 재발송";
				document.getElementById("verificationSmsCode").removeAttribute("readonly");
				document.getElementById("verificationSmsCode").removeAttribute("disabled");
				document.getElementById("verifySmsCodeButton").removeAttribute("disabled");
				requestPhone = reqPhone;

				onVerificationCodeSent();
			} else {
				message.style.color = 'red';
				if (xhr.status === 429) {
					message.innerText = "잠시 후 다시 시도해주세요.";
				}else if(xhr.status === 500){
					message.innerText = "서버 장애 발생";
				} else {
					message.innerText = "잠시 후 다시 시도해주세요.";
				}
			}
		}
	}

	function verifySmsCode() {
		var reqCode = document.getElementById("verificationSmsCode").value;
		var message = document.getElementById("verificationSmsMessage");
		if (reqCode.length < 5) {
			message.style.color = 'red';
			message.innerText = "인증번호를 다시 확인해주세요.";
		} else {
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/api/verify/comparecode', false); // 동기식 요청으로 변경
			xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
			xhr.send('reqCode=' + encodeURIComponent(reqCode));

			if (xhr.status === 200) {
				message.style.color = 'green';
				message.innerText = "인증 성공";
				console.log("휴대폰 인증 성공.(성공)");
				document.getElementById("phone").setAttribute("readonly", true);
				document.getElementById("phone").setAttribute("disabled", true);
				document.getElementById("sendSmsButton").setAttribute("disabled", true);
				document.getElementById("verificationSmsCode").setAttribute("readonly", true);
				document.getElementById("verificationSmsCode").setAttribute("disabled", true);
				document.getElementById("verifySmsCodeButton").setAttribute("disabled", true);
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null; // 타이머 초기화
				return true;
			} else {
				message.style.color = 'red';
				if (xhr.status === 401) {
					message.innerText = "인증번호를 다시 확인해주세요.";
				}else if(xhr.status === 408 ){
					message.innerText = "잠시 후 다시 시도해주세요."
				}else {
	                message.innerText = "서버 장애 발생.";
				}
				return false;
			}
		}
	}
	
	function onVerificationCodeSent() {
	    // 3분 타이머 시작
	    let timeLeft = 180; // 3분 = 180초
	    const timerElement = document.getElementById('verificationTimeMessage');
	    var message = document.getElementById("verificationSmsMessage");
	    timerElement.textContent = formatTime(timeLeft);

	    timerInterval = setInterval(() => {
	        timeLeft--;
	        timerElement.textContent = formatTime(timeLeft);

	        if (timeLeft <= 0) {
	            message.style.color = 'red';
	            message.innerText = '인증 시간이 초과되었습니다. 재발송 시도 해주세요.' ;
	            timerElement.textContent = '00:00' ;
	            document.getElementById("verificationSmsCode").setAttribute("readonly", true);
	            document.getElementById("verificationSmsCode").setAttribute("disabled", true);
	            document.getElementById("verifySmsCodeButton").setAttribute("disabled", true);
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null; // 타이머 초기화
	        }
	    }, 1000);
	}

    function formatTime(seconds) {
        var minutes = Math.floor(seconds / 60);
        var remainingSeconds = seconds % 60;
        
        function pad(number) {
            return (number < 10 ? '0' : '') + number;
        }

        return pad(minutes) + ':' + pad(remainingSeconds);
    }
</script>

<!--  휴대폰 중복 검사 api -->
<script type="text/javascript">
function validatePhone() {
	var reqPhone = document.getElementById("phone").value;
	var message = document.getElementById("sendSmsMessage");
	console.log(reqPhone);
	if (reqPhone.length === 13) {
		var xhr = new XMLHttpRequest();
		xhr.open('POST', '/user/exist/phone', false); // 동기식 요청으로 변경
		xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
		xhr.send('reqPhone=' + encodeURIComponent(reqPhone));

		if (xhr.status === 404) {
			return true;
		} else {
			message.style.color = 'red';
			if (xhr.status === 200) {
				message.innerText = "이미 가입된 휴대폰 번호 입니다.";
			}else if(xhr.status === 500){
				message.innerText = "서버 장애 발생";
			}else {
				message.innerText = "서버 장애 발생.";
			}
			return false;
		}
	} else {
		message.style.color = 'red';
		message.innerText = "올바른 전화번호 형식을 입력 해주세요.";
		return false;
	}
}
</script>
<!-- 휴대폰 번호 input 방지 -->
<script type="text/javascript">
document.getElementById("phone").addEventListener("input",function(){
	if(requestPhone){
		document.getElementById("phone").value=requestPhone;
	}
});
</script>

<!-- 메인 -->
<script type="text/javascript">
var currentStep = 1;
var totalSteps = 5;

function formatName() {
	var name = document.getElementById("name").value.trim();
	var message = document.getElementById("nameBirthMessage");
	message.style.color = 'red';

	// 정규 표현식: 공백 또는 특수기호
	var regex = /[!@#$%^&*(),.?":{}|<>]/;

	// 1. name 비어있거나 공백이 있거나 특수기호가 들어간 경우 message에 확인 메세지 입력.
	if (name === "") {
		message.innerText = "이름을 공백으로 할 수 없습니다.";
		return false;
	}else if (regex.test(name)){
		message.innerText = "이름에 특수기호를 포함 시킬 수 없습니다.";
		return false;
		}
	else {
		message.innerText = "";
		return true;
	}
}

function formatBirth() {
	var birth = document.getElementById("birth").value.trim();
	var message = document.getElementById("nameBirthMessage");
	message.style.color = 'red';

	// 정규 표현식: 공백 또는 특수기호
	var regex = /[!@#$%^&*(),.?":{}|<>]/;

	if (birth === "" || regex.test(birth) || birth.length !== 8) {
		message.innerText = "올바른 생년월일 형식을 입력 해주세요.";
		return false;
	} else {
		message.innerText = "";
		return true;
	}
}

function formatPhoneNumber(input) {
	let value = input.value.replace(/[^0-9]/g, ''); // 숫자 이외의 문자를 제거합니다.
	let formattedValue = value;

	// 앞 세 자리를 "010"으로 고정합니다.
	if (value.startsWith('010')) {
		value = value.slice(3); // 앞 세 자리("010")를 잘라냅니다.
	}

	if (value.length <= 4) {
		formattedValue = '010-' + value; // 4자리 이하의 숫자만 있을 경우
	} else if (value.length <= 7) {
		formattedValue = '010-' + value.slice(0, 4) + '-' + value.slice(4); // 5~7자리의 경우
	} else {
		formattedValue = '010-' + value.slice(0, 4) + '-'
				+ value.slice(4, 8); // 8자리 이상의 경우
	}

	input.value = formattedValue;
}
function formatCode(input) {
	input.value = input.value.replace(/[^0-9]/g, '');
}

function validateStep(step) {
	var postcode = document.getElementById("postcode").value;
	var addressMessage = document.getElementById("addressMessage");
	var verificationSmsMessage = document.getElementById("verificationSmsMessage");
	var agreeToTerms = document.getElementById("agreeToTerms");
	
	switch (step) {
	case 1:
		return formatName();
	case 2:
		return formatBirth();
	case 3:
		if(!verifySmsCode()){
			verificationSmsMessage.style.color = 'red';
			verificationSmsMessage.innerText = "휴대폰 인증을 진행해 주세요.";
			return false;
		}else{
			verificationSmsMessage.innerText = "";
			return true;
		}
	case 4:
		if(postcode.length===0){
			addressMessage.style.color = 'red';
			addressMessage.innerText="주소를 입력해 주세요."
			return false;
		}
		else{
			addressMessage.innerText=""
			return true;
		}
	case 5:
		if(agreeToTerms.checked===true){
			return true;
		}else{
			return false;
		}
		
	default:
		return false;
	}
}

function validateForm() {
	for (var step = 1; step <= totalSteps; step++) {
		if (!validateStep(step)) {
			alert("모든 입력 조건을 충족해야 합니다.");
			return false;
		}
	}
	return true;
}

// ***** 회원가입 *****
document.getElementById('detailForm').addEventListener('submit', function(event) {
    event.preventDefault();

    if (validateForm()) {
        // User 데이터 수집
        var userDto = {
            email: document.getElementById("email").value,
            password: document.getElementById("password").value,
            name: document.getElementById("name").value,
            birth: document.getElementById("birth").value,
            phone: document.getElementById("phone").value,
            marketingReceivedStatus: document.getElementById("marketingReceivedStatus").checked
        };

        // Address 데이터 수집
        var addressDto = {
            userSeq: null, 
            name: document.getElementById("name").value,
            postcode: document.getElementById("postcode").value,
            mainAddress: document.getElementById("mainAddress").value,
            detailAddress: document.getElementById("detailAddress").value
        };

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/user/join/second', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');

        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) { // 요청 완료
                if (xhr.status === 200) { // 성공
                	alert("회원가입 완료");
                    window.location.href = '/login';
                } else {
                    console.error(xhr.statusText);
                    // 에러 처리
                }
            }
        };

        xhr.onerror = function() {
            console.error('Request failed');
            // 네트워크 오류 또는 기타 문제 처리
        };

        var data = JSON.stringify({
            userDto: userDto,
            addressDto: addressDto
        });

        xhr.send(data);
    }
});
</script>

</html>