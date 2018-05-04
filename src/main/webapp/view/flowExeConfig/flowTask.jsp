<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.List"%>
<%@ page import="com.guttv.pm.core.cache.TaskCache"%>
<%@ page import="com.guttv.pm.core.task.AbstractTask"%>
<%@ page import="com.guttv.pm.core.task.AbstractRecycleTask"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>流程执行配置信息 - Powered By GUTTV</title>
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
	function changeDisplay(id){
		if($("#"+id).is(":hidden")){
      		 $("#"+id).show();    //如果元素为隐藏,则将它显现
		}else{
		      $("#"+id).hide();     //如果元素为显现,则将其隐藏
		}
	}
</script>
</head>
<body class="input">
	<div class="body">

	<table class="inputTable">
		<tr>
		<th style="text-align:center" colspan="10">
		&nbsp;&nbsp;<input type="button" class="formButton" onclick="window.location.reload()" value="刷  新" hidefocus />
		</th>
		</tr>
		<tr>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				任务ID
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				任务名称
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点ID
			</th>
			<th  style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				线程状态
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				心跳时间
			</th>
			<th  style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				是否暂停
			</th>
		</tr>
		
		<%
			String flowExeCode = request.getParameter("flowExeCode");
			if(flowExeCode==null || flowExeCode.trim().length() == 0){ %>
		<tr>
			<td  colspan="10"  style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;流程编码为空
			</td>
		</tr>
			<%} %>
		<%
			List<AbstractTask> tasks = TaskCache.getInstance().getTasksByFlowExeCode(flowExeCode);
			if(tasks==null||tasks.size()==0){%>
		<tr>
			<td  colspan="10"  style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<font color="red">没有任何任务</font>
			</td>
		</tr>
		<%}else{
			for(AbstractTask at : tasks){
		 %>
		<tr ondblclick="javascript:changeDisplay('<%=at.getId()%>')" title="双击显示任务线程堆栈" >
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%=at.getId() %>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%=at.getName() %>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%=at.getNodeID() %>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%=at.getThreadState()%>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%=format.format(new Date(at.getLastHeartBeatTime())) %>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;<%if(at instanceof AbstractRecycleTask){
					AbstractRecycleTask art = (AbstractRecycleTask)at;
					%>
					<%=art.isPause()%>
				<%}%>
			</td>
		</tr>
		<tr id="<%=at.getId()%>" style="display:none">
			<td colspan="10">
				<%String stack = at.getThreadStackTrace();
					if (stack == null) stack="";
					stack = stack.replace("\r\n","<br/>"); 
					stack = stack.replace("com.guttv","<font color='red'>com.guttv</font>");
					if(stack.trim().length() > 0){%>
				<p style="word-wrap: break-word"><%=stack%></p>
				<%}else{ %>
				<font color="red">没有任何堆栈信息，确认线程是否终止。</font>
				<%} %>
			</td>
		</tr>
		<%}} %>
	</table>
	</div>
</body>
</html>