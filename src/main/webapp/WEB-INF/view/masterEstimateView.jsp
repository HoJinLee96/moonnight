<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="dto.EstimateSearchRequest" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>견적서 목록</title>
<style type="text/css">
/* body{
background-color: #1f1f1f !important;
color: white !important;
} */
#main{
    max-width: 1900px;
    min-width: 980px;
    width: 99vw;
    padding: 0px 50px;
}
#headerContainer, .top_inner, .top_main {
	min-width: 1600px !important;
}
#quotes{
    max-width: 1800px;
    min-width: 980px;
    width: 93vw;
    position: relative;
}
table{
	border-radius: 10px;
    border-collapse: separate;
	max-width: 1800px;
    min-width: 1600px;
    width: 93vw;
    text-align: center;
    border-spacing: 0px 0px;
    margin-top: 10px;
    padding-top: 30px;
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
	margin-top: 30px;
}
#pagination a{
	text-decoration: none;
	color: black
}
#pagination button {
    margin: 0 2px;
    border: 1px solid #efefef;
    border-radius: 5px;
	background-color: unset;
	cursor: pointer;
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
#filter{
}
#statusTypes{
	display: flex;
	margin-top: 10px;
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
	/* margin-right: 5px;
	padding: 10px 20px; */
    border: 1px solid #346aff !important;
    border-radius: 5px !important;
    background-color: #f1f6ff !important;
}
#sort{
	margin-top: 15px;
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
.statusType > * {
  pointer-events: none; /* 자식 요소는 클릭 불가 */
}
#selectCount{
	display: inline-block;
    position: absolute;
	left: 5px;
}
#sizeSelect{
	display: inline-block;
    position: absolute;
    right: 0px;
}
</style>
</head>
<body>
<%@include file="master_header.jsp" %>

<div id="main">
	<div id="filter">
		<div id="statusTypes">
			<div class ="statusType" id="all"><div>전체</div><div id="allCount">0</div></div>
			<div class ="statusType selectType" id="received"><div>신청</div><div id="receivedCount">0</div></div>
			<div class ="statusType" id="in_progress"><div>처리중</div><div id="inprogressCount">0</div></div>
			<div class ="statusType" id="completed"><div>완료</div><div id="completedCount">0</div></div>
		</div>
		<div id="sort">
			<div id="dateSelects">
				<div style="display:inline-block;margin-right: 10px;">기간</div>
				<button class="dateButton selectType" id="all">전체</button>
				<button class="dateButton" id="today">오늘</button>
				<button class="dateButton" id="days7">지날 7일</button>
				<button class="dateButton" id="days30">지난 30일</button>
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
		</div>
	</div>
    <div id="quotes">
		<div id="selectCount">총 0건</div>
		<select id="sizeSelect" >
			<option value="10">10개씩 보기</option>
			<option value="20">20개씩 보기</option>
			<option value="30">30개씩 보기</option>
			<option value="40">40개씩 보기</option>
			<option value="50">50개씩 보기</option>
		</select>
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

<!--  페이지 로드 -->
<script>
let currentGroup = 1; // 현재 그룹
const buttonsPerGroup = 10; // 한 그룹당 몇개의 버튼
let quotesPerPage = 10; // 한 페이지당 사이즈
let quotesStatus = "RECEIVED"; // 조회할 상태값
let periodType = "ALL";

const statusDivs = document.querySelectorAll(".statusType"); //상태 태그들
const allCount = document.getElementById('allCount'); // 전체
const receivedCount = document.getElementById('receivedCount'); // 신청
const inprogressCount = document.getElementById('inprogressCount'); // 진행중
const completedCount = document.getElementById('completedCount'); //완료
const sizeSelect = document.getElementById("sizeSelect"); // 사이즈
const dateButtons = document.querySelectorAll(".dateButton"); //기간 태그들
const selectCount = document.getElementById("selectCount"); // 선택한 조건 견적서 갯수


document.addEventListener('DOMContentLoaded', async () => {
	// API 견적서 카운트
	const countMap = await getTotalQuotesCount();
    
    // 카운트 기입
    allCount.innerText = countMap.ALL; 
    receivedCount.innerText = countMap.RECEIVED;
    inprogressCount.innerText = countMap.IN_PROGRESS;
    completedCount.innerText = countMap.COMPLETED;
    
    // 상태 클릭시
	statusDivs.forEach(div => {
        div.addEventListener("click", statusClick);
      });
    // 기간 클릭시
	dateButtons.forEach(div => {
        div.addEventListener("click", dateClick);
      });
    // 사이즈 변동시
	sizeSelect.addEventListener("change", sizeChange);
    
	
    const totalCount = countMap.ALL;
    const totalPages = Math.ceil(totalCount / quotesPerPage); // 총 페이지 수
    
    createPaginationButtons(totalPages); // 동적html 페이지 번호 생성
    
    // API 견적서 리스트 
    loadQuotesPage(1,quotesPerPage,quotesStatus); // 첫 페이지 로드
});

// 카운트 api 함수
async function getTotalQuotesCount() {
    const response = await fetch('/estimate/getCountAll');
    const countMap = await response.json();
    return countMap;
}


// 버튼 생성 함수
function createPaginationButtons(totalCount) {
	
	const totalPages = Math.ceil(totalCount / quotesPerPage); // 총 페이지 수
	
	selectCount.innerText = "총 " + totalCount + "건";

    const pageNumberDiv = document.getElementById('pageNumberDiv');
    pageNumberDiv.innerHTML = '';

    const totalGroups = Math.ceil(totalPages / buttonsPerGroup); // 총 버튼 그룹 

    if (currentGroup > 1) { // 현재 버튼 그룹이 2 이상일때 "<" 버튼 생성
        const prevButton = document.createElement('button');
        prevButton.innerText = '<';
        prevButton.addEventListener('click', () => {
            currentGroup--;
            createPaginationButtons(totalPages);
            const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            loadQuotesPage(startPage,quotesPerPage,quotesStatus);
        });
        pageNumberDiv.appendChild(prevButton);
    }

    const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 예시: 1, 11, 21, 31 ...
    const endPage = Math.min(currentGroup * buttonsPerGroup, totalPages); // 좌우 둘 중 작은 값 반환

    for (let i = startPage; i <= endPage; i++) {
        const button = document.createElement('button');
        button.innerText = i;
        button.addEventListener('click', () => loadQuotesPage(i,quotesPerPage,quotesStatus));
        pageNumberDiv.appendChild(button);
    }

    if (currentGroup < totalGroups) { // 현재 버튼 그룹 다음 버튼 그룹이 있을때
        const nextButton = document.createElement('button');
        nextButton.innerText = '>';
        nextButton.addEventListener('click', () => {
            currentGroup++;
            createPaginationButtons(totalPages);
            const startPage = (currentGroup - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            loadQuotesPage(startPage,quotesPerPage,quotesStatus);
        });
        pageNumberDiv.appendChild(nextButton);
    }
}

function updatePagination(totalPages) {
    createPaginationButtons(totalPages);
}



function displayQuotes(list) {
	console.log(list);
    const quotesContainer = document.getElementById('quotes');
    const table = document.getElementById('table');
    const tbody = document.getElementById('tbody');

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

<!-- 견적서 요청 API -->
<script type="text/javascript">
async function loadQuotesPage(pageNumber,size,status,periodType,startDate,endDate,year,month,searchType,searchWords) {
    
	console.log(pageNumber +"번 페이지 로드 시작");
 	const tbody = document.querySelector("tbody"); // <tbody> 요소 선택
 	const pageNumberDiv = document.querySelector("#pageNumberDiv");
 	const buttons = pageNumberDiv.querySelectorAll("button");
	
 	// 현제 페이지 버튼 회색으로 변경
 	buttons.forEach(button => {
 	    if (button.innerText == pageNumber) { 
 	        button.style.color = "#bebebe";
 	    }else{
 	    	button.style.color = "black";
 	    }
 	});
 	
	tbody.replaceChildren();
	
	const estimateSearchRequest = {
		    status: status,               // enum 값
		    page: pageNumber,                     // 페이지 번호 (양수)
		    size: size,                    // 페이지 크기 (양수)
		    sortType: "DESC",            // enum 값
		    periodType: periodType || "ALL"      // enum 값 (e.g., MONTHLY, RANGE, etc.)
/*  		    startDate: "",             // 기간 시작일 (RANGE 타입일 경우 사용)
		    endDate: "",               // 기간 종료일 (RANGE 타입일 경우 사용) 
		    year: "2024",                // 기간 연도 (MONTHLY 타입일 경우 사용)
		    month: "12",                 // 기간 월 (MONTHLY 타입일 경우 사용)
		    searchType: "NAME",          // enum 값 (e.g., NAME, EMAIL, etc.)
		    searchWords: "John Doe"      // 검색어 */
		};
	
	try {
	    validateEstimateSearchRequest(estimateSearchRequest);
	    console.log("Validation successful!");
	} catch (error) {
	    alert("비정상 접근.");
	    console.error("Validation error:", error.message);
	}
	
    const queryString = new URLSearchParams(estimateSearchRequest).toString();
    console.log(queryString);
	
	try {
		const response = await fetch("/estimate/getAllEstimate?"+queryString, {
			method: "GET"
		});
        if (!response.ok) {
        	const message = await response.text(); // 서버에서 반환된 메시지 읽기
        	console.log(message);
            throw new Error("Network response was not ok: "+message);
        }
        const data = await response.json();
        if (!data.list || !Array.isArray(data.list)) {
            throw new Error('Invalid data format');
        }
		displayQuotes(data.list);
		/* const totalPages = Math.ceil(data.count / quotesPerPage); // 총 페이지 수 */
	    createPaginationButtons(data.count); // 동적html 페이지 번호 생성
	    /* selectCount.innerText = "총 " + data.count + "건"; */
    } catch (error) {
    	alert("로딩 실패.");
        console.error('Fetch error:', error);
    }

}
</script>

<!-- 상태 클릭시 -->
<script type="text/javascript">
function statusClick(event) {
  const clickedDiv = event.target;
  const id = clickedDiv.id; // 클릭한 div의 ID를 가져옴
  quotesStatus = id.toUpperCase();
  currentGroup = 1;
  loadQuotesPage(1,quotesPerPage,quotesStatus,periodType);
  
  // 모든 div의 클래스 초기화
  statusDivs.forEach(div => {
    div.className = "";
    div.className = "statusType";
  });
  // 클릭된 div만 클래스 설정
  clickedDiv.className = "";
  clickedDiv.className = "statusType selectType";
}
</script>

<!-- 기간 클릭시 -->
<script type="text/javascript">
function dateClick(event) {
	  const clickedDiv = event.target;
	  const id = clickedDiv.id; // 클릭한 div의 ID를 가져옴
	  periodType = id.toUpperCase();
	  console.log(periodType);
	  currentGroup = 1;
	  loadQuotesPage(1,quotesPerPage,quotesStatus,periodType);
	  /* async function loadQuotesPage(pageNumber,size,status,periodType,startDate,endDate,year,month,searchType,searchWords) */

	  
	  // 모든 div의 클래스 초기화
	  dateButtons.forEach(div => {
	    div.className = "";
	    div.className = "dateButton";
	  });
	  // 클릭된 div만 클래스 설정
	  clickedDiv.className = "";
	  clickedDiv.className = "dateButton selectType";
	}
</script>
<!-- 사이즈 변동시 -->
<script type="text/javascript">
function sizeChange(event) {
	quotesPerPage = event.target.value;
	currentGroup = 1;
	loadQuotesPage(1,quotesPerPage,quotesStatus,periodType); 
	}
</script>

<!-- validateEstimateSearchRequest 검증 함수 -->
<script type="text/javascript">
function validateEstimateSearchRequest(request) {
    // Pagination validation
    if (request.page < 1 || request.size < 10) {
        throw new Error("페이지는 1 이상, 사이즈는 10 이상 값이어야 합니다.");
    }

    // Period validation
    const today = new Date();
    const minDate = new Date("1950-01-01");
    if (request.periodType === "RANGE") {
        if (!request.startDate || !request.endDate) {
            throw new Error("기간 타입은 시작 날짜와 종료 날짜가 필요합니다.");
        }

        const startDate = new Date(request.startDate);
        const endDate = new Date(request.endDate);

        if (startDate < minDate || startDate > today) {
            throw new Error("시작 날짜는 1950년부터 현재 날짜 사이여야 합니다.");
        }
        if (endDate < minDate || endDate > today) {
            throw new Error("종료 날짜는 1950년부터 현재 날짜 사이여야 합니다.");
        }
        if (startDate > endDate) {
            throw new Error("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
    } else if (request.periodType === "MONTHLY") {
        if (!request.year || !request.month) {
            throw new Error("월 타입은 연도와 월이 필요합니다.");
        }

        const year = parseInt(request.year, 10);
        const month = parseInt(request.month, 10);

        if (isNaN(year) || year < 1950 || year > today.getFullYear()) {
            throw new Error("연도는 1950년부터 현재 연도 사이여야 합니다.");
        }
        if (isNaN(month) || month < 1 || month > 12) {
            throw new Error("월은 1부터 12 사이여야 합니다.");
        }

        const yearMonth = new Date(year, month, 0); // Last day of the given month
        if (yearMonth > today) {
            throw new Error("연도와 월은 현재 날짜 이전이어야 합니다.");
        }
    }

    // Search validation
    if (request.searchType && request.searchWords) {
        switch (request.searchType) {
            case "ADDRESS":
            case "NAME":
                if (!request.searchWords.trim()) {
                    throw new Error("검색어는 빈 문자열일 수 없습니다.");
                }
                break;
            case "EMAIL":
                const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
                if (!emailRegex.test(request.searchWords)) {
                    throw new Error("이메일 형식이 올바르지 않습니다.");
                }
                break;
            case "ESTIMATE_SEQ":
                if (!/^\d+$/.test(request.searchWords)) {
                    throw new Error("접수번호는 숫자만 포함해야 합니다.");
                }
                break;
            case "PHONE":
                const phoneRegex = /^\d{3,4}-\d{3,4}-\d{4}$/;
                if (!phoneRegex.test(request.searchWords)) {
                    throw new Error("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
                }
                break;
            default:
                throw new Error("유효하지 않은 검색 타입입니다: "+request.searchType);
        }
    }

    return true; // If all validations pass
}
</script>
</html>