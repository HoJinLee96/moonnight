import { formatEmail,formatPasswords,validateConfirmPasswords,formatName,formatBirth,formatPhoneNumber } from '/static/js/format.js';
 
export async function signIn(email, password, rememberEmail, successHandler, errorHandler){
	
	if (!formatEmail(email)) {
	errorHandler({ type: "VALIDATION", message: "이메일 형식이 올바르지 않습니다." });
	  return;
	}
	if (!formatPasswords(password)) {
	errorHandler({ type: "VALIDATION", message: "비밀번호 형식이 올바르지 않습니다." });
	  return;
	}
	const data = {
		email:email,
		password:password
	};
	
	const response = await fetch("/api/sign/public/in/local", {
	  method: "POST",
	  headers: {
	    "Content-Type": "application/json"
	  },
	  body: JSON.stringify(data)
	});

	if (response.ok) {
		const authorization = response.headers.get("AUTHORIZATION"); // response 객체에서 직접 헤더 가져오기
	   const redirectUrl = response.headers.get("Location"); // response 객체에서 직접 헤더 가져오기
		if(authorization!=null && authorization.startsWith("Bearer ")){
		const accessToken = authorization.substring(7);
			if(accessToken){
				localStorage.setItem("accessToken",accessToken);
				const redirectUrl = response.headers.get("Location");
				window.location.href = redirectUrl ? redirectUrl: '/home';
			}else{
				errorHandler({ type: "SERVER", status: 500, message: "죄송합니다. 현재 접속이 불가능 합니다."});
				return;
			}
		} else {
			errorHandler({ type: "SERVER", status: 500, message: "죄송합니다. 현재 접속이 불가능 합니다."});
			return;
		}
		if (rememberEmail) {
		  localStorage.setItem("rememberEmail", email);
		} else {
		  localStorage.removeItem("rememberEmail");
		}
		successHandler();
	} else {
	  errorHandler({ type: "SERVER", status: response.status});
	}
}

export async function confirmAccessToken(successHandler, errorHandler) {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
            const response = await fetch("/api/sign/public/access", {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + accessToken
                }
            });

            if (response.ok) { 
                const json = await response.json(); 
                const user = json.data;
                if (user) {
                    successHandler(user); 
                } else {
                    // 응답은 성공(200)했지만 데이터가 없는 경우 (서버 로직에 따라 발생 가능)
                    errorHandler({ status: response.status, code: 'NO_USER_DATA', message: '사용자 데이터를 찾을 수 없습니다.' });
                }
            } else {
                errorHandler(response.status);
            }
    } else {
        // 토큰 없음 처리 개선
        errorHandler(500);
    }
}

export async function signOut(successHandler, errorHandler) {
	const accessToken = localStorage.getItem('accessToken');
	if (accessToken) {
	        const response = await fetch("/api/sign/public/out", {
	            method: "POST",
	            headers: {
	                "Authorization": "Bearer " + accessToken
	            }
	        });
	        if (response.ok) {
				successHandler();
	        } else {
	            errorHandler(response.status);
	        }
	} else {
		location.replace("/home");
	}
}

export async function signUpStep1(email, password, confirmPassword, successHandler, errorHandler) {
  if (!formatEmail(email)) {
	errorHandler({ type: "VALIDATION", message: "이메일 형식이 올바르지 않습니다." });
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

    const response = await fetch("/api/sign/public/up/first", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
	  body: new URLSearchParams({ email, password, confirmPassword })
    });

    if (response.ok) {
      const json = await response.json();
      successHandler(json);
    } else {
	  errorHandler({ type: "SERVER", status: response.status, message:  response.message});
    }
 
}


export async function signUpStep2(
	name,
	birthInput,
	phoneInput,
	postcode,
	mainAddress,
	detailAddress,
	agreeToTerms,
	marketingReceivedStatus,
	successHandler, errorHandler) {
		
		if (!formatName(name)) {
			errorHandler({ type: "VALIDATION", message: "이름 형식이 올바르지 않습니다." });
		  	return;
		}
		if (!formatBirth(birthInput)) {
			errorHandler({ type: "VALIDATION", message: "생년월일 형식이 올바르지 않습니다." });
		  	return;
		}
		if (!formatPhoneNumber(phoneInput)) {
			errorHandler({ type: "VALIDATION", message: "휴대폰 형식이 올바르지 않습니다." });
			return;
		}
		if (postcode==="" || mainAddress==="") {
			errorHandler({ type: "VALIDATION", message: "주소 형식이 올바르지 않습니다." });
			return;
		}
		if (agreeToTerms===false) {
			errorHandler({ type: "VALIDATION", message: "개인정보 저장 동의는 필수 입니다." });
			return;
		}

		const body = {
			name: name,
			birth: birthInput.value,
			phone: phoneInput.value,
			postcode: postcode,
			mainAddress: mainAddress,
			detailAddress: detailAddress,
			marketingReceivedStatus: marketingReceivedStatus
		};
  

    const response = await fetch("/api/sign/public/up/second", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body)
    });

    if (response.ok) {
      const json = await response.json();
      successHandler(json);
    } else {
		errorHandler({ type: "SERVER", status: response.status, message:  response.message});
	}
	
}
