<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>아이디 찾기</title>
<style type="text/css">
@font-face {
	font-family: 'SF_HambakSnow';
	src:
		url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2106@1.1/SF_HambakSnow.woff')
		format('woff');
	font-weight: normal;
	font-style: normal;
}

* {
	font-family: 'SF_HambakSnow', sans-serif;
}
</style>
<style type="text/css">
.container {
	max-width: 1200px;
	margin: 0 auto;
	padding-top: 20px;
	min-height: 700px;
	display: flex;
	justify-content: center; /* 수평 중앙 정렬 */
}
#loginForm label[for="phone"] {
	text-align: left;
	display: block;
	margin: 0px;
	padding: 0px;
	margin-top: 15px; /* input과의 간격을 조정 */
	font-size: 14px;
}

#phone, #verificationSmsCode {
	width: 250px;
	height: 40px;
	border: none;
	border-bottom: 2px solid #ccc;
	outline: none;
	transition: border-bottom-color 0.3s;
	margin-right: 20px;
}

#phone:focus {
	border-bottom: 2px solid #20367a;
}

#input-wrapper {
	position: relative;
	display: none; /* none <-> inline-block */
}

#verificationTimeMessage {
	position: absolute;
	top: 60%;
	right: 24px;
	transform: translateY(-50%);
	font-size: 14px;
	color: red;
	pointer-events: none; /* 타이머가 클릭되지 않도록 설정 */
}

#sendSmsButton, #verifySmsCodeButton {
	border: none;
	background-color: #20367a;
	color: white;
	width: 130px;
	height: 40px;
	margin: 0px 3px;
	border-radius: 8px;
}

#sendSmsButton:hover, #verifySmsCodeButton:hover, #confirmButton:hover {
	border: 1px solid #20367a;
	background-color: white;
	color: #20367a;
	cursor: pointer;
}

#confirmDiv {
	display: none; /* none <-> block */
	text-align: center;
}

#confirmButton {
	border: none;
	background-color: #20367a;
	color: white;
	width: 130px;
	height: 40px;
	border-radius: 8px;
	margin: 0 auto;
}

#confirmEmail {
	padding: 10px 20px;
	background-color: #ededed;
}

/* 닫기 버튼 스타일 */
.close {
	position: absolute;
	top: 10px;
	left: 10px; /* 왼쪽 위에 위치 */
	color: #aaa;
	font-size: 28px;
	font-weight: bold;
	cursor: pointer;
}

.close:hover, .close:focus {
	color: #20367a;
	text-decoration: none;
	cursor: pointer;
}
</style>
</head>

<body>
	<div class="container">
		<div id="formDiv">
			<span class="close" onclick="window.close()">&times;</span>
			<h2>이메일 찾기</h2>
			<br>
			<label for="phone">휴대폰 번호</label>
			<br>
			<input type="text" id="phone" name="phone" required oninput="formatPhoneNumber(this)"maxlength="13" value="010-" >
			<button class="sendSmsButton" id="sendSmsButton" type="button"onclick="sendSms()">인증번호 발송</button>
			<br> 
			<span id="sendSmsMessage"></span> 
			<br><br>
			<div id="input-wrapper">
				<label for="verificationCode">인증번호</label> 
				<br> 
				<input type="text" id="verificationSmsCode" name="verificationSmsCode" required oninput="formatCode(this)" maxlength="5" readonly disabled>
				<div id="verificationTimeMessage"></div>
			</div>
				<input type="hidden" id="smsSeq" value="" />
				<button class="verifySmsCodeButton" id="verifySmsCodeButton" type="button" onclick="verifySmsCode()" disabled style="display:none;">인증번호 확인</button>
				<br> 
				<span id="verificationSmsMessage"></span>
			<br> <br> <br> <br>
			<div id="confirmDiv">
				<span>가입 하신 이메일 </span>
				<br> <br> <br>
				<span id="confirmEmail"></span>
				<br> <br> <br>
				<button id="confirmButton" type="button" onclick="window.close()">확인</button>
			</div>


		</div>
	</div>

</body>

<!-- 휴대폰 번호로 이메일 조회 -->
<script type="text/javascript">
function getEmailByPhone() {
	var confirmEmail = document.getElementById("confirmEmail");
	var phone = document.getElementById("phone").value;
	
	var xhr = new XMLHttpRequest();
	xhr.open('POST', '/user/get/emailByPhone', false); // 동기식 요청으로 변경
	xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
	xhr.send('phone=' + encodeURIComponent(phone));
    if (xhr.status === 200) {
        confirmEmail.textContent = xhr.responseText;
    } else if (xhr.status === 404) {
        confirmEmail.textContent = "해당 번호로 가입된 이메일이 없습니다.";
    } else if (xhr.status === 500) {
        confirmEmail.textContent = "서버 오류가 발생했습니다. 다시 시도해주세요.";
    } else {
        confirmEmail.textContent = "알 수 없는 오류가 발생했습니다.";
    }
}
</script>

<!-- sms 인증 api -->
<script type="text/javascript">
	var timerInterval; // 타이머 인터벌을 저장할 변수
	
	function sendSms() {
		
		var message = document.getElementById("sendSmsMessage");
		var verMessage = document.getElementById("verificationSmsMessage");
		var reqPhone = document.getElementById("phone").value.replace(/[^0-9]/g, '');
		if (reqPhone.length !== 11){
			alert("휴대폰 번호를 확인해 주세요.");
			message.style.color = 'red';
			message.innerText = "휴대폰 번호를 확인해 주세요.";
		} 
		else {
			console.log("sms인증 시작")
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/api/verify/sendsms', false); // 동기식 요청으로 변경
			xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
			xhr.send('reqPhone=' + encodeURIComponent(reqPhone));
			if (xhr.status === 200) {
				alert("인증번호 발송 완료");
				message.style.color = 'green';
				message.innerText = "인증번호 발송 완료";
				verMessage.innerText = "";
				document.getElementById("phone").setAttribute("readonly", true);
				document.getElementById("phone").setAttribute("disabled", true);
				document.getElementById("sendSmsButton").innerText = "인증번호 재발송";
				document.getElementById("input-wrapper").style.display = "inline-block";
				document.getElementById("verifySmsCodeButton").style.display = "";
				document.getElementById("verificationSmsCode").removeAttribute("readonly");
				document.getElementById("verificationSmsCode").removeAttribute("disabled");
				document.getElementById("verifySmsCodeButton").removeAttribute("disabled");
				onVerificationCodeSent();
			} else {
				message.style.color = 'red';
				if (xhr.status === 429) {
					message.innerText = "시도 초과. 잠시 후 다시 시도 해주세요.";
				}else if(xhr.status === 500){
					message.innerText = "서버 오류가 발생했습니다. 다시 시도해주세요.";
				} else {
					message.innerText = "알 수 없는 오류가 발생했습니다. \n 재발송 시도 해주세요.";
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
				document.getElementById("phone").setAttribute("readonly", true);
				document.getElementById("phone").setAttribute("disabled", true);
				document.getElementById("sendSmsButton").setAttribute("disabled", true);
				document.getElementById("verificationSmsCode").setAttribute("readonly", true);
				document.getElementById("verificationSmsCode").setAttribute("disabled", true);
				document.getElementById("verifySmsCodeButton").setAttribute("disabled", true);
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null; // 타이머 초기화
				document.getElementById("confirmDiv").style.display = "block";
	            getEmailByPhone();
				return true;
			} else {
				message.style.color = 'red';
				if (xhr.status === 408) {
					message.innerText = xhr.responseText;
				}else if( xhr.status === 401){
					message.innerText = xhr.responseText;
				}else if( xhr.status === 500){
					message.innerText = xhr.responseText;
				}else {
					message.innerText = "알 수 없는 장애 발생. \n 재발송 시도 해주세요.";
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
	            message.innerText = '인증 시간이 초과되었습니다. \n 재발송 시도 해주세요.' ;
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

<script type="text/javascript">
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

</script>
</html>