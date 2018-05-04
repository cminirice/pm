<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.guttv.pm.utils.Enums.FlowExecuteStatus"%>
<%
	request.setAttribute("INIT",FlowExecuteStatus.INIT);
	request.setAttribute("STARTING",FlowExecuteStatus.STARTING);
	request.setAttribute("RUNNING",FlowExecuteStatus.RUNNING);
	request.setAttribute("PAUSE",FlowExecuteStatus.PAUSE);
	request.setAttribute("STOPPED",FlowExecuteStatus.STOPPED);
	request.setAttribute("FINISH",FlowExecuteStatus.FINISH);
	request.setAttribute("ERROR",FlowExecuteStatus.ERROR);
	request.setAttribute("LOCKED",FlowExecuteStatus.LOCKED);
	request.setAttribute("FORBIDDEN",FlowExecuteStatus.FORBIDDEN);
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>执行容器中流程执行配置列表 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.pager.js"></script>
<script type="text/javascript" src="${base}/theme/common/jquery/layer.min.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/admin.js"></script>
<script type="text/javascript">

	$().ready( function() {
		<c:if test="${not empty errMsg}">
				$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
		</c:if>
		<c:if test="${not empty message}">
			$.message({type: "success", content: "${message}"});
		</c:if>
	});
	
	
	function stopFlowExecConfig(flowExeCode){
		
		$.dialog({type: "warn", content: "确认要停止流程执行配置", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/containerFlowExeConf/stopFlowExecConfig",
				data: "containerID=${containerID}&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						setTimeout("$('#listForm').submit();",1000);
					}
				}
			});
		}});
	}

	function startFlowExecConfig(flowExeCode){
		$.dialog({type: "warn", content: "确认要启动流程执行配置", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/containerFlowExeConf/startFlowExecConfig",
				data: "containerID=${containerID}&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						setTimeout("$('#listForm').submit();",1000);
					}
				}
			});
		}});
	}

	function initFlowExecConfig(flowExeCode){
		
		$.dialog({type: "warn", content: "确认要初始化流程执行配置", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/containerFlowExeConf/initFlowExecConfig",
				data: "containerID=${containerID}&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						setTimeout("$('#listForm').submit();",1000);
					}
				}
			});
		}});
	}

	function deleteFlowExeConfig(flowExeCode,name){
		$.dialog({type: "warn", content: "确认要删除流程执行配置["+name+"]："+ flowExeCode, ok: "删>_<", cancel: "再想想~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/containerFlowExeConf/deleteFlowExeConfig",
				data: "containerID=${containerID}&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						setTimeout("$('#listForm').submit();",1000);
					}
				}
			});
		}});
	}

</script>
</head>
<body class="list">
	<div class="bar">
		容器[${containerID}]流程执行配置列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
	</div>
	<div class="body">
		<form id="listForm" action="${base}/containerFlowExeConf/list" method="post">
			<div class="listBar">
				<input type="button" class="formButton" onclick="location.href='${base}/containerFlowExeConf/beforeAdd?containerID=${containerID}'" value="添加" hidefocus />
				&nbsp;&nbsp;
				<input type="button" class="formButton" onclick="location.href='${base}/container/list'" value="返回"  hidefocus />
				&nbsp;&nbsp;
				<input type="button" class="formButton" onclick="window.location.reload()" value="刷  新" hidefocus />
				&nbsp;&nbsp;&nbsp;&nbsp;
				<select name="searchBy">
					<option value="flowExeCode" <c:if test="${pager.searchBy eq 'flowExeCode'}"> selected</c:if>>
						执行编码
					</option>
					<option value="flowCode" <c:if test="${pager.searchBy eq 'flowCode'}"> selected</c:if>>
						流程编码
					</option>
					<option value="flowName" <c:if test="${pager.searchBy eq 'flowName'}"> selected</c:if>>
						流程名称
					</option>
				</select>
				<input type="text" name="keyword" value="${pager.keyword}" />
				<input type="button" id="searchButton" class="formButton" value="搜 索" hidefocus />
				&nbsp;&nbsp;
				<label>每页显示: </label>
				<select name="pageSize" id="pageSize">
					<option value="15"<c:if test="${pager.pageSize eq 15}"> selected</c:if>>
						15
					</option>
					<option value="30"<c:if test="${pager.pageSize eq 30}"> selected</c:if>>
						30
					</option>
					<option value="50"<c:if test="${pager.pageSize eq 50}"> selected</c:if>>
						50
					</option>
					<option value="100"<c:if test="${pager.pageSize eq 100}"> selected</c:if>>
						100
					</option>
				</select>
			</div>
			<table id="listTable" class="listTable">
				<tr>
					<th class="check">
						<input type="checkbox" class="allCheck" />
					</th>
					<th>
						<a href="#"  class="sort" name="flowExeCode" hidefocus>流程执行编码</a>
					</th>
					<th>
						<a href="#"  class="sort" name="flowCode" hidefocus>流程编码</a>
					</th>
					<th>
						<a href="#"  class="sort" name="flowName" hidefocus>流程名称</a>
					</th>
					<th>
						<a href="#"  class="sort" name="status" hidefocus>执行状态</a>
					</th>
					<th>
						<a href="#"  name="statusDesc" hidefocus>状态描述</a>
					</th>
					<th>
						<a href="#" class="sort" name="updateTime" hidefocus>修改日期</a>
					</th>
					<th>
						<span>操作</span>
					</th>
				</tr>
				<c:set var="empt" value="true"></c:set>
				<c:forEach var="entity" items="${pager.result}">
				<c:set var="empt" value="false"></c:set>
					<tr>
						<td>
							<input type="checkbox" name="ids" value="${entity.flowExeCode}" />
						</td>
						<td>
							${entity.flowExeCode}
						</td>
						<td>
							${entity.flow.code}
						</td>
						<td>
							${entity.flow.name}
						</td>
						<td>
							${entity.status.name}
						</td>
						<td title="${entity.statusDesc}">
							${fn:substring(entity.statusDesc,0,25)}
						</td>
						<td>
							<span title="修改时间"><fmt:formatDate value="${entity.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
						</td>
						<td>
							&nbsp;<a href="${base}/containerFlowExeConf/view?flowExeCode=${entity.flowExeCode}&containerID=${containerID}" title="查看流程执行配置">[查看]</a>
							&nbsp;<a href="${base}/flowExeConfig/viewFlow?flowExeCode=${entity.flowExeCode}" title="平台中的流程图，与容器真实执行的流程可能存在差异">[源流程图]</a>
							<c:if test="${entity.status.value==RUNNING.value}">
								&nbsp;<a href="javascript:stopFlowExecConfig('${entity.flowExeCode}');" title="停止">[停止]</a>
							</c:if>
							<c:if test="${entity.status.value==INIT.value || entity.status.value==FINISH.value}">
								&nbsp;<a href="javascript:startFlowExecConfig('${entity.flowExeCode}');" title="启动">[启动]</a>
							</c:if>
							<c:if test="${entity.status.value!=INIT.value && entity.status.value!=FINISH.value && entity.status.value!=RUNNING.value}">
								&nbsp;<a href="javascript:initFlowExecConfig('${entity.flowExeCode}',${INIT.value},'强制复位');" title="强制复位">[复位]</a>
							</c:if>
							&nbsp;<a href="javascript:deleteFlowExeConfig('${entity.flowExeCode}','${entity.flow.name}');" title="停止并删除">[删除]</a>
						</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${empt eq false}">
				<div class="pagerBar">
					<div class="pager">
						<%@ include file="/view/pager.jsp" %>
					</div>
				</div>
			</c:if>
			<input type="hidden" name="containerID" id="containerID" value="${containerID}" />
			<c:if test="${empt eq true}">
				<div class="noRecord">没有找到任何记录!</div>
			</c:if>
		</form>
	</div>
</body>
</html>