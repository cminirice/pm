<%--
  Created by IntelliJ IDEA.
  User: 闫涛
  Date: 2018/1/14
  Time: 12:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>修改角色 - Powered By GUTTV</title>
    <meta name="Author" content="GUTTV Team" />
    <meta name="Copyright" content="GUTTV" />
    <link rel="icon" href="favicon.ico" type="image/x-icon" />
    <link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
    <link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
    <link href="${base}/theme/default/css/user.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="${base}/theme/common/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
</head>
<body class="input">
<div class="body">
    <h2 class="title-role" style="color: #5686af">修改角色</h2>
    <form id="roleForm" action="" method="post" enctype="multipart/form-data">
        <label class="label-role margin-letf" for="roleId">角色编号:</label>
        <input class="input-role" type="number" id="roleId" name="id" autocomplete="off" value="${role.id}"/></br>
        <label class="label-role margin-letf" for="roleName">角色名称:</label>
        <input class="input-role" type="text" id="roleName" name="name" autocomplete="off" value="${role.name}"/></br>
        <label class="label-role margin-letf" for="roleDescription">角色描述:</label>
        <input class="input-role" type="text" id="roleDescription" name="description" autocomplete="off" value="${role.description}"/></br>
        <label class="label-role margin-letf">角色权限:</label></br>
        <c:forEach items="${authList}" var="auth">
            <c:if test="${auth.parentAuthId != null and auth.parentAuthId != 0}">&nbsp;&nbsp;</c:if>
            <input type="checkbox" name="auths" value="${auth.id}" style="margin-left: 80px;"
                   class="input-role"
                   id="<c:if test="${auth.parentAuthId != null and auth.parentAuthId != 0}">${auth.parentAuthId}</c:if><c:if test="${auth.parentAuthId == null or auth.parentAuthId == 0}">0000</c:if>${auth.id}" <c:if test="${fn:contains(role.auths, auth.id) }">checked="checked"</c:if>/>
            <label for="<c:if test="${auth.parentAuthId != null and auth.parentAuthId != 0}">${auth.parentAuthId}</c:if><c:if test="${auth.parentAuthId == null or auth.parentAuthId == 0}">0000</c:if>${auth.id}" class="label-role" <c:if test="${auth.parentAuthId != null and auth.parentAuthId != 0}">style="font-size: 16px;"</c:if>>
                    ${auth.name}</label ></br>
        </c:forEach>
        <div>
            <input type="button" id="updateRoleButton" class="button-role" value="保存"/>
        </div>
    </form>
</div>
</body>
<script>
    $(function () {
        $(":checkbox").click(function(){
            var id = this.id;
            if (id.substr(0, 4) == 0000) {
                //说明是父节点
                //获取父节点的选中状态
                var status  = $(this).is(':checked');
                if (status == false) {
                    var relid = id.substr(4);
                    //取消父节点的时候把子节点也取消
                    var children = $(":checkbox[id^=" + relid + "]");
                    $(children).each(function(i){
                        $(children[i]).prop("checked",false);
                    });
                } else if (status == true) {
                    var relid = id.substr(4);
                    //选中父节点的时候把子节点也选中
                    var children = $(":checkbox[id^=" + relid + "]");
                    $(children).each(function(i){
                        $(children[i]).prop("checked",true);
                    });
                }
            } else if (id.substr(0, 4) != 0000) {
                //子节点
                //获取子节点的选中状态
                var status  = $(this).is(':checked');
                if (status == true) {
                    var relid = id.substr(0, 4);
                    //选中子节点的时候把父节点也选中
                    var parent = $(":checkbox[id$=" + relid + "]");
                    $(parent).each(function(i){
                        $(parent[i]).prop("checked",true);
                    });
                }
            }
        })
    });
    $("#updateRoleButton").click(function () {
        var id = $("#roleId").val();
        var name = $("#roleName").val();
        var description = $("#roleDescription").val();
        var auths=[];
        $("input[name=auths]:checked").each(
            function (i) {
                auths.push($(this).val());
            }
        );
        if(id == null || id === ""){
            alert("id不能为空！");
            $("#id").focus();
            return false;
        } else if(name == null || name === ""){
            alert("角色名称不能为空！");
            $("#name").focus();
            return false;
        }
        $('#formButton').attr('disabled','true');
        var authobj = new Object();
        authobj.id = id;
        authobj.name = name;
        authobj.description = description;
        authobj.auths = auths;
        //可以提交
        $.ajax({
            type : "POST",
            url : "${base}/Role/update",
            data : JSON.stringify(authobj),
            contentType: "application/json",
            processData : false,
            cache : false,
            timeout : 60000,
            success : function(data) {
                //转成json对象
                var obj = jQuery.parseJSON(data);
                //保存失败给出提示信息
                if (!obj.status) {
                    $.dialog({
                        type : "warn",
                        content : "保存失败：" + obj.message,
                        modal : true,
                        autoCloseTime : 10000
                    });
                } else {
                    $.dialog({
                        type : "warn",
                        content : "保存成功",
                        modal : true,
                        autoCloseTime : 1000
                    });
                    window.location.href="${base}/Role/list";
                }
            },
            error : function() {
                $('#formButton').removeAttr("disabled");
            }
        });
    });
</script>
</html>
