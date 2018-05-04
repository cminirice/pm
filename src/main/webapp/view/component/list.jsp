<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>组件列表 - Powered By GUTTV</title>
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
	<c:if test="${not empty message}">
		$.message({type: "success", content: "${message}"});
	</c:if>
	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "修改失败：${errMsg}", modal: true, autoCloseTime: 3000});
	</c:if>
});

	function view(clz){
		var width = ($(window).width())*0.95;
		var height = ($(window).height())*0.95;
		$.layer({
            type : 2,
            title: '查看组件信息',
            shadeClose: true,
            area: [width, height],
            iframe: {
                src : '${base}/component/view?clz=' + clz,
            }
        });
	}
	
	function updateStatus(clz,status){
		var desc = "启动";
		if(status==0){
			desc = "停用";
		}
		$.dialog({type: "warn", content: "是否要["+desc+"]用组件：" + clz, ok: "改吧>_<", cancel: "稍等~~", modal: true, okCallback: function () {
			$.ajax({
				url: "${base}/component/updateStatus",
				data: "status="+status+"&clz="+clz,
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
	
</script>
</head>
<body class="list">
	<div class="bar">
		组件列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
	</div>
	<div class="body">
		<form id="listForm" action="${base}/component/list" method="post">
			<div class="listBar">
				
				&nbsp;&nbsp;
				<select name="searchBy">
					<option value="comID" <c:if test="${pager.searchBy eq 'comID'}"> selected</c:if>>
						组件标识
					</option>
					<option value="group" <c:if test="${pager.searchBy eq 'group'}"> selected</c:if>>
						所属组
					</option>
					<option value="name" <c:if test="${pager.searchBy eq 'name'}"> selected</c:if>>
						名称
					</option>
					<option value="cn" <c:if test="${pager.searchBy eq 'cn'}"> selected</c:if>>
						中文名
					</option>
					<option value="clz" <c:if test="${pager.searchBy eq 'clz'}"> selected</c:if>>
						类名
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
					<th class="check" >
						<input type="checkbox" class="allCheck" />
					</th>
					
					<th>
						<a href="#" class="sort" name="comID" hidefocus>组件标识</a>
					</th>
					<th>
						<a href="#" class="sort" name="group" hidefocus>所属组</a>
					</th>
					<th>
						<a href="#" class="sort" name="name" hidefocus>名称</a>
					</th>
					<th>
						<a href="#" class="sort" name="cn" hidefocus>中文名</a>
					</th>
					<!-- th>
						<a href="#" class="sort"  name="clz" hidefocus>类名</a>
					</th-->
					<th>
						<a href="#"  class="sort" name="method" hidefocus>方法名</a>
					</th>
					<th>
						<a href="#" class="sort"  name="runType" hidefocus>运行类型</a>
					</th>
					<th>
						<a href="#" class="sort" name="updateTime" hidefocus>修改日期</a>
					</th>
					<th>
						<a href="#" class="sort" name="status" hidefocus>状态</a>
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
							${entity.comID}
						</td>
						<td>
							${entity.group}
						</td>
						<td>
							${entity.name}
						</td>
						<td>
							${entity.cn}
						</td>
						<!--td>
							${entity.clz}
						</td-->
						<td>
							${entity.method}
						</td>
						<td>
							${entity.runType}
						</td>
						<td>
							<span title="修改时间"><fmt:formatDate value="${entity.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
						</td>
						<td>
							<c:choose>
								<c:when test="${entity.status==1}">
									正常
								</c:when>
								<c:when test="${entity.status==0}">
									停用
								</c:when>
							</c:choose>
						</td>
						<td>
							<c:if test="${fn:contains(sessionScope.auths,'1201')}">
								&nbsp;<a href="javascript:view('${entity.clz}');" title="查看">[查看]</a>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1202')}">
								<c:if test="${entity.status==1}">
									&nbsp;<a href="javascript:updateStatus('${entity.clz}',0);" title="停用">[停用]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1203')}">
								<c:if test="${entity.status==1}">
									&nbsp;<a href="javascript:window.location.href='${base}/component/beforeUpdateProperties?clz=${entity.clz}'" title="修改默认属性值">[修改属性]</a>
								</c:if>
							</c:if>
							<c:if test="${fn:contains(sessionScope.auths,'1204')}">
								<c:if test="${entity.status==0}">
									&nbsp;<a href="javascript:updateStatus('${entity.clz}',1);" title="启用">[启用]</a>
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