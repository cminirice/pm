<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>环球合一流程制作管理中心 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
</head>
<frameset id="parentFrameset" rows="60,*" cols="*" frameborder="no" border="0" framespacing="0">
	<frame id="headerFrame" name="headerFrame" src="${base}/view/menu/menu_header.jsp" frameborder="no" scrolling="no" noresize="noresize" />
	<frameset id="mainFrameset" name="mainFrameset" cols="130,6,*" frameborder="no" border="0" framespacing="0">
		<frame id="menuFrame" name="menuFrame" src="${base}/view/menu/menu_meta.jsp" frameborder="no" scrolling="no" noresize="noresize" />
		<frame id="middleFrame" name="middleFrame" src="${base}/view/menu/menu_middle.jsp" frameborder="no" scrolling="no" noresize="noresize" />
		<frame id="mainFrame" name="mainFrame" src="${base}/view/menu/menu_index.jsp" frameborder="no" noresize="noresize" />
	</frameset>
</frameset>
<noframes>
	<body>
		noframes
	</body>
</noframes>
</html>