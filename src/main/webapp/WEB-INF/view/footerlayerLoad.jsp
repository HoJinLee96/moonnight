<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
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
 width:800px;
 height:650px;
     background-color: white;
     padding: 20px;
     border-radius: 10px;
     box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
     margin:0px auto;
     margin-top:150px;
     position: relative;
 }
 .close-btn {
 float: right;
 display:inline;
 margin-bottom:10px;
     cursor: pointer;
 }

</style>
</head>
<script type="text/javascript">
function footerlayerLoad(filePath) {
    var overlay = $('<div class="overlay"></div>');
    var popContent = $('<div class="popContent"></div>');
    var closeBtn = $('<div class="close-btn">X</div>');
    var iframe = $('<iframe src="' + filePath + '" width="800" height="600"></iframe>');

    closeBtn.on('click', function() {
        overlay.remove();
    });
    
    popContent.on('click', function(event) {
        event.stopPropagation();
    });
    
    overlay.on('click', function() {
        overlay.remove();
    });

    popContent.append(closeBtn);
    popContent.append(iframe);
    overlay.append(popContent);
    $('body').append(overlay);
    overlay.fadeIn();
}

</script>
</html>