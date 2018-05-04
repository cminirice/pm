<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Spring配置文件列表 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript">
	$().ready( function() {
		<c:if test="${not empty errMsg}">
				$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
		</c:if>
		<c:if test="${not empty message}">
			$.message({type: "success", content: "${message}"});
		</c:if>
	});
	
	function refreshConfig(){
		$.ajax({
			url: "${base}/springconfig/refresh",
			data: "",
			type: "POST",
			dataType: "json",
			cache: false,
			success: function(data) {
				$.message({type: data.status, content: data.message});
				if(data.status=='success'){
					setTimeout("window.location.href='${base}/springconfig/view'",1000);
				}
			}
		});
	}
</script>
</head>
<body class="input">
	<div class="bar">
		Spring配置文件列表 &nbsp;&nbsp;
		
	</div>
	<div class="body">
		<table class="inputTable">
			<tr><th colspan="2">
			<c:if test="${fn:contains(sessionScope.auths,'1901')}">
			<input type="button" class="formButton" onclick="javascript:refreshConfig();" value="更新缓存" hidefocus />
			</c:if>
			</th></tr>
			<tr>
				<th>主键</th>
				<th style="text-align:left">键值</th>
			</tr>
			<c:forEach var="config" items="${properties}">
			<tr>
				<th>${config.key }:&nbsp;&nbsp;</th>
				<td>&nbsp;&nbsp;${config.value}</td>
			</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>