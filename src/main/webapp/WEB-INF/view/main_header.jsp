<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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
#headerContainer{
	max-width: 1920px;
	/* min-width: 1024px; */
	margin: 0 auto;
}
.background-image {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
	max-width: 1920px;
	min-width: 1600px;
    height:125px;
    margin: 0 auto;
    background-image: url('/static/img/headerBackground2.jpg'); /* 배경 이미지 경로 */
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
    display: flex;
    justify-content: flex-end;
	padding: 8px 20px 0px 20px;
	max-width: 1550px;
	/* min-width: 984px; */
	min-width: 1550px;
	margin: 0 auto;
	z-index: 3;
}
.top_nav{
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
.top_nav ul li a{
	text-decoration: none;
	color: white;
	padding-left: 10px;
	font-size: 14px;
	font-weight: 300;

}
.top_main {
	display: flex;
	max-width: 1550px;
	/* min-width: 984px; */
	min-width: 1550px;
	margin: 0 auto;
	padding: 0px 20px;
	justify-content: space-between;
}
#log_div{
 display: flex;
 flex-direction: column;
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
}
.main_nav a{
text-decoration: none;
color: white;
font-size: 23px;
padding-left: 20px;

}
</style>
</head>
<div id ="headerContainer">
        <div class="background-image"></div>

	<div class = "top_inner">
		<nav class = "top_nav">
			<ul>
				<li><a href="">고객센터</a></li>
				
				<c:choose>
					<c:when test="${empty sessionScope.userDto and empty sessionScope.oAuthToken}">
						<!-- 비로그인 상태 -->
					<li><a href="/login">로그인</a></li>
					<li><a href="/join">회원가입</a></li>
					</c:when>
					<c:otherwise>
						<!-- 로그인 상태 -->
					<li><a href="/my">마이페이지</a></li>
					<li><a href="/logout">로그아웃</a></li>
					</c:otherwise>
				</c:choose>
			</ul>
		</nav>
	</div>
	<div class = "top_main">
		<div id="log_div">
			<a id="logopng" href="/home">
				<img src="/static/img/headerLogo.png" alt="Logo" class="logopng">
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
<script type="text/javascript">
var previousURI = document.referrer;
console.log("previousURI = document.referrer는 " + previousURI);

var previousDomain = new URL(previousURI).origin;
console.log("previousDomain = new URL(previousURI).origin는 " + previousDomain);

var previousPath = new URL(previousURI).pathname;
console.log("previousPath = new URL(previousURI).pathname 는 " + previousPath);

var currentDomain = window.location.origin;
console.log("currentDomain = window.location.origin는 " + currentDomain);

</script>
</html>