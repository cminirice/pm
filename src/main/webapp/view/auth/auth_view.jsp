<%--
  Created by IntelliJ IDEA.
  User: 闫涛
  Date: 2018/1/15
  Time: 18:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>修改权限 - Powered By GUTTV</title>
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
        <form id="authForm" action="${base}/Auth/update" method="post">
            <table class="inputTable">
                <input style="display:none">
                <label class="label-user-name" style="padding-left: 80px;" for="authId">权限编号</label>
                <input type="number" id="authId" name="id" autocomplete="off" value="${auth.id}" readonly="readonly"/></br>
                <label class="label-user-name" style="padding-left: 80px;" for="authName">权限名称</label>
                <input type="text" id="authName" name="name" autocomplete="off" value="${auth.name}"/></br>
                <label class="label-user-name" style="padding-left: 80px;" for="authParentAuthId">父权限id</label>
                <input type="number" id="authParentAuthId" name="parentAuthId" autocomplete="off" value="${auth.parentAuthId}" readonly="readonly"/></br>
                <label class="label-user-name" style="padding-left: 80px;" for="authUrl">权限路径</label>
                <input type="text" id="authUrl" name="url" autocomplete="off" value="${auth.url}"/></br>
            </table>
            <div class="buttonArea">
                <input type="button" id="updateAuthButton" class="formButton" value="更新"/>
            </div>
        </form>
    </div>
</body>
<script>
    $("#updateAuthButton").click(function () {
        var authId = $("#authId").val();
        var authName = $("#authName").val();
        var authParentAuthId = $("#authParentAuthId").val();
        var authUrl = $("#authUrl").val();
        if(authId == null || authId === ""){
            alert("编码不能为空！");
            $("#authId").focus();
            return false;
        } else if(authName == null || authName === ""){
            alert("名称不能为空！");
            $("#authName").focus();
            return false;
        }
        $('#formButton').attr('disabled','true');
        var obj = new Object();
        obj.id = authId;
        obj.name = authName;
        obj.parentAuthId = authParentAuthId;
        obj.url = authUrl;
        //可以提交
        $.ajax({
            type : "POST",
            url : "${base}/Auth/update",
            data : JSON.stringify(obj),
            contentType: "application/json",
            processData : false,
            cache : false,
            timeout : 60000,
            success : function(data) {
                //转成json对象
                var obj = jQuery.parseJSON(data);
                //注册失败给出提示信息
                if (!obj.status) {
                    $.dialog({
                        type : "warn",
                        content : "更新失败：" + obj.message,
                        modal : true,
                        autoCloseTime : 10000
                    });
                } else {
                    $.dialog({
                        type : "warn",
                        content : "更新成功",
                        modal : true,
                        autoCloseTime : 1000
                    });
                    self.setTimeout("window.parent.location.reload();window.close()", 1100);
                }
            },
            error : function() {
                $('#formButton').removeAttr("disabled");
            }
        });
    });
</script>
</html>