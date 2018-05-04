<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>执行容器列表 - Powered By GUTTV</title>
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
	
	function view(id){
		var width = ($(window).width())*0.95;
		var height = ($(window).height())*0.95;
		$.layer({
            type : 2,
            title: '查看执行容器信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${base}/container/view?containerID=' + id,
            }
        });
	}
	
	function shutdown(containerID){
		var content = "是否要关闭容器</br>1：会把所有的正在执行的流程停掉</br>2：会把执行容器的进程关闭"
		$.dialog({type: "warn", content: content, ok: "关闭", cancel: "再用用", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/container/shutdown",
				data: "containerID="+containerID,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					setTimeout("$('#listForm').submit();",1000);
				}
			});
		}});
	}
	
	function forbbiden(containerID){
		var content = "是否要禁用容器</br>1：确保执行容器正常运行</br>2：会把执行容器所有任务停止</br>3：不会关闭执行容器进程"
		$.dialog({type: "warn", content: content, ok: "禁用", cancel: "不!!!", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/container/forbbiden",
				data: "containerID="+containerID,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					setTimeout("$('#listForm').submit();",1000);
				}
			});
		}});
	}
	
	function start(containerID){
		var content = "是否要启用容器</br>1：要确认执行容器正常运行</br>2：确认其有干活能力"
		$.dialog({type: "warn", content: content, ok: "启用吧", cancel: "不!!!", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/container/start",
				data: "containerID="+containerID,
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
	
	function deleteContainer(containerID){
		var content = "是否要删除容器</br>1：会把容器注册路径下的所有信息删除</br>2：只有重启才能重新使用"
		$.dialog({type: "warn", content: content, ok: "删吧", cancel: "不!!!", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/container/deleteContainer",
				data: "containerID="+containerID,
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
		执行容器列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
	</div>
	<div class="body">
		<form id="listForm" action="${base}/container/list" method="post">
			<div class="listBar">

				<select name="searchBy">
				
					<option value="alias" <c:if test="${pager.searchBy eq 'alias'}"> selected</c:if>>
						容器别名
					</option>
					<option value="ip" <c:if test="${pager.searchBy eq 'ip'}"> selected</c:if>>
						容器IP
					</option>
					<option value="hostname" <c:if test="${pager.searchBy eq 'hostname'}"> selected</c:if>>
						主机名称
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
					<th>
						<a href="#"  name="containerID" hidefocus>容ID</a>
					</th>
					<th>
						<a href="#" class="sort" name="alias" hidefocus>容器别名</a>
					</th>
					<th>
						<a href="#" class="sort" name="ip" hidefocus>容器IP</a>
					</th>
					<th>
						<a href="#"  name="serverPort" hidefocus>容器端口</a>
					</th>
					<th>
						<a href="#" class="sort" name="hostname" hidefocus>主机名称</a>
					</th>
					<th>
						<a href="#"  name="pid" hidefocus>进程ID</a>
					</th>
					<th>
						<a href="#"  name="contextPath" hidefocus>上下文</a>
					</th>
					<th>
						<a href="#" class="sort" name="heartbeatTime" hidefocus>心跳时间</a>
					</th>
					<th>
						<a href="#" class="sort" name="status" hidefocus>容器状态</a>
					</th>

					<th>
						<a href="#" name="heartbeatPeriod" hidefocus>心跳周期</a>
					</th>
					<th>
						<a href="#" name="onlyContainer" hidefocus title="执行容器的启动模式">仅容器</a>
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
							${entity.containerID}
						</td>
						<td>
							${entity.alias}
						</td>
						<td>
							${entity.ip}
						</td>
						<td>
							${entity.serverPort}
						</td>
						<td>
							${entity.hostname}
						</td>
						<td>
							${entity.pid}
						</td>
						<td>
							${entity.contextPath}
						</td>
						<td>
							${entity.heartbeat.heartbeatTime}
						</td>
						<td>
							${entity.status.name}
						</td>
						<td>
							${entity.heartbeatPeriod}
						</td>
						<td>
							${entity.onlyContainer}
						</td>
						<td>
							<c:if test="${fn:contains(sessionScope.auths,'1001')}">
								&nbsp;<a href="javascript:view('${entity.containerID}');" title="查看">[查看]</a>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1002')}">
								<c:if test="${entity.status.value==1}">
								&nbsp;<a href="${base}/container/input?containerID=${entity.containerID}" title="编辑">[编辑]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1003')}">
								<c:if test="${entity.status.value==1}">
								&nbsp;<a href="${base}/container/viewSpringconfig?containerID=${entity.containerID}" title="查看Springboot配置信息">[配置]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1004')}">
								<c:if test="${entity.status.value==1}">
								&nbsp;<a href="javascript:forbbiden('${entity.containerID}');" title="禁用">[禁用]</a>
								</c:if>
								<c:if test="${entity.status.value==3}">
								&nbsp;<a href="javascript:start('${entity.containerID}');" title="启用">[启用]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1005')}">
								<c:if test="${entity.status.value==1}">
								&nbsp;<a href="${base}/containerFlowExeConf/list?containerID=${entity.containerID}" title="执行流程配置列表">[流程]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1006')}">
								<c:if test="${entity.status.value==1}">
								&nbsp;<a href="javascript:shutdown('${entity.containerID}');" title="关闭">[关闭]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1007')}">
								<c:if test="${entity.status.value!=1}">
								&nbsp;<a href="javascript:deleteContainer('${entity.containerID}');" title="删除">[删除]</a>
								</c:if>
							</c:if>
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
			<c:if test="${empt eq true}">
				<div class="noRecord">没有找到任何记录!</div>
			</c:if>
		</form>
	</div>
</body>
</html>