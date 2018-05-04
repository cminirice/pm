<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>查看流程信息 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
</head>
<body class="input">
	<div class="body">
	<table class="inputTable">
		<tr>
			<th>
				流程编码: 
			</th>
			<td>
					&nbsp;${flow.code}
			</td>
			<th>
				流程名称: 
			</th>
			<td>
					&nbsp;${flow.name}
			</td>
		</tr>
		<tr>
			<th>
				状态: 
			</th>
			<td>
					&nbsp;							
					<c:choose>
						<c:when test="${flow.status.value==1}">
							正常
						</c:when>
						<c:when test="${flow.status.value==0}">
							停用
						</c:when>
					</c:choose>
			</td>
			<th>
				状态描述: 
			</th>
			<td>
					&nbsp;${flow.statusDesc}
			</td>
		</tr>
		<tr>
			<th>
				备注: 
			</th>
			<td>
					&nbsp;${flow.remark}
			</td>
			<th>
				&nbsp;
			</th>
			<td>
				&nbsp;
			</td>
		</tr>
		<tr>
			<th>
				生成时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${flow.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
			<th>
				最后修改时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${flow.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
	</table>
	</div>
</body>
</html>