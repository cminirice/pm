<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.guttv.pm.utils.Constants"%>
<%
	response.setHeader("progma", "no-cache");
	response.setHeader("Cache-Control", "no-cache");
	response.setHeader("Cache-Control", "no-store");
	response.setDateHeader("Expires", 0);

	String base = request.getServletContext().getContextPath();
	request.getServletContext().setAttribute("base", base);
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + "/" + base;
	request.getSession().getServletContext().setAttribute("basePath", basePath);
	if(request.getSession().getAttribute(Constants.CURRENT_USER) != null){
		response.sendRedirect(base + Constants.MAIN_URL);
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>用户登陆 - Powered By GUT-TV</title>
<meta name="Author" content="GUT-TV Team" />
<meta name="Copyright" content="GUT-TV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/layer.min.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jQuery.md5.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>

<script type="text/javascript">
$().ready( function() {

	var $loginForm = $("#loginForm");
	var $username = $("#username");
	var $password = $("#password");
	var $captcha = $("#captcha");
	var $captchaImage = $("#captchaImage");
	var $isRememberUsername = $("#isRememberUsername");
	
	// 提交表单验证,记住登录用户名
	$loginForm.submit( function() {
		if ($username.val() == "") {
			$.dialog({type: "warn", content: "请输入您的用户名!", modal: true, autoCloseTime: 3000});
			return false;
		}
		if ($password.val() == "") {
			$.dialog({type: "warn", content: "请输入您的密码!", modal: true, autoCloseTime: 3000});
			return false;
		}
		
		if ($captcha.val() == "") {
			$.dialog({type: "warn", content: "请输入您的验证码!", modal: true, autoCloseTime: 3000});
			return false;
		}
		
		var captchaCheck = true;
		
		
		<c:if test="${checkKaptcha eq true}">
			var url = "${base}/login/checkKaptcha";
			var layerIndex = layer.load(0,1);
			$.ajax({
						url: url,
						data: "j_captcha=" + $captcha.val()+"&timestamp=" + (new Date()).valueOf(),
						type: "POST",
						async: false,
						dataType: "text",
						cache: false,
						success: function(data) {
							if (data == "success") {
								captchaCheck = true;
							}else{
								captchaCheck = false;
							}
						},
						complete:function(data) {
							layer.close(layerIndex)
						}
				});
			</c:if>
			
			if(!captchaCheck){
				$captchaImage.click();
				$.dialog({type: "warn", content: "验证码错误!", modal: true, autoCloseTime: 1000});
				$captcha.select();
				return false;
			}
			$("#passwordMD5").val($.md5($password.val()).toUpperCase());
	});

	// 刷新验证码
	$captchaImage.click( function() {
		var timestamp = (new Date()).valueOf();
		var imageSrc = $captchaImage.attr("src");
		if(imageSrc.indexOf("?") >= 0) {
			imageSrc = imageSrc.substring(0, imageSrc.indexOf("?"));
		}
		imageSrc = imageSrc + "?timestamp=" + timestamp;
		$captchaImage.attr("src", imageSrc);
	});
	
	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "登陆失败：${errMsg}", modal: true, autoCloseTime: 3000});
	</c:if>
});
</script>
</head>
<body class="login">
	<script type="text/javascript">

		// 登录页面若在框架内，则跳出框架
		if (self != top) {
			top.location = self.location;
		};

	</script>
	<div class="blank"></div>
	<div class="blank"></div>
	<div class="blank"></div>
	<div class="body">
		<form id="loginForm" action="${base}/login/checkUser" method="post">
            <table class="loginTable">
            	<tr>
            		<td rowspan="3" >
            			<img src="${base}/theme/default/images/login_logo.gif" alt="流程制作管制平台" />
            		</td>
                    <th>
                    	用户名:
                    </th>
					<td>
                    	<input type="text" id="username" name="username" class="formText" value="admin"/>
                    </td>
                </tr>
                <tr>
					<th>
						密&nbsp;&nbsp;&nbsp;码:
					</th>
                    <td>
                    	<input type="password" id="password" name="pwd" class="formText" value="admin"/>
                    	<input type="hidden" id="passwordMD5" name="password" class="formText"/>
                    </td>
                </tr>
				<tr>
               	<c:if test="${checkKaptcha eq true}">
                	<th>
                		验证码:
                	</th>
                    <td>
                    	<input type="text" id="captcha" name="j_captcha" class="formText captcha" />
                   		<img id="captchaImage" class="captchaImage" src="${base}/login/kaptcha" alt="换一张" />
                    </td>
                </c:if>
                </tr>
                <tr>
                	<td>
                		&nbsp;
                	</td>
                	<th>
                		&nbsp;
                	</th>
                    <td>
                        <input type="button" class="homeButton" value="" onclick="window.open('http://www.guttv.cn')" hidefocus /><input type="submit" class="submitButton" value="登 录" hidefocus />
                    </td>
                </tr>
            </table>
            <div class="powered">
            	COPYRIGHT © 2015-2017 GUTTV.CN ALL RIGHTS RESERVED.
            </div>
            <div class="link">
            	<a href="${basePath}/login">首页</a> |
				<a href="http://www.guttv.cn" target="_blank">环球合一</a> 
            </div>
        </form>
	</div>
</body>
</html>