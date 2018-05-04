<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ page language="java" import="com.guttv.pm.utils.Utils" %>
<%
    int CurrentID = Utils.getInt(request.getParameter("id"), 0);
    int IsView = Utils.getInt(request.getParameter("isview"), 0);
    String scriptCode = Utils.getString(request.getParameter("code"));
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>新增管理员 - Powered By GUTTV</title>
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
                <th>服务器编号:</th>
                <td><input type="text" id="serverCode" name="serverCode" autocomplete="off" value="${entity.serverCode}"></td>
            </tr>
            <tr>
                <th>脚本编号:</th>
                <td><input type="text" id="scriptCode" name="scriptCode" autocomplete="off" value="${entity.scriptCode}"></td>
            </tr>
            <tr style="display: none">
                <th> 编号:</th>
                <td><input type="text" id="code" name="code" autocomplete="off" value="${entity.code}"></td>
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
        var scriptCode = "<%= scriptCode %>";
        if(scriptCode!="" || scriptCode!="0"){
            $("#scriptCode").val(scriptCode);
        }
        f_loaded();
    });

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
        actionRoot = rootPath + "/mapping/saveOrUpdate";
        mainform.attr("action", actionRoot);
    }
    if (isAddNew) {
        actionRoot = rootPath + "/mapping/saveOrUpdate";
        mainform.attr("action", actionRoot);
    }

    function f_save() {
        CM.submitForm(mainform, function (data) {
            if (!data.status) {
                alert('错误:' + data.message);
            }
            else {
                alert('保存成功');
                f_return();
            }
        });
    }


    /* 返回 */
    function f_return() {
        parent.location.reload();//完成刷新
    }
    function f_loaded() {
        if (!isView) return;
        //查看状态，控制不能编辑
        $("input,select,textarea", mainform).attr("readonly", "readonly");
        $("#addUserButton").hide();
    }

</script>
</html>
