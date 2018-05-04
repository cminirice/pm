<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>管理中心 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript">
$().ready(function() {

	var $menuItem = $("#menu .menuItem");
	var $previousMenuItem;
	
	$menuItem.click( function() {
		var $this = $(this);
		if ($previousMenuItem != null) {
			$previousMenuItem.removeClass("current");
		}
		$previousMenuItem = $this;
		$this.addClass("current");
	})

})
</script>
</head>
<body class="header">
	<div class="body">
		<div class="bodyLeft">
			<div class="logo"></div>
		</div>
		<div class="bodyRight">
			<div class="link">
				<span class="welcome">
					<strong>${currentUser.name}</strong>&nbsp;您好!&nbsp;
				</span>
				<a href="http://www.guttv.cn" target="_blank">公司首页</a>|
            	<a href="mailto:minimice@126.com?subject=求助&body=您的问题写在这儿！" target="_blank">技术支持</a>|
                <a href="http://www.guttv.cn/a/about/" target="_blank">关于我们</a>|
                <a href="http://www.guttv.cn/list/?id=26" target="_blank">联系我们</a>
			</div>
			<div id="menu" class="menu">
				<ul>
				<c:if test="${singleServer ne true }">
					<li class="menuItem">
						<a href="${base}/view/menu/menu_container.jsp" target="menuFrame" hidefocus>容器管理</a>
					</li>
				</c:if>
					<li class="menuItem">
						<a href="${base}/view/menu/menu_meta.jsp" target="menuFrame" hidefocus>元数据管理</a>
					</li>
					<li class="menuItem">
						<a href="${base}/view/menu/menu_system.jsp" target="menuFrame" hidefocus>系统管理</a>
					</li>
					<li class="home">
						<a href="${base}/view/menu/menu_index.jsp" target="mainFrame" hidefocus>首页</a>
					</li>
	            </ul>
	            <div class="info">
					<c:if test="${currentUser.name!='admin'}">
					<a class="profile" href="${base}/User/userCenter?name=${currentUser.name}" target="mainFrame">个人资料</a>
					</c:if>
					<a class="logout" href="${base}/logout" target="_top">退出</a>
				</div>
			</div>
		</div>
	</div>
</body>
</html>