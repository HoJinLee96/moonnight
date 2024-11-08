<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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

#headerContainer{
	max-width:1920px;
	min-width:800px;
	margin: 0px auto;
	border-bottom: 1.5px solid #efefef;
	padding-bottom: 15px;
}
.top_inner{
    display: flex;
    justify-content: flex-end;
	padding: 0px 20px;
	max-width: 1200px;
	margin: 0 auto;
	min-width: 800px;
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
	color: black;
	padding-left: 10px;
	font-size: 14px;
	font-weight: 300;
}
.top_main {
	display: flex;
	max-width: 1920px;
	min-width: 800px;
	margin: 0 auto;
	padding: 0px 20px;
	justify-content: space-between;
}
#log_div{
 display: flex;
 flex-direction: column;
 background-image: 
}
.logo {
	list-style: none;
	display:flex;
	padding: 0px;
	margin-top: auto;
	margin-bottom: 0px;
}
.logo a {
    text-decoration: none;
    color: black;
	font-weight: 700;
	padding-right: 2px;
}
#main_nav_div{
 display: flex;
 flex-direction: column;
}
.main_nav{
	display:flex;
	list-style: none;
	margin-top: auto;
	margin-bottom: 6px;
}
.main_nav a{
text-decoration: none;
color: black;
font-size: 23px;
padding-left: 20px;

}
#logo1{
font-size: 50px;
}
#logo2{
font-size: 45px;
color: #fef200;
}
#logo3{
font-size: 50px;
}

</style>
</head>

<div id ="headerContainer">

	<div class = "top_inner">
		<nav class = "top_nav">
			<ul>
			</ul>
		</nav>
	</div>
	<div class = "top_main">
		<div id="log_div">
			<ul class= "logo">
				<li>
					<a id="logo1" href="/master/home">달밤</a><a id="logo2" href="/master/home">N</a><a id="logo3" href="/master/home">청소</a>
				</li>
			</ul>
		</div>
			<div id="main_nav_div">
			<ul class= "main_nav">
				<li><a href="/master/estimateView">모든 견적서</a></li>
			</ul>
		</div>
	</div>
	
	</div>



<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>

</html>