<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>执行容器管理 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<script type="text/javascript"
	src="${base}/theme/common/jquery/jquery.js"></script>
<link href="${base}/theme/default/css/base.css" rel="stylesheet"
	type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet"
	type="text/css" />
<script type="text/javascript">
	$().ready(function() {
		$("dt").each(function(i) {
			$(this).bind('click', function() {
				var dds = $(this).nextAll("dd");
				$(dds).each(function(i) {
					var dis = $(this).css("display");
					if ("none" == dis) {
						$(this).css("display", "block");
					} else {
						$(this).css("display", "none");
					}
				});
			});
		});

		$("dd").each(function(i) {
			$(this).css("display", "none");
		});
	});
</script>
</head>
<body class="menu">
	<div class="body">
		<dl>
			<dt>
				<span>执行容器&nbsp;</span>
			</dt>
			<c:if test="${fn:contains(sessionScope.auths,'1000')}">
			<dd>
				<a href="${base}/container/list" target="mainFrame">执行容器列表</a>
			</dd>
			</c:if>
			<!-- 
			<!-- -->
			<!--dd>
				<a href="${base}/server/list" target="mainFrame">服务器列表</a>
			</dd-->
			<!-- dd>
				<a href="${base}/plugin/list" target="mainFrame">插件列表</a>
			</dd-->
			<!--dd>
				<a href="${base}/script/list" target="mainFrame">脚本列表</a>
			</dd-->
			<!-- dd>
				<a href="${base}/containerVersion/list" target="mainFrame">容器程序列表</a>
			</dd-->

		</dl>
	</div>
</body>
</html>
