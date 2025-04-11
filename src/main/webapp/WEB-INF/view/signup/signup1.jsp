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
				<input type="email" id="userEmail" name="userEmail" required placeholder="example@example.com">
				<button type="button" class="sendMailButtons" id="sendMailButton">인증번호 발송</button>
				<span id="emailMessage"></span>
			</div>
			<div class="step" id="step2">
				<label for="emailVerificationCode">인증번호</label>
				<div id="input-wrapper">
				<input type="text" id="verificationMailCode" name="verificationMailCode" required maxlength="6" readonly disabled>
				<div id ="verificationTimeMessage">03:00</div>
				</div>
				<input type="hidden" id="mailSeq" value="" />
				<button type="button" class="verifyMailCodeButton" id="verifyMailCodeButton" disabled>인증번호 확인</button>
				<span id="verificationMailMessage"></span>
			</div>
			<div class="step" id="step3">
				<label for="userPassword">비밀번호</label>
				<input type="password" id="userPassword" name="userPassword" required maxlength="60"> <span id="passwordMessage"></span>
				<label for="userConfirmPassword">비밀번호 확인</label>
				<input type="password" id="userConfirmPassword" name="userConfirmPassword" required maxlength="60">
				<span id="passwordConfirmMessage"></span>
				<div id="buttonContainer">
				<button type="button" id="submitButton">가입하기</button>
				</div>
			</div>
			
		</form>
	</div>
	<%@ include file="/WEB-INF/view/main/main_footer.jsp"%>
</body>
<script type="module">
  import {
    sendMail,
    validateEmail,
    startVerificationTimer,
	verifyMailCode
  } from '/static/js/emailVerification.js';

document.getElementById("sendMailButton").addEventListener("click", () => {
	const message = document.getElementById("emailMessage");
	const verMessage = document.getElementById("verificationMailMessage");

    // 1. 이메일 중복 검사 먼저 수행
    validateEmail(
      () => {
        // 2. 중복 없음 → 인증 메일 발송
        sendMail(
          () => {
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
				document.getElementById("step2").style.display = "block";
            startVerificationTimer(() => {
              document.getElementById("verificationMailMessage").innerText = "시간 초과. 다시 인증해주세요.";
            });
          },
          (status) => {
            const msg = status === 429 ? "잠시 후 다시 시도해주세요." : "서버 오류 발생";
            document.getElementById("emailMessage").innerText = msg;
          }
        );
      },
      (status) => {
		let msg;
		if(status === 404){
			msg = "이메일을 확인해 주세요.";
		}else if(status === 409){
			msg = "이미 가입된 이메일 입니다.";
		}else{
			msg = "서버 오류 발생";
		}
		message.style.color = 'red';
		message.innerText = msg;
	  }
    );
  });

document.getElementById("verifyMailCodeButton").addEventListener("click", () => {
	var message = document.getElementById("verificationMailMessage");

	verifyMailCode(() => {
		alert("인증 성공");
		message.style.color = 'green';
	    message.innerText = "인증 성공";
	            document.getElementById("userEmail").setAttribute("readonly",true);
	            document.getElementById("userEmail").setAttribute("disabled",true);
	            document.getElementById("sendMailButton").setAttribute("disabled", true);
	            document.getElementById("verificationMailCode").setAttribute("readonly", true);
	            document.getElementById("verificationMailCode").setAttribute("disabled", true);
	            document.getElementById("verifyMailCodeButton").setAttribute("disabled", true);
		document.getElementById("step3").style.display = "block";
	},
	(status) =>{
		let msg;
		if(status === 400){
			msg = "인증 번호를 확인해 주세요.";
		}
		else if(status === 408){
			msg = "시간 초과. 다시 인증해주세요.";
		}else if(status === 404){
			msg = "잘못된 요청입니다.";
		}else{
			msg = "서버 오류 발생";
		}
		message.style.color = 'red';
		message.innerText = msg;
	});
});
</script>
<script type="module">
import {
  sendJoinStep1Request
} from '/static/js/signup.js';

	document.getElementById("submitButton").addEventListener("click", (e) => {
	e.preventDefault();
	const email = document.getElementById("userEmail").value;
	const password = document.getElementById("userPassword").value;
	const confirmPassword = document.getElementById("userConfirmPassword").value;
	sendJoinStep1Request(
	    email,
	    password,
		confirmPassword,
		(json)=>{
			localStorage.setItem("accessJoinToken", json.data);
			location.replace("/signup2");
		},
		(error)=>{
			if (error.type === "VALIDATION") {
		    	alert(error.message);
		  	} else if (error.type === "SERVER") {
				if(error.status === 401){
		    	alert("잘못된 요청입니다.");
				} else if(error.status === 409){
		    	alert("이미 가입되어 있는 이메일 입니다.");
				}  else if(error.status === 500){
		    	alert("서버 오류. 잠시후 다시 시도해 주세요.");
				}else{
		    	alert("회원가입 실패.\n서버 오류 코드: " + error.status + "\n내용: "+error.message);
				}
			}
		});
	});
</script>

<script type="module">
  import {
    formatEmail,
	formaVerifyCode,
	formatPasswords,
	validateConfirmPasswords
  } from '/static/js/format.js';

document.getElementById("userEmail").addEventListener("input", (e)=> {
	var message = document.getElementById("emailMessage");
	formatEmail(e.target.value,message);
});

document.getElementById("verificationMailCode").addEventListener("input", (e)=> {
	formaVerifyCode(e);
});

document.getElementById("userPassword").addEventListener("input", (e)=> {
	var message = document.getElementById("passwordMessage");
	var confirmMessage = document.getElementById("passwordConfirmMessage");
	formatPasswords(e.target.value,message,confirmMessage);
});

document.getElementById("userConfirmPassword").addEventListener("input", (e)=> {
	var password = document.getElementById("userPassword").value;
	var message = document.getElementById("passwordConfirmMessage");
	validateConfirmPasswords(password, e.target.value, message);
});

</script>


</html>