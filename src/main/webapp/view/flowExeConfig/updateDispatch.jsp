<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>流程执行配置信息 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/layer.min.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/admin.js"></script>
<script type="text/javascript">
$().ready( function() {
	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
	</c:if>
});
</script>
</head>
<body class="input">
	<div class="bar">
		流程配置管理 / 修改通道
	</div>
	<div class="body">

		<form id="validateForm" action="${base}/flowExeConfig/updateDispatch" method="post">
			<input type="hidden" name="flowExeCode" value="${flowExeConfig.flowExeCode}" />
			<table class="inputTable">
				<tr>
					<th colspan="7" style="text-align:center">
						通道信息
					</th>
				</tr>
				<tr>
					<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						连接线ID
					</th>
					<th style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						起点类名
					</th>
					<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						起点ID
					</th>
					<th style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						终点类名
					</th>
					<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						终点ID
					</th>
					<th style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						队列名称
					</th>
					<th style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						发送规则
					</th>
				</tr>
				<c:forEach var="dispatch" items="${flowExeConfig.comDispatchs}">
				<tr>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${dispatch.lineID}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${dispatch.fromComponent}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${dispatch.fromNode}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${dispatch.toComponent}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${dispatch.toNode}</td>
					<td style="text-align:left;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;
					<input type="text" value="${dispatch.queue}" name="queue${dispatch.lineID}"></input>
					</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;
					<input type="text" value="${dispatch.rule}" name="rule${dispatch.lineID}"></input>
					</td>
				</tr>
				</c:forEach>
				
			</table>
			<div class="buttonArea">
				<c:if test="${empty errMsg && not empty flowExeConfig.comDispatchs}">
				<input type="submit" class="formButton" value="保  存" hidefocus />&nbsp;&nbsp;
				</c:if>
				<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
			</div>
		</form>
		
	</div>
</body>
</html>