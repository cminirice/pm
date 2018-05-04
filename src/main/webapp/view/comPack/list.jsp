<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>组件包列表 - Powered By GUTTV</title>
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

	function view(comID) {
		var width = ($(window).width())*0.9;
		var height = ($(window).height())*0.9;
		$.layer({
            type : 2,
            title: '查看组件包信息',
            shadeClose: true,
            area: [width, height],
			iframe : {
				src : '${base}/comPack/view?comID=' + comID,
			}
		});
	}

	//注册组件包
	function regist() {
		$.layer({
			type : 2,
			title : '注册组件',
			shadeClose : true,
			area : [ '500px', '300px' ],
			iframe : {
				src : '${base}/view/comPack/regist.jsp',
			}
		});
	}

	//卸载组件包
	function unRegist(comID) {
		var content = "确定要卸载[" + comID + "]</br>1：鞍前马后，怎么忍心</br>2：V_V</br>3：...";
		$.dialog({
			type : "warn",
			content : content,
			ok : "果断卸载",
			cancel : "再用用",
			modal : true,
			okCallback : function() {
				$.ajax({
					url : "${base}/comPack/unRegist",
					data : "comID=" + comID,
					type : "POST",
					dataType : "json",
					cache : false,
					success : function(data) {
						$.message({
							type : data.status,
							content : data.message
						});
						if (data.status && data.status == 'success') {
							$("#listForm").submit();
						}
					}
				});
			}
		});
	}
</script>
</head>
<body class="list">
	<div class="bar">组件包列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)</div>
	<div class="body">
		<form id="listForm" action="${base}/comPack/list" method="post">
			<div class="listBar">
				<c:if test="${fn:contains(sessionScope.auths,'1101')}">
				<input type="button" class="formButton" onclick="javascript:regist();" value="注册" hidefocus />
				</c:if>
				&nbsp;&nbsp;
				<select name="searchBy">
					<option value="comID"
						<c:if test="${pager.searchBy eq 'comID'}"> selected</c:if>> 组件标识
					</option>
				</select> <input type="text" name="keyword" value="${pager.keyword}" /> 
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
					<th class="check"><input type="checkbox" class="allCheck" />
					</th>

					<th><a href="#" class="sort" name="comID" hidefocus>组件标识</a></th>
					<th><a href="#" class="sort" name="md5" hidefocus>包MD5值</a></th>
					<th><a href="#" class="sort" name="comPackageFilePath" hidefocus>组件包路径</a></th>
					<th><a href="#" class="sort" name="updateTime" hidefocus>修改日期</a>
					</th>
					<th><span>操作</span></th>
				</tr>
				<c:set var="empt" value="true"></c:set>
				<c:forEach var="entity" items="${pager.result}">
					<c:set var="empt" value="false"></c:set>
					<tr>
						<td><input type="checkbox" name="ids" value="${entity.id}" />
						</td>

						<td>${entity.comID}</td>
						<td>${entity.md5}</td>
						<td>${entity.comPackageFilePath}</td>

						<td><span title="修改时间"><fmt:formatDate value="${entity.updateTime}" pattern="yyyy-MM-dd HH:mm:ss" /></span></td>
						<td>
						<c:if test="${fn:contains(sessionScope.auths,'1102')}">
						&nbsp;<a href="javascript:view('${entity.comID}');" title="查看">[查看]</a>
						</c:if>
						<c:if test="${fn:contains(sessionScope.auths,'1103')}">
						&nbsp;<a href="javascript:unRegist('${entity.comID}');" title="卸载">[卸载]</a>
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