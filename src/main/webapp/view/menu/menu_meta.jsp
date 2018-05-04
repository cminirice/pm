<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>元数据管理 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript">
$().ready(function(){
	$("dt").each(function(i){
		$(this).bind('click',function(){
			var dds = $(this).nextAll("dd");
			$(dds).each(function(i){
				var dis = $(this).css("display");
				if("none" == dis){
					$(this).css("display","block");
				}else{
					$(this).css("display","none");	
				}
			});
		});
	});
	
	$("dd").each(function(i){
		$(this).css("display","none");
	});
});
</script>
</head>
<body class="menu">
	<div class="body">
		<dl>
			<dt>
				<span>组件&nbsp;</span>
			</dt>
			<c:if test="${fn:contains(sessionScope.auths,'1100')}">
			<dd>
				<a href="${base}/comPack/list" target="mainFrame">组件包列表</a>
			</dd>
			</c:if>
			<c:if test="${fn:contains(sessionScope.auths,'1200')}">
			<dd>
				<a href="${base}/component/list" target="mainFrame">组件列表</a>
			</dd>
			</c:if>
		</dl>
		<dl>
			<dt>
				<span>流程&nbsp;</span>
			</dt>
			<c:if test="${fn:contains(sessionScope.auths,'1300')}">
			<dd>
				<a href="${base}/flow/buildFlow" target="mainFrame">流程制作</a>
			</dd>
			</c:if>
			<c:if test="${fn:contains(sessionScope.auths,'1400')}">
			<dd>
				<a href="${base}/flow/list" target="mainFrame">流程列表</a>
			</dd>
			</c:if>
			<c:if test="${fn:contains(sessionScope.auths,'1500')}">
			<dd>
				<a href="${base}/flowExeConfig/list" target="mainFrame">执行列表</a>
			</dd>
			</c:if>
			<c:if test="${singleServer eq true }">
			<dd>
				<a href="${base}/view/flowExeConfig/task.jsp" target="mainFrame">任务列表</a>
			</dd>
			</c:if>
		</dl>
	</div>
</body>
</html>