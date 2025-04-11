import { getVerificationEmailToken } from '/static/js/emailVerification.js';
import { formatEmail,formatPasswords,validateConfirmPasswords } from '/static/js/format.js';

export async function sendJoinStep1Request(email, password, confirmPassword, successHandler, errorHandler) {
  const token = getVerificationEmailToken(); 
  if (!formatEmail(email)) {
	errorHandler({ type: "VALIDATION", message: "이메일 형식이 올바르지 않습니다." });
    return;
  }
  if (!token) {
	errorHandler({ type: "VALIDATION", message: "이메일 인증이 필요합니다." });
    return;
  }
  if (!formatPasswords(password)) {
	errorHandler({ type: "VALIDATION", message: "비밀번호 형식이 올바르지 않습니다." });
    return;
  }
  if (!validateConfirmPasswords(password, confirmPassword)) {
	errorHandler({ type: "VALIDATION", message: "비밀번호가 일치하지 않습니다." });
    return;
  }

  const body = {
    email: email,
    password: password,
	confirmPassword: confirmPassword
  };
  console.log("body: "+JSON.stringify(body)+", token: "+ token);

  try {
    const response = await fetch("/api/public/sign/up/first", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        "X-Verification-Email-Token": token
      },
	  body: new URLSearchParams({ email, password, confirmPassword })
    });

    if (response.ok) {
      const json = await response.json();
      successHandler(json);
    } else {
	  errorHandler({ type: "SERVER", status: response.status, message:  response.message});
    }
  } catch (e) {
	console.log(e);
	errorHandler({ type: "SERVER", status: 500 });
  }
}


export async function sendJoinStep2Request(
	verificationPhoneToken, 
	name,
	birth,
	phone,
	postcode,
	mainAddress,
	detailAddress,
	marketingReceivedStatus,
	successHandler, errorHandler) {

	const accessJoinToken = localStorage.getItem("accessJoinToken");
 	if (!accessJoinToken || !verificationPhoneToken) {
		errorHandler("이메일 인증이 필요합니다.");
		return;
	}

  const body = {
    name: name,
    birth: birth,
	phone: phone,
	postcode: postcode,
	mainAddress: mainAddress,
	detailAddress: detailAddress,
	marketingReceivedStatus: marketingReceivedStatus
  };
  
  console.log("body: "+JSON.stringify(body)+", accessJoinToken: "+ accessJoinToken+", verificationPhoneToken: "+verificationPhoneToken);

  try {
    const response = await fetch("/api/public/sign/up/second", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Access-Join-Token": accessJoinToken,
		"X-Verification-Phone-Token": verificationPhoneToken
      },
      body: JSON.stringify(body)
    });

    if (response.ok) {
      const json = await response.json();
      successHandler(json);
    } else {
      errorHandler(response.status);
    }
  } catch (e) {
    errorHandler(500);
  }
}