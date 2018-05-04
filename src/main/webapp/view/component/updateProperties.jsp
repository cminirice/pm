<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>组件属性信息 - Powered By GUTTV</title>
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
		<c:if test="${not empty message}">
			$.message({type: "success", content: "${message}"});
		</c:if>
		<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "修改失败：${errMsg}", modal: true, autoCloseTime: 3000});
		</c:if>
	});
</script>
</head>
<body class="input">
	<div class="bar">
		流程配置管理 / 修改属性
	</div>
	<div class="body">

		<form id="validateForm" action="${base}/component/updateProperties" method="post">
			<input type="hidden" name="clz" value="${com.clz}" />
			
			<table class="inputTable">
				<tr>
					<th colspan="5" style="text-align:center">
						属性信息
					</th>
				</tr>
				<tr>
					<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						类型
					</th>
					<th style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						中文名
					</th>
					<th style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						英文名
					</th>
					<th style="width:50%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
						值
					</th>
				</tr>
				<c:forEach var="pro" items="${com.componentPros}">
				<tr>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${pro.type}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${pro.cn}</td>
					<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;${pro.name}</td>
					<td style="text-align:left;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">&nbsp;<input type="text" value="${pro.value}" name="${pro.type}_${pro.name}"></input></td>
				</tr>
				</c:forEach>
				
			</table>
			<div class="buttonArea">
				<c:if test="${empty errMsg}">
				<input type="submit" class="formButton" value="保  存" hidefocus />&nbsp;&nbsp;
				</c:if>
				<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
			</div>
		</form>
		
		<table class="inputTable">
			<tr>
				<th colspan="4" style="text-align:center">
					组件信息
				</th>
			</tr>
			<tr>
				<th>
					组件标识: 
				</th>
				<td>
						&nbsp;${com.comID}
				</td>
				<th>
					名称: 
				</th>
				<td>
						&nbsp;${com.name}
				</td>
			</tr>
			<tr>
				<th>
					中文名: 
				</th>
				<td>
						&nbsp;${com.cn}
				</td>
				<th>
					类名: 
				</th>
				<td>
						&nbsp;${com.clz}
				</td>
			</tr>
			<tr>
				<th>
					方法名: 
				</th>
				<td>
						&nbsp;${com.method}
				</td>
				<th>
					运行类型: 
				</th>
				<td>
						&nbsp;${com.runType}
				</td>
			</tr>
			<tr>
				<th>
					线程数: 
				</th>
				<td>
						&nbsp;${com.threadNum}
				</td>
				<th>
					状态: 
				</th>
				<td>
						&nbsp;${com.status}
				</td>
			</tr>
			<tr>
				<th>
					需要读: 
				</th>
				<td>
						&nbsp;${com.needRead}
				</td>
				<th>
					需要写: 
				</th>
				<td>
						&nbsp;${com.needWrite}
				</td>
			</tr>
			<tr>
				<th>
					队列类型: 
				</th>
				<td>
						&nbsp;${com.queueType}
				</td>
				<th>
					接收队列: 
				</th>
				<td>
						&nbsp;${com.receive}
				</td>
			</tr>
			<tr>
				<th>
					描述信息: 
				</th>
				<td colspan="3">
						&nbsp;${com.description}
				</td>
			</tr>
			<tr>
				<th>
					生成时间: 
				</th>
				<td>
					&nbsp;<fmt:formatDate value="${com.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
				<th>
					最后修改时间: 
				</th>
				<td>
					&nbsp;<fmt:formatDate value="${com.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
			</tr>
		</table>
		
	</div>
</body>
</html>