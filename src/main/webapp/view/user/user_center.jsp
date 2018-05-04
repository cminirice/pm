<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>个人资料 - Powered By GUTTV</title>
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
<body class="input">
<div class="body">
    <form id="listForm" action="" method="post">
        <table class="inputTable">
            <tr>
                <th colspan="10" style="text-align:center">
                    个人资料展示
                </th>
            </tr>
            <tr>
                <th>
                    用户名:
                </th>
                <td>
                    ${user.name}
                </td>
                <th>
                    密码:
                </th>
                <td>
                    ******
                </td>
            </tr>
            <tr>
                <th>
                    角色id:
                </th>
                <td>
                    ${user.roleId}
                </td>
                <th>
                    邮箱:
                </th>
                <td>
                    ${user.email}
                </td>
            </tr>
        </table>
        <div class="buttonArea">
            <input type="button" onclick="updatePassword()" class="formButton" value="修改密码"/>
        </div>
    </form>
</div>
</body>
<script>
function updatePassword() {
    $.layer({
        type : 2,
        title : '修改密码',
        shadeClose : true,
        area : [ '400px', '300px' ],
        iframe : {
            src : '${base}/view/user/update_password.jsp',
        }
    });
}
</script>
</html>
