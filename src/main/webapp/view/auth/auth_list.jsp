<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <title>权限管理 - Powered By GUTTV</title>
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
        <form id="listForm" action="${base}/Auth/list" method="post">
            <div class="listBar">
                <c:if test="${fn:contains(sessionScope.auths,'1801')}">
                <input type="button" class="formButton" onclick="javascript:insert();" value="新增" hidefocus />
                </c:if>
                <input type="hidden" value="" id="roleList">
            </div>
            <table id="listTable" class="listTable" align="center">
                <tr>
                    <th width="20%"><a href="#" class="sort" name="id" hidefocus>权限编码</a></th>
                    <th width="20%"><a href="#" class="sort" name="name" hidefocus>权限名称</a></th>
                    <th width="20%"><a href="#" class="sort" name="parentAuthId" hidefocus>权限父id</a></th>
                    <th width="20%"><a href="#" class="sort" name="url" hidefocus>url</a></th>
                    <th width="20%"><span>操作</span></th>
                </tr>
                <c:set var="empt" value="true"></c:set>
                <c:forEach var="auth" items="${authList}">
                    <c:set var="empt" value="false"></c:set>
                    <tr onclick="javascript:getSecondAuth('${auth.id}');" id="${auth.id}" class="true" style="background-color: #e9f0f4">
                        <td>${auth.id}</td>
                        <td>${auth.name}</td>
                        <td>${auth.parentAuthId}</td>
                        <td>${auth.url}</td>
                        <td>
                            <c:if test="${fn:contains(sessionScope.auths,'1802')}">
                            &nbsp;<a href="javascript:view('${auth.id}','${auth.parentAuthId}');" title="修改">[修改]</a>
                            </c:if>
                            <c:if test="${fn:contains(sessionScope.auths,'1803')}">
                            &nbsp;<a href="javascript:del('${auth.id}','${auth.parentAuthId}');" title="删除">[删除]</a>
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
    function getSecondAuth(pid) {
        var flag = $("#" + pid).attr("class");
        $.ajax({
            type : "GET",
            enctype : 'multipart/form-data',
            url : "${base}/Auth/getSecondAuth?parentAuthId=" + pid,
            timeout : 60000,
            success : function(data) {
                //转成json对象
                var obj = jQuery.parseJSON(data);

                if(flag == "true") {
                    var table = "<tr id=\"table" + pid + "\"><td colspan='5'><table class='listTable' style='margin-left: 15px;margin-right: 15px'>";
                    $.each(obj, function (i, auth) {
                        table += "<tr id=\"" + auth.id + "\" class='children'>" +
                            "<td width=\"20%\">" + auth.id + "</td>\n" +
                            "<td width=\"20%\">" + auth.name + "</td>\n" +
                            "<td width=\"20%\">" + auth.parentAuthId + "</td>\n" +
                            "<td width=\"20%\">" + auth.url + "</td>\n" +
                            "<td width=\"20%\">\n" +
                            "&nbsp;<a href=\"javascript:view('" + auth.id + "','" + auth.parentAuthId + "');\" title=\"修改\">[修改]</a>\n" +
                            "&nbsp;<a href=\"javascript:del('" + auth.id + "','" + auth.parentAuthId + "');\" title=\"删除\">[删除]</a>\n" +
                            "</td>\n" +
                            "</tr>"
                    });
                    table += "</table></td></tr>";
                    $("#" + pid).attr("class","false");
                    //在该行后添加新行
                    $("#" + pid).after(table);
                } else {
                    $("#table" + pid).remove();
                    $("#" + pid).attr("class","true");
                }
            }
        })
    }
    function insert() {
        $.layer({
            type : 2,
            title : '新增权限',
            shadeClose : true,
            area : [ '400px', '300px' ],
            iframe : {
                src : '${base}/view/auth/auth_add.jsp',
            }
        });
    }
    function view(id,parentAuthId) {
        $.layer({
            type : 2,
            title : '修改权限',
            shadeClose : true,
            area : [ '500px', '300px' ],
            iframe : {
                src : '${base}/Auth/get?id=' + id + '&parentAuthId=' + parentAuthId,
            }
        });
    }
    function del(id,parentAuthId) {
        if (confirm("确认删除吗？")){
            $.ajax({
                type : "GET",
                enctype : 'multipart/form-data',
                url : "${base}/Auth/del?id=" + id + '&parentAuthId=' + parentAuthId,
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
