<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page language="java" import="com.guttv.pm.utils.Utils" %>
<%
    int CurrentID = Utils.getInt(request.getParameter("id"), 0);
    int IsView = Utils.getInt(request.getParameter("isview"), 0);
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>服务器 - Powered By GUTTV</title>
    <meta name="Author" content="GUTTV Team"/>
    <meta name="Copyright" content="GUTTV"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>
    <link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css"/>
    <link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css"/>
    <link href="${base}/theme/default/css/user.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="${base}/theme/common/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
    <script type="text/javascript" src="${base}/theme/default/js/CM.js"></script>
    <script type="text/javascript" src="${base}/theme/common/jquery/jquery.form.js"></script>
</head>
<body class="input">
<div class="body">
    <form id="validateForm" method="post">
        <table class="inputTable">
            <tr>
                <th>服务器IP地址:</th>

                <td><input type="text" id="ip" name="ip" autocomplete="off" value="${entity.ip}"></td>
            </tr>
            <tr>
                <th>端口号:</th>
                <td><input type="text" id="port" name="port" autocomplete="off" value="${entity.port}"></td>
            </tr>
            <tr>
                <th> 用户名:</th>
                <td><input type="text" id="userName" name="userName" autocomplete="off" value="${entity.userName}"></td>
            </tr>
            <tr>
                <th> 密码:</th>
                <td><input type="password" id="password" name="password" autocomplete="off" value="${entity.password}">
                </td>
            </tr>
            <tr>
                <th> 远程路径:</th>
                <td><input type="text" id="homePath" name="homePath" autocomplete="off" value="${entity.homePath}"></td>
            </tr>

            <tr style="display: none">
                <th> 编号:</th>
                <td><input type="text" id="code" name="code" autocomplete="off" value="${entity.code}"></td>
            </tr>

            <tr id="createTimeTr">
                <th> 创建时间:</th>
                <td>
                    <input type="text" id="createTime" name="createTime" autocomplete="off"
                           value="${entity.createTime}">
                </td>
            </tr>
            <tr id="updateTimeTr">
                <th> 修改时间:</th>
                <td>
                    <input type="text" id="updateTime" name="updateTime" autocomplete="off"
                           value="${entity.updateTime}">
                </td>
            </tr>
            <tr>
                <td></td>
                <td><input type="button" id="addUserButton" class="formButton" value="保存" onclick="f_save()"/>
                    <input type="button" id="bt_cancel" class="formButton" value="返回" onclick="f_return()"/>
                </td>
            </tr>

        </table>
    </form>
</div>
</body>
<script>
    $(document).ready(function () {
        var createTime = $("#createTime").val();
        var updateTime = $("#updateTime").val();
        $("#createTime").val(timeFtt(createTime));
        $("#updateTime").val(timeFtt(updateTime));

        f_loaded();
    });

    function timeFtt(val) {
        if (val != null) {
            return dateFormat(val,'yyyy-MM-dd HH:mm:ss');
        }
    }

    dateFormat = function (date, format) {
        date = new Date(date);
        var o = {
            'M+': date.getMonth() + 1, //month
            'd+': date.getDate()-1, //day
            'H+': date.getHours() + 810, //hour+8小时
            'm+': date.getMinutes(), //minute
            's+': date.getSeconds(), //second
            'q+': Math.floor((date.getMonth() + 3) / 3), //quarter
            'S': date.getMilliseconds() //millisecond
        };
        if (/(y+)/.test(format))
            format = format.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));

        for (var k in o)
            if (new RegExp('(' + k + ')').test(format))
                format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length));

        return format;
    }

    //相对路径
    var rootPath = "${base}";
    //当前ID
    var currentID = '<%= CurrentID %>';
    //是否新增状态
    var isAddNew = currentID == "" || currentID == "0";
    //是否查看状态
    var isView = <%=IsView %>;
    //是否编辑状态
    var isEdit = !isAddNew && !isView;

    //创建表单结构
    var mainform = $("#validateForm");
    var actionRoot = "";
    if (isEdit) {
        actionRoot = rootPath + "/server/saveOrUpdate";
        mainform.attr("action", actionRoot);
    }
    if (isAddNew) {
        actionRoot = rootPath + "/server/saveOrUpdate";
        mainform.attr("action", actionRoot);
    }

    function f_save() {
        CM.submitForm(mainform, function (data) {
            if (!data.status) {
                $.message({type: data.status, content: '错误:' + data.message});
            }
            else {
                $.message({type: data.status, content: '保存成功'});
                setTimeout("f_return();", 1000);
            }
        });
    }


    /* 返回 */
    function f_return() {
        parent.location.reload();//完成刷新
    }
    function f_loaded() {
        if (!isView) {
            $("#createTimeTr").hide();
            $("#updateTimeTr").hide();
            return;
        }
        //查看状态，控制不能编辑
        $("input,select,textarea", mainform).attr("readonly", "readonly");


    }

</script>
</html>
