<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>注册组件 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript">

	function fileUpload() {
		var formData = new FormData();
		if (!$('#comPackFile')[0].files[0]) {
			$('#formButton').removeAttr("disabled");
			alert("请选择组件包 >_<");
			return false;
		}
		
		$('#formButton').attr('disabled','true');
		formData.append('comPackFile', $('#comPackFile')[0].files[0]);

		$.ajax({
			type : "POST",
			enctype : 'multipart/form-data',
			url : "${base}/comPack/regist",
			data : formData,
			processData : false,
			contentType : false,
			cache : false,
			timeout : 60000,
			success : function(data) {
				//转成json对象
				var obj = jQuery.parseJSON(data);

				//注册失败给出提示信息
				if (!obj.status) {
					$.dialog({
						type : "warn",
						content : "注册失败：" + obj.message,
						modal : true,
						autoCloseTime : 10000
					});
				} else {
					$.dialog({
						type : "warn",
						content : "注册成功",
						modal : true,
						autoCloseTime : 1000
					});
					//刷新父页面，关闭当前页面
					self.setTimeout("window.parent.location.reload();window.close()", 1100);
				}
			},
			error : function(e) {
				$('#formButton').removeAttr("disabled");
			}
		});
	}
</script>
</head>
<body class="input">

	<div class="body">
		<form id="validateForm" action="" method="post">
			<table class="inputTable">
				<tr>
					<th>选择注册包 :</th>
					<td><input id="comPackFile" type="file" name="comPackFile" />
					</td>
				</tr>
			</table>

			<div class="buttonArea">
				<input type="button" id="formButton" class="formButton" value="注册"
					onclick="javascript:fileUpload();"
					hidefocus />&nbsp;&nbsp;
			</div>
		</form>
	</div>
</body>
</html>