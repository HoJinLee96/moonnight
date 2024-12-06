<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>달밤청소</title>
<style type="text/css">
.container {
	min-width: 980px;
	margin: 0 auto;
	min-height: 1080px;
	padding-top: 30px;
}
.mainImg {
	max-width: 900px;
	min-width: 900px; 
	margin: 0px auto;
}
.mainImg img{
	display:block;
	width: 100%;
}
#speedEstimateContainer{
	width: 100vw;
	min-width: 980px;
    max-width: 1900px;
    margin: 0px auto;
	line-height: 60px;
	background-color: #dbf1fd;
	font-size: 14px;
}
#speedEstimate{
	width: 100vw;
	min-width: 980px;
    max-width: 1900px;
	text-align: center;
}
#speedEstimate form{
	display: inline;
}
#phone{
	width:140px;
}
#phoneFirst{
	width: 50px;
	outline: none;
	border:none;
	font-size: 14px;
}
#phone, #cleaningService, #region{
	outline: none;
	border:none;
	margin-left: 3px;
	margin-right: 5px;
	font-size: 14px;
}
#speedEstimate a{
	text-decoration: none;
	color:black;
}
#submitButton{
	border:2px solid #20367a;
	border-radius:7px;
	background-color: #20367a;
	color: white;
	cursor: pointer;
	padding: 3px 8px;
	font-size: 14px;
	margin-right: 3px;
}
#submitButton:hover{
	border:2px solid #20367a;
	background-color: white;
	color: #20367a;
}
#agreement{
	margin: 0px;
	position: relative;
	top: 1.5px;
}
label[for="agreement"]{
	cursor: pointer;
}
.fixed-top {
	width:100%;
    position: fixed;
    top: 0;
}
#agreeMentDiv{
	display: inline;
}
iframe{

}
</style>
</head>
<%@ include file="main_header.jsp"%>
<script type="text/javascript">
	
document.addEventListener('DOMContentLoaded', function () {
    const nav = document.querySelector('#speedEstimateContainer');
    const container = document.querySelector('.container');
    const navOffsetTop = nav.offsetTop;
	
    window.addEventListener('scroll', function () {
    /* var scrollLeft = window.scrollX; */
        if (window.scrollY >= navOffsetTop) {
            nav.classList.add('fixed-top');
            container.style.paddingTop = '90px';
        } else {
            nav.classList.remove('fixed-top');
            container.style.paddingTop = '30px';
        }
        /* nav.style.left = -scrollLeft + 'px'; */

    });
});
</script>
<body>
	<div id="speedEstimateContainer">
		<div id="speedEstimate">
				<label for="phone">연락처 </label>
				<select id="phoneFirst" name="phoneFirst">
					<option value="">선택</option>
					<option value="010">010</option>
					<option value="02">02</option>
					<option value="031">031</option>
					<option value="032">032</option>
					<option value="033">033</option>
					<option value="041">041</option>
					<option value="042">042</option>
					<option value="044">044</option>
					<option value="051">051</option>
					<option value="052">052</option>
					<option value="053">053</option>
					<option value="054">054</option>
					<option value="055">055</option>
					<option value="061">061</option>
					<option value="062">062</option>
					<option value="063">063</option>
					<option value="064">064</option>
					<option value="070">070</option>
					<option value="080">080</option>
					<option value="0130">0130</option>
					<option value="0303">0303</option>
					<option value="0502">0502</option>
					<option value="0503">0503</option>
					<option value="0504">0504</option>
					<option value="0505">0505</option>
					<option value="0506">0506</option>
					<option value="0507">0507</option>
					<option value="0508">0508</option>
					<option value="050">050</option>
				</select> 
				<input type="text" id="phone" name="phone" oninput="formatPhoneNumber(this)" placeholder="연락처" maxlength="9" required autocomplete="off">
				<label for="cleaningService">청소 서비스 선택 </label>
				<select id="cleaningService" name="cleaningService">
					<option value="">선택</option>
					<option value="신축">신축 입주 청소</option>
					<option value="이사">이사 입주 청소</option>
					<option value="거주">거주 청소</option>
					<option value="리모델링">리모델링 청소</option>
					<option value="준공">준공 청소</option>
					<option value="상가">상가 청소</option>
					<option value="오피스">오피스 청소</option>
					<option value="기타">기타 청소</option>
				</select> 
				<label for="region">지역 </label>
				<select id="region" name="region">
					<option value="">선택</option>
					<option value="서울">서울특별시</option>
					<option value="부산">부산광역시</option>
					<option value="대구">대구광역시</option>
					<option value="인천">인천광역시</option>
					<option value="광주">광주광역시</option>
					<option value="대전">대전광역시</option>
					<option value="울산">울산광역시</option>
					<option value="세종">세종특별자치시</option>
					<option value="경기">경기도</option>
					<option value="강원">강원도</option>
					<option value="충북">충청북도</option>
					<option value="충남">충청남도</option>
					<option value="전북">전라북도</option>
					<option value="전남">전라남도</option>
					<option value="경북">경상북도</option>
					<option value="경남">경상남도</option>
					<option value="제주">제주특별자치도</option>
				</select>
				<button id="submitButton">간편 견적 신청</button>
			<div id="agreeMentDiv">
				<input type="checkbox" id="agreement" name="agreement">
				<label for="agreement" style="font-size: 14px;">개인정보 수집 및 이용 동의</label>
			</div>
			<a style="font-size: 14px;" href="javascript:;"
				onclick="javascript:footerlayerLoad('/static/infoAgreement.html'); return false;">[원본]</a>
		</div>
	</div>

	<div class="container">

		<div class="mainImg" id="img1">
			<img src="static/img/dal1.png" alt="dal1">
			<img src="static/img/dal2.png" alt="dal2">
			<img src="static/img/dal3.png" alt="dal4">
			<img src="static/img/dal4.png" alt="dal4">
			<img src="static/img/dal5.png" alt="dal5">	
		</div>

	</div>

<%@ include file="main_footer.jsp"%>
<%@ include file="footerlayerLoad.jsp"%>
</body>
<script type="text/javascript">

//휴대폰 번호 규칙
function formatPhoneNumber(input) {
	let value = input.value.replace(/[^0-9]/g, ''); 
	let formattedValue = value;

	if (value.length >= 4) {
		formattedValue = value.slice(0, 4) + '-' + value.slice(4);
	}

	input.value = formattedValue;
}

$('#submitButton').on('click', function(event) {
	event.preventDefault();
    
    var phoneFirst = $('#phoneFirst').val();
    var phone = $('#phone').val().trim();
    var cleaningService = $('#cleaningService').val();
    var region = $('#region').val();
    var agreement = $('#agreement');

    if (phoneFirst === '' || phone === '') {
        alert('연락처를 입력해주세요.');
        return;
    }
    phone = phoneFirst + phone;

    if (cleaningService === '') {
        alert('청소 서비스를 선택해주세요.');
        return;
    }

    if (region === '') {
        alert('지역을 선택해주세요.');
        return;
    }
    
    if (!agreement.prop('checked')) {
        alert('개인정보 수집 및 이용 동의 체크해주세요.');
        return;
    }

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/estimate/speedRegister', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
    var data = 'phone=' + encodeURIComponent(phone) +
               '&cleaningService=' + encodeURIComponent(cleaningService)+
               '&region=' + encodeURIComponent(region);
	xhr.onload = function() {
	    if (xhr.status === 200) {
	        alert("신청해 주셔서 감사합니다. \n빠른 시일 내에 연락 드리겠습니다.");
	    } else if (xhr.status === 429) {
	        alert("너무 많은 시도 입니다. \n잠시 후 다시 시도해주세요.");
	    } else if (xhr.status === 500) {
	        alert("서버에 오류가 발생했습니다. \n잠시 후 다시 시도해주세요.");
	    } else {
	        alert("잠시 후 다시 시도해주세요.");
	    }
		location.href = "/home";
    };
    xhr.send(data);
});
	</script>
</html>
