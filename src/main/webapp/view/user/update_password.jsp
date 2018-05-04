<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>修改密码 - Powered By GUTTV</title>
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
    <form id="userForm" action="" method="post">
        <label class="label-user-name w3" style="padding-left: 80px;" for="oldPassword">旧密码</label>
        <input type="text" id="oldPassword" name="oldPassword" autocomplete="off"/></br>
        <label class="label-user-name w3" style="padding-left: 80px;" for="newPassword">新密码</label>
        <input type="password" id="newPassword" name="newPassword" autocomplete="off"></br>
        <label class="label-user-name" style="padding-left: 80px;" for="newPassword2">再次确认</label>
        <input type="password" id="newPassword2" name="newPassword2" autocomplete="off"/></br>
        <div class="buttonArea">
            <input type="button" id="updatePasswordButton" class="formButton" value="确认修改"/>
        </div>
    </form>
</div>
</body>
<script>
    $("#updatePasswordButton").click(function () {
        var name = "${currentUser.name}";
        var oldPassword = $("#oldPassword").val();
        var newPassword = $("#newPassword").val();
        var newPassword2 = $("#newPassword2").val();
        if(oldPassword == null || oldPassword === ""){
            alert("旧密码不能为空！");
            $("#oldPassword").focus();
            return false;
        } else if(newPassword == null || newPassword === ""){
            alert("新密码不能为空！");
            $("#newPassword").focus();
            return false;
        } else if(name == null || name === ""){
            alert("当前登陆用户为空");
            $("#name").focus();
            return false;
        } else if(newPassword != newPassword2) {
            alert("两次密码不一致");
            $("#newPassword2").focus();
            return false;
        }
        $('#formButton').attr('disabled','true');
        //可以提交
        var form = new FormData();
        form.append("name",name);
        form.append("oldPassword",oldPassword);
        form.append("newPassword",newPassword);
        $.ajax({
            type : "POST",
            url : "${base}/User/updatePassword",
            data : form,
            contentType: false,
            processData : false,
            cache : false,
            timeout : 60000,
            success : function(data) {
                //转成json对象
                var obj = jQuery.parseJSON(data);
                //修改失败给出提示信息
                if (!obj.status) {
                    $.dialog({
                        type : "warn",
                        content : "修改失败：" + obj.message,
                        modal : true,
                        autoCloseTime : 10000
                    });
                } else {
                    $.dialog({
                        type : "warn",
                        content : "修改成功",
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
