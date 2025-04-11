export function formatEmail(email,message) {
	const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	const isValid = emailPattern.test(email);

	if (message) {
		message.style.color = isValid ? 'green' : 'red';
		message.innerText = isValid ? " " : "올바른 이메일 형식을 입력 해주세요.";
	}
	return isValid;
}

export function formatPasswords(password,message,confirmMessage) {
	var passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
	if(confirmMessage){
		confirmMessage.innerText = ' ';
	}
	const isValid = passwordPattern.test(password);

	if (message) {
		message.style.color = isValid ? 'green' : 'red';
		message.innerText = isValid ? "유효한 비밀번호 입니다." : "최소 8자 이상이며, 영어, 숫자, 특수기호를 포함해야 합니다.";
	}
	return isValid;
}

export function validateConfirmPasswords(password, confirmPassword, message) {
	const isValid = (password===confirmPassword);
	if (message) {
		message.style.color = isValid ? 'green' : 'red';
		message.innerText = isValid ? "비밀번호가 일치합니다." : "비밀번호가 일치하지 않습니다.";
	}
	return isValid;
}

export function formaVerifyCode(codeInput) {
		codeInput.value = codeInput.value.replace(/[^0-9]/g, '');
}


export function formatName(name, message) {
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

export function formatBirth() {
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

export function formatPhoneNumber(input) {
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
export function formatCode(input) {
	input.value = input.value.replace(/[^0-9]/g, '');
}