<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>添加/编辑流程 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.validate.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.validate.methods.js"></script>
<script type="text/javascript">

$().ready(function() {

	var $validateErrorContainer = $("#validateErrorContainer");
	var $validateErrorLabelContainer = $("#validateErrorContainer ul");
	var $validateForm = $("#validateForm");
	
	// 表单验证
	$validateForm.validate({
		errorContainer: $validateErrorContainer,
		errorLabelContainer: $validateErrorLabelContainer,
		wrapper: "li",
		errorClass: "validateError",
		ignoreTitle: true,
		rules: {
			"flow.name": "required",
		},
		messages: {
			"flow.name": "请填写流程名称",
		},
		submitHandler: function(form) {
			$(form).find(":submit").attr("disabled", true);
			form.submit();
		}
	});
	
	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "异常：${errMsg}", modal: true, autoCloseTime: 3000});
	</c:if>
	<c:if test="${not empty message}">
		$.message({type: "success", content: "${message}"});
	</c:if>
})
</script>
</head>
<body class="input">
	<div class="bar">
		流程管理 / 
		<c:choose>
			<c:when test="${empty flow.code}">
				添加流程
			</c:when>
			<c:otherwise>
				编辑流程
			</c:otherwise>
		</c:choose>
	</div>
	<div id="validateErrorContainer" class="validateErrorContainer">
		<div class="validateErrorTitle">以下信息填写有误,请重新填写</div>
		<ul></ul>
	</div>
	<div class="body">
		<form id="validateForm" action="${base}/flow/saveOrUpdate" method="post">
			<input type="hidden" name="code" value="${flow.code}" />
			
			<table class="inputTable">
				<tr>
					<th>
						流程名称: 
					</th>
					<td>
						<textarea name="name" class="formText" isMultiLine="true" cols="50" rows="5" maxLength="128">${(flow.name)}</textarea>
						<label class="requireField"><font color="red">*</font></label>
					</td>
					<th>
						状态描述: 
					</th>
					<td>
						<textarea name="statusDesc" class="formText" isMultiLine="true" cols="50" rows="5" maxLength="256">${(flow.statusDesc)}</textarea>
					</td>
				</tr>
				<tr>
					<th>
						备注: 
					</th>
					<td>
						<textarea name="remark" class="formText" isMultiLine="true" cols="50" rows="5" maxLength="1024">${(flow.remark)}</textarea>
					</td>
					<th>
						&nbsp;
					</th>
					<td>
						&nbsp;
					</td>
				</tr>
			</table>
			
			<div class="buttonArea">
				<input type="submit" class="formButton" value="确  定" hidefocus />&nbsp;&nbsp;
				<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
			</div>
		</form>
	</div>
</body>
</html>