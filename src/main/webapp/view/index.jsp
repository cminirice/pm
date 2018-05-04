<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String base = request.getSession().getServletContext().getContextPath();
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+base+"/";
	response.sendRedirect(base + "/view/login.jsp");
%>