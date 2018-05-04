<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.guttv.pm.utils.Enums.ComponentNodeStatus"%>
<%
	request.setAttribute("INIT",ComponentNodeStatus.INIT);
	request.setAttribute("STARTING",ComponentNodeStatus.STARTING);
	request.setAttribute("RUNNING",ComponentNodeStatus.RUNNING);
	request.setAttribute("PAUSE",ComponentNodeStatus.PAUSE);
	request.setAttribute("STOPPED",ComponentNodeStatus.STOPPED);
	request.setAttribute("FINISH",ComponentNodeStatus.FINISH);
	request.setAttribute("ERROR",ComponentNodeStatus.ERROR);
	request.setAttribute("LOCKED",ComponentNodeStatus.LOCKED);
	request.setAttribute("FORBIDDEN",ComponentNodeStatus.FORBIDDEN);
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
	$().ready( function() {
		<c:if test="${not empty errMsg}">
				$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
		</c:if>
		<c:if test="${not empty message}">
			$.message({type: "success", content: "${message}"});
		</c:if>
	});

	function changeDisplay(id){
		if($("#"+id).is(":hidden")){
      		 $("#"+id).show();    //如果元素为隐藏,则将它显现
		}else{
		      $("#"+id).hide();     //如果元素为显现,则将其隐藏
		}
	}
	
	function updateProperties(flowExeCode,clz,nodeID){
		window.location.href='${base}/flowExeConfig/beforeUpdateProperties?flowExeCode=' + flowExeCode+'&nodeID='+nodeID+'&clz='+clz;
	}
	
	function updateNodeStatus(flowExeCode,nodeID,status,statusDesc){
		
		$.dialog({type: "warn", content: "确认要["+statusDesc+"]流程执行配置", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/updateNodeStatus",
				data: "status="+status+"&flowExeCode="+flowExeCode+"&nodeID="+nodeID,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					window.location.href='${base}/flowExeConfig/view?flowExeCode='+flowExeCode;
				}
			});
		}});
	}
	
	function updateFlowExecName(flowExeCode){
		var srcName = $("#flowExecNameDiv").html().trim();
		var name = $("#flowExecNameInput").val().trim();
		
		if(name=="" || srcName==("&nbsp;"+name)){
			$('#flowExecNameInputDiv').hide();
			$('#flowExecNameDiv').show();
			return;
		}
		
		$.dialog({type: "warn", content: "确认要修改流程执行配置名称为["+name+"]", ok: "OK >_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flowExeConfig/updateFlowExecName",
				data: "name="+name+"&flowExeCode="+flowExeCode,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					$.message({type: data.status, content: data.message});
					window.location.href='${base}/flowExeConfig/view?flowExeCode='+flowExeCode;
				}
			});
		},cancelCallback: function () {
			$('#flowExecNameInputDiv').hide();
			$('#flowExecNameDiv').show();
		}});
	}
</script>
</head>
<body class="input">
	<div class="buttonArea">
		<input type="button" class="formButton" onclick="window.location.href='${base}/flowExeConfig/list'" value="返  回" hidefocus />
		&nbsp;&nbsp;<input type="button" class="formButton" onclick="window.location.reload()" value="刷  新" hidefocus />
	</div>
	<div class="body">
	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				基本信息(<font color="red"><b>双击流程名称编辑</b></font>)
			</th>
		</tr>
		<tr>
			<th>
				流程配置编码: 
			</th>
			<td>
					&nbsp;${flowExeConfig.flowExeCode}
			</td>
			<th>
				流程编码: 
			</th>
			<td>
					&nbsp;${flowExeConfig.flow.code}
			</td>
		</tr>
		<tr>
			<th>
				流程配置状态: 
			</th>
			<td>
					&nbsp;${flowExeConfig.status.name}
			</td>
			<th>
				流程配置状态描述: 
			</th>
			<td>
					&nbsp;${flowExeConfig.statusDesc}
			</td>
		</tr>
		<tr>
			<th>
				流程名称: 
			</th>
			<td ondblclick="javascript:$('#flowExecNameInputDiv').show();$('#flowExecNameDiv').hide();">
				<div id="flowExecNameDiv">&nbsp;${flowExeConfig.flow.name}</div>
				<div  id="flowExecNameInputDiv" style="display:none;">
				<input type="text" id="flowExecNameInput" value="${flowExeConfig.flow.name}"  maxLength="128"/>
				<c:if test="${fn:contains(sessionScope.auths,'1513')}">
				<input type="button" class="formButton" value="保存"  onclick="javascript:updateFlowExecName('${flowExeConfig.flowExeCode}')"/>
				<input type="button" class="formButton" value="取消"  onclick="javascript:$('#flowExecNameInputDiv').hide();$('#flowExecNameDiv').show();"/>
				</c:if>
				</div>
			</td>
			<th>
				流程状态: 
			</th>
			<td>&nbsp;
			<c:choose>
				<c:when test="${flowExeConfig.flow.status=='NORMAL'}">
					正常
				</c:when>
				<c:when test="${flowExeConfig.flow.status=='FORBIDDEN'}">
					停用
				</c:when>
			</c:choose>
			</td>
		</tr>
		<tr>
			<th>
				生成时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${flowExeConfig.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
			<th>
				最后修改时间: 
			</th>
			<td>
				&nbsp;<fmt:formatDate value="${flowExeConfig.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
	</table>
	
	<div class="blank"></div>
	
	<table class="inputTable">
		<tr>
			<th colspan="15" style="text-align:center">
				组件信息
				<c:if test="${flowExeConfig.status.value==INIT.value || flowExeConfig.status.value==FINISH.value}">
				<c:if test="${fn:contains(sessionScope.auths,'1514')}">
					&nbsp;<a href="javascript:window.location.href='${base}/flowExeConfig/beforeUpdateAllProperties?flowExeCode=${flowExeConfig.flowExeCode}'" title="修改所有组件普通属性">[<font color="red">修改属性</font>]</a>
				</c:if>
				</c:if>
			</th>
		</tr>
		<tr>
			<th  style="width:12%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				流程编码
			</th>
			<th  style="width:12%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点ID
			</th>
			<th  style="width:12%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:5%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				状态
			</th>
			<th  style="width:30%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				类
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				修改时间
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				操作
			</th>
		</tr>
		
		<c:forEach var="node" items="${flowExeConfig.comNodes}">
		<tr ondblclick="javascript:changeDisplay('pro${node.nodeID}tr')">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${node.flowCode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${node.nodeID}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${node.componentCn}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${node.status.name}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${node.componentClz}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				<fmt:formatDate value="${node.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				<c:if test="${node.status.value==INIT.value || node.status.value==FINISH.value}">
				<c:if test="${fn:contains(sessionScope.auths,'1515')}">
					&nbsp;<a href="javascript:updateProperties('${flowExeConfig.flowExeCode}','${node.componentClz}','${node.nodeID}');" title="修改本组件所有属性">[修改属性]</a>
				</c:if>
				</c:if>
				<c:if test="${node.status.value==INIT.value || node.status.value==FINISH.value}">
				<c:if test="${fn:contains(sessionScope.auths,'1516')}">
					&nbsp;<a href="javascript:updateNodeStatus('${flowExeConfig.flowExeCode}','${node.nodeID}',${FORBIDDEN.value},'禁用');" title="禁用">[禁用]</a>
				</c:if>
				</c:if>
				<c:if test="${node.status.value==FORBIDDEN.value}">
				<c:if test="${fn:contains(sessionScope.auths,'1518')}">
					&nbsp;<a href="javascript:updateNodeStatus('${flowExeConfig.flowExeCode}','${node.nodeID}',${INIT.value},'重新启用');" title="重新启用">[启用]</a>
				</c:if>
				</c:if>
				<c:if test="${singleServer eq true }">
					<c:if test="${node.status.value==RUNNING.value || node.status.value==PAUSE.value}">
						<c:if test="${fn:contains(sessionScope.auths,'1519')}">
						&nbsp;<a href="javascript:updateNodeStatus('${flowExeConfig.flowExeCode}','${node.nodeID}',${STOPPED.value},'停止');" title="停止">[停止]</a>
						</c:if>
					</c:if>
					<c:if test="${node.status.value==PAUSE.value}">
						<c:if test="${fn:contains(sessionScope.auths,'1520')}">
						&nbsp;<a href="javascript:updateNodeStatus('${flowExeConfig.flowExeCode}','${node.nodeID}',${STARTING.value},'继续执行');" title="继续执行">[继续]</a>
						</c:if>
					</c:if>
					<c:if test="${node.status.value==RUNNING.value}">
						<c:if test="${fn:contains(sessionScope.auths,'1521')}">
						&nbsp;<a href="javascript:updateNodeStatus('${flowExeConfig.flowExeCode}','${node.nodeID}',${PAUSE.value},'暂停');" title="暂停">[暂停]</a>
						</c:if>
					</c:if>
				</c:if>
			</td>
		</tr>
		<tr id="pro${node.nodeID}tr" style="display:none">
			<td colspan="10">
				<table class="inputTable">
					<tr>
						<th colspan="7" style="text-align:center">
							属性信息
						</th>
					</tr>
					<tr>
						<th  style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							类型
						</th>
						<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							中文名
						</th>
						<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							英文名
						</th>
						<th  style="width:40%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							值
						</th>
						<th  style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							修改时间
						</th>
					</tr>
					
					<c:forEach var="pro" items="${flowExeConfig.comFlowProsMap[node.nodeID]}">
					<tr>
						<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							&nbsp;${pro.type}
						</td>
						<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							&nbsp;${pro.cn}
						</td>
						<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							&nbsp;${pro.name}
						</td>
						<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							&nbsp;${pro.value}
						</td>
						<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							&nbsp;<fmt:formatDate value="${pro.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
						</td>
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		</c:forEach>
	</table>

	<div class="blank"></div>

	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				通道信息 &nbsp;
				<c:if test="${flowExeConfig.status.value==INIT.value || flowExeConfig.status.value==FINISH.value}">
				<c:if test="${fn:contains(sessionScope.auths,'1517')}">
					&nbsp;<a href="javascript:window.location.href='${base}/flowExeConfig/beforeUpdateDispatch?flowExeCode=${flowExeConfig.flowExeCode}'" title="修改通道">[<font color="red">修改通道</font>]</a>
				</c:if>
				</c:if>
			</th>
		</tr>
		<tr>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				通道连线ID
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				起点ID
			</th>
			<th  style="width:18%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				起点类
			</th>
			<th  style="width:8%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				终点ID
			</th>
			<th  style="width:18%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				终点类
			</th>
			<th  style="width:16%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				队列名称
			</th>
			<th  style="width:13%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				分发规则
			</th>
			<th  style="width:12%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				修改时间
			</th>
		</tr>
		
		<c:forEach var="dispatch" items="${flowExeConfig.comDispatchs}">
		<tr>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.lineID}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.fromNode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.fromComponent}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.toNode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.toComponent}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.queue}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${dispatch.rule}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				<fmt:formatDate value="${dispatch.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
		</c:forEach>
	</table>
	
	<div class="blank"></div>

	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				状态描述历史信息
			</th>
		</tr>
		<c:forEach var="statusDesc" items="${flowExeConfig.statusDescList}">
		<tr>
			<td style="text-align:left;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				${statusDesc.desc}
			</td>
			<td style="text-align:left;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				<fmt:formatDate value="${statusDesc.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/>
			</td>
		</tr>
		</c:forEach>
	</table>
	<div class="buttonArea">
		<input type="button" class="formButton" onclick="window.location.href='${base}/flowExeConfig/list'" value="返  回" hidefocus />
		&nbsp;&nbsp;<input type="button" class="formButton" onclick="window.location.reload()" value="刷  新" hidefocus />
	</div>
	</div>
</body>
</html>