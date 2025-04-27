// @Deprecated
export async function validateSignUpToken(successHandler, errorHandler) {
  const accessSignUpToken = localStorage.getItem("accessSignUpToken"); 
  if(!accessSignUpToken){
	errorHandler("비정상적인 접근입니다.");
	return;
  }

  const body = {
    "X-Access-SignUp-Token": accessSignUpToken
  };

  isToken(body,successHandler, errorHandler);
}

// @Deprecated
async function isToken(body,successHandler, errorHandler){
    const response = await fetch("/api/verify/public/confirm", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
	  body: JSON.stringify(body)
    });

    if (response.ok) {
      successHandler();
    } else {
	  errorHandler(response.status);
    }
}