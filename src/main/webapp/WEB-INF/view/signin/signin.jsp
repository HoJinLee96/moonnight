<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>로그인</title>
<style type="text/css">
.container {
	/* display: flex; */
	max-width: 1200px;
	margin: 0 auto;
	padding-top: 50px;
	min-height: 1080px;
}
.loginform{
	max-width: 400px;
	min-width: 400px;
	margin: 0 auto;
	border: 2px solid #20367a;
	padding : 0px 100px;
	border-radius: 10px;
	text-align: center;
}
.title{
text-align: left;
padding : 20px 0px;
margin-bottom: 30px;
}
#loginForm label[for="email"],
#loginForm label[for="password"] {
    text-align: left;
    display: block;
    margin: 0px;
    padding: 0px;
    margin-top: 15px; /* input과의 간격을 조정 */
    font-size: 14px;
}
#email, #password{
    width: 400px;
    height: 40px;
    border: none;
    border-bottom: 2px solid #ccc;
    outline: none;
    transition: border-bottom-color 0.3s;
}
#email:focus, #password:focus{
    border-bottom: 2px solid #20367a;
}
#loginForm button{
    width: 400px;
    height: 52px;
    border: none;
    border-radius: 10px;
    background-color: #20367a;
    color: white;
    font-size: 18px;
    margin: 25px 0px 10px 0px;
    cursor: pointer;
}
#loginForm button:hover{
    background-color: white;
    border: 2px solid #20367a;
    color:#20367a;
}
#joinButton{
    width: 400px;
    height: 52px;
    border: 2px solid #20367a;
    border-radius: 10px;
    background-color: white;
    color: #20367a;
    font-size: 18px;
    margin-bottom: 10px;
    cursor: pointer;
}
#OAutoLoginBlcok{
padding:10px 0px 30px 0px;
    display: flex;
    justify-content: center; /* 아이템 사이에 여백을 넣어 균등 배치 */
    align-items: center; /* 수직 정렬 */
}
#OAutoLoginBlcok a{
	text-decoration: none;
	display: block;
    width: 60px;
    height: 60px;
    margin: 0px 5px;
    
}
#OAutoLoginBlcok img {
	cursor: pointer;
    width: 60px;
    height: 60px;
}
#rememmberEmailCheckbox{
    appearance: none; /* 기본 스타일 제거 */
    -webkit-appearance: none;
    -moz-appearance: none;
    background-color: #fff;
    border: 2px solid #ccc;
    border-radius: 50%; /* 동그랗게 만들기 */
    width: 15px;
    height: 15px;
    cursor: pointer;
    position: relative;
    outline: none;
    transition: background-color 0.2s, border-color 0.2s;
}
#rememmberEmailCheckbox:checked {
    background-color: #20367a; /* 체크된 배경색 */
    border-color: #20367a;
}
#rememmberEmailCheckbox:checked::after {
    content: '';
    position: absolute;
    top: 40%;
    left: 50%;
    width: 4px;
    height: 8px;
    border: solid white;
    border-width: 0 2px 2px 0;
    transform: translate(-50%, -50%) rotate(45deg);
}
label[for="rememmberEmailCheckbox"]{
font-size: 14px;
margin-right: 140px;
color: #666;
}
#findEmail, #findPassword{
text-decoration: none;
color: #b1b1b1;
font-size: 14px;
margin-left: 5px;
}
#findEmail::after{
content: '';
border-right: 1px solid #e1e1e1;
padding-left: 5px;
}
#findEmail:hover, #findPassword:hover{
color: #20367a;
cursor:  
}
#etcActionDiv{
    display: flex;
    align-items: center; 
margin-top: 20px;
line-height:normal; 
}
#underline-text{
width: 400px;
position: relative;
display: inline-block;
margin: 10px 0px;
}
#underline-text::after {
content:"다른 방법 로그인";
    color: #b1b1b1;
    background-color: white;
    padding: 0px 15px;
    font-size: 15px;
}
#underline-text::before {
    content: '';
    position: absolute;
    width: 100%;
    height: 1px;
    background-color: #e1e1e1;
    top:10px;
    left: 0px;
    z-index: -1;
}

.emailDiv{
display:inline;
position: relative;
}
#emailInitButton{
display:none;
position: absolute;
right: 10px;
top: 30px;
border: 1px solid #d0d0d0;
border-radius: 50%;
padding: 0px 4px;
font-size: 12px;
color: white;
background: #d0d0d0;
cursor: pointer;
}

.passwordDiv{
display:inline;
position: relative;
}
#passwordViewButton{
background-image: url('https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/eyeHiddenIcon.png'); 
background-size: cover;
width: 17px;
height: 17px;

display:none;
position: absolute;
right: 40px;
top: 43px;
cursor: pointer;
}
#passwordInitButton{
display:none;
position: absolute;
right: 10px;
top: 43px;
border: 1px solid #d0d0d0;
border-radius: 50%;
padding: 0px 4px;
font-size: 12px;
color: white;
background: #d0d0d0;
cursor: pointer;
}
</style>
<script type="text/javascript">
document.addEventListener('DOMContentLoaded', function() {
    const rememberedEmail = localStorage.getItem('rememberEmail');
    if (rememberedEmail) {
        document.getElementById('email').value = rememberedEmail;
        document.getElementById('rememmberEmailCheckbox').checked =true;
    }
});
</script>
</head>
<body>
<%@ include file = "/WEB-INF/view/main/main_header.jsp" %>

<div class ="container">

	<div class ="loginform">
	    <h2 class = "title">로그인</h2>
		<div id="loginForm">
			<div class = "emailDiv">
		        <label for="email">이메일</label>
		        <input type="email" id=email name="email" required autofocus placeholder="example@example.com">
		        <div id = "emailInitButton">&times;</div>
			</div>
			<div class = "passwordDiv">
		        <label for="password">비밀번호</label>
		        <input type="password" id="password" name="password" required placeholder="password">
		        <div id = "passwordViewButton"></div>
		        <div id = "passwordInitButton">&times;</div>
			</div>
	        <div id ="etcActionDiv">
	        	<input type="checkbox" id="rememmberEmailCheckbox" name="rememmberEmailCheckbox">
	        	<label for="rememmberEmailCheckbox">이메일 저장</label>
				<a href="" id="findEmail" onclick="openFindWindow('/find/email')">이메일 찾기</a>
				<a href="" id="findPassword" onclick="openFindWindow('/update/password/blank')">비밀번호 찾기</a>
	        </div>
	        <button id = "signInButton" type="button">로그인</button>
	    </div>
	        <button id="joinButton" type="button">회원가입</button>
	    <div id = "underline-text"></div>
	    <div id = "OAutoLoginBlcok">
	        <a href="/oauth2/authorization/kakao" id="kakao-login">
        <img src="https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/kakaoLogin.png" alt="Kakao Login Logo">
    </a>
	 <a href="/oauth2/authorization/naver" id="naver-login">
        <img src="https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/naverLogin.png" alt="Naver Login Logo">
    </a>

    </div>
    </div>
    
</div>

<%@ include file = "/WEB-INF/view/main/main_footer.jsp" %>
</body>

<script type="text/javascript">
var email = document.getElementById('email');
var emailInitButton = document.getElementById('emailInitButton');
email.addEventListener('input', function() {
    buttonDisplay(email, emailInitButton);
});
email.addEventListener('blur', function() {
    buttonDisplay(email, emailInitButton);
});
emailInitButton.addEventListener('click', function() {
    init(email,emailInitButton);
});

var password = document.getElementById('password');
var passwordInitButton = document.getElementById('passwordInitButton');
var passwordViewButton = document.getElementById('passwordViewButton');
password.addEventListener('input', function() {
    buttonDisplay(password, passwordInitButton);
    buttonDisplay(password, passwordViewButton);
});
password.addEventListener('blur', function() {
    buttonDisplay(password, passwordInitButton);
    buttonDisplay(password, passwordViewButton);
});
passwordInitButton.addEventListener('click', function() {
    init(password,passwordInitButton);
    init(password,passwordViewButton);
});
passwordViewButton.addEventListener('click', function() {
    view(password,passwordViewButton);
});

function view(input,button){
    if (input.type === 'password') {
    	input.type = 'text';
    	button.style.backgroundImage= "url('https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/eyeIcon.png')";
	} else {
    	input.type = 'password';
    	button.style.backgroundImage= "url('https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/eyeHiddenIcon.png')";
    }
}
function init(input,button){
	input.value='';
	button.style.display="none";
}

function buttonDisplay(input,button){
	if(input.value){
		button.style.display="inline";
	}else{
		button.style.display="none";
	}
}
</script>

<script type="text/javascript">
function openFindWindow(url) {
    // 새 창의 크기 지정
    const width = 500;
    const height = 800;

    // 창의 중앙 위치 계산
    const left = window.screenX + (window.outerWidth / 2) - (width / 2);
    const top = window.screenY + (window.outerHeight / 2) - (height / 2);

    // 새로운 창 열기
    window.open(
        url,  // 열고자 하는 URL
        '_blank',  // 새 창의 이름 또는 _blank (새 탭)
        'width=' + width + ',height=' + height + ',top=' + top + ',left=' + left  // 창의 크기와 위치
    );
    return false; // 기본 링크 동작을 막기 위해 false를 반환
}

document.getElementById('joinButton').addEventListener('click',function(){
	window.location.href="/signup1";
});
</script>

<script>
  function loginWithKakao() {
    // 실제 환경에서는 클라이언트 ID와 리다이렉션 URI를 안전하게 관리해야 합니다.
    const KAKAO_CLIENT_ID = 'YOUR_KAKAO_CLIENT_ID'; // 실제 값으로 변경
    const REDIRECT_URI = 'http://localhost:8080/oauth/callback/kakao'; // 백엔드 콜백 처리 경로와 일치

    // 카카오 로그인 페이지 URL 생성
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;

    // 카카오 로그인 페이지로 이동
    window.location.href = kakaoAuthUrl;
  }

  function loginWithNaver() {
    const NAVER_CLIENT_ID = 'YOUR_NAVER_CLIENT_ID'; // 실제 값으로 변경
    const REDIRECT_URI = 'http://localhost:8080/oauth/callback/naver'; // 백엔드 콜백 처리 경로와 일치
    const STATE = 'RANDOM_STATE_STRING'; // CSRF 방어를 위한 상태 토큰 (선택적이지만 권장)

    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${NAVER_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&state=${STATE}`;

    window.location.href = naverAuthUrl;
  }
</script>

<script type="module">
  import { signIn } from '/static/js/sign.js';

document.getElementById("signInButton").addEventListener("click", (e)=> {
	var email = document.getElementById("email").value;
	var password = document.getElementById("password").value;
	var rememmberEmail = document.getElementById("rememmberEmailCheckbox").checked;
	signIn(email,password,rememmberEmail,
	()=>{
	alert("로그인성공");
	},
	(error)=>{
		if (error.type === "VALIDATION") {
		    	alert(error.message);
		  	} else if (error.type === "SERVER") {
				if(error.status === 401 || error.status === 404){
		    	alert("이메일과 비밀번호를 확인해 주세요.");
				}  else if(error.status === 500){
		    	alert("죄송합니다. 현재 접속이 불가능 합니다.");
				}else{
		    	alert("죄송합니다. 현재 접속이 불가능 합니다.");
				}
		}
	});
});

</script>

</html>