<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>계정 생성</title>
<style>
.container {
	max-width: 1200px;
	margin: 0 auto;
	padding-top: 70px;
	min-height: 1600px;
}

/* 폼 스타일 */
#registrationForm {
	max-width: 400px;
	min-width: 440px;
	margin: 0 auto;
	padding: 60px 200px;
	border: 1px solid #efefef;
	border-radius: 10px;
}

#registrationForm label {
	text-align: left;
	display: block;
	margin: 0px;
	padding: 0px;
	margin-top: 10px; /* input과의 간격을 조정 */
	font-size: 15px;
	font-weight: bold;
}

#verificationMailCode,#userEmail {
	width: 192px !important;
}

#postcode {
	width: 108px !important;
}

#registrationForm input {
	width: 400px;
	height: 40px;
	border: none;
	border-bottom: 2px solid #ccc;
	outline: none;
	transition: border-bottom-color 0.3s;
	margin-bottom: 10px;
}

#registrationForm input:focus {
	border-bottom: 2px solid #20367a;
}

#submitButton{
height: 40px  !important;
width: 150px !important;
cursor: pointer;
}
#registrationForm button {
	border: none;
	background-color: #20367a;
	color: white;
	width: 190px;
	height: 30px;
	margin: 0px 3px;
	border-radius: 8px;
}

#registrationForm button:hover {
	border: 1px solid #efefef;
	background-color: white;
	color: #20367a;
	cursor: pointer;
}

#buttonContainer {
	margin-top: 30px;
	text-align: center;
}
#registrationForm span {
	display: inline-block; /* block 요소처럼 동작하도록 설정 */
	height: 14px;
	margin: 0px;
	font-size: 14px;
}
#registrationForm h2{
margin-top:0px;
margin-bottom: 50px;
}
 .step {
    display: none;
}

.step.active {
    display: block;
} 
#input-wrapper {
            position: relative;
            display: inline-block;
        }
#verificationTimeMessage{
            position: absolute;
            top: 40%;
            right: 10px;
            transform: translateY(-50%);
            font-size: 12px;
            color: red;
            pointer-events: none; /* 타이머가 클릭되지 않도록 설정 */}
</style>
</head>
<body>
	<%@ include file="/WEB-INF/view/main/main_header.jsp"%>

	<div class="container">
		<form id="registrationForm">
			<h2>1 단계 : 계정 생성</h2>
			<div class="step active" id="step1">
				<label for="userEmail">이메일</label>
				<input type="email" id="userEmail" name="userEmail" required oninput="formatEmail()" placeholder="example@example.com">
				<button type="button" class="sendMailButtons" id="sendMailButton" onclick="nextStep()">인증번호 발송</button>
				<span id="emailMessage"></span>
			</div>
			<div class="step" id="step2">
				<label for="emailVerificationCode">이메일 인증번호</label>
				<div id="input-wrapper">
				<input type="text" id="verificationMailCode" name="verificationMailCode" required oninput="formatCode(this)" maxlength="5" readonly disabled>
				<div id ="verificationTimeMessage">03:00</div>
				</div>
				<input type="hidden" id="mailSeq" value="" />
				<button type="button" class="verifyMailCodeButton" id="verifyMailCodeButton" onclick="nextStep()" disabled>인증번호 확인</button>
				<span id="verificationMailMessage"></span>
			</div>
			<div class="step" id="step3">
				<label for="userPassword">비밀번호</label>
				<input type="password" id="userPassword" name="userPassword" required oninput="formatPasswords();"> <span id="passwordMessage"></span>
				<label for="userConfirmPassword">비밀번호 확인</label>
				<input type="password" id="userConfirmPassword" name="userConfirmPassword" required oninput="validateConfirmPasswords()">
				<span id="passwordConfirmMessage"></span>
				<div id="buttonContainer">
				<button type="submit" id="submitButton">가입하기</button>
				</div>
			</div>
			
		</form>
	</div>
	<%@ include file="/WEB-INF/view/main/main_footer.jsp"%>
</body>

<!-- 이메일 인증 api -->
<script type="text/javascript">
	var timerInterval; // 타이머 인터벌을 저장할 변수
	var requestEmail;
	
	function sendMail() {
		var message = document.getElementById("emailMessage");
		var verMessage = document.getElementById("verificationMailMessage");
		var reqEmail = document.getElementById("userEmail").value;

		if (validateEmail()) {
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/mail/send/verify', false); // 동기식 요청으로 변경
			xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
	        var data = 'reqEmail=' + encodeURIComponent(reqEmail);
	        xhr.send(data);
			
			if (xhr.status === 200) {
				alert("인증번호 발송 완료");
				message.style.color = 'green';
				message.innerText = "인증번호 발송 완료";
				verMessage.innerText = "";
				console.log("인증번호 발송 완료");
				document.getElementById("userEmail").setAttribute("readonly",true);
				document.getElementById("userEmail").setAttribute("disabled",true);
				document.getElementById("sendMailButton").innerText = "인증번호 재발송";
				document.getElementById("sendMailButton").setAttribute("onclick", "sendMail()");
				document.getElementById("verificationMailCode").removeAttribute("readonly");
				document.getElementById("verificationMailCode").removeAttribute("disabled");
				document.getElementById("verifyMailCodeButton").removeAttribute("disabled");
				requestEmail = reqEmail;
				onVerificationCodeSent();
				return true;
			} else {
				message.style.color = 'red';
				if (xhr.status === 429) {
					message.innerText = "잠시 후 시도해 주세요.";
				}else if(xhr.status === 500){
					message.innerText = "서버 장애 발생";
				} else {
					message.innerText = "서버 장애 발생.";
				}
				return false;
			}
		}
	}
	
	function verifyMailCode() {
	    var reqCode = document.getElementById("verificationMailCode").value;
	    var message = document.getElementById("verificationMailMessage");

	    if (mailSeq===0) {
	        message.style.color = 'red';
	        message.innerText = "인증번호 발송을 해주세요.";
	    }else if(reqCode.length<5){
	        message.style.color = 'red';
	        message.innerText = "인증번호를 다시 확인해주세요.";
	    }else {
	        var xhr = new XMLHttpRequest();
	        xhr.open('POST', '/verify/comparecode', false); // 동기식 요청으로 변경
	        xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
	        xhr.send('reqCode=' + encodeURIComponent(reqCode));

	        if (xhr.status === 200) {
	            message.style.color = 'green';
	            message.innerText = "인증 성공";
	            document.getElementById("userEmail").setAttribute("readonly",true);
	            document.getElementById("userEmail").setAttribute("disabled",true);
	            document.getElementById("sendMailButton").setAttribute("disabled", true);
	            document.getElementById("verificationMailCode").setAttribute("readonly", true);
	            document.getElementById("verificationMailCode").setAttribute("disabled", true);
	            document.getElementById("verifyMailCodeButton").setAttribute("disabled", true);
	            console.log(requestEmail);
	            
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null;

	            return true;
	        } else {
	            message.style.color = 'red';
	            if (xhr.status === 401) {
					message.innerText = "인증번호를 다시 확인해주세요.";
	            }else if(xhr.status === 500){
					message.innerText = "서버 장애 발생";
				} else {
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
	    var message = document.getElementById("verificationMailMessage");
	    timerElement.textContent = formatTime(timeLeft);

	    timerInterval = setInterval(() => {
	        timeLeft--;
	        timerElement.textContent = formatTime(timeLeft);

	        if (timeLeft <= 0) {
	            message.style.color = 'red';
	            message.innerText = '인증 시간이 초과되었습니다. 재발송 시도 해주세요.' ;
	            timerElement.textContent = '00:00' ;
	            document.getElementById("verificationMailCode").setAttribute("readonly", true);
	            document.getElementById("verificationMailCode").setAttribute("disabled", true);
	            document.getElementById("verifyMailCodeButton").setAttribute("disabled", true);
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null;
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
<!--  중복 검사 api -->
<script type="text/javascript">


	//***** 이메일 중복 검사 *****
	function validateEmail() {
		var reqEmail = document.getElementById("userEmail").value;
		var message = document.getElementById("emailMessage");
		if (formatEmail()) {
			var xhr = new XMLHttpRequest();
			xhr.open('POST', '/user/exist/email', false); // 동기식 요청으로 변경
			xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
			var data = 'reqEmail=' + encodeURIComponent(reqEmail);
            xhr.send(data);

        	if (xhr.status === 404) {
				console.log("이메일 중복 검사 완료.(성공)")
				return true;
			}  else {
				message.style.color = 'red';
				if (xhr.status === 200) {
					message.innerText = "해당 계정으로 가입할 수 없습니다.";
				}else if(xhr.status === 500){
					message.innerText = "서버 장애 발생";
				}else {
					message.innerText = "서버 장애 발생.";
				}
				return false;
			}
		}
	}
</script>
<!-- 이메일 input 방지 -->
<script type="text/javascript">
var requestEmail;
document.getElementById("userEmail").addEventListener("input",function(){
	if(requestEmail){
		document.getElementById("userEmail").value =requestEmail;
	}
});
</script>
<!-- 메인 -->
<script type="text/javascript">
	var currentStep = 1;
	var totalSteps = 3;

	function nextStep() {
		if (validateStep(currentStep)) {
			currentStep++;
			document.getElementById('step' + currentStep).classList.add('active');
		}
	}

	function formatEmail() {
		const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		var email = document.getElementById("userEmail").value;
		var message = document.getElementById("emailMessage");
		if (!emailPattern.test(email)) {
			message.style.color = 'red';
			message.innerText = "올바른 이메일 형식을 입력 해주세요.";
			return false;
		} else {
			message.style.color = 'green';
			message.innerText = " ";
			return true;
		}
	}

	function formatPasswords() {
		var password = document.getElementById("userPassword").value;
		var message = document.getElementById("passwordMessage");
		var confirmMessage = document.getElementById("passwordConfirmMessage");
		confirmMessage.innerText = ' ';

		var passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;

		if (!passwordPattern.test(password)) {
			message.style.color = 'red';
			message.innerText = "최소 8자 이상이며, 영어, 숫자, 특수기호를 포함해야 합니다.";
			return false;
		} else {
			message.style.color = 'green';
			message.innerText = "유효한 비밀번호 입니다.";
			return true;
		}
	}

	function validateConfirmPasswords() {
		var password = document.getElementById("userPassword").value;
		var confirmPassword = document.getElementById("userConfirmPassword").value;
		var message = document.getElementById("passwordConfirmMessage");

		if (formatPasswords()) {
			if (password !== confirmPassword) {
				message.style.color = 'red';
				message.innerText = "비밀번호가 일치하지 않습니다.";
				return false;
			} else {
				message.style.color = 'green';
				message.innerText = "비밀번호가 일치합니다.";
				return true;
			}
		}
	}

	function formatCode(input) {
		input.value = input.value.replace(/[^0-9]/g, '');
	}

	function validateStep(step) {
		switch (step) {
		case 1:
			return sendMail();
		case 2:
			return verifyMailCode();
		case 3:
			return validateConfirmPasswords();
		default:
			return false;
		}
	}

	// ***** 계성 생성 *****
	function registerUser() {
		// User 데이터 수집
		var registerUserDto = {
			email : $("#userEmail").val(),
			password : $("#userPassword").val()
		};

		$.ajax({
			url : '/user/join/once',
			type : 'POST',
			contentType : 'application/json; charset=UTF-8',
			data : JSON.stringify({
				registerUserDto : registerUserDto
			}),
			success : function(response) {
		        window.location.href = '/joinDetail';
			},
			error : function(xhr, status, error) {
				console.error(error);
				// 에러 처리
			}
		});
	}

	$('#registrationForm').on('submit', function(event) {
		event.preventDefault();
		if(!verifyMailCode()){
			var message = document.getElementById("verificationMailMessage");
			message.style.color = 'red';
			message.innerText = "이메일 인증을 진행해 주세요.";
		}else if(!validateConfirmPasswords()){
		}else{
			registerUser();
		}
	});
</script>
</html>