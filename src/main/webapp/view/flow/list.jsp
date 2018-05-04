<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>流程列表 - Powered By GUTTV</title>
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

	function view(code){
		var width = ($(window).width())*0.9;
		var height = ($(window).height())*0.9;
		$.layer({
            type : 2,
            title: '查看流程信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${base}/flow/view?code=' + code,
            }
        });
	}

	function updateStatus(code,status){
		var desc = "启";
		if(status=='FORBIDDEN'){
			desc = "停";
		}
		$.dialog({type: "warn", content: "确认要"+desc+"用流程", ok: desc+"吧>_<", cancel: "慢~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flow/updateStatus",
				data: "status="+status+"&code="+code,
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
	
	function deleteFlow(code,name){
		$.dialog({type: "warn", content: "确认要删除流程["+name+"]："+ code, ok: "删>_<", cancel: "再想想~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flow/delete",
				data: "code="+code,
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
	
	
	function createFlowExecuteConfig(code,name){
		$.dialog({type: "warn", content: "确认要把流程["+name+"]生成执行配置："+ code, ok: "确认>_<", cancel: "再改改~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flow/createFlowExecuteConfig",
				data: "code="+code,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					
					if(data.status && data.status=='success'){
						$.message({type: data.status, content: "已经生成流程执行配置，具体请在执行列表查看"});
					}else{
						$.message({type: data.status, content: data.message});
					}
				}
			});
		}});
	}
	
	function copyFlow(code,name){
		$.dialog({type: "warn", content: "确认要在流程["+name+"]的基础上创建其它流程", ok: "确认>_<", cancel: "需求没定~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/flow/copyFlow",
				data: "code="+code,
				type: "POST",
				dataType: "json",
				cache: false,
				success: function(data) {
					
					if(data.status && data.status=='success'){
						$.message({type: data.status, content: "复制成功"});
						$("#listForm").submit();
					}else{
						$.message({type: data.status, content: data.message});
					}
				}
			});
		}});
	}
</script>
</head>
<body class="list">
	<div class="bar">
		流程列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
	</div>
	<div class="body">
		<form id="listForm" action="${base}/flow/list" method="post">
			<div class="listBar">
				<c:if test="${fn:contains(sessionScope.auths,'1401')}">
				<input type="button" class="formButton" onclick="location.href='${base}/flow/buildFlow'" value="新建流程" hidefocus />
				</c:if>
				&nbsp;&nbsp;
				<select name="searchBy">
				
					<option value="code" <c:if test="${pager.searchBy eq 'code'}"> selected</c:if>>
						流程编码
					</option>
					<option value="name" <c:if test="${pager.searchBy eq 'name'}"> selected</c:if>>
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
						<a href="#" class="sort"  name="code" hidefocus>流程编码</a>
					</th>
					<th>
						<a href="#"  class="sort" name="name" hidefocus>流程名称</a>
					</th>
					<th>
						<a href="#" class="sort"  name="status" hidefocus>状态</a>
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
							<input type="checkbox" name="ids" value="${entity.id}" />
						</td>
						
						<td>
							${entity.code}
						</td>
						<td>
							${entity.name}
						</td>
						<td>
							<c:choose>
								<c:when test="${entity.status=='NORMAL'}">
									正常
								</c:when>
								<c:when test="${entity.status=='FORBIDDEN'}">
									停用
								</c:when>
							</c:choose>
						</td>
						<td>
							<span title="修改时间"><fmt:formatDate value="${entity.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
						</td>
						<td>
						<c:if test="${fn:contains(sessionScope.auths,'1402')}">
							<c:if test="${entity.status=='NORMAL'}">
								<a href="${base}/flow/buildFlow?code=${entity.code}" title="编辑流程">[编辑流程]</a>
							</c:if>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1403')}">
							<a href="${base}/flow/viewFlow?code=${entity.code}" title="查看流程">[查看流程]</a>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1404')}">
							&nbsp;<a href="javascript:view('${entity.code}');" title="查看基本信息">[查看]</a>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1405')}">
							<c:if test="${entity.status=='NORMAL'}">
							&nbsp;<a href="${base}/flow/input?code=${entity.code}" title="编辑基本信息">[编辑]</a>
							</c:if>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1406')}">
							<c:choose>
								<c:when test="${entity.status=='NORMAL'}">
									&nbsp;<a href="javascript:updateStatus('${entity.code}','FORBIDDEN');" title="停用">[停用]</a>
								</c:when>
								<c:when test="${entity.status.value==0}">
									&nbsp;<a href="javascript:updateStatus('${entity.code}','NORMAL');" title="启用">[启用]</a>
								</c:when>
							</c:choose>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1407')}">
							&nbsp;<a href="javascript:deleteFlow('${entity.code}','${entity.name}');" title="删除">[删除]</a>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1408')}">
							<c:if test="${entity.status=='NORMAL'}">
							&nbsp;<a href="javascript:createFlowExecuteConfig('${entity.code}','${entity.name}');" title="生成执行配置">[执行配置]</a>
							</c:if>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1409')}">
							<c:if test="${entity.status=='NORMAL'}">
							&nbsp;<a href="javascript:copyFlow('${entity.code}','${entity.name}');" title="复制">[复制]</a>
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