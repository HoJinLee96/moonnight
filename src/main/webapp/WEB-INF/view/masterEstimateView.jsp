<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>견적서 목록</title>
<style type="text/css">
#pagination button {
    margin: 0 2px;
}
#quotes{

}
table{
margin: 0px auto;
border: 1px solid #efefef;
border-radius: 10px;
border-spacing: 0 10px; /* 행 사이 공간 추가 */
}
th{
margin-bottom: 10px;
font-weight: bold;
border-bottom: 2px solid #000; /* 아래 밑줄 추가 */
padding: 10px; /* 여백 추가 */
}
td {
    padding: 10px; /* 여백 추가 */
}
button {
    cursor: pointer;
    background: none;
    border: none;
    font-size: 16px;
}
</style>
</head>
<body>
<%@include file="master_header.jsp" %>

<div id="main">
    <div id="quotes"></div>
    <div id="pagination"></div>

</div>

</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
let currentGroup = 1;
const buttonsPerGroup = 10;


document.addEventListener('DOMContentLoaded', async () => {
	console.log("시작");
    const totalCount = await getTotalQuotesCount();
    const quotesPerPage = 50;
    const totalPages = calculateTotalPages(totalCount, quotesPerPage);
    createPaginationButtons(totalPages);
});

async function getTotalQuotesCount() {
	console.log("getTotalQuotesCount 시작");
    const response = await fetch('/estimate/getCountAll');
    const data = await response.json();
    return data.totalCount;
}

function calculateTotalPages(totalCount, quotesPerPage) {
    return Math.ceil(totalCount / quotesPerPage);
}

function createPaginationButtons(totalPages) {
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = '';

    const totalGroups = Math.ceil(totalPages / buttonsPerGroup);

    if (currentGroup > 1) {
        const prevButton = document.createElement('button');
        prevButton.innerText = '<';
        prevButton.addEventListener('click', () => {
            currentGroup--;
            updatePagination(totalPages);
        });
        paginationContainer.appendChild(prevButton);
    }

    const startPage = (currentGroup - 1) * buttonsPerGroup + 1;
    const endPage = Math.min(currentGroup * buttonsPerGroup, totalPages);

    for (let i = startPage; i <= endPage; i++) {
        const button = document.createElement('button');
        button.innerText = i;
        button.addEventListener('click', () => loadQuotesPage(i));
        paginationContainer.appendChild(button);
    }

    if (currentGroup < totalGroups) {
        const nextButton = document.createElement('button');
        nextButton.innerText = '>';
        nextButton.addEventListener('click', () => {
            currentGroup++;
            updatePagination(totalPages);
        });
        paginationContainer.appendChild(nextButton);
    }
}

function updatePagination(totalPages) {
    createPaginationButtons(totalPages);
}

async function loadQuotesPage(pageNumber) {
	try {
		const response = await fetch('/estimate/getAllEstimate?page=' + pageNumber + '&size=50');
        if (!response.ok) {
            throw new Error(`Network response was not ok: ${response.statusText}`);
        }
        const data = await response.json();
        if (!data.list || !Array.isArray(data.list)) {
            throw new Error('Invalid data format');
        }
        displayQuotes(data.list);
    } catch (error) {
        console.error('Fetch error:', error);
    }
}
function displayQuotes(list) {
    const quotesContainer = document.getElementById('quotes');
    quotesContainer.innerHTML = '';

    const table = document.createElement('table');
    const thead = document.createElement('thead');
    const tbody = document.createElement('tbody');

    // 테이블 헤더 설정
    const headers = ["접수번호","상태","이름", "연락처", "이메일", "이메일 동의", "SMS 동의", "전화 동의", "우편번호", "주소", "상세 주소", "생성일시", ""];
    const headerRow = document.createElement('tr');
    
    headers.forEach(headerText => {
        const th = document.createElement('th');
        th.innerText = headerText;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);

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

    // 테이블 본문 설정
    list.forEach(estimate => {
        const row = document.createElement('tr');
	console.log(estimate.createdAt);
        const rowData = [
        	estimate.estimateSeq,
        	estimate.status,
            estimate.name,
            estimate.phone,
            estimate.email,
            estimate.emailAgree ? "예" : "아니오",
            estimate.smsAgree ? "예" : "아니오",
            estimate.callAgree ? "예" : "아니오",
            estimate.postcode,
            estimate.mainAddress,
            estimate.detailAddress,
            formatDate(estimate.createdAt)
        ];

        rowData.forEach(data => {
            const td = document.createElement('td');
            td.innerText = data;
            row.appendChild(td);
        });

        // 펼칠 수 있는 버튼 추가
        const buttonTd = document.createElement('td');
        const button = document.createElement('button');
        button.innerHTML = "&#9660;"; // 아래 화살표
        buttonTd.appendChild(button);
        row.appendChild(buttonTd);

        tbody.appendChild(row);
    });

    table.appendChild(thead);
    table.appendChild(tbody);
    quotesContainer.appendChild(table);
}
document.addEventListener('DOMContentLoaded', () => {
    loadQuotesPage(1); // 첫 페이지 로드
});
    </script>
</html>