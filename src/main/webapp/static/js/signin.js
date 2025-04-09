async function signin(event) {
  event.preventDefault();

  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  const saveEmail = document.getElementById("saveEmail").checked;

  if (saveEmail) {
    localStorage.setItem("savedEmail", email);
  } else {
    localStorage.removeItem("savedEmail");
  }

  const response = await fetch(contextPath + "/in/local", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  });

  if (response.ok) {
    const contentType = response.headers.get("Content-Type");

    if (contentType && contentType.includes("application/json")) {
      const result = await response.json();
      alert("AccessToken: " + result.accessToken);
    } else {
      const location = response.headers.get("Location");
      if (location) {
        window.location.href = location;
      }
    }
  } else {
    const errorText = await response.text();
    alert("Login failed: " + errorText);
  }
}

// 저장된 이메일 복원
document.addEventListener("DOMContentLoaded", () => {
  const saved = localStorage.getItem("savedEmail");
  if (saved) {
    document.getElementById("email").value = saved;
    document.getElementById("saveEmail").checked = true;
  }
});