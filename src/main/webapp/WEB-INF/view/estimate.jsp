<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="dto.EstimateDto" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>견적 문의</title>
<style type="text/css">

#mainContainer{
	max-width: 1350px;
	margin: 0 auto;
	padding-top: 50px;
	min-height: 1080px;
}
#mainContainer h1{
    border-bottom: 4px solid #20367a;
    padding-bottom: 15px;
    margin-bottom: 20px;
    }
#formContainer{

} 
#estimate{
	display: flex;
}
#rightDiv{
}
h1{
margin-top: 0px;
margin-bottom: 10px;
}
table{
margin-right: 60px;
    margin-left: 20px;
}
.tableHeader {
    height: 30px;
}
#phone{
width: 200px !important;
}
#formContainer input[type="text"], #formContainer input[type="email"]{
    border: 1.5px solid #efefef;
    width: 500px;
    height: 30px;
    border-radius: 5px;
    padding-left: 10px;
    outline:none;
}
#formContainer input:focus{
	border: 1.5px solid black;
	transition: border-color 0.3s;
}
#postcode{
width:100px !important;
}
#content {
    border: 1.5px solid #efefef;
    width: 500px;
    height: 400px;
    border-radius: 5px;
    padding-left: 10px;
    padding-top: 10px;
    resize: none;
    outline:none;
}
#content:focus {
    border-color: black;
   	transition: border-color 0.3s;
}
#submitButton {
    width: 120px;
    height: 50px;
    font-size: 18px;
    border: none;
    background-color: #20367a;
    color: white;
    border-radius: 5px;
}
#submitButton:hover {
cursor:pointer;
    border: 1px solid #20367a;
    padding-left: 10px;
    background-color: white;
    color: #20367a;
}
.buttonDiv{
    text-align: right;
    width: 730px;
    margin-top: 80px;
}
#agreeMentDiv{
margin-bottom:15px;
    position: relative;
}
#agreementClick{
    text-decoration: none;
    color: black;
}
 #agreement:hover{
cursor:pointer;
    position: relative;
    top: 1px;
}
label[for="agreement"]{
cursor:pointer;
}
#agreementMessage{
    position: absolute;
    right: 0px;
    top: -40px;
    }
</style>

<!-- 이미지 업로드 -->
<style type="text/css">
#preview{
	display: flex;
    flex-wrap: wrap;
    width: 720px;
    height: 280px;
    border: 1px solid #efefef;
    border-radius: 5px;
    margin-top: 15px;
    gap:5px;
}
.image-preview{
    position: relative;
}
.image-preview img{
    border-radius: 20px;
}
.image-preview button{
    position: absolute;
    right: 0px;
    top: 0px;
    border: none;
    background: none;
    font-size: 25px;
}
.image-preview button:hover{
cursor:pointer;
color:#efefef;
}
#dal6{
width: 730px;
height: 280px;
margin-top: 50px;
}
	
.custom-image-label {
	display: inline-block;
    padding: 5px 10px;
    background-color: #20367a;
    color: white;
    border: 1px solid #20367a;
    border-radius: 5px;
    cursor: pointer;
    font-size: 14px;
    margin-left: 3px;
}

.custom-image-label:hover {
border: 1px solid #20367a;
    background-color: white;
    color:#20367a;
}
</style>


</head>
<script type="text/javascript">
document.addEventListener("DOMContentLoaded", function() {
	var name ="";
	var phone ="010-";
	var email ="";
	var postcode ="";
	var mainAddress ="";
	var detailAddress ="";
	
    if (${sessionScope.userDto != null}) {
    	name = "${sessionScope.userDto.name}";
		phone = "${sessionScope.userDto.phone}";
		email = "${sessionScope.userDto.email}";
		postcode = "${sessionScope.addressList[0].postcode}";
		mainAddress = "${sessionScope.addressList[0].mainAddress}";
		detailAddress = "${sessionScope.addressList[0].detailAddress}";
    }else if (${sessionScope.oAuthDto != null}) {
    	name = "${sessionScope.oAuthDto.name}";
		phone = "${sessionScope.oAuthDto.phone}";
		email = "${sessionScope.userDto.email}";
    }
	
	document.getElementById("name").value=name;
	document.getElementById("phone").value=phone;
	document.getElementById("email").value=email;
	document.getElementById("postcode").value=postcode;
	document.getElementById("mainAddress").value=mainAddress;
	document.getElementById("detailAddress").value=detailAddress;
});



</script>
<body>
<%@ include file = "main_header.jsp" %>

	<div id="mainContainer">

		<h1>청소 견적 문의하기</h1>
		<div id="formContainer">
			<form id="estimate">
				<div>
					<table>
						<tr>
							<td class="tableHeader">성함 (상호명)</td>
						</tr>
						<tr>
							<td><input type="text" id="name" maxlength="20"></td>
						</tr>

						<tr>
							<td class="tableHeader">연락처<span style="color: red">＊</span></td>
						</tr>
						<tr>
							<td>
							<input type="text" id="phone" oninput="formatPhoneNumber(this)" maxlength="13" value="010-" required>
							수신 동의 :
							<input type="checkbox" class="agreeInput" id="eamilAgree">
							<label for="eamilAgree">이메일</label> 
							<input type="checkbox" class="agreeInput" id="smsAgree">
							<label for="smsAgree">문자</label>
							<input type="checkbox" class="agreeInput" id="callAgree">
							<label for="callAgree">전화</label>
							</td>
						</tr>
						<tr>
							<td><span id="receiveAgreeMessage"></span></td>
						</tr>

						<tr>
							<td class="tableHeader">이메일</td>
						</tr>
						<tr>
							<td><input type="email" id="email"
								maxlength="30"></td>
						</tr>

						<tr>
							<td class="tableHeader">주소<span style="color: red">＊</span></td>
						</tr>
						<tr>
							<td><input type="text" id="postcode" placeholder="우편번호" readonly></td>
						</tr>
						<tr>
							<td><input type="text" id="mainAddress"
								placeholder="주소" readonly></td>
						</tr>
						<tr>
							<td><input type="text" id="detailAddress"
								 autocomplete="off" placeholder="상세주소"></td>
						</tr>
						<tr>
							<td><span id="addressMessage"></span></td>
						</tr>

						<tr>
							<td class="tableHeader">내용</td>
						</tr>
						<tr>
							<td><textarea id="content" placeholder="접수 내용"
									></textarea></td>
						</tr>
					</table>
				</div>

				<div id="rightDiv">
					이미지 첨부 <input type="file" id="image-input" accept="image/*"
						multiple style="display: none;"> <label for="image-input"
						class="custom-image-label">사진 선택</label>
					<div id="preview"></div>
					<img id="dal6" src="static/img/dal6.png" alt="dal6">
					<div class="buttonDiv">
						<div id="agreeMentDiv">
						<p id="agreementMessage"> </p>
							<input type="checkbox" id="agreement">
							<label for ="agreement"> 개인정보 수집 및 이용 동의</label>
						<a id="agreementClick" href="javascript:;"
							onclick="javascript:footerlayerLoad('static/InfoAgreement.html'); return false;">[원본]</a>
						</div>
						<input id="submitButton" type="submit" value="등록">
					</div>
				</div>
				<div id="overlay"></div>
			</form>
		</div>

	</div>

	<%@ include file = "main_footer.jsp" %>
<%@ include file="footerlayerLoad.jsp"%>

</body>
<script type="text/javascript">
var postcodeInput = document.getElementById('postcode');
var mainAddress = document.getElementById('mainAddress');
var postcodeValue = "";
var mainAddressValue = "";

postcodeInput.addEventListener('click', function() {
	searchAddress(function(postcode, address){
        postcodeValue = postcode;
        mainAddressValue = address;
        postcodeInput.value=postcode;
        mainAddress.value=address;
		document.getElementById("detailAddress").focus();
	});
});
mainAddress.addEventListener('click', function() {
	searchAddress(function(postcode, address){
        postcodeValue = postcode;
        mainAddressValue = address;
        postcodeInput.value=postcode;
        mainAddress.value=address;
		document.getElementById("detailAddress").focus();
	});
});
document.getElementById('mainAddress').addEventListener('input',function(event){
	event.target.value=mainAddressValue;
});
document.getElementById('postcode').addEventListener('input',function(event){
	event.target.value=postcodeValue;
});

</script>
<!-- 주소 검색 api -->
<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="/static/js/daumAddressSearch4.js"></script>
<!-- 이미지 압축 Compressor.js 라이브러리 -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/compressorjs/1.0.6/compressor.min.js"></script>

<script type="text/javascript">
//휴대폰 번호 규칙
function formatPhoneNumber(input) {
	let value = input.value.replace(/[^0-9]/g, ''); // 숫자 이외의 문자를 제거합니다.
	let formattedValue = value;

	// 앞 세 자리를 "010"으로 고정합니다.
	if (value.startsWith('010')) {
		value = value.slice(3); // 앞 세 자리("010")를 잘라냅니다.
	}

	if (value.length <= 4) {
		formattedValue = '010-' + value; // 4자리 이하의 숫자만 있을 경우
	} else if (value.length <= 7) {
		formattedValue = '010-' + value.slice(0, 4) + '-' + value.slice(4); // 5~7자리의 경우
	} else {
		formattedValue = '010-' + value.slice(0, 4) + '-'
				+ value.slice(4, 8); // 8자리 이상의 경우
	}

	input.value = formattedValue;
}
</script>

<!-- 이미지 첨부 -->
<script type="text/javascript">
const MAX_IMAGES = 10; // 최대 이미지 수
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

let selectedImages = []; // 업로드된 base64 이미지 저장소
let selectedFiles = [];  // 실제 파일 객체 저장소

const imageInput = document.getElementById('image-input');
const previewContainer = document.getElementById('preview');
const form = document.getElementById('estimate');

// 이미지 파일 선택 시 처리
imageInput.addEventListener('change', handleImageSelection);

function handleImageSelection(event) {
    const files = Array.from(event.target.files);
    const remainingSlots = MAX_IMAGES - selectedImages.length;

    // 추가할 이미지가 최대 개수를 초과할 경우, 남은 슬롯만큼만 자르기
    if (files.length > remainingSlots) {
        alert('최대 ${MAX_IMAGES}장의 이미지만 업로드할 수 있습니다.');
        files.splice(remainingSlots);  // 남은 슬롯 만큼만 파일을 선택
    }

    files.forEach(file => {
        if (file.size > MAX_FILE_SIZE) {
            alert('이미지 파일의 크기는 최대 10MB까지 허용됩니다.');
        } else {
            selectedFiles.push(file); // 선택된 파일을 배열에 저장
            compressAndPreviewImage(file);  // 파일 압축 및 미리보기
        }
    });

    updateInputFiles(); // input의 파일 리스트를 동기화
}

// Compressor.js를 이용해 이미지 압축 및 미리보기 처리
function compressAndPreviewImage(file) {
    new Compressor(file, {
        quality: 0.5,
        success(result) {
            const reader = new FileReader();
            reader.readAsDataURL(result);
            reader.onload = () => {
                const imageDataUrl = reader.result;
                selectedImages.push(imageDataUrl);
                displayPreview(imageDataUrl);
            };
        },
        error(err) {
            console.error(err.message);
        }
    });
}

// 미리보기 이미지 생성 및 삭제 버튼 추가
function displayPreview(imageDataUrl) {
    const div = document.createElement('div');
    div.classList.add('image-preview');
    
    const img = document.createElement('img');
    img.src = imageDataUrl;
    img.style.width = '140px';
    img.style.height = '140px';

    const removeButton = document.createElement('button');
    removeButton.innerHTML = '&times;';
    removeButton.addEventListener('click', () => {
        const index = selectedImages.indexOf(imageDataUrl);
        if (index !== -1) {
            selectedImages.splice(index, 1);
            selectedFiles.splice(index, 1); // 파일 배열에서도 동일한 위치에서 제거
            div.remove();
            updateInputFiles(); // input 파일 리스트 업데이트
        }
    });

    div.appendChild(img);
    div.appendChild(removeButton);
    previewContainer.appendChild(div);
}

// input의 파일 리스트를 selectedFiles 배열과 동기화
function updateInputFiles() {
    const dataTransfer = new DataTransfer();
    selectedFiles.forEach(file => {
        dataTransfer.items.add(file); // 선택된 파일들을 다시 input에 추가
    });
    imageInput.files = dataTransfer.files; // input 파일 리스트 업데이트
}
</script>

<!-- 메인 js ===================================================================== -->
<script type="text/javascript">
form.addEventListener('submit', (event) => {
    event.preventDefault();

	  const agreementCheck = document.getElementById('agreement');
	  const agreementMessage = document.getElementById('agreementMessage');
	  
	  if(!agreementCheck.checked){
		  agreementMessage.style.color="red";
		  agreementMessage.textContent = "개인정보처리방침에 동의해 주세요.";
		  return;
	  }else{
		  agreementMessage.textContent ="";
	  }
	  
	  //휴대폰
	  let phone = document.getElementById("phone").value;
	  const receiveAgreeMessage = document.getElementById('receiveAgreeMessage');
	  if(phone.length<13){
		  receiveAgreeMessage.style.color="red";
		  receiveAgreeMessage.innerText="휴대폰 번호를 확인해 주세요.";
	  }else{
		  receiveAgreeMessage.innerText="";
	  }
	  
	  //주소 값 가져옴
	  let mainAddress = document.getElementById("mainAddress").value;
	  //주소 에러 메시지 요소
	  const addressMessage = document.getElementById('addressMessage');
	  
	  if(mainAddress.length===0){
		  addressMessage.style.color="red";
		  addressMessage.innerText="주소를 입력 해주세요.";
		  return;
	  }else{
		  addressMessage.innerText="";
	  }

	  // 모든 체크박스를 가져옴
	  const checkboxes = document.querySelectorAll('.agreeInput');
	  let checkedCount = 0;

	  // 선택된 체크박스 수를 셈
	  checkboxes.forEach(function(checkbox) {
	    if (checkbox.checked) {
	      checkedCount++;
	    }
	  });

	  // 에러 메시지 요소
	  receiveAgreeMessage.style.color="red";

	  // 최소 1개
	  if (checkedCount < 1) {
		  receiveAgreeMessage.textContent = "수신 방법을 최소 1개를 선택해야 합니다.";
		  return;
	  } else {
		  receiveAgreeMessage.textContent = "";
	  }

    const name = document.getElementById('name').value;
    /* const phone = document.getElementById('phone').value; */
    const eamilAgree = document.getElementById('eamilAgree').checked;
    const smsAgree = document.getElementById('smsAgree').checked;
    const callAgree = document.getElementById('callAgree').checked;
    const email = document.getElementById('email').value;
    const postcode = document.getElementById('postcode').value;
    /* const mainAddress = document.getElementById('mainAddress').value; */
    const detailAddress = document.getElementById('detailAddress').value;
    const content = document.getElementById('content').value;
    const estimateDto = {
        name: name,
        phone: phone,
        email: email,
        emailAgree: eamilAgree,
        smsAgree: smsAgree,
        callAgree: callAgree,
        postcode: postcode,
        mainAddress: mainAddress,
        detailAddress: detailAddress,
        content: content
    };

    const requestEstimateDto = {
        estimateDto: estimateDto,
        imageList: selectedImages // base64 인코딩된 이미지 리스트
    };

    // 서버로 데이터 전송
    sendEstimateToServer(requestEstimateDto);
});

// 서버로 데이터 전송하는 함수
function sendEstimateToServer(requestEstimateDto) {
    fetch('/estimate/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestEstimateDto)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('서버 응답 오류');
        }
        alert("이용해 주셔서 감사합니다.\n빠른 답변 드리겠습니다.");
        window.location.href = '/home';
    })
    .catch(error => {
        console.error('에러 발생:', error);
        alert('제출에 실패했습니다.');
    });
}


/* //폼 검사 및 제출
$('#estimate').on('submit', function(event) {
	  event.preventDefault(); // 폼 제출을 막음
	  
	  const agreementCheck = document.getElementById('agreement');
	  const agreementMessage = document.getElementById('agreementMessage');
	  
	  if(!agreementCheck.checked){
		  agreementMessage.style.color="red";
		  agreementMessage.textContent = "개인정보처리방침에 동의해 주세요.";
		  return;
	  }else{
		  agreementMessage.textContent ="";
	  }
	  
	  //휴대폰
	  let phone = document.getElementById("phone").value;
	  const receiveAgreeMessage = document.getElementById('receiveAgreeMessage');
	  if(phone.length<13){
		  receiveAgreeMessage.style.color="red";
		  receiveAgreeMessage.innerText="휴대폰 번호를 확인해 주세요.";
	  }else{
		  receiveAgreeMessage.innerText="";
	  }
	  
	  //주소 값 가져옴
	  let mainAddress = document.getElementById("mainAddress").value;
	  //주소 에러 메시지 요소
	  const addressMessage = document.getElementById('addressMessage');
	  
	  if(mainAddress.length===0){
		  addressMessage.style.color="red";
		  addressMessage.innerText="주소를 입력 해주세요.";
		  return;
	  }else{
		  addressMessage.innerText="";
	  }

	  // 모든 체크박스를 가져옴
	  const checkboxes = document.querySelectorAll('.agreeInput');
	  let checkedCount = 0;

	  // 선택된 체크박스 수를 셈
	  checkboxes.forEach(function(checkbox) {
	    if (checkbox.checked) {
	      checkedCount++;
	    }
	  });

	  // 에러 메시지 요소
	  receiveAgreeMessage.style.color="red";

	  // 최소 1개
	  if (checkedCount < 1) {
		  receiveAgreeMessage.textContent = "수신 방법을 최소 1개를 선택해야 합니다.";
		  return;
	  } else {
		  receiveAgreeMessage.textContent = "";
	  }
	    submitForm(event);
});
function submitForm(event) {
	event.preventDefault();
	
    var form = document.getElementById('estimate');
    var formData = new FormData(form);
    var data = {};

    // Add files to formData
    for (var i = 1; i <= 10; i++) {
        var fileInput = document.getElementById('fileInputHidden' + i);
        if (fileInput.value) {
            // base64 문자열을 Blob으로 변환하여 추가
            var byteString = atob(fileInput.value.split(',')[1]);
            var mimeString = fileInput.value.split(',')[0].split(':')[1].split(';')[0];
            var ab = new ArrayBuffer(byteString.length);
            var ia = new Uint8Array(ab);
            for (var j = 0; j < byteString.length; j++) {
                ia[j] = byteString.charCodeAt(j);
            }
            var blob = new Blob([ab], { type: mimeString });
            
            // Blob을 FormData 대신 JSON 형식으로 추가할 수 있도록 base64로 다시 인코딩하여 객체에 저장
            var reader = new FileReader();
            reader.readAsDataURL(blob);
            reader.onloadend = function() {
                data['image' + i] = reader.result;
                console.log(i + "번째 사진 추가");
            };
        }
    }
	
    $.ajax({
        url: '/estimate/register',
        type: 'POST',
        processData: false,
        contentType: false,
        data: formData,
        success: function(response) {
            alert("이용해 주셔서 감사합니다.\n빠른 답변 드리겠습니다.");
            window.location.href = '/home';
        },
        error: function(xhr, status, error) {
            console.log(xhr);
            console.log(status);
            console.error(error);
        }
    });
} */
</script>


</html>