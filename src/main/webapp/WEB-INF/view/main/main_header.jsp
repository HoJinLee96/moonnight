<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style type="text/css">
@font-face {
    font-family: 'SF_HambakSnow';
    src: url('https://fastly.jsdelivr.net/gh/projectnoonnu/noonfonts_2106@1.1/SF_HambakSnow.woff') format('woff');
    font-weight: normal;
    font-style: normal;
}
*{font-family: 'SF_HambakSnow', sans-serif;}
</style>

<style type="text/css">
body{
    min-width: 980px;
    max-width: 1900px;
    width: 100vw;
 	margin: 0px auto;
}
#headerContainer{
 	min-width: 980px;
    max-width: 1900px;
 	height: 117px;
 	padding-top: 8px;
}
.background-image {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
	min-width: 980px;
    max-width: 1905px;
    width: 100vw;
    height:125px;
    margin: 0 auto;
    background-image: url('https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/headerBackground2.jpg'); /* 배경 이미지 경로 */
    background-size: cover;
    /* background-position: center; */
    z-index: 1; /* 로고보다 뒤에 배치되도록 z-index 설정 */
}
#logopng{
    position: relative;
    z-index: 2; /* 배경 이미지보다 앞에 배치되도록 z-index 설정 */
    display: block;
    height: 100%;
    bottom: 3px;
}
.logopng{
    height: 100%;
    object-fit: contain;
    display: block;
    margin: auto; /* 로고가 중앙에 오도록 설정 */
}
.top_inner{
	min-width: 980px;
    max-width: 1900px;
    display: flex;
    justify-content: flex-end;
	z-index: 3;
}
.top_nav{
	padding-right: 5vw;
	z-index: 3;
}
.top_nav ul{
	display:flex;
	text-align: right;
	list-style-type: none;
	list-style: none;
	margin: 0px;
	padding: 0px;

}
.top_nav ul li a, #user-welcome{
	text-decoration: none;
	color: white;
	padding-left: 10px;
	font-size: 14px;
	font-weight: 300;

}
.top_main {
	min-width: 980px;
    max-width: 1900px;
	display: flex;
	justify-content: space-between;
}
#log_div{
 display: flex;
 flex-direction: column;
 margin-left: 5vw;
}

#main_nav_div{
 display: flex;
 flex-direction: column;
 	z-index: 3;
}
.main_nav{
	display:flex;
	list-style: none;
	margin-top: auto;
	margin-bottom: 6px;
	margin-right: 5vw;
}
.main_nav a{
text-decoration: none;
color: white;
font-size: 23px;
padding-left: 20px;

}
.container{
min-height: 1080px;
}
</style>
<script type="module">
  import {
	confirmAccessToken,
	signOut
  } from '/static/js/sign.js';

	document.addEventListener('DOMContentLoaded', function() {
		confirmAccessToken(
		(user)=>{
    		document.getElementById("login-header").style.display = "none";
    		document.getElementById("user-header").style.display = "flex";
    		document.getElementById("user-welcome").innerText = user.name+"님 환영합니다!";
		},
		(status)=>{
 		   	document.getElementById("login-header").style.display = "flex";
 		   	document.getElementById("user-header").style.display = "none";
		}
		);
	});

document.getElementById("signOut").addEventListener("click", (e)=> {
	signOut(
		()=>{
			localStorage.removeItem("accessToken"); 
    		location.reload(true);
		},
		(status)=>{
			alert(status);
		}
	);

});
</script>
</head>
<div id ="headerContainer">
        <div class="background-image"></div>

	<div class = "top_inner">
		<nav class = "top_nav">
			<ul id="login-header">
				<li><a href="/signin">로그인</a></li>
				<li><a href="/signup1">회원가입</a></li>
				<li><a href="">고객센터</a></li>
			</ul>
			<ul id="user-header" style="display:none">
				<li><span id ="user-welcome"></span></li>
				<li><a href="/my">마이페이지</a></li>
				<li><span id ="signOut">로그아웃</span></li>
				<li><a href="">고객센터</a></li>
			</ul>
			<ul>
			</ul>
		</nav>
	</div>
	<div class = "top_main">
		<div id="log_div">
			<a id="logopng" href="/">
				<img src="https://chamman.s3.ap-northeast-2.amazonaws.com/static/img/headerLogo.png" alt="Logo" class="logopng">
			</a>
		</div>
			<div id="main_nav_div">
			<ul class= "main_nav">
				<li><a href="/estimate">견적신청</a></li>
				<li><a href="/review">현장사진</a></li>
			</ul>
		</div>
	</div>
	
	</div>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

</html>