<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
<style type="text/css">

.footer{
	width: 100%;
	padding-top: 20px;
	min-height: 300px;
	border-top: 1px solid #efefef;
	min-width: 840px;
	max-width:1920px;
	margin : 0px auto;
	margin-top: 100px;
}
.footerDiv{
	display : flex;
	width: 100%;
	min-width: 840px;
	max-width:1230px;
	margin: 0px auto;
}
.footer ul{
margin:5px 10px;
padding:0px;
list-style: none;
}
.footer ul li a{
text-decoration: none;
color:#bababa;

}
.footer ul li a:hover{
color:black;
}
.info{
margin-right: auto;

}
.line1{
display:flex;
}
.line1 li{
margin-right: 10px;
}
.sns{
margin-left: auto;

}
</style>
</head>

<div class ="footer">
<div class="footerDiv">
	<nav class = "info">
		<ul>
			<li>
				<ul class ="line1">
					<li><a href="">회사소개</a></li>
					<li><a href="">이용약관</a></li>
					<li><a href="">개인정보처리방침</a></li>
				</ul>
			</li>
			
			<li>
				<ul>
					<li><a href="">회사소개</a></li>
					<li><a href="">이용약관</a></li>
					<li><a href="">개인정보처리방침</a></li>
				</ul>
			</li>
		</ul>
	</nav>
	<nav class ="sns">
		<ul class ="line1">
			<li><a href="">페이스북</a></li>
			<li><a href="">인스타</a></li>
			<li><a href="">카카오톡</a></li>
		</ul>
	</nav>
</div>
</div>

</html>