<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>添加/编辑执行容器 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.validate.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.validate.methods.js"></script>
<script type="text/javascript" src="${base}/theme/common/datePicker/WdatePicker.js"></script>
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
			"executeContainer.alias": "required",
			"executeContainer.heartbeatPeriod": "required digits",
		},
		messages: {
			"executeContainer.alias": "请填写容器别名",
			"executeContainer.heartbeatPeriod": "心跳周期为非空数字,并且需要设置正确的范围",
		},
		submitHandler: function(form) {
			$(form).find(":submit").attr("disabled", true);
			$("input:text").each(function(){
				var name = $(this).attr("name");
				if(name){
					var spi = name.split(".");
					if(spi.length==2){
						$(this).attr("name",spi[1]);
					}
				}
			});
			$("textarea").each(function(){
				var name = $(this).attr("name");
				if(name){
					var spi = name.split(".");
					if(spi.length==2){
						$(this).attr("name",spi[1]);
					}
				}
			});
			form.submit();
		}
	});
	
})
</script>
</head>
<body class="input">
	<div class="bar">
		执行容器管理 / 编辑执行容器
	</div>
	<div id="validateErrorContainer" class="validateErrorContainer">
		<div class="validateErrorTitle">以下信息填写有误,请重新填写</div>
		<ul></ul>
	</div>
	<div class="body">
		<form id="validateForm" action="${base}/container/saveOrUpdate" method="post">
			<input type="hidden" name="containerID" value="${executeContainer.containerID}" />
			
			<table class="inputTable">
				<tr>
					<th>
						容器别名: 
					</th>
					<td>
						<input type="text" name="executeContainer.alias" class="formText" value="${(executeContainer.alias)}"  maxLength="64"/>
						<label class="requireField"><font color="red">*</font></label>
					</td>
					<th>
						心跳周期: 
					</th>
					<td>
						<input type="text" name="executeContainer.heartbeatPeriod" class="formText" value="${(executeContainer.heartbeatPeriod)}"  min="10000" max="120000"/>
						<label class="requireField"><font color="red">*&nbsp;&nbsp;&nbsp;[10,000-120,000]</font></label>
					</td>
				</tr>
				<tr>
					<th>
						备注: 
					</th>
					<td>
						<textarea name="executeContainer.remark" class="formText" isMultiLine="true" cols="50" rows="5" maxLength="256">${(executeContainer.remark)}</textarea>
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