<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>提示信息 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jQuery.md5.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript">
$().ready( function() {
	
	function redirectUrl() {
		if("${redirectUrl}" != ""){
			window.location.href = "${redirectUrl}"
		}else{
			window.history.back();
		}
	}
	
	var errorMessages = "${errorMessages}";
	if(errorMessages == ""){
		errorMessages = "您的操作出现错误!";
	}
	$.dialog({type: "error", title: "操作提示", content: errorMessages, ok: "确定", okCallback: redirectUrl, cancelCallback: redirectUrl, width: 380, modal: true});
});
</script>
</head>
<body class="error">
</body>
</html>