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
<title>流程执行配置列表 - Powered By GUTTV</title>
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
	
	function view(flowExeCode){
		var width = ($(window).width())*0.95;
		var height = ($(window).height())*0.95;
		$.layer({
            type : 2,
            title: '查看流程执行配置信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${base}/flowExeConfig/view?flowExeCode=' + flowExeCode,
            }
        });
	}
	
	function tasksList(flowExeCode){
		var width = ($(window).width())*0.9;
		var height = ($(window).height())*0.95;
		$.layer({
            type : 2,
            title: '查看流程执行任务信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${base}/view/flowExeConfig/flowTask.jsp?flowExeCode=' + flowExeCode,
            }
        });
	}

	function updateStatus(flowExeCode,status,statusDesc){
		
		$.dialog({type: "warn", content: "确认要["+statusDesc+"]流程执行配置", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/updateStatus",
				data: "status="+status+"&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						$("#listForm").submit();
					}
				}
			});
		}});
	}
	
	function deleteFlowExeConfig(flowExeCode,name){
		$.dialog({type: "warn", content: "确认要删除流程执行配置["+name+"]："+ flowExeCode, ok: "删>_<", cancel: "再想想~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/delete",
				data: "flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					$("#listForm").submit(); //改为不管成功失败，都更新列表
					if(data.status && data.status=='success'){
						
					}
				}
			});
		}});
	}

	function rebuild(flowExeCode){
		var content = "是否要重新生成流程执行配置？<br />1：由原流程重新生成执行配置<br/>2：保留原有节点属性值及通道配置<br />3：<font color='red'>只会增减</font>节点及通道<br/>4：节点ID变更后，不保留属性值<br/>5：通道两端节点变更不保留信息";
		$.dialog({type: "warn", content: content , ok: "重铸吧", cancel: "再改改~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/rebuild",
				data: "flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					if(data.status && data.status=='success'){
						$("#listForm").submit(); //改为不管成功失败，都更新列表
					}
				}
			});
		}});
	}
	
	function recovery(flowExeCode){
		var content = "是否要还原流程图？<br />1：确认原流程图已经不存在<br/>2：否则需要先删除原流程图<br />3：或者把原流程图复制备份后删除";
		$.dialog({type: "warn", content: content , ok: "还原", cancel: "稍后~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/recovery",
				data: "flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
				}
			});
		}});
	}
</script>
</head>
<body class="list">
	<div class="bar">
		流程执行配置列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
	</div>
	<div class="body">
		<form id="listForm" action="${base}/flowExeConfig/list" method="post">
			<div class="listBar">
				&nbsp;&nbsp;
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
							<c:if test="${fn:contains(sessionScope.auths,'1501')}">
								&nbsp;<a href="${base}/flowExeConfig/view?flowExeCode=${entity.flowExeCode}" title="查看流程执行配置">[查看]</a>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1502')}">
								&nbsp;<a href="${base}/flowExeConfig/viewFlow?flowExeCode=${entity.flowExeCode}" title="查看流程图">[查看流程]</a>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1503')}">
								<c:if test="${entity.status.value==INIT.value || entity.status.value==FINISH.value}">
									&nbsp;<a href="javascript:rebuild('${entity.flowExeCode}');" title="由原流程生新生成">[重生]</a>
								</c:if>
							</c:if>
							<c:if test="${singleServer eq true }">
								<c:if test="${fn:contains(sessionScope.auths,'1506')}">
									<c:if test="${entity.status.value==RUNNING.value || entity.status.value==PAUSE.value}">
									&nbsp;<a href="javascript:tasksList('${entity.flowExeCode}');" title="查看任务状态">[任务]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1507')}">
									<c:if test="${entity.status.value==RUNNING.value || entity.status.value==PAUSE.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${STOPPED.value},'停止');" title="停止">[停止]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1508')}">
									<c:if test="${entity.status.value==PAUSE.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${STARTING.value},'继续执行');" title="继续执行">[继续]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1509')}">
									<c:if test="${entity.status.value==RUNNING.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${PAUSE.value},'暂停');" title="暂停">[暂停]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1510')}">
									<c:if test="${entity.status.value==INIT.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${STARTING.value},'启动');" title="启动">[启动]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1511')}">
									<c:if test="${entity.status.value==FINISH.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${STARTING.value},'重新启动');" title="启动">[重启]</a>
									</c:if>
								</c:if>
								<c:if test="${fn:contains(sessionScope.auths,'1512')}">
									<c:if test="${entity.status.value==ERROR.value || entity.status.value==STARTING.value || entity.status.value==LOCKED.value}">
										&nbsp;<a href="javascript:updateStatus('${entity.flowExeCode}',${INIT.value},'强制复位');" title="强制复位">[复位]</a>
									</c:if>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1504')}">
								<c:if test="${entity.status.value==INIT.value || entity.status.value==FINISH.value || entity.status.value==ERROR.value}">
									&nbsp;<a href="javascript:deleteFlowExeConfig('${entity.flowExeCode}','${entity.flow.name}');" title="删除">[删除]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1505')}">
								&nbsp;<a href="javascript:recovery('${entity.flowExeCode}');" title="恢复原流程图">[还原]</a>
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