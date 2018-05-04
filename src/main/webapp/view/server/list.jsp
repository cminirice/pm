<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>服务器配置 - Powered By GUTTV</title>
    <meta name="Author" content="GUTTV Team"/>
    <meta name="Copyright" content="GUTTV"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>
    <link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css"/>
    <link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css"/>
    <script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
    <script type="text/javascript" src="${base}/theme/common/jquery/jquery.pager.js"></script>
    <script type="text/javascript" src="${base}/theme/common/jquery/layer.min.js"></script>
    <script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
    <script type="text/javascript" src="${base}/theme/default/js/admin.js"></script>
    <script type="text/javascript">

        $().ready(function () {
            <c:if test="${not empty errMsg}">
            $.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
            </c:if>
            <c:if test="${not empty message}">
            $.message({type: "success", content: "${message}"});
            </c:if>
        });

        function view(code) {
            $.layer({
                type: 2,
                title: '查看服务器信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/server/detail?isview=1&code=' + code,
                }
            });
        }

        function add(code) {
            $.layer({
                type: 2,
                title: '新增服务器信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/server/detail?code='+code,
                }
            });
        }


        function update(code) {
            $.layer({
                type: 2,
                title: '编辑服务器信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/server/detail?code='+code,
                }
            });
        }

        function deleteServer(code,ip){
            $.dialog({type: "warn", content: "确认要删除服务器["+ip+"]："+ code, ok: "删>_<", cancel: "再想想~~", modal: true, okCallback: function () {
                $.ajax({
                    url: "${base}/server/delete",
                    data: "code="+code,
                    type: "POST",
                    dataType: "json",
                    cache: false,
                    success: function(data) {
                        //注册失败给出提示信息
                        if (!data.status) {
                            $.message({type: data.status, content: "删除失败"});
                        } else {
                            $.message({type: data.status, content: "删除成功"});
                            setTimeout("location.reload();",1000);
                        }
                    }
                });
            }});
        }

    </script>
</head>
<body class="list">
<div class="bar">
    服务器列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
</div>
<div class="body">
    <form id="listForm" action="${base}/server/list" method="post">
        <div class="listBar">
            <input type="button" class="formButton" onclick="javascript:add(0);" value="新增"  hidefocus/>
            <%--<input type="button" class="formButton" onclick="location.href='${base}/server/detail?code=0'" value="新建服务器" hidefocus/>--%>
            &nbsp;&nbsp;
            <select name="searchBy">
                <option value="ip" <c:if test="${pager.searchBy eq 'ip'}"> selected</c:if>>
                    ip
                </option>
                <option value="code" <c:if test="${pager.searchBy eq 'code'}"> selected</c:if>>
                    编号
                </option>
                <option value="userName" <c:if test="${pager.searchBy eq 'userName'}"> selected</c:if>>
                    用户名称
                </option>
            </select>
            <input type="text" name="keyword" value="${pager.keyword}"/>
            <input type="button" id="searchButton" class="formButton" value="搜 索" hidefocus/>
            &nbsp;&nbsp;
            <label>每页显示: </label>
            <select name="pageSize" id="pageSize">
                <option value="15"<c:if test="${pager.pageSize eq 15}"> selected</c:if>>
                    15
                </option>
                <option value="30"<c:if test="${pager.pageSize eq 30}"> selected</c:if>>
                    30
                </option>
                <option value="50"<c:if test="${pager.pageSize eq 50}"> selected</c:if>>
                    50
                </option>
                <option value="100"<c:if test="${pager.pageSize eq 100}"> selected</c:if>>
                    100
                </option>
            </select>

        </div>
        <table id="listTable" class="listTable">
            <tr>
                <th class="check"> <input type="checkbox" class="allCheck"/>                </th>
                <th> <a href="#" class="sort" name="code" hidefocus>编号</a>                </th>
                <th> <a href="#" class="sort" name="ip" hidefocus>服务器IP地址</a>                </th>
                <th> <a href="#" class="sort" name="port" hidefocus>端口号</a>                </th>
                <th> <a href="#" class="sort" name="userName" hidefocus>用户名</a>                </th>
                <th> <a href="#" name="homePath" hidefocus>远程跟路径</a>                </th>
                <th> <a href="#" name="createTime" hidefocus>创建时间</a>                </th>
                <th> <a href="#" name="updateTime" hidefocus>修改时间</a>                </th>
                <th>
                    <span>操作</span>
                </th>
            </tr>
            <c:set var="empt" value="true"></c:set>
            <c:forEach var="entity" items="${pager.result}">
                <c:set var="empt" value="false"></c:set>
                <tr>
                    <td> <input type="checkbox" name="ids" value="${entity.code}"/> </td>
                    <td> ${entity.code} </td>
                    <td> ${entity.ip} </td>
                    <td> ${entity.port} </td>
                    <td> ${entity.userName} </td>
                    <td> ${entity.homePath} </td>
                    <td>
						<span title="创建时间"><fmt:formatDate value="${entity.createTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
					</td>
                    <td>
                        <span title="修改时间"><fmt:formatDate value="${entity.updateTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
                    </td>
                    <td>
                        &nbsp;<a href="javascript:deleteServer('${entity.code}','${entity.ip}');" title="删除">[删除]</a>
                        &nbsp;<a href="javascript:view('${entity.code}');" title="查看基本信息">[查看]</a>
                        &nbsp;<a href="javascript:update('${entity.code}');" title="编辑基本信息">[编辑]</a>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${empt eq false}">
            <div class="pagerBar">
                <div class="pager">
                    <%@ include file="/view/pager.jsp" %>
                </div>
            </div>
        </c:if>
        <c:if test="${empt eq true}">
            <div class="noRecord">没有找到任何记录!</div>
        </c:if>
    </form>
</div>
</body>
</html>