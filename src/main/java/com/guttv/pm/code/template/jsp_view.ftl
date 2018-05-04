<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>查看${codeEntity.cn}信息 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${r"${base}"}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${r"${base}"}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
</head>
<body class="input">
	<div class="body">
	<table class="inputTable">
	<#assign sign=0/>
	<#list codeEntity.fields?keys as field>
	<#if sign=0>
		<tr>
	</#if>
			<th>
				${codeEntity.fields[field].cn}: 
			</th>
			<td>
				<#if codeEntity.fields[field].type=='Date'>
				&nbsp;<s:date name="%{${codeEntity.fsName}.${field}}" format="yyyy-MM-dd HH:mm:ss" />
				<#else>
					&nbsp;${r"${"}${codeEntity.fsName}.${field}}
				</#if>
			</td>
	<#if sign=1>
		</tr>
	</#if>
	<#if sign=0>
	<#assign sign=1/>
	<#else>
	<#assign sign=0/>
	</#if>
	</#list>
	<#if sign=1>
			<th>
				&nbsp;
			</th>
			<td>
				&nbsp;
			</td>
		</tr>
	</#if>
		<tr>
			<th>
				生成时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${r"${"}${codeEntity.fsName}.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
			<th>
				最后修改时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${r"${"}${codeEntity.fsName}.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
	</table>
	</div>
</body>
</html>