<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>견적서 목록</title>
<style type="text/css">
#main{
    max-width: 1920px;
    min-width: 980px;
    width: 99vw;
}
#pagination button {
    margin: 0 2px;
}
#quotes{
    max-width: 1800px;
    min-width: 980px;
    width: 93vw;
    padding: 20px 50px;
}
table{
	border-radius: 10px;
    border-collapse: separate;
	max-width: 1800px;
    min-width: 1600px;
    width: 93vw;
    text-align: center;
    border-spacing: 0px 0px;
}
table th{
    font-size: 14px;
	font-weight: bold;
	border-bottom: 2px solid black; /* 아래 밑줄 추가 */
	padding-bottom: 10px;
}
table tr{
}
table td {
	padding: 5px;
   	border-bottom: 1px solid #efefef; 
    white-space: nowrap;        /* 한 줄로 표시 */
    overflow: hidden;           /* 넘치는 내용 숨김 */
    text-overflow: ellipsis;    /* 생략(...) 처리 */
}
.seeMore {
    cursor: pointer;
    background: none;
    border: none;
    font-size: 16px;
}
#pagination{
    text-align: center;
	padding: 20px 0px 50px 0px;
}
#pagination a{
	text-decoration: none;
	color: black
}
#pageNumberDiv{
 	margin : 0px auto;
	display : inline-block
}
.name {
    max-width: 100px;
    min-width: 100px;
}
.email{
    max-width: 200px;           
    min-width: 200px;           
}
.mainAddress {
    max-width: 400px;
    min-width: 400px;
}
.phone{
    max-width: 150px;
    min-width: 150px;
}
#headerContainer, .top_inner, .top_main {
	min-width: 1600px !important;
}
#filter{
	padding: 10px;
}
#statusTypes{
	display: flex;
	padding: 10px 50px;
}
.statusType{
	margin-right: 5px;
	padding: 10px 20px;
	border: 1px solid #efefef;
	border-radius: 5px;
	cursor: pointer;
}
.statusType:hover{
	background-color: #efefef;
}
.selectType{
	margin-right: 5px;
	padding: 10px 20px;
    border: 1px solid #346aff;
    border-radius: 5px;
    background-color: #f1f6ff;
}
#sort{
	padding: 10px 50px;
}
#dateSelects{
	display: inline-block;
}
.dateButton{
	margin-right: 5px;
	padding: 5px 10px;
    border: 1px solid #efefef;
    border-radius: 5px;
	background-color: unset;
	cursor: pointer;
}
.dateButton:hover{
	background-color: #efefef;
}
#monthSelect{
	border: 1px solid #efefef;
	border-radius: 5px; 
	padding: 5px 10px;
}
#startDate, #endDate, #sizeSelect{
	outline: none;
	border: 1px solid #efefef;
	border-radius: 5px; 
	padding: 5px 10px;
	cursor: pointer;
}
</style>
</head>
<body>
<%@include file="master_header.jsp" %>

<div id="main">
	<div id="filter">
		<div id="statusTypes">
			<div class ="selectType" id="all"><div>전체</div><div id="allCount">0</div></div>
			<div class ="statusType" id="received"><div>신청</div><div id="receivedCount">0</div></div>
			<div class ="statusType" id="inprogress"><div>처리중</div><div id="inprogressCount">0</div></div>
			<div class ="statusType" id="completed"><div>완료</div><div id="completedCount">0</div></div>
		</div>
		<div id="sort">
			<div id="dateSelects">
				<div style="display:inline-block;margin-right: 10px;">기간</div>
				<button class="dateButton" id="today">오늘</button>
				<button class="dateButton" id="last7">지날 7일</button>
				<button class="dateButton" id="last30">지난 30일</button>
				<select id="monthSelect" >
					<option value="">월별</option>
					<option value="1">1월</option>
					<option value="2">2월</option>
					<option value="3">3월</option>
					<option value="4">4월</option>
					<option value="5">5월</option>
					<option value="6">6월</option>
					<option value="7">7월</option>
					<option value="8">8월</option>
					<option value="9">9월</option>
					<option value="10">10월</option>
					<option value="11">11월</option>
					<option value="12">12월</option>
				</select>
				<div style="display:inline-block;margin: 0px 10px; color:#efefef">|</div>
				<input id="startDate" type="date">
				<div style="display:inline-block;margin: 0px 10px;">~</div>
				<input id="endDate"type="date">
				<button class="dateButton" id="last30">조회</button>
			</div>
			<select id="sizeSelect" >
				<option value="10">10개씩 보기</option>
				<option value="20">20개씩 보기</option>
				<option value="30">30개씩 보기</option>
				<option value="40">40개씩 보기</option>
				<option value="50">50개씩 보기</option>
			</select>
		</div>
	</div>
    <div id="quotes">
    	<table id="table">
    		<thead id="thead">
    			<tr>
	   			<th>접수번호</th>
	   			<th>상태</th>
	   			<th>이름</th>
	   			<th>연락처</th>
	   			<th>이메일</th>
	   			<th>이메일 동의</th>
	   			<th>SMS 동의</th>
	   			<th>전화 동의</th>
	   			<th>주소</th>
	   			<th>생성일시</th>
	   			<th></th>
    			</tr>
    		</thead>
    		<tbody id="tbody">
    		</tbody>
    	</table>
    </div>
    <div id="pagination"> 
	    <div id="pageNumberDiv">
	    </div>
	</div>
</div>

</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
let currentGroup = 1; // 현재 그룹
const buttonsPerGroup = 10; // 한 그룹당 몇개의 버튼

document.addEventListener('DOMContentLoaded', async () => {
    const totalCount = await getTotalQuotesCount();
    const quotesPerPage = 50;
    const totalPages = Math.ceil(totalCount / quotesPerPage); // 총 페이지 수
    createPaginationButtons(totalPages);
    loadQuotesPage(1); // 첫 페이지 로드
});

async function getTotalQuotesCount() {
    const response = await fetch('/estimate/getCountAll');
    const data = await response.json();
    return data.totalCount;
}

function createPaginationButtons(totalPages) {
    const pageNumberDiv = document.getElementById('pageNumberDiv');
    pageNumberDiv.innerHTML = '';

    const totalGroups = Math.ceil(totalPages / buttonsPerGroup); // 총 버튼 그룹 

    if (currentGroup > 1) { // 현재 버튼 그룹이 2 이상일때 "<" 버튼 생성
        const prevButton = document.createElement('button');
        prevButton.innerText = '<';
        prevButton.addEventListener('click', () => {
            currentGroup--;
            updatePagination(totalPages);
            const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            loadQuotesPage(startPage);
        });
        pageNumberDiv.appendChild(prevButton);
    }

    const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 예시: 1, 11, 21, 31 ...
    const endPage = Math.min(currentGroup * buttonsPerGroup, totalPages); // 좌우 둘 중 작은 값 반환

    for (let i = startPage; i <= endPage; i++) {
        const button = document.createElement('button');
        button.innerText = i;
        button.addEventListener('click', () => loadQuotesPage(i));
        pageNumberDiv.appendChild(button);
    }

    if (currentGroup < totalGroups) { // 현재 버튼 그룹 다음 버튼 그룹이 있을때
        const nextButton = document.createElement('button');
        nextButton.innerText = '>';
        nextButton.addEventListener('click', () => {
            currentGroup++;
            updatePagination(totalPages);
            const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            loadQuotesPage(startPage);
        });
        pageNumberDiv.appendChild(nextButton);
    }
}

function updatePagination(totalPages) {
    createPaginationButtons(totalPages);
}

async function loadQuotesPage(pageNumber) {
	console.log(pageNumber +"번 페이지 로드 시작");
 	const tbody = document.querySelector("tbody"); // <tbody> 요소 선택
 	const pageNumberDiv = document.querySelector("#pageNumberDiv");
 	const buttons = pageNumberDiv.querySelectorAll("button");

 	buttons.forEach(button => {
 	    if (button.innerText == pageNumber) { 
 	        button.style.color = "#bebebe";
 	    }else{
 	    	button.style.color = "black";
 	    }
 	    
 	});
 	
	tbody.replaceChildren();
	try {
		const response = await fetch('/estimate/getAllEstimate?page=' + pageNumber + '&size=50');
        if (!response.ok) {
            throw new Error('Network response was not ok: ${response.statusText}');
        }
        const data = await response.json();
        if (!data.list || !Array.isArray(data.list)) {
            throw new Error('Invalid data format');
        }
        displayQuotes(data.list);
    } catch (error) {
    	alert("로딩 실패.");
        console.error('Fetch error:', error);
    }
}

function displayQuotes(list) {
    const quotesContainer = document.getElementById('quotes');
    const table = document.getElementById('table');
    const tbody = document.getElementById('tbody');
/*     quotesContainer.innerHTML = '';

    const table = document.createElement('table');
    const thead = document.createElement('thead');

    // 테이블 헤더 설정
    const headers = ["접수번호","상태","이름", "연락처", "이메일", "이메일 동의", "SMS 동의", "전화 동의", "우편번호", "주소", "상세 주소", "생성일시", ""];
    const headerRow = document.createElement('tr');
    
    headers.forEach(headerText => {
        const th = document.createElement('th');
        th.innerText = headerText;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow); */

    // 날짜 형식 변환 함수
    function formatDate(dateArray) {
        if (!Array.isArray(dateArray) || dateArray.length < 3) return 'Invalid Date';

        const [year, month, day, hour = 0, minute = 0] = dateArray;
        const date = new Date(year, month - 1, day, hour, minute);

        let formattedDate = date.toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });

        // 불필요한 공백 제거
        formattedDate = formattedDate.replace(/\. /g, '.').replace(/, /g, ' ');

        return formattedDate;
    }
    
    const statusMapping = {
    	    RECEIVED: "신청",
    	    IN_PROGRESS: "처리중",
    	    COMPLETED: "완료",
    	    DELETE: "삭제"
    	};

    // 테이블 본문 설정
    list.forEach(estimate => {
        const row = document.createElement('tr');
        const rowData = [
        	estimate.estimateSeq,
        	statusMapping[estimate.status],
            estimate.name,
            estimate.phone,
            estimate.email,
            estimate.emailAgree ? "수락" : "거부",
            estimate.smsAgree ? "수락" : "거부",
            estimate.callAgree ? "수락" : "거부",
            estimate.mainAddress,
            formatDate(estimate.createdAt)
        ];

        rowData.forEach((data,index) => {
            const td = document.createElement('td');
            // 상태 텍스트의 색상 변경
            if (index === 0) { 
                td.classList.add("estimate_seq");
            }
            if (index === 1) { 
                td.classList.add("status");
            }
            if (index === 2) { 
                td.classList.add("name");
            }
            if (index === 3) { 
                td.classList.add("phone");
            }
            if (index === 4) { 
                td.classList.add("email");
            }
            if (index === 8) { 
                td.classList.add("mainAddress");
            } 
            td.innerText = data;
            
            row.appendChild(td);
        });

        // 펼칠 수 있는 버튼 추가
        const buttonTd = document.createElement('td');
        const button = document.createElement('button');
        button.classList.add("seeMore");
        button.innerHTML = "&#9660;"; // 아래 화살표
        buttonTd.appendChild(button);
        row.appendChild(buttonTd);

        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    quotesContainer.appendChild(table);
}
    </script>
</html>