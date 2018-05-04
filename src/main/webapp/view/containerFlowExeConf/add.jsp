<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>流程执行配置列表 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/admin.js"></script>
<script type="text/javascript">

	$().ready( function() {
		<c:if test="${not empty errMsg}">
				$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
		</c:if>
		<c:if test="${not empty message}">
			$.message({type: "success", content: "${message}"});
		</c:if>
	});
</script>
</head>
<body class="list">
	<div class="bar">
		向执行容器[<font color="red">${containerID}</font>]中添加流程执行配置，共[<font color="red">${pager.totalCount}</font>]个可用
	</div>
	<div class="body">
		<form id="listForm" action="${base}/containerFlowExeConf/add" method="post">
			<table id="listTable" class="listTable">
				<tr>
					<th class="check">
						<input type="checkbox" class="allCheck" />
					</th>
					<th>
						<a href="#"  class="sort" name="flowExeCode" hidefocus>流程执行编码</a>
					</th>
					<th>
						<a href="#"  class="sort" name="flowCode" hidefocus>流程编码</a>
					</th>
					<th>
						<a href="#"  class="sort" name="flowName" hidefocus>流程名称</a>
					</th>
				</tr>
				<c:set var="empt" value="true"></c:set>
				<c:forEach var="entity" items="${pager.result}">
				<c:set var="empt" value="false"></c:set>
					<tr>
						<td>
							<input type="checkbox" name="ids" value="${entity.flowExeCode}" />
						</td>
						<td>
							${entity.flowExeCode}
						</td>
						<td>
							${entity.flow.code}
						</td>
						<td>
							${entity.flow.name}
						</td>
					</tr>
				</c:forEach>
			</table>
			<input type="hidden" name="containerID" id="containerID" value="${containerID}" />
			<div class="blank"></div>
			<div align="center">
			<c:if test="${empt eq false}">
			<input type="submit" class="formButton"  value="添加" hidefocus />
			&nbsp;&nbsp;&nbsp;&nbsp;
			</c:if>
			<input type="button" class="formButton" onclick="location.href='${base}/containerFlowExeConf/list?containerID=${containerID}'" value="返回" hidefocus />
			
			</div>
			<c:if test="${empt eq true}">
				<div class="noRecord">没有可用的流程执行配置!</div>
			</c:if>
		</form>
	</div>
</body>
</html>