<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
	
</script>
</head>
<body class="input">
	<div class="buttonArea">
		<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
	</div>
	<div class="body">
	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				基本信息<span style="width:10%;text-align:right;">&nbsp;<a href="${base}/flowExeConfig/view?flowExeCode=${flowExeConfig.flowExeCode}" title="编辑信息">[编辑信息]</a></span>
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
			<td>
					&nbsp;${flowExeConfig.flow.name}
			</td>
			<th>
				流程状态: 
			</th>
			<td>&nbsp;
			<c:choose>
				<c:when test="${flowExeConfig.flow.status==1}">
					正常
				</c:when>
				<c:when test="${flowExeConfig.flow.status==0}">
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
	
	<table class="inputTable">
		<tr>
			<th colspan="15" style="text-align:center">
				组件信息
			</th>
		</tr>
		<tr>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				流程编码
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点ID
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:3%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				状态
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				类
			</th>
			<th  style="width:1px;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				流程编码
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点ID
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				中文名
			</th>
			<th  style="width:3%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				状态
			</th>
			<th  style="width:20%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				类
			</th>
		</tr>
		
		<c:set var="flag" value="0"></c:set>
		<c:forEach var="node" items="${flowExeConfig.comNodes}">
		
		<c:if test="${flag=='0'}">
		<tr>
		</c:if>
		
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
		<c:choose>
			<c:when test="${node.status==1}">
				正常
			</c:when>
			<c:when test="${node.status==0}">
				停用
			</c:when>
		</c:choose>
		</td>
		<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
			${node.componentClz}
		</td>
		<c:choose>
		<c:when test="${flag=='1'}">
		</tr>
		<c:set var="flag" value="0"></c:set>
		</c:when>
		<c:otherwise>
        	<td>
				&nbsp;
			</td>
		<c:set var="flag" value="1"></c:set>
        </c:otherwise>
		</c:choose>
		</c:forEach>
		<c:if test="${flag=='1'}">
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;
			</td>
		</tr>
		</c:if>
	</table>


	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				通道信息
			</th>
		</tr>
		<tr>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				流程编码
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				通道连线ID
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				起点ID
			</th>
			<th  style="width:16%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				起点类
			</th>
			<th  style="width:9%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				终点ID
			</th>
			<th  style="width:16%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				终点类
			</th>
			<th  style="width:16%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				队列名称
			</th>
			<th  style="width:16%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				分发规则
			</th>
		</tr>
		
		<c:forEach var="dispatch" items="${flowExeConfig.comDispatch}">
		<tr>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.flowCode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.lineID}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.fromNode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.fromComponent}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.toNode}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.toComponent}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.queue}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${dispatch.rule}
			</td>
		</tr>
		</c:forEach>
	</table>
	
	<table class="inputTable">
		<tr>
			<th colspan="10" style="text-align:center">
				属性信息
			</th>
		</tr>
		<tr>
			<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点ID
			</th>
			<th style="width:10%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				节点中文名
			</th>
			<th style="width:80%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				属性信息
			</th>
		</tr>
		
		<c:forEach var="node" items="${flowExeConfig.comNodes}">
		<tr>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${node.nodeID}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				&nbsp;${node.componentCn}
			</td>
			<td style="text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
				<table class="inputTable">
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
						<th  style="width:50%;text-align:center;border-left: 1px solid #dde9f5;border-right: 1px solid #dde9f5;">
							值
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
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
		</c:forEach>
	</table>
	</div>
		<div class="buttonArea">
		<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
	</div>
</body>
</html>