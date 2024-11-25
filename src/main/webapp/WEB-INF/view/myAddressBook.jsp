<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>주소록</title>
<style type="text/css">
p{
	margin: 0px;
}
.contentTitle{
	padding-bottom : 15px;
	border-bottom: 4px solid #20367a;
}
.address{
	display: flex;
	border: 2px solid #afafaf;
	border-radius: 10px;
	padding: 15px 15px;
	margin-top: 10px;
}
.sortedAddress{
	width: 100vw;
    min-width: 615px;
    max-width: 899px;
}
.buttonDiv{
	line-height: 42px;
}
.buttonDiv button{
	width: 47px;
	color: #20367a;
	background: white;
	border: 1px solid #20367a;
	border-radius: 10px;
	cursor: pointer;
	padding: 5px 10px;
}
.buttonDiv button:hover{
	background: #20367a;
	border: 1px solid white;
	color: white;
}
.addressNickname{
    min-width: 615px;
    max-width: 799px;
}
.sortedAddressText{
    min-width: 615px;
    max-width: 799px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
</style>

<!--오버레이-->
<style type="text/css">
.overlay {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: none;
	justify-content: center;
	align-items: center;
	z-index: 1000;
}

.popContent {
	width: 500px;
	height: 650px;
	background-color: white;
	padding: 40px 30px;
	border-radius: 10px;
	box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
	margin: 0px auto;
	margin-top: 150px;
	position: relative;
}

.closeBtn {
	position:absolute;
	top:5px;
	left:15px;
	color:#aaa;
	font-size:34px;
	font-weight:bold;
	margin-bottom: 10px;
	cursor: pointer;
}
.close:hover {
	color:#20367a;
}
.name, .postcode, .mainAddress, .detailAddress{
	display:block;
	background: none;
	border: none;
	border-bottom: 2px solid #aaa;
	font-size: 16px;
	width: 500px;
	padding: 10px 5px;
	outline: none;
	transition: border-bottom-color 0.3s;
}
.name{
	margin-bottom: 30px;
}
.name:focus{
	border-bottom: 2px solid #20367a;
}
.postcode{
	width: 100px; !important
}
.postcode, .mainAddress{
	height:43px;
	cursor: text;
	margin-bottom: 10px;
	text-align: left;
}
#detailAddress{
	height: 43px;
}
#detailAddress:focus{
	border-bottom: 2px solid #20367a;
}
.iframe p{
	color: #8b8b8b;
}

.addAddress{
	position:relative;
	border: 2px solid #afafaf;
	border-radius: 10px;
	border-style:dotted;
	padding: 15px 15px;
	margin-top: 10px;
	height: 42px;
	cursor: pointer;
}
.plusButton{
	position: absolute;
	color:#aaa;
	font-size:24px;
	top: 30%;
	left: 50%;
}

.updateButton{
	font-size:16px;
    position: absolute;
    width: 500px;
    bottom: 20px;
	color: white;
    background: #20367a;
    border: 1px solid white;
    border-radius: 10px;
    cursor: pointer;
    padding: 10px 20px;
}


</style>
</head>
<script type="text/javascript">

document.addEventListener("DOMContentLoaded", function() {
	if(${sessionScope.addressListJson!=null}){
			var addressList = JSON.parse('${addressListJson}');
		    var contentDiv = document.querySelector('.content');

		    // addressList를 반복하여 HTML 요소를 생성
		    addressList.forEach(function(address, index) {
		        // 주소 요소 생성
		        var addressDiv = document.createElement('div');
		        addressDiv.classList.add('address');
		        addressDiv.id = "address" + index;  // id에 인덱스를 추가하여 유일하게 만듦


		        // 배송지명 값
		        var addressNicknameP = document.createElement('p');
		        addressNicknameP.classList.add('addressNickname');
		        addressNicknameP.textContent = address.name || '이름 없음';  // AddressDto에 'name'이 있는 경우 사용

		        // 주소 값
		        var sortedAddressText = document.createElement('p');
		        sortedAddressText.classList.add('sortedAddressText');
		        sortedAddressText.textContent = '(' + address.postcode + ') ' + address.mainAddress + ' ' + address.detailAddress;
		        
		        // 주소 합친 div
		        var sortedAddressDiv = document.createElement('div');
		        sortedAddressDiv.classList.add('sortedAddress');
		        sortedAddressDiv.id = "sortedAddress" + index;  // id에 인덱스를 추가하여 유일하게 만듦

		        sortedAddressDiv.appendChild(addressNicknameP);
		        sortedAddressDiv.appendChild(sortedAddressText);

		        // hidden input (addressSeq)
		        var addressSeqInput = document.createElement('input');
		        addressSeqInput.type = 'hidden';
		        addressSeqInput.id = 'addressSeq' + index;
		        addressSeqInput.value = address.addressSeq; // AddressDto에 'addressSeq'가 있는 경우 사용

		        // 버튼 div
		        var buttonDiv = document.createElement('div');
		        buttonDiv.classList.add('buttonDiv');

		        // 대표 선택 버튼
		        if(index!=0){
		        var choiceButton = document.createElement('button');
		        choiceButton.type = 'button';
		        choiceButton.id = 'choiceAddress' + index;
		        choiceButton.textContent = '대표 선택';
		        buttonDiv.appendChild(choiceButton);
		        }
		        
		        // 수정 버튼
		        var updateButton = document.createElement('button');
		        updateButton.type = 'button';
		        updateButton.id = 'updateAddress' + index;
		        updateButton.textContent = '수정';
		        buttonDiv.appendChild(updateButton);
		        
		        if(index!=0){
		        // 삭제 버튼
		        var deleteButton = document.createElement('button');
		        deleteButton.type = 'button';
		        deleteButton.id = 'deleteAddress' + index;
		        deleteButton.textContent = '삭제';
		        buttonDiv.appendChild(deleteButton);
		        }

		        // 요소들을 addressDiv에 추가
		        /* addressDiv.appendChild(nameP); */
		        addressDiv.appendChild(sortedAddressDiv);
		        addressDiv.appendChild(addressSeqInput);
		        addressDiv.appendChild(buttonDiv);

		        // addressDiv를 contentDiv에 추가
		        contentDiv.appendChild(addressDiv);
		    });
		    
		    var updateButtons = document.querySelectorAll('button[id^="updateAddress"]');
		    var deleteButtons = document.querySelectorAll('button[id^="deleteAddress"]');
		    var choiceButtons = document.querySelectorAll('button[id^="choiceAddress"]');

		    updateButtons.forEach(function(button, index) {
		        button.addEventListener('click', function() {
		            var addressSeq = this.closest('.address').querySelector('input[id^="addressSeq"]').value;
		            var address = addressList.find(function(item) {
		                return String(item.addressSeq) === String(addressSeq);
		            });
		        	footerlayerLoad(address);
		        });
		    });

		    deleteButtons.forEach(function(button) {
		        button.addEventListener('click', function() {
		            var addressSeq = this.closest('.address').querySelector('input[id^="addressSeq"]').value;
		            deleteAddress(addressSeq);
		        });
		    });
		    
		    choiceButtons.forEach(function(button) {
		        button.addEventListener('click', function() {
		            var addressSeq = this.closest('.address').querySelector('input[id^="addressSeq"]').value;
		            updateAddressSeq(addressSeq);
		        });
		    });
	}
	if(${sessionScope.addressListJson}.length<5){
        var addAddressDiv = document.createElement('div');
        addAddressDiv.classList.add('addAddress');
        addAddressDiv.addEventListener('click', function() {
        	footerlayerLoad();
        });
        
        var plusButton = document.createElement('div');
        plusButton.classList.add('plusButton');
        plusButton.textContent = '+';
        
        addAddressDiv.appendChild(plusButton);
        contentDiv.appendChild(addAddressDiv);

	}
});
</script>
<body>
<%@include file ="main_header.jsp" %>
	<div class="container">
		<%@ include file="mypageSidebar.jsp"%>
		<div class="content">
			<p class="headerFont contentTitle">주소록</p>

		</div>
	</div>
<%@include file ="main_footer.jsp" %>
</body>
<script type="text/javascript">

// 삭제 버튼 클릭 시 호출되는 함수
function deleteAddress(addressSeq) {
    if(confirm("정말로 삭제하시겠습니까?")) {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/address/delete', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        var data = JSON.stringify(addressSeq); 
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) { // 요청 완료
                if (xhr.status === 200) { // 성공
                	window.location.reload();
               	}
            }
        };
        xhr.send(data);
    }
}

function updateInfo() {
 	var xhr = new XMLHttpRequest();
    xhr.open('POST', '/user/update/info', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    var data = JSON.stringify(userJson); 
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // 요청 완료
            if (xhr.status === 200) { // 성공
            	window.location.reload();
           	}
        }
    };
    xhr.send(data); 
}

function footerlayerLoad(address) {

    var overlay = $('<div class="overlay"></div>');
    var popContent = $('<div class="popContent"></div>');
    var closeBtn = $('<span class="closeBtn" onclick="window.close()">&times;</span>');
    var head = $('<h3>입력한 정보를 확인 후 저장해주세요</h3>');
	var iframe = $('<div class ="iframe"></div>')
    .attr('id', address?address.addressSeq:'');
    var nameLabel = $('<p>이름</p>');
	var name = $('<input type="text" class="name" id="name" maxlength="20" placeholder="이름">' )
    .val(address?address.name:'');
    var addressLabel = $('<p>주소</p>');
    var postcode = $('<input class="postcode" id="postcode" placeholder="우편번호" readonly>')
    .val(address?address.postcode:'')
	.on('click', function() {
    searchAddress();
	});
    var mainAddress = $('<input class="mainAddress" id="mainAddress" placeholder="주소" readonly>')
    .val(address?address.mainAddress:'')
    .on('click', function() {
    searchAddress();
	});
    var detailAddress = $('<input type="text" class="detailAddress" id="detailAddress" type="text" maxlength="50" placeholder="상세주소">')
    .val(address?address.detailAddress:'');
    var buttonDiv = $('<div class="overayButtonDiv"></div>');
    var updateButton = $('<button type="button" class="updateButton">확인</button>')
    .on('click', function(){
        if (address) {
            updateAddress(address);
        } else {
        	registerAddress();
        }
    });

    
    closeBtn.on('click', function() {
        overlay.remove();
    });
    
    popContent.on('click', function(event) {
        event.stopPropagation();
    });
    
    overlay.on('click', function() {
        overlay.remove();
    });

    iframe.append(closeBtn);
    iframe.append(nameLabel);
    iframe.append(name);
    iframe.append(addressLabel);
    iframe.append(postcode);
    iframe.append(mainAddress);
    iframe.append(detailAddress);
    iframe.append(updateButton);
    iframe.append(buttonDiv);
    popContent.append(head);
    popContent.append(iframe);
    overlay.append(popContent);
    $('body').append(overlay);
    overlay.fadeIn();
}

function updateAddress(address){
	
    address.name = document.getElementById('name').value;
    address.postcode = document.getElementById('postcode').value;
    address.mainAddress = document.getElementById('mainAddress').value;
    address.detailAddress = document.getElementById('detailAddress').value;
 	alert(address.postcode+address.mainAddress);
    if(address.postcode=="" || address.mainAddress==""){
    	alert("주소를 입력해 주세요.");
    	return;
    }
    
 	var xhr = new XMLHttpRequest();
    xhr.open('POST', '/address/update', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8'); 
    var data = JSON.stringify(address); 
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // 요청 완료
            if (xhr.status === 200) { // 성공
            	alert("수정 완료");
            	window.location.reload();
            } else {
            	alert("수정 오류");
            }
        }
    };
    xhr.send(data);
}

function registerAddress(){
 	var address ={
			userSeq : ${userDto.userSeq},
			name : document.getElementById('name').value,
			postcode : document.getElementById('postcode').value,
			mainAddress : document.getElementById('mainAddress').value,
			detailAddress : document.getElementById('detailAddress').value
	};
 	alert(address.postcode+address.mainAddress);
    if(address.postcode=="" || address.mainAddress==""){
    	alert("주소를 입력해 주세요.");
    	return;
    }
    
 	var xhr = new XMLHttpRequest();
    xhr.open('POST', '/address/register', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8'); 
    var data = JSON.stringify(address); 
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // 요청 완료
            if (xhr.status === 200) { // 성공
            }else if(xhr.status === 400){
            	alert("잘못된 시도입니다.");
            }else if(xhr.status === 500){
            	alert("잠시 후 재시도 해주세요.");
            } else {
            	alert("잠시 후 재시도 해주세요.");
            }
            window.location.reload();
        }
    };
    xhr.send(data); 
}

function updateAddressSeq(addressSeq){
	var userSeq = ${sessionScope.userDto.userSeq};
	
	var xhr = new XMLHttpRequest();
    xhr.open('POST', '/user/update/addressSeq', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8'); 
    var data = JSON.stringify({userSeq:userSeq, addressSeq:addressSeq}); 
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) { // 요청 완료
            if (xhr.status === 200) { // 성공
            	window.location.reload();
            } else {
            	alert("잠시 후 재시도 해주세요.");
            }
        }
    };
    xhr.send(data);
}

</script>

<!-- 주소 검색 api -->
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="../static/js/daumAddressSearch4.js"></script>
</html>