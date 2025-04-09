<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- <%@ page import="dto.request.EstimateSearchRequest" %> --%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>견적서 목록</title>
<style type="text/css">
#main{
    max-width: 1800px;
    min-width: 980px;
    width: 94vw;
    padding: 0px 40px;
}
#headerContainer, .top_inner, .top_main {
	min-width: 1600px !important;
}
#quotes{
    max-width: 1800px;
    min-width: 980px;
    width: 94vw;
    position: relative;
}
#mainTable{
	border-radius: 10px;
    border-collapse: separate;
    max-width: 1800px;
    min-width: 1600px;
    width: 94vw;
    text-align: center;
    border-spacing: 0px 0px;
    margin-top: 15px;
    padding-top: 45px;
    table-layout: fixed;
}
#mainTable th{
    font-size: 14px;
	font-weight: bold;
	border-bottom: 2px solid black; /* 아래 밑줄 추가 */
	padding-bottom: 10px;
}
#mainTable td {
	padding: 5px;
   	border-bottom: 1px solid #efefef; 
    white-space: nowrap;        /* 한 줄로 표시 */
    overflow: hidden;           /* 넘치는 내용 숨김 */
    text-overflow: ellipsis;    /* 생략(...) 처리 */
}
.checkTd{
	width: 2.5%;
}
.seemoreTd{
	width: 2.5%;
	cursor: pointer;
}
.estimateSeqTd, .statusTd, .nameTd, .smsReceivedTd, .callReceivedTd{
    width: 5%;
}
.emailReceivedTd{
    width: 6%;
}
.phoneTd, .created_atTd{
	width: 10%;
}
.emailTd{
	width: 20%;
}
.addressTd{
	width: 25%;
} 
.seeMore {
    cursor: pointer;
    background: none;
    border: none;
    font-size: 16px;
}
#pagination{
    text-align: center;
    padding: 30px 0px;
	width: 94vw;
	max-width: 1800px;
	min-width: 1600px; 
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
    border: 1px solid #346aff !important;
    border-radius: 5px !important;
    background-color: #f1f6ff !important;
}
#conditionsSelects, #searchSelects{
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
#monthSelect,#yearSelect{
	border: 1px solid #efefef;
	border-radius: 5px; 
	padding: 5px 10px;
}
#startDate, #endDate, #sizeSelect, #rangeButton, #searchTypeSelect, #searchWordsInput, #searchButton{
	outline: none;
	border: 1px solid #efefef;
	border-radius: 5px; 
	padding: 5px 10px;
	cursor: pointer;
}
#searchWordsInput{
	cursor: text !important;
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
.searchItem{
	display: inline-block;
    padding: 5px 0px 5px 10px;
    font-size: 15px;
}
#emptyDiv{
	text-align: center;
    padding-top: 50px;
}
.searchItemCloseButton{
    margin-left: 5px;
    border: none;
    background-color: unset;
    cursor: pointer;
    position: relative;
    top: -5px;
}
.details-row {
    display: table-row;
}

.details-row.hidden, .loadingDiv.hidden {
    display: none; /* 숨김 */
}

.details-table {
    max-width: 1790px;
	min-width: 1600px;
    width: 93.5vw;
    border: 1px solid black;
    border-radius: 10px;
    padding: 5px;
    text-align: center;
    table-layout: fixed;
}
.selectCheckbox{
    transform: scale(1.4);
}
.commentInput{
    width: 90%;
    height: 100px;
    vertical-align: bottom;
}
.commentRegiButton{
	outline: none;
	border: 1px solid #efefef;
	border-radius: 5px; 
	padding: 10px 20px;
	cursor: pointer;
	vertical-align: bottom;
	margin-left: 5px;
}
.loading-spinner-container {
	width: 94vw;
	max-width: 1800px;
	min-width: 1600px;
    display: flex;
    flex-direction: column; /* 수직으로 배치 */
    align-items: center;
    justify-content: center;
    height: 140px; /* 원하는 높이 설정 */
    opacity: 0; /* 초기 투명도 */
    transform: translateY(-20px); /* 위로 약간 이동된 상태 */
    transition: opacity 0.5s ease, transform 0.5s ease; /* 부드러운 애니메이션 */
}

.loading-spinner-container.show {
    opacity: 1; /* 불투명 */
    transform: translateY(0); /* 원래 위치로 이동 */
}

.loading-spinner {
    display: inline-block;
    width: 40px; /* 스피너 크기 */
    height: 40px;
    border: 4px solid rgba(0, 0, 0, 0.1); /* 회색 테두리 */
    border-radius: 50%;
    border-top-color: #007bff; /* 파란색 회전 테두리 */
    animation: spin 1s linear infinite; /* 회전 애니메이션 */
}

@keyframes spin {
    to {
        transform: rotate(360deg); /* 360도 회전 */
    }
}

.loading-spinner-container span {
    margin-top: 10px;
    font-size: 14px;
    color: #555; /* 메시지 색상 */
}
.estimate_img{
	width: 140px;
	height: 140px;
}
.imgListTr{
	height: 146px;
	line-height: 0px;
}
.commentValueTd, .imageValueTd, .contentValueTd, .addressValueTd, .emailValueTd{
	text-align: left !important;
}
.commentValue{
    border: 1px solid #d2d2d2;
    border-radius: 5px;
    padding: 20px;
    margin-bottom: 5px;
    overflow-wrap: break-word;
    white-space: normal;
}

</style>
</head>
<body>
<%@include file="master_header.jsp" %>

<div id="main">
<!-- <div id="loadingSpinner"></div> -->
	<div id="filter">
		<div id="statusTypes">
			<div class ="statusType" id="all"><div>전체</div><div id="allCount">0</div></div>
			<div class ="statusType selectType" id="received"><div>신청</div><div id="receivedCount">0</div></div>
			<div class ="statusType" id="in_progress"><div>처리중</div><div id="inprogressCount">0</div></div>
			<div class ="statusType" id="completed"><div>완료</div><div id="completedCount">0</div></div>
		</div>
		<div id="conditionsSelects">
			<div id="dateSelects">
				<div style="display:inline-block;margin-right: 10px;">기간</div>
				<button class="dateButton" id="all">전체</button>
				<button class="dateButton" id="today">오늘</button>
				<button class="dateButton" id="days7">지날 7일</button>
				<button class="dateButton selectType" id="days30">지난 30일</button>
				<div style="display:inline-block;margin: 0px 10px; color:#efefef">|</div>
				<div style="display:inline-block;margin-right: 10px;">월별</div>
				<select class ="monthButton" id="yearSelect" >
				<option value="">년도</option>
				</select>
				<select class ="monthButton" id="monthSelect" >
					<option value="">월</option>
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
				<div style="display:inline-block;margin-right: 10px;">입력 기간</div>
				<input class="rangeInput" id="startDate" type="date">
				<div style="display:inline-block;margin: 0px 10px;">~</div>
				<input class="rangeInput" id="endDate"type="date">
				<button class="rangeButton" id="rangeButton">조회</button>
			</div>
			<div id="searchSelects">
			<div style="display:inline-block;margin-right: 10px;">검색</div>
				<select class="searchInput" id="searchTypeSelect" >
					<option value="">선택</option>
					<option value="estimate_Seq">접수번호</option>
					<option value="name">이름</option>
					<option value="phone">연락처</option>
					<option value="email">이메일</option>
					<option value="address">주소</option>
				</select>
				<input class="searchInput" id="searchWordsInput"type="text" maxlength="30">
				<button class="searchButton" id="searchButton">조회</button>
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
    	<table id="mainTable">
    		<thead id="thead">
    			<tr>
	   			<th class="checkTd"><input type="checkbox" class="selectCheckbox" id ="allCheck"></th>
	   			<th class="estimateSeqTd">접수번호</th>
	   			<th class="statusTd">상태</th>
	   			<th class="nameTd">이름</th>
	   			<th class="phoneTd">연락처</th>
	   			<th class="emailTd">이메일</th>
	   			<th class="emailReceivedTd">이메일 동의</th>
	   			<th class="smsReceivedTd">SMS 동의</th>
	   			<th class="callReceivedTd">전화 동의</th>
	   			<th class="addressTd">주소</th>
	   			<th class="created_atTd">생성일시</th>
	   			<th class="seemoreTd" id="allClose">접기</th>
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
<script type="text/javascript">
let buttonGroupField = 1; // 현재 그룹
const buttonsPerGroup = 10; // 한 그룹당 몇개의 버튼
let estimatesPerPageField = 10; // 한 페이지당 사이즈
let statusField = "RECEIVED"; // 조회할 상태값
let periodTypeField = "DAYS30";
let yearField = "";
let monthField = "";
let startDateField = "";
let endDateField = "";
let searchTypeField = "";
let searchWordsField ="";
let sortTypeField = "DESC";

//타입 변환 객체
const typeMapping = {
    ADDRESS: "주소",
    NAME: "이름",
    EMAIL: "이메일",
    ESTIMATE_SEQ: "접수번호",
    PHONE: "연락처"
};
const statusMapping = {
	    RECEIVED: "신청",
	    IN_PROGRESS: "처리중",
	    COMPLETED: "완료",
	    DELETE: "삭제"
	};

const startYear = 2024; // 최소 연도
const endYear = new Date().getFullYear(); // 최대 연도

const allCheck = document.getElementById("allCheck"); //전체 선택 태그
const statusDivs = document.querySelectorAll(".statusType"); //상태 태그들
const allCount = document.getElementById('allCount'); // 전체 카운트 태그
const receivedCount = document.getElementById('receivedCount'); // 신청
const inprogressCount = document.getElementById('inprogressCount'); // 진행중
const completedCount = document.getElementById('completedCount'); //완료
const sizeSelect = document.getElementById("sizeSelect"); // 사이즈
const dateButtons = document.querySelectorAll(".dateButton"); //기간 태그들
const monthButtons = document.querySelectorAll(".monthButton"); //년월 태그들
const rangeInputs = document.querySelectorAll(".rangeInput"); //입력기간 태그들
const rangeButton = document.getElementById("rangeButton"); //입력기간 조회 태그
const selectCount = document.getElementById("selectCount"); // 조건에 맞는 견적서 갯수 태그
const yearSelect = document.getElementById("yearSelect"); //연도 태그
const searchWordsInput = document.getElementById("searchWordsInput"); //검색 input 태그
const searchButton = document.getElementById("searchButton"); //검색 태그
const allClose = document.getElementById("allClose"); //전부 닫기 태그
const tbody = document.getElementById("tbody");


document.addEventListener('DOMContentLoaded', async () => 
{
	// 연도 옵션 생성
    for (let i = startYear; i <= endYear; i++) {
        const option = document.createElement("option");
        option.value = i;
        option.textContent = i;
        yearSelect.appendChild(option);
      }
    
    allCheck.addEventListener("click", rowCheckToggle); // 전체 선택 클릭시
    addEventListeners(statusDivs, "click", statusClick); // 상태 클릭시
    addEventListeners(dateButtons, "click", dateClick); // 기간 클릭시
    addEventListeners(monthButtons, "change", monthChange); // 월별 변동시
	rangeButton.addEventListener("click", rangeClick); // 입력 기간 조회 클릭시
	sizeSelect.addEventListener("change", sizeChange); // 사이즈 변동시
	searchButton.addEventListener("click", searchClick); // 검색 클릭시
	allClose.addEventListener("click", allSeemoreCloseClick); // 검색어 조회시
	searchWordsInput.addEventListener("keydown", (event) => {
	    if (event.key === "Enter") { // Enter 키 감지
	        event.preventDefault(); // 기본 동작 방지
	        searchClick(); // 검색 함수 호출
	    }
	});
	
	const countMap = await getTotalQuotesCount();
    innerTextTotalQuotesCount(countMap);
   
    //const totalCount = countMap.ALL;
    //const totalPages = Math.ceil(totalCount / estimatesPerPageField); // 총 페이지 수
    
	try{
		await estimatePageController(1);
	}catch(error){
		alert(error.message);
		return;
	}
});

</script>
<!-- 견적서 리스트 렌더링 함수 -->
<script type="text/javascript">
function displayQuotes(list) {
	tbody.replaceChildren();

    // 테이블 본문 설정
    list.forEach(estimate => {
        const row = document.createElement('tr');
        row.id = estimate.estimateSeq;
        
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
        const inputTd = document.createElement('td');
        const input = document.createElement('input');
        input.type="checkbox";
        input.classList.add("selectCheckbox");
        input.addEventListener('change',() => rowCheckToggle(event));
        input.id=estimate.estimateSeq+"checkbox";
        inputTd.appendChild(input);
        row.appendChild(inputTd);
        
        rowData.forEach((data,index) => {
            const td = document.createElement('td');
            td.innerText = data;
            row.appendChild(td);
        });

        // 펼칠 수 있는 버튼 추가
        const buttonTd = document.createElement('td');
        const button = document.createElement('button');
        button.classList.add("seeMore");
        button.id = estimate.estimateSeq;
        button.innerHTML = "&#9660;"; // 아래 화살표
        button.addEventListener('click', async() => toggleDetails(row, estimate.estimateSeq));
        buttonTd.appendChild(button);
        row.appendChild(buttonTd);

        tbody.appendChild(row);
    });

}
</script>
<!-- 견적서 상세 페이지 렌더링 함수 -->
<script type="text/javascript">
function renderingEstimateDetail(data){
    console.log("renderingEstimateDetail 시작 : " + new Date().toISOString());

	const estimateSeq = data.estimateSeq;
	const estimateSeqTd = document.getElementById(estimateSeq+"EstimateSeq");
	const statusTd = document.getElementById(estimateSeq+"Status");
	const createdAtTd = document.getElementById(estimateSeq+"CreatedAt");
	const updatedAtTd = document.getElementById(estimateSeq+"UpdatedAt");
	const nameTd = document.getElementById(estimateSeq+"Name");
	const phoneTd = document.getElementById(estimateSeq+"Phone");
	const emailTd = document.getElementById(estimateSeq+"Email");
	const emailAgreeTd = document.getElementById(estimateSeq+"EmailAgree");
	const smsAgreeTd = document.getElementById(estimateSeq+"SmsAgree");
	const callAgreeTd = document.getElementById(estimateSeq+"CallAgree");
	const addressTd = document.getElementById(estimateSeq+"Address");
	const contentTd = document.getElementById(estimateSeq+"Content");
	
    statusTd.innerText=statusMapping[data.status];
    createdAtTd.innerText=formatDate(data.createdAt);
    updatedAtTd.innerText=formatDate(data.updatedAt);
    nameTd.innerText=data.name ? data.name : "";
    phoneTd.innerText=data.phone;
    emailTd.innerText=data.email;
    emailAgreeTd.innerText=data.emailAgree ? "수락" : "거부";
    smsAgreeTd.innerText=data.smsAgree ? "수락" : "거부";
    callAgreeTd.innerText=data.callAgree ? "수락" : "거부";
    addressTd.innerText="("+data.postcode+") "+data.mainAddress+" "+data.detailAddress ? data.detailAddress : "";
    contentTd.innerText=data.content ? data.content : "";
    console.log("renderingEstimateDetail 완료 : " + new Date().toISOString());
}

//이미지 렌더링
function renderingEstimateImage(estimateSeq,imageList){
    console.log("renderingEstimateImage 시작 : " + new Date().toISOString());

    const mimeType = "image/png";
    const imgListTd = document.getElementById(estimateSeq+"ImgList");
    imgListTd.replaceChildren();

    if(!imageList){return;}
    
    imageList.forEach((image,index)=>{
	    const img = document.createElement('img');
	    img.classList.add("estimate_img");
	    const base64Image = `data:\${mimeType};base64,\${image}`;
	    img.src = base64Image;
	    imgListTd.appendChild(img);
    });
    console.log("renderingEstimateImage 완료 : " + new Date().toISOString());
}
//답글 렌더링
function renderingEstimateComment(estimateSeq,commentList){
    console.log("renderingEstimateComment 시작 : " + new Date().toISOString());
    console.log(commentList);
    const commentListTd = document.getElementById(estimateSeq+"Comment");
    commentListTd.replaceChildren();
    
    // 댓글 입력 영역 추가 (공통)
    const appendInputArea = () => {
        const div = document.createElement('div');
        const commentInput = document.createElement('textarea');
        commentInput.classList.add("commentInput");
        const commentRegiButton = document.createElement('button');
        commentRegiButton.classList.add("commentRegiButton");
        commentRegiButton.innerText = "등록";
        div.appendChild(commentInput);
        div.appendChild(commentRegiButton);
        commentListTd.appendChild(div);
    };
    
    if(!commentList){
    	appendInputArea();
    	return;
    }
    
    commentList.forEach((comment,index)=>{
        const div = document.createElement('div');
        div.classList.add("commentValue");
        div.id=estimateSeq+"Comment";
        const p1 = document.createElement('p');
        p1.innerText=comment.createdAt;
        const p2 = document.createElement('p');
        p2.innerText=comment.commentText;
        div.appendChild(p1);
        div.appendChild(p2);
        commentListTd.appendChild(div);
    });

    appendInputArea();
    
    console.log("renderingEstimateComment 완료 : " + new Date().toISOString());
}
</script>

<!-- 버튼 생성 렌더링 함수 -->
<script type="text/javascript">
function createPaginationButtons(totalCount,page) {
	selectCount.innerText = "총 " + totalCount + "건"; // 카운트 출력
	
	const totalPages = Math.ceil(totalCount / estimatesPerPageField); // 총 페이지 수

    const pageNumberDiv = document.getElementById('pageNumberDiv');
    pageNumberDiv.innerHTML = '';

    const totalGroups = Math.ceil(totalPages / buttonsPerGroup); // 총 버튼 그룹 

    if (buttonGroupField > 1) { // 현재 버튼 그룹이 2 이상일때 "<" 버튼 생성
        const prevButton = document.createElement('button');
        prevButton.innerText = '<';
        prevButton.addEventListener('click', async() => {
            buttonGroupField--;
            const startPage = (buttonGroupField - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            try {
                await estimatePageController(startPage);
            } catch (error) {
            	alert(error.message);
            	consoel.log(error.message);
            }
        });
        pageNumberDiv.appendChild(prevButton);
    }

    const startPage = (buttonGroupField - 1) * buttonsPerGroup + 1; // 예시: 1, 11, 21, 31 ...
    const endPage = Math.min(buttonGroupField * buttonsPerGroup, totalPages); // 좌우 둘 중 작은 값 반환

    for (let i = startPage; i <= endPage; i++) {
        const button = document.createElement('button');
        button.innerText = i;
        button.addEventListener('click', async () => {
            try {
                await estimatePageController(i); // 비동기 함수 호출
            } catch (error) {
                alert(error.message);
                consoel.log(error.message);
            }
        });
        if(i==page){
        	button.style.color = "#bebebe";
        }
        pageNumberDiv.appendChild(button);
    }

    if (buttonGroupField < totalGroups) { // 현재 버튼 그룹 다음 버튼 그룹이 있을때
        const nextButton = document.createElement('button');
        nextButton.innerText = '>';
        nextButton.addEventListener('click', async() => {
            buttonGroupField++;
            const startPage = (buttonGroupField - 1) * buttonsPerGroup + 1; // 새 그룹에 맞게 계산
            try {
                await estimatePageController(startPage);
            } catch (error) {
            	alert(error.message);
            	consoel.log(error.message);
            }
        });
        pageNumberDiv.appendChild(nextButton);
    }
}
</script>
<!-- 견적서 페이지 리스트 컨트롤러 -->
<script type="text/javascript">
async function estimatePageController(pageNumber) {
	
	const estimateSearchRequest = {
		    status: statusField,
		    page: pageNumber,
		    size: estimatesPerPageField,                
		    sortType: sortTypeField,
		    periodType: periodTypeField,
		    ...(periodTypeField == "RANGE" && { startDate:startDateField }),
		    ...(periodTypeField == "RANGE" && { endDate:endDateField }),
		    ...(periodTypeField == "MONTHLY" && { year:yearField }),
		    ...(periodTypeField == "MONTHLY" && { month:monthField }),
		    ...(searchWordsField != "" && { searchType:searchTypeField }),
		    ...(searchWordsField != "" && { searchWords:searchWordsField })
	  };
	
	try{
    	validateEstimateSearchRequest(estimateSearchRequest);
	}catch(error){
		throw error;
	}
	
    // 로딩 상태 추가
	const loadingRow = document.createElement('tr');
    const loadingTd = document.createElement('td');
    loadingTd.colSpan = 12; // 테이블 열 수에 맞게 조정
    loadingTd.style.textAlign = "center";
    const loadingSpinner = document.createElement('div');
    loadingSpinner.className = "loading-spinner-container";
    loadingSpinner.innerHTML = `
        <div class="loading-spinner"></div>
        <span>로딩 중...</span>
    `;

	loadingTd.appendChild(loadingSpinner);
	loadingRow.appendChild(loadingTd);
	tbody.appendChild(loadingRow);
	
	// 부드러운 등장 효과 적용
	setTimeout(() => {
	    loadingSpinner.classList.add('show'); // 애니메이션 시작
	}, 300); 
	
	
    const queryString = new URLSearchParams(estimateSearchRequest).toString();
    try{
    	const data = await getEstimateListByEstimateSearchRequest(queryString);
        const emptyDiv = document.getElementById("emptyDiv"); // 이전 내용을 지우기
        if (emptyDiv) {
            emptyDiv.remove();
        }
        createPaginationButtons(data.count,estimateSearchRequest.page); // 동적html 페이지 번호 생성
    	displayQuotes(data.list); // 페이지 리스트 렌더링
    }catch(error){
        tbody.replaceChildren(); // 로딩 상태 제거

        const td = document.createElement('td');
        td.colSpan = 12; 
        td.style.textAlign = "center"; 

        const emptyDiv = document.createElement('div'); 
        emptyDiv.id = "emptyDiv";
        emptyDiv.innerText = error.message; 

        td.appendChild(emptyDiv); 
        const errorRow = document.createElement('tr');
        errorRow.appendChild(td); 
        tbody.appendChild(errorRow); 

        throw error; 
    }
}
</script>

<!-- 견적서 상세 페이지 토글 클릭시 -->
<script type="text/javascript">
async function toggleDetails(row, estimateSeq) {
    console.log("toggleDetails 시작 : " + new Date().toISOString());

    const nextRow = row.nextElementSibling;

    if (nextRow && nextRow.classList.contains('details-row')) {
        nextRow.classList.toggle('hidden');
    } else {
        const detailsRow = document.createElement('tr');
        detailsRow.classList.add('details-row');

        const detailsTd = document.createElement('td');
        detailsTd.colSpan = 12; // 전체 열 차지
        detailsTd.innerHTML = `
            <table class="details-table">
                <tbody>
                    <tr>
                        <td>접수번호</td>
                        <td id=\${estimateSeq}EstimateSeq>\${estimateSeq}</td>
                        <td>상태</td>
                        <td id=\${estimateSeq}Status></td>
                        <td>생성일시</td>
                        <td id=\${estimateSeq}CreatedAt></td>
                        <td>수정 일시</td>
                        <td id=\${estimateSeq}UpdatedAt></td>
                    </tr>
                    <tr>
                        <td>이름</td>
                        <td id=\${estimateSeq}Name></td>
                        <td>연락처</td>
                        <td id=\${estimateSeq}Phone></td>
                        <td>이메일</td>
                        <td id=\${estimateSeq}Email class='emailValueTd' colspan='3'></td>
                    </tr>
                    <tr>
                        <td>이메일 동의</td>
                        <td id=\${estimateSeq}EmailAgree></td>
                        <td>SMS 동의</td>
                        <td id=\${estimateSeq}SmsAgree></td>
                        <td>전화 동의</td>
                        <td id=\${estimateSeq}CallAgree></td>
                        <td></td>
                        <td></td>
                    </tr>
                    <tr>
	                    <td>주소</td>
	                    <td id=\${estimateSeq}Address class='addressValueTd' colspan='7'></td>
                	</tr>
                	<tr>
	                    <td>내용</td>
	                    <td id=\${estimateSeq}Content class='contentValueTd' colspan='7'></td>
                	</tr>
                	<tr class="imgListTr">
	                    <td>이미지</td>
	                    <td id=\${estimateSeq}ImgList class='imageValueTd' colspan='7'>
	                    <div class="loading-spinner-container show">
	        				<div class="loading-spinner"></div>
	        	            <span>로딩 중...</span>
    					</div>
	                    </td>
            		</tr>
                	<tr>
    	                <td>답글</td>
	        			<td id=\${estimateSeq}Comment class='commentValueTd' colspan='7'>
	        				<div class="loading-spinner-container show">
		        				<div class="loading-spinner"></div>
		        	            <span>로딩 중...</span>
	        				</div>
	        			</td>
	        		</tr>
                </tbody>
            </table>
        `;

        detailsRow.appendChild(detailsTd);
        row.after(detailsRow);

        try{
        	const data = await getEstimateTextByEstimateSeq(estimateSeq);
        	renderingEstimateDetail(data);
        	const commentList = await getCommentListByEstimateSeq(estimateSeq);
        	renderingEstimateComment(estimateSeq,commentList);
        	const imageList = await getEstimateImagesByEstimateSeq(estimateSeq);
        	renderingEstimateImage(estimateSeq,imageList);
        }catch(error){
        	alert(error.message);
        }
    }
    console.log("toggleDetails 종료 : " + new Date().toISOString());

}
</script>
<!-- 상태 클릭시 -->
<script type="text/javascript">
async function statusClick(event) {
  const clickedDiv = event.target;
  const id = clickedDiv.id; // 클릭한 div의 ID를 가져옴
  const oldStatusField = statusField;
  const oldButtonGroupField = buttonGroupField;
  
  //필드 값 설정
  statusField = id.toUpperCase();
  buttonGroupField = 1;
  
  try{
	await estimatePageController(1);
	
	// 클릭된 div만 클래스 설정
	statusDivs.forEach(div => div.classList.remove("selectType"));
	clickedDiv.classList.add("selectType");

  }catch(error){
	// 필드 값 복구
	statusField = oldStatusField;
	buttonGroupField = oldButtonGroupField;
	
	alert(error.message);
	return;
  }
}
</script>
<!-- 기간(전체,오늘,7일,30일) 클릭시 --><script type="text/javascript">
async function dateClick(event) {
	const clickedDiv = event.target;
	const oldPeriodTypeField = periodTypeField;
	const oldButtonGroupField = buttonGroupField;
	  
	  //필드 값 설정
	  periodTypeField = clickedDiv.id.toUpperCase();
	  buttonGroupField = 1;
	 
	try{
		  await estimatePageController(1);
		  
	      resetStyles(dateButtons, "selectType");
	      resetStyles(monthButtons, "selectType");
	      resetStyles(rangeInputs, "selectType");
	      clickedDiv.classList.add("selectType");
      
	}catch(error){
		// 필드값 복구
		
		periodTypeField = oldPeriodTypeField;
		buttonGroupField = oldButtonGroupField;
	
		alert(error.message);
	 }
	  
}
</script>

<!-- 기간(월별) 클릭시 -->
<script type="text/javascript">
async function monthChange(event) {
	
	//필드 값 설정1
	yearField = document.getElementById("yearSelect").value;
	monthField = document.getElementById("monthSelect").value;

	if(!yearField || !monthField || isNaN(yearField) || isNaN(monthField)){
		yearField="";
		monthField="";
		return;
	}

	 //필드 값 설정2
	 periodTypeField = "MONTHLY";
	 buttonGroupField = 1;
	 
	 try{
	 	await estimatePageController(1);
	 	
		resetStyles(dateButtons, "selectType");
		resetStyles(rangeInputs, "selectType");
		 monthButtons.forEach(div => {
		  div.classList.add("selectType");
		 });
		 
	 }catch(error){
		//필드 값 복구
		yearField="";
		monthField="";
		alert(error.message);
	 }
}
</script>
<!-- 기간(범위) 클릭시 -->
<script type="text/javascript">
async function rangeClick(event) {
	
	//필드 값 설정
	startDateField = document.getElementById("startDate").value;
	endDateField = document.getElementById("endDate").value;
	periodTypeField = "RANGE";
	buttonGroupField = 1;
	
	try{
		await estimatePageController(1);
		resetStyles(dateButtons, "selectType");
		resetStyles(monthButtons, "selectType");
		rangeInputs.forEach(div => {
		 div.classList.add("selectType");
		});
	}catch(error){
		//필드 값 복구
		startDateField = "";
		endDateField = "";
		alert(error.message);
	}
}
</script>
<!-- 검색 클릭시 -->
<script type="text/javascript">
async function searchClick(event) {
	const searchTypeSelect = document.getElementById("searchTypeSelect");
	const searchWordsInput = document.getElementById("searchWordsInput");

	//필드 값 설정
	searchTypeField = searchTypeSelect.value.toUpperCase();
	searchWordsField = searchWordsInput.value;
	buttonGroupField = 1;
	
	if(!searchTypeField || !searchWordsField ){
		searchTypeField="";
		searchWordsField="";
		alert("검색어를 선택 및 입력해 주세요.");
		return;
	}
	
	//검색창 비우기
	searchWordsInput.value = "";
	
	try{
		await estimatePageController(1);
		createSearchItemDiv();
	}catch(error){
		searchTypeField="";
		searchWordsField="";
		alert(error.message);
	}
}

function createSearchItemDiv() {
	 const oldSearchItem = document.querySelector(".searchItem");
     if(oldSearchItem) {
    	 oldSearchItem.remove();
     }
     
    const searchSelects = document.getElementById("searchSelects"); // div 컨테이너
    const searchItem = document.createElement("div"); // 새로운 div 생성
    searchItem.classList.add("searchItem"); 
    searchItem.classList.add("selectType");
    
    const displayType = typeMapping[searchTypeField] || searchTypeField; // 변환 실패 시 원래 값을 사용

    // 텍스트 노드 추가
    const textNode = document.createTextNode(displayType + " : " + searchWordsField);
    searchItem.appendChild(textNode);

    // x 버튼 생성
    const searchItemCloseButton = document.createElement("button");
    searchItemCloseButton.className = "searchItemCloseButton";
    searchItemCloseButton.textContent = "x";

    // x 버튼 클릭 이벤트 추가
    searchItemCloseButton.addEventListener("click", function () {
    	deleteSearchItemDiv(searchItem); // B 함수 호출 (div를 삭제)
    });

    // x 버튼을 div에 추가
    searchItem.appendChild(searchItemCloseButton);

    // 생성된 div를 컨테이너에 추가
    searchSelects.appendChild(searchItem);
}
function deleteSearchItemDiv(targetDiv) {
    // div 삭제
    targetDiv.remove();

    // 필드 변수 초기화
    searchTypeField = "";
    searchWordsField = "";
    estimatePageController(1);
}
</script>

<!-- 사이즈 변동시 -->
<script type="text/javascript">
function sizeChange(event) {
	estimatesPerPageField = event.target.value;
	buttonGroupField = 1;
	estimatePageController(1);
}
</script>
<!-- 선택 체크 클릭시 -->
<script type="text/javascript">
function rowCheckToggle(event) {
    const id = event.target.id; // 이벤트가 발생한 요소의 id 가져오기
    const result = event.target.checked; // 이벤트가 발생한 요소의 id 가져오기
    const backgroundColor = result ? "#f1f6ff" : "unset";
    
    if (id === "allCheck") { // id가 "allCheck"인 경우 (전체 선택)
        const presentTbody = document.getElementById("tbody"); // tbody 요소 가져오기
        const allTr = presentTbody.querySelectorAll("tr"); // tbody 내 모든 tr 요소 가져오기
        const selectCheckbox = presentTbody.querySelectorAll(".selectCheckbox"); // tbody 내 모든 tr 요소 가져오기

        // 각 tr 요소의 배경색 변경
        allTr.forEach(row => {row.style.backgroundColor = backgroundColor;});
        // 각 checkbox 변경
        selectCheckbox.forEach(checkbox => {checkbox.checked=result;});
    } else { // 개별 체크박스일 경우
        const estimateSeq = id.replace("checkbox", ""); // id에서 "checkbox" 제거하여 행 ID 추출
        const row = document.getElementById(estimateSeq); // 해당 행 요소 가져오기
        row.style.backgroundColor = backgroundColor; // 해당 행의 배경색 변경
    }
}
</script>

<!-- 접기 클릭시 -->
<script type="text/javascript">
function allSeemoreCloseClick() {
	const detailsRow = document.querySelectorAll('.details-row');
	detailsRow.forEach((row)=>{row.classList.add('hidden');});
}
</script>


<!-- validateEstimateSearchRequest 검증 함수 -->
<script type="text/javascript">
function validateEstimateSearchRequest(request) {
	
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

        const yearMonth = new Date(year, month-1, 1); // 해당 년도, 해당 월, 1일
        console.log(yearMonth + " / " + today);
        if (yearMonth > today) {
            throw new Error("연도와 월이 현재보다 이후 일 수 없습니다.");
        }
    }

    // Search validation
    if (request.searchType && request.searchWords) {
        request.searchWords = request.searchWords.trim();
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
<script type="text/javascript">
/* 현재 모든 상태 카운트 기입 함수 */
function innerTextTotalQuotesCount(countMap) {
    allCount.innerText = countMap.ALL; 
    receivedCount.innerText = countMap.RECEIVED;
    inprogressCount.innerText = countMap.IN_PROGRESS;
    completedCount.innerText = countMap.COMPLETED;
}
/* 공통 이벤트 등록 함수 */
function addEventListeners(elements, eventType, handler) {
    elements.forEach(element => {
        element.addEventListener(eventType, handler);
    });
}
/* 공통 초기화 함수 */
function resetStyles(elements, className) {
    elements.forEach(element => {
        element.classList.remove(className);
        if (element.tagName === "INPUT" || element.tagName === "SELECT") {
            element.value = ""; // 입력값 초기화
        }
    });
}
/* 날짜 형식 변환 함수 */
function formatDate(dateArray) {
    if (!Array.isArray(dateArray) || dateArray.length < 3) return 'Invalid Date';

    const [year, month, day, hour = 0, minute = 0] = dateArray;
    const date = new Date(year, month - 1, day, hour, minute);

    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).replace(/\. /g, '.').replace(/, /g, ' ');
}
</script>
<!-- 전체 카운트 요청 API -->
<script type="text/javascript">
async function getTotalQuotesCount() {
    const response = await fetch('/estimate/getCountAll');
    const countMap = await response.json();
    return countMap;
}
</script>
<!-- 견적서 요청 API -->
<script type="text/javascript">
async function getEstimateListByEstimateSearchRequest(estimateSearchRequest) {
	
	const response = await fetch("/estimate/getEstimateListByEstimateSearchRequest?"+estimateSearchRequest, {method: "GET"});
	
	if (response.status === 404) {
	    const message = await response.text();
	    console.log(message); 
	    throw new Error(message);
	} else if (!response.ok) {
	    const message = await response.text(); 
	    console.log(message);
	    throw new Error("서버 접속 실패");
	}
    
    const data = await response.json();
    console.log(data);
	return data;
}
</script>
<!-- 상세 견적서 요청 API -->
<script type="text/javascript">
async function getEstimateTextByEstimateSeq(estimateSeq){
    console.log("getEstimateTextByEstimateSeq 시작 : " + new Date().toISOString());
    
	const response = await fetch("/estimate/getEstimateTextByEstimateSeq?estimate_seq="+estimateSeq);
	
	if (response.status === 404) {
	    const message = await response.text();
	    console.log(message); 
	    throw new Error(message);
	} else if (!response.ok) {
	    const message = await response.text(); 
	    console.log(message);
	    throw new Error("서버 접속 실패");
	}
    
    const data = await response.json();
    
    if (data) {
    	console.log(data);
	    console.log("getEstimateTextByEstimateSeq 종료 : " + new Date().toISOString());
    	return data;
    }
}
</script>
<!-- 이미지 요청 API -->
<script type="text/javascript">
async function getEstimateImagesByEstimateSeq(estimateSeq){
    console.log("getEstimateImagesByEstimateSeq 시작 : " + new Date().toISOString());
    
	const response = await fetch("/estimate/getEstimateImagesByEstimateSeq?estimateSeq="+estimateSeq);
	
	if (response.status === 204) {
	    const message = await response.text();
	    console.log(message); 
	    return;
	} else if (!response.ok) {
	    const message = await response.text(); 
	    console.log(message);
	    throw new Error("서버 접속 실패");
	}
    
    const data = await response.json();
    
    if (data) {
    	console.log(data);
	    console.log("getEstimateImagesByEstimateSeq 종료 : " + new Date().toISOString());
    	return data;
    }
}
</script>
<!-- 답글 요청 API -->
<script type="text/javascript">
async function getCommentListByEstimateSeq(estimateSeq){
    console.log("getCommentListByEstimateSeq 시작 : " + new Date().toISOString());
    
	const response = await fetch("/comment/getCommentListByEstimateSeq?estimateSeq="+estimateSeq);
	
	if (response.status === 204) {
	    const message = await response.text();
	    console.log(message); 
	    return;
	} else if (!response.ok) {
	    const message = await response.text(); 
	    console.log(message);
	    throw new Error("서버 접속 실패");
	}
    
    const data = await response.json();
    
    if (data) {
    	console.log(data);
	    console.log("getCommentListByEstimateSeq 종료 : " + new Date().toISOString());
    	return data;
    }
}
</script>
</html>