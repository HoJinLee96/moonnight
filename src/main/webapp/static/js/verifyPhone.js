/**
var phoneInput = document.getElementById("phone");
var sendSmsButton = document.getElementById("sendSmsButton");
var verificationSmsCodeInput = document.getElementById("verificationSmsCode");
var inputWrapper = document.getElementById("inputWrapper");
var verifySmsCodeButton = document.getElementById("verifySmsCodeButton");
var message = document.getElementById("verificationSmsMessage");
const timerElement = document.getElementById('verificationTimeMessage');
 * 
 */


var timerInterval; // 타이머 인터벌을 저장할 변수
function sendSms() {
	var reqPhone = phoneInput.value.replace(/[^0-9]/g, '');
	console.log(reqPhone);
	if (reqPhone.length !== 11){
		alert("휴대폰 번호를 확인해 주세요.");
	} 
	else {
		var xhr = new XMLHttpRequest();
		xhr.open('POST', '/api/verify/sendsms', true);
		xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');
		
	    xhr.onload = function() {
			if (xhr.status === 200) {
				alert("인증번호 발송 완료");
				phoneInput.setAttribute("readonly", true);
				phoneInput.setAttribute("disabled", true);
				sendSmsButton.innerText = "인증번호 재발송";
				inputWrapper.style.display = "inline-block";
				verificationSmsCodeInput.removeAttribute("readonly");
				verificationSmsCodeInput.removeAttribute("disabled");
				verifySmsCodeButton.removeAttribute("disabled");
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
	    };
    xhr.send('reqPhone=' + encodeURIComponent(reqPhone));
	}
}

	function verifySmsCode(callback) {
	var reqCode = verificationSmsCodeInput.value;
		if (reqCode.length < 5) {
			alert("인증번호를 다시 확인해주세요.");
			message.style.color = 'red';
			message.innerText = "인증번호를 다시 확인해주세요.";
			callback(false);
			return;
		}
		var xhr = new XMLHttpRequest();
		xhr.open('POST', '/api/verify/comparecode', true); 
		xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded; charset=UTF-8');

	    xhr.onload = function() {
			if (xhr.status === 200) {
				message.style.color = 'green';
				message.innerText = "인증 성공";
				phoneInput.setAttribute("readonly", true);
				phoneInput.setAttribute("disabled", true);
				sendSmsButton.setAttribute("disabled", true);
				verificationSmsCodeInput.setAttribute("readonly", true);
				verificationSmsCodeInput.setAttribute("disabled", true);
				verifySmsCodeButton.setAttribute("disabled", true);
	            clearInterval(timerInterval); // 타이머 중지
	            timerInterval = null; // 타이머 초기화
	            callback(true);
			} else {
				message.style.color = 'red';
				if (xhr.status === 408) {
					alert("인증번호 기간이 완료 되었습니다. 재발급 시도해 주세요.");
					message.innerText = "인증번호 기간이 완료 되었습니다.";
				}else if( xhr.status === 401){
					alert("인증번호가 일치하지 않습니다.");
					message.innerText = "인증번호가 일치하지 않습니다.";
				}else if( xhr.status === 500){
					alert("서버 장애.");
					message.innerText = "서버 장애.";
				}else if( xhr.status === 404){
					alert("죄송합니다. 잠시 후 다시 시도 해주세요.");
					message.innerText = "죄송합니다. 잠시 후 다시 시도 해주세요.";
				}else {
					alert("죄송합니다. 잠시 후 다시 시도 해주세요.");
					message.innerText = "죄송합니다. 잠시 후 다시 시도 해주세요.";
				}
			callback(false);
			}
		};
	xhr.send('reqCode=' + encodeURIComponent(reqCode));
	}
	
	function onVerificationCodeSent() {
	    // 3분 타이머 시작
	    let timeLeft = 180; // 3분 = 180초
	    timerElement.textContent = formatTime(timeLeft);

	    timerInterval = setInterval(() => {
	        timeLeft--;
	        timerElement.textContent = formatTime(timeLeft);

	        if (timeLeft <= 0) {
	            message.style.color = 'red';
	            message.innerText = '인증 시간이 초과되었습니다.' ;
	            timerElement.textContent = '00:00' ;
	            verificationSmsCodeInput.setAttribute("readonly", true);
	            verificationSmsCodeInput.setAttribute("disabled", true);
	            verifySmsCodeButton.setAttribute("disabled", true);
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
