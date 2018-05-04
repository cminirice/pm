<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  Created by IntelliJ IDEA.
  User: 闫涛
  Date: 2018/1/12
  Time: 18:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>角色管理 - Powered By GUTTV</title>
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
</head>
<body class="list">
    <div class="body">
        <form id="listForm" action="${base}/Role/list" method="post">
            <div class="listBar">
                <c:if test="${fn:contains(sessionScope.auths,'1701')}">
                <input type="button" class="formButton" onclick="javascript:insert();" value="新增" hidefocus />
                </c:if>
            </div>
            <table id="listTable" class="listTable">
                <tr>
                    <th><a href="#" class="sort" name="id" hidefocus>角色id</a></th>
                    <th><a href="#" class="sort" name="name" hidefocus>角色name</a></th>
                    <th><a href="#" class="sort" name="description" hidefocus>角色描述</a></th>
                    <th><a href="#" class="sort" name="email" hidefocus>创建时间</a></th>
                    <th><a href="#" class="sort" name="email" hidefocus>修改时间</a></th>
                    <th><span>操作</span></th>
                </tr>
                <c:set var="empt" value="true"></c:set>
                <c:forEach var="role" items="${roleList}">
                    <c:set var="empt" value="false"></c:set>
                    <tr>
                        <td>${role.id}</td>
                        <td>${role.name}</td>
                        <td>${role.description}</td>
                        <td><fmt:formatDate value="${role.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                        <td><fmt:formatDate value="${role.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                        <td>
                            <c:if test="${fn:contains(sessionScope.auths,'1702')}">
                            &nbsp;<a href="javascript:view('${role.id}');" title="修改">[修改]</a>
                            </c:if>
                            <c:if test="${fn:contains(sessionScope.auths,'1703')}">
                            &nbsp;<a href="javascript:del('${role.id}');" title="删除">[删除]</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <c:if test="${empt eq true}">
                <div class="noRecord">没有找到任何记录!</div>
            </c:if>
        </form>
    </div>
</body>
<script>
    function insert() {
        window.location.href="${base}/Role/addView";
    }
    function view(id) {
        window.location.href="${base}/Role/updateView?id=" + id;
    }
    function del(id) {
        if (confirm("确认删除吗？")){
            $.ajax({
                type : "GET",
                enctype : 'multipart/form-data',
                url : "${base}/Role/del?id=" + id,
                timeout : 60000,
                success : function(data) {
                    //转成json对象
                    var obj = jQuery.parseJSON(data);
                    //注册失败给出提示信息
                    if (!obj.status) {
                        alert("删除失败");
                    } else {
                        alert("删除成功");
                        location.reload();
                    }
                }
            })
        } else {
            alert("取消删除")
        }
    }
</script>
</html>
