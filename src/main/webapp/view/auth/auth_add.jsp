<%--
  Created by IntelliJ IDEA.
  User: 闫涛
  Date: 2018/1/14
  Time: 12:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>新增权限 - Powered By GUTTV</title>
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
        <form id="authForm" action="" method="post">
            <label class="label-user-name" style="padding-left: 80px;" for="authId">权限编号</label>
            <input type="number" id="authId" name="id" autocomplete="off"/></br>
            <label class="label-user-name" style="padding-left: 80px;" for="authName">权限名称</label>
            <input type="text" id="authName" name="name" autocomplete="off"/></br>
            <label class="label-user-name" style="padding-left: 80px;" for="authParentAuthId">父权限id</label>
            <input type="number" id="authParentAuthId" name="parentAuthId" autocomplete="off"/></br>
            <label class="label-user-name" style="padding-left: 80px;" for="authUrl">权限路径</label>
            <input type="text" id="authUrl" name="url" autocomplete="off"/></br>
            <div class="buttonArea">
                <input type="button" id="addAuthButton" class="formButton" value="保存"/>
            </div>
        </form>
    </div>
</body>
<script>
    $("#addAuthButton").click(function () {
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
            url : "${base}/Auth/add",
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
                        content : "新增失败：" + obj.message,
                        modal : true,
                        autoCloseTime : 10000
                    });
                } else {
                    $.dialog({
                        type : "warn",
                        content : "新增成功",
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
