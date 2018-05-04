<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>查看执行容器信息 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet"
	type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet"
	type="text/css" />
<script type="text/javascript"
	src="${base}/theme/common/jquery/jquery.js"></script>
</head>
<body class="input">
	<div class="body">
		<table class="inputTable">
			<tr>
				<th>容器别名:</th>
				<td>&nbsp;${executeContainer.alias}</td>
				<th>容器IP:</th>
				<td>&nbsp;${executeContainer.ip}</td>
			</tr>
			<tr>
				<th>容器端口:</th>
				<td>&nbsp;${executeContainer.serverPort}</td>
				<th>主机名称:</th>
				<td>&nbsp;${executeContainer.hostname}</td>
			</tr>
			<tr>
				<th>进程ID:</th>
				<td>&nbsp;${executeContainer.pid}</td>
				<th>启动时间:</th>
				<td>&nbsp;${executeContainer.latestStartTime}</td>
			</tr>
			<tr>
				<th>上下文:</th>
				<td>&nbsp;${executeContainer.contextPath}</td>
				<th>检测路径:</th>
				<td>&nbsp;http://${executeContainer.ip}:${executeContainer.serverPort}${executeContainer.contextPath}/pingContainer</td>
			</tr>
			<tr>
				<th>容器状态:</th>
				<td>&nbsp;${executeContainer.status}</td>
				<th>容ID:</th>
				<td>&nbsp;${executeContainer.containerID}</td>
			</tr>
			<tr>
				<th>RPC端口:</th>
				<td>&nbsp;${executeContainer.rpcServerPort}</td>
				<th>IP列表:</th>
				<td>&nbsp;${executeContainer.ipList}</td>
			</tr>
			<tr>
				<th>用户目录:</th>
				<td>&nbsp;${executeContainer.userDir}</td>
				<th>心跳周期:</th>
				<td>&nbsp;${executeContainer.heartbeatPeriod}</td>
			</tr>
			<tr>
				<th>注册路径:</th>
				<td>&nbsp;${executeContainer.registPath}</td>
				<th>容器目录:</th>
				<td>&nbsp;${executeContainer.location}</td>
			</tr>
			<tr>
				<th>创建时间:</th>
				<td>&nbsp;${executeContainer.createTime}</td>
				<th>启动用户:</th>
				<td>&nbsp;${executeContainer.operator}</td>
			</tr>
			<tr>
				<th>更新时间:</th>
				<td>&nbsp;${executeContainer.updateTime}</td>
				<th>备注:</th>
				<td>&nbsp;${executeContainer.remark}</td>
			</tr>
			<tr>
				<th>状态描述:</th>
				<td>&nbsp;${executeContainer.statusDesc}</td>
				<th>仅容器</th>
				<td>&nbsp;${executeContainer.onlyContainer}</td>
			</tr>
			<tr>
				<th>心跳信息:</th>
				<td colspan="3"><c:if
						test="${not empty executeContainer.heartbeat}">
						<table class="inputTable">
							<tr>
								<th>心跳时间:</th>
								<td>&nbsp;${executeContainer.heartbeat.heartbeatTime}</td>
							</tr>
							<c:forEach items="${executeContainer.heartbeat.info}" var="info">
								<tr>
									<th>${info.key}:</th>
									<td>&nbsp;${info.value}</td>
								</tr>
							</c:forEach>
						</table>
					</c:if></td>
			</tr>
		</table>
	</div>
</body>
</html>