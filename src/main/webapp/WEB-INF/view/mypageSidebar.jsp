<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
<head>
<style type="text/css">
.container {
	display: flex;
	max-width: 1400px;
	margin: 0 auto;
	padding-top: 50px;
	min-height: 1080px;
}

.sidebar {
	min-width: 120px;
	padding-right: 25px;
	padding-left: 20px;
	margin: 0;
	border-right: 2px solid #efefef;
}

.sidebar ul {
	list-style-type: none;
	padding: 0;
	margin: 0;
}

.sidebar a {
	text-decoration: none;
	color: black;
}

.category-title {
	padding-bottom: 20px;
}

.category-title li {
	padding-bottom: 10px;
}

.content {
	min-width: 1100px;
	padding: 0px 50px;
}

.sidebar li a:not(.headerFont) {
	color: #afafaf;
}
.headerFont{
	margin:0px;
	font-size: 24px;
}
.subHeaderFont{
	margin:0px;
	font-size: 20px;
	
}

</style>
</head>
		<nav class="sidebar">
			<ul>
				<li style="margin-bottom:18px;"><a class="headerFont" href="/my" >마이페이지</a></li>
				<li><a href="/my/estimate">견적 내역</a></li>
				<li class="category-title"><h3>내정보</h3>
					<ul class="sidebar2">
						<li><a href="/my/loginInfo">로그인 정보</a></li>
						<li><a href="/my/profile">프로필 관리</a></li>
						<li><a href="/my/addressBook">주소록</a></li>
					</ul>
				</li>
			</ul>
		</nav>
		<script type="text/javascript">
		document.querySelectorAll('.sidebar li a').forEach(function(link) {
		    if (link.href === window.location.href) {
		        link.style.color = 'black';
		    }
		});
		</script>
</html>