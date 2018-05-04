<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>查看组件信息 - Powered By GUTTV</title>
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
	<div class="body">
	<table class="inputTable">
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
				所属组: 
			</th>
			<td>
					&nbsp;${com.group}
			</td>
			<th>
				
			</th>
			<td>
					&nbsp;
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
				初始化方法: 
			</th>
			<td>
					&nbsp;${com.initMethod}
			</td>
			<th>
				关闭方法: 
			</th>
			<td>
					&nbsp;${com.closeMethod}
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
					&nbsp;
					<c:choose>
						<c:when test="${com.status==1}">
							正常
						</c:when>
						<c:when test="${com.status==0}">
							停用
						</c:when>
					</c:choose>
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
				<c:if test="${empty com.description}"><font color="red">这个开发人员比较懒，没有留下任何描述信息！</font></c:if>
				<c:if test="${not empty com.description}">&nbsp;${com.description}</c:if>
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
	
	<table class="inputTable">
		<tr>
			<th colspan="7" style="text-align:center">
				组件属性信息
			</th>
		</tr>
		<tr>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				名称
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				属性值
			</th>
			<th  style="width:1px;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				名称
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				属性值
			</th>
		</tr>
		
		<c:set var="flag" value="0"></c:set>
		<c:forEach var="cp" items="${com.componentPros}">
		
		<c:if test="${flag=='0'}">
		<tr>
		</c:if>
		
		<c:if test="${cp.type.value == 1}">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.name}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.cn}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.value}
			</td>
			<c:choose>
			<c:when test="${flag=='1'}">
			</tr>
			<c:set var="flag" value="0"></c:set>
			</c:when>
			<c:otherwise>
	        	<td>
					&nbsp;
				</td>
			<c:set var="flag" value="1"></c:set>
	        </c:otherwise>
			</c:choose>
		</c:if>
		</c:forEach>
		<c:if test="${flag=='1'}">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
		</tr>
		</c:if>
	</table>
	
	<table class="inputTable">
		<tr>
			<th colspan="7" style="text-align:center">
				研发属性信息（流程控制）
			</th>
		</tr>
		<tr>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				名称
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				属性值
			</th>
			<th  style="width:1px;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				名称
			</th>
			<th  style="width:15%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				属性值
			</th>
		</tr>
		
		<c:set var="flag" value="0"></c:set>
		<c:forEach var="cp" items="${com.componentPros}">
		
		<c:if test="${flag=='0'}">
		<tr>
		</c:if>
		
		<c:if test="${cp.type.value == 2}">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.name}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.cn}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${cp.value}
			</td>
			<c:choose>
			<c:when test="${flag=='1'}">
			</tr>
			<c:set var="flag" value="0"></c:set>
			</c:when>
			<c:otherwise>
	        	<td>
					&nbsp;
				</td>
			<c:set var="flag" value="1"></c:set>
	        </c:otherwise>
			</c:choose>
		</c:if>
		</c:forEach>
		<c:if test="${flag=='1'}">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
		</tr>
		</c:if>
	</table>
	</div>
</body>
</html>