<%--
  Created by IntelliJ IDEA.
  User: 闫涛
  Date: 2018/1/15
  Time: 18:131
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>修改管理员 - Powered By GUTTV</title>
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
        <form id="userForm" action="${base}/User/update" method="post">
            <table class="inputTable" style="font-size: medium;">
                <label class="label-user-name w3" style="margin-left: 80px;" for="username">用户名</label>
                <input type="text" class="input-user-name" id="username" name="name" autocomplete="off" readonly value="${user.name}" style="margin-left: 30px;"/></br>
                <input type="hidden" id="userPassword" name="password" class="input-user-name" value="${user.password}"/>
                <label class="label-user-name w2" style="margin-left: 80px;" for="userRoleId">角色</label>
                <select id="userRoleId" name="role" style="margin-left: 30px;">
                </select></br>
                <label class="label-user-name w2" style="margin-left: 80px;" for="userEmail">邮箱</label>
                <input type="text" class="input-user-name" id="userEmail" name="email" autocomplete="off"
                    value="${user.email}" style="margin-left: 30px;"/></br>
            </table>
            <div class="buttonArea">
                <input type="button" id="updateUserButton" class="formButton" value="更新"/>
            </div>
        </form>
    </div>
</body>
<script>
    $(function(){
        var data = parent.$("#roleList").val();
        obj = JSON.parse(decodeURIComponent(data));
        var roleSelect = $("#userRoleId");
        //遍历obj,给下拉框赋值
        for (var i = 0; i < obj.length; i++) {
            roleSelect.append("<option value="+ obj[i].id +">" + obj[i].name + "</option>")
        }
        //下拉框回显
        <c:if test="${not empty user.roleId}">
        for (var j = 0; j < roleSelect[0].options.length; j++){
            if(roleSelect[0][j].value == ${user.roleId})
                roleSelect[0][j].selected = true;
        }
        </c:if>
    });
    $("#updateUserButton").click(function(){
        var username = $("#username").val();
        var userPassword = $("#userPassword").val();
        var userRoleId = $("#userRoleId").val();
        var userEmail = $("#userEmail").val();

        if(username == null || username === ""){
            alert("用户名不能为空！");
            $("#username").focus();
            return false;
        } else if(userPassword == null || userPassword === ""){
            alert("密码不能为空！");
            $("#userPassword").focus();
            return false;
        }
        $('#formButton').attr('disabled','true');
        var obj = new Object();
        obj.name = username;
        obj.password = userPassword;
        obj.roleId = userRoleId;
        obj.email = userEmail;
        //可以提交
        $.ajax({
            type : "POST",
            url : "${base}/User/update",
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