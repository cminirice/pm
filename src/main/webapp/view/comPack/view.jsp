<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>查看组件包信息 - Powered By GUTTV</title>
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
				组件标识: 
			</th>
			<td>
					&nbsp;${comPack.comID}
			</td>
			<th>
				包MD5值: 
			</th>
			<td>
					&nbsp;${comPack.md5}
			</td>
		</tr>
		<tr>
			<th>
				组件包路径: 
			</th>
			<td colspan="3">
					&nbsp;${comPack.comPackageFilePath}
			</td>
		</tr>
		<tr>
			<th>
				组件包源文件名: 
			</th>
			<td colspan="3">
					&nbsp;${comPack.srcFileName}
			</td>
		</tr>
		<tr>
			<th>
				生成时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${comPack.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
			<th>
				最后修改时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${comPack.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
	</table>
	
	<table class="inputTable">
		<tr>
			<th colspan="15" style="text-align:center">
				组件信息
			</th>
		</tr>
		<tr>
			<th  style="width:14%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				名称
			</th>
			<th  style="width:14%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:40%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				类名
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				方法名
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				运行类型
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				需要读
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				需要写
			</th>
		</tr>
		<c:forEach var="entity" items="${comPack.components}">

		<tr>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.name}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.cn}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.clz}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.method}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.runType}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.needRead}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${entity.needWrite}
			</td>
		</tr>
		</c:forEach>
	</table>
	
	</div>
</body>
</html>