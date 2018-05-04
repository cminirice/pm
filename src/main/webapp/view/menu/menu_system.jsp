<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>管理菜单 - Powered By GUTTV</title>
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
				<span>管理员&nbsp;</span>
			</dt>
			<c:if test="${fn:contains(sessionScope.auths,'1600')}">
			<dd style="display:none">
				<a href="${base}/User/list" target="mainFrame">管理员列表</a>
			</dd>
			</c:if>
			<c:if test="${fn:contains(sessionScope.auths,'1700')}">
			<dd>
				<a href="${base}/Role/list" target="mainFrame">角色列表</a>
			</dd>
			</c:if>
			<c:if test="${fn:contains(sessionScope.auths,'1800')}">
			<dd>
				<a href="${base}/Auth/list" target="mainFrame">权限列表</a>
			</dd>
			</c:if>
		</dl>

		<dl>
			<dt>
				<span>缓存管理&nbsp;</span>
			</dt>
			<c:if test="${fn:contains(sessionScope.auths,'1900')}">
			<dd>
				<a href="${base}/springconfig/view" target="mainFrame">配置属性列表</a>
			</dd>
			</c:if>
		</dl>
	</div>
</body>
</html>