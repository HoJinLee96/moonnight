let timerInterval = null;
let mailSeq = 0; // 이메일 인증 여부

// ======= 이메일 유효성 + 중복 검사 =======
export async function validateEmail(successHandler, errorHandler) {
  const email = document.getElementById("userEmail").value;

  // 1차 형식 검증
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errorHandler(404);
    return;
  }

  try {
    // [POST] /api/public/local/user/exist/email
    const response = await fetch("/api/public/local/user/exist/email", {
      method: "POST",
	  headers: { "Content-Type": "application/x-www-form-urlencoded" },
	  body: new URLSearchParams({ email })
	});


	if (response.ok) {
	  // 200 → 중복 없음
	  successHandler();
	} else if (response.status === 409) {
	  // 409 → 중복됨
	  errorHandler(409);
	} else {
	  errorHandler(response.status);
	}
  } catch (e) {
    errorHandler(500);
  }
}

// ======= 이메일 인증번호 발송 =======
export async function sendMail(successHandler, errorHandler) {
	
  const email = document.getElementById("userEmail").value;

  try {
    // [POST] /api/public/verify/email  
    const response = await fetch("/api/public/verify/email", {
      method: "POST",
	  headers: { "Content-Type": "application/x-www-form-urlencoded" },
	  body: new URLSearchParams({ email })
    });

    if (response.ok) {
      mailSeq = 1;
      successHandler();
    } else {
      errorHandler(response.status);
    }
  } catch (e) {
    errorHandler(500);
  }
  
}

// ======= 이메일 인증번호 검증 =======
let verificationEmailToken = null;

export async function verifyMailCode(successHandler, errorHandler) {
  const code = document.getElementById("verificationMailCode").value;
  const email = document.getElementById("userEmail").value;

  if (mailSeq === 0 || code.length < 6) {
    errorHandler(400); // 인증번호 없음 또는 너무 짧음
    return;
  }

  try {
    // [POST] /api/public/verify/compare/email/uuid
    // RequestBody: { email: xxx, verificationCode: xxx }
    const body = {
      email: email,
      verificationCode: code
    };

    const response = await fetch("/api/public/verify/compare/email/uuid", {
      method: "POST",
	  headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    if (response.ok) {
	  const json = await response.json();
	  verificationEmailToken = json.data["X-Verfication-Email-Token"];
      clearInterval(timerInterval);
      timerInterval = null;
      successHandler();
    } else {
      errorHandler(response.status);
    }
  } catch (e) {
    errorHandler(500);
  }
}

export function startVerificationTimer(onTimeout) {
  let timeLeft = 180;
  const timerElement = document.getElementById("verificationTimeMessage");

  timerElement.textContent = formatTime(timeLeft);

  if (timerInterval) clearInterval(timerInterval); // 중복 방지

  timerInterval = setInterval(() => {
    timeLeft--;
    timerElement.textContent = formatTime(timeLeft);

    if (timeLeft <= 0) {
      clearInterval(timerInterval);
      timerInterval = null;
      onTimeout();
    }
  }, 1000);
}

function formatTime(seconds) {
  const m = String(Math.floor(seconds / 60)).padStart(2, "0");
  const s = String(seconds % 60).padStart(2, "0");
  return `${m}:${s}`;
}

export function getVerificationEmailToken() {
  return verificationEmailToken;
}