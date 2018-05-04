<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>${codeEntity.cn}列表 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${r"${base}"}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${r"${base}"}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/jquery.pager.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/layer.min.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/default/js/admin.js"></script>
<script type="text/javascript">

	function view(id){
		var width = ($(window).width())*0.8;
		var height = ($(window).height())*0.8;
		$.layer({
            type : 2,
            title: '查看${codeEntity.cn}信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${r"${base}"}/${(codeEntity.tableMeta.pkg)!}/view?id=' + id,
            }
        });
	}

</script>
</head>
<body class="list">
	<div class="bar">
		${codeEntity.cn}列表&nbsp;总记录数: ${r"${pager.totalCount}"} (共${r"${pager.pageCount}"}页)
	</div>
	<div class="body">
		<form id="listForm" action="${r"${base}"}/${(codeEntity.tableMeta.pkg)!}/list" method="post">
			<div class="listBar">
				<input type="button" class="formButton" onclick="location.href='${r"${base}"}/${(codeEntity.tableMeta.pkg)!}/add'" value="添加" hidefocus />
				&nbsp;&nbsp;
				<select name="searchBy">
				
					<#list codeEntity.searchFields as search>
					<option value="${search}" <c:if test="${r"${"}pager.searchBy eq '${search}'}"> selected</c:if>>
						${codeEntity.fields[search].cn}
					</option>
					</#list>
					
				</select>
				<input type="text" name="keyword" value="${r"${pager.keyword}"}" />
				<input type="button" id="searchButton" class="formButton" value="搜 索" hidefocus />
				&nbsp;&nbsp;
				<label>每页显示: </label>
				<select name="pageSize" id="pageSize">
					<option value="10"<c:if test="${r"${pager.pageSize eq 10}"}"> selected</c:if>>
						10
					</option>
					<option value="20"<c:if test="${r"${pager.pageSize eq 20}"}"> selected</c:if>>
						20
					</option>
					<option value="50"<c:if test="${r"${pager.pageSize eq 50}"}"> selected</c:if>>
						50
					</option>
					<option value="100"<c:if test="${r"${pager.pageSize eq 100}"}"> selected</c:if>>
						100
					</option>
				</select>
			</div>
			<table id="listTable" class="listTable">
				<tr>
					<th class="check">
						<input type="checkbox" class="allCheck" />
					</th>
					
					<#list codeEntity.listFields as listField>
					<th>
						<a href="#" <#if codeEntity.fields[listField].sort>class="sort"</#if> name="${listField}" hidefocus>${codeEntity.fields[listField].cn}</a>
					</th>
					</#list>
					
					<th>
						<a href="#" name="updateTime" hidefocus>修改日期</a>
					</th>
					<th>
						<span>操作</span>
					</th>
				</tr>
				<c:set var="empt" value="true"></c:set>
				<c:forEach var="entity" items="${r"${pager.result}"}">
				<c:set var="empt" value="false"></c:set>
					<tr>
						<td>
							<input type="checkbox" name="ids" value="${r"${entity.id}"}" />
						</td>
						
						<#list codeEntity.listFields as listField>
						<td>
							${r"${"}entity.${listField}}
						</td>
						</#list>
						
						<td>
							<span title="修改时间"><fmt:formatDate value="${r"${entity.updateTime}"}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
						</td>
						<td>
							<a href="${r"${base}"}/${(codeEntity.tableMeta.pkg)!}/edit?id=${r"${entity.id}"}" title="编辑">[编辑]</a>
							&nbsp;<a href="javascript:view('${r"${entity.id}"}');" title="查看">[查看]</a>
						</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${r"${empt eq false}"}">
				<div class="pagerBar">
					<div class="delete">
						<input type="button" id="deleteButton" class="formButton" url="${r"${base}"}/${(codeEntity.tableMeta.pkg)!}/delete" value="删 除" disabled hidefocus />
					</div>
					<div class="pager">
						<%@ include file="/view/pager.jsp" %>
					</div>
				</div>
			</c:if>
			<c:if test="${r"${empt eq true}"}">
				<div class="noRecord">没有找到任何记录!</div>
			</c:if>
		</form>
	</div>
</body>
</html>