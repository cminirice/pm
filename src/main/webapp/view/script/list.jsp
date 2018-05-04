<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="com.guttv.pm.utils.Enums.FlowExecuteStatus" %>
<%
    request.setAttribute("INIT", FlowExecuteStatus.INIT);
    request.setAttribute("STARTING", FlowExecuteStatus.STARTING);
    request.setAttribute("RUNNING", FlowExecuteStatus.RUNNING);
    request.setAttribute("PAUSE", FlowExecuteStatus.PAUSE);
    request.setAttribute("STOPPED", FlowExecuteStatus.STOPPED);
    request.setAttribute("FINISH", FlowExecuteStatus.FINISH);
    request.setAttribute("ERROR", FlowExecuteStatus.ERROR);
    request.setAttribute("LOCKED", FlowExecuteStatus.LOCKED);
    request.setAttribute("FORBIDDEN", FlowExecuteStatus.FORBIDDEN);
%>
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
                title: '查看脚本信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/script/detail?isview=1&code=' + code,
                }
            });
        }

        function add(code) {
            $.layer({
                type: 2,
                title: '新增脚本信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/script/detail?code=' + code,
                }
            });
        }


        function update(code) {
            $.layer({
                type: 2,
                title: '编辑脚本信息',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/script/detail?code=' + code,
                }
            });
        }

        function deletescript(code, fileName) {
            $.dialog({
                type: "warn",
                content: "确认要删除脚本[" + fileName + "]：" + code,
                ok: "删>_<",
                cancel: "再想想~~",
                modal: true,
                okCallback: function () {
                    $.ajax({
                        url: "${base}/script/delete",
                        data: "code=" + code,
                        type: "POST",
                        dataType: "json",
                        cache: false,
                        success: function (data) {
                            //注册失败给出提示信息
                            if (!data.status) {
                                alert("删除失败");
                            } else {
                                alert("删除成功");
                                location.reload();
                            }
                        }
                    });
                }
            });
        }

        function binding(code) {
            $.layer({
                type: 2,
                title: '脚本绑定服务器',
                shadeClose: true,
                area: ['500px', '400px'],
                iframe: {
                    src: '${base}/mapping/detail?code=' + code,
                }
            });
        }

        function execute(code, fileName,status) {
            if(status == 2){
                alert("脚本执行成功，无法再次执行。");
                return;
            }
            $.dialog({
                type: "warn",
                content: "确认要执行脚本[" + fileName + "]：" + code,
                ok: "执行",
                cancel: "再想想~~",
                modal: true,
                okCallback: function () {
                    $.ajax({
                        url: "${base}/script/execute",
                        data: "code=" + code,
                        type: "GET",
                        dataType: "json",
                        cache: false,
                        success: function (data) {
                            if (!data.status) {
                                alert("执行失败");
                            } else {
                                alert("执行中，请稍后刷新列表查看。");
                                location.reload();
                            }
                        }
                    });

                }
            });
        }

        function shutdown(code, fileName,status) {
            if(status != 2){
                alert("脚本未执行成功，无法停止。");
                return;
            }
            $.dialog({
                type: "warn",
                content: "确认要停止脚本[" + fileName + "]：" + code,
                ok: "停止",
                cancel: "再想想~~",
                modal: true,
                okCallback: function () {
                    $.ajax({
                        url: "${base}/script/shutdown",
                        data: "code=" + code,
                        type: "GET",
                        dataType: "json",
                        cache: false,
                        success: function (data) {
                            if (!data.status) {
                                alert("停止失败");
                            } else {
                                alert("停止中，请稍后刷新列表查看。");
                                location.reload();
                            }
                        }
                    });

                }
            });
        }

        function copy(code, fileName,status) {
            $.dialog({
                type: "warn",
                content: "确认要复制脚本[" + fileName + "]：" + code,
                ok: "复制",
                cancel: "再想想~~",
                modal: true,
                okCallback: function () {
                    $.ajax({
                        url: "${base}/script/copy",
                        data: "code=" + code,
                        type: "GET",
                        dataType: "json",
                        cache: false,
                        success: function (data) {
                            if (!data.status) {
                                alert("复制失败");
                            } else {
                                alert("复制中，请稍后刷新列表查看。");
                                location.reload();
                            }
                        }
                    });

                }
            });
        }
    </script>
</head>
<body class="list">
<div class="bar">
    服务器列表&nbsp;总记录数: ${pager.totalCount} (共${pager.pageCount}页)
</div>
<div class="body">
    <form id="listForm" action="${base}/script/list" method="post">
        <div class="listBar">
            <input type="button" class="formButton" onclick="javascript:add(0);" value="新增" hidefocus/>
            &nbsp;&nbsp;
            <select name="searchBy">
                <option value="fileName" <c:if test="${pager.searchBy eq 'fileName'}"> selected</c:if>>
                    文件名
                </option>
                <option value="code" <c:if test="${pager.searchBy eq 'code'}"> selected</c:if>>
                    编号
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
                <th class="check"><input type="checkbox" class="allCheck"/></th>
                <th><a href="#" class="sort" name="code" hidefocus>编号</a></th>
                <th><a href="#" class="sort" name="fileName" hidefocus>文件名</a></th>
                <th><a href="#" class="sort" name="filePath" hidefocus>文件路径</a></th>
                <th><a href="#" class="sort" name="remoteTarget" hidefocus>远端路径</a></th>
                <th><a href="#" class="sort" name="decompressionCMD" hidefocus>解压命令</a></th>
                <th><a href="#" class="sort" name="shCMD" >启动命令</a></th>
                <th><a href="#" class="sort" name="shutdown" >停止命令</a></th>
                <th><a href="#" class="sort" name="status" >状态</a></th>
                <%--<th><a href="#" class="sort" name="md5" >md5</a></th>--%>
                <th>
                    <span>操作</span>
                </th>
            </tr>
            <c:set var="empt" value="true"></c:set>
            <c:forEach var="entity" items="${pager.result}">
                <c:set var="empt" value="false"></c:set>
                <tr>
                    <td><input type="checkbox" name="ids" value="${entity.code}"/></td>
                    <td>
                            <%--${entity.code}--%>
                            ${fn:substring(entity.code,0,10)}
                    </td>
                    <td> ${entity.fileName} </td>
                    <td> ${entity.filePath} </td>
                    <td> ${entity.remoteTarget} </td>
                    <td>
                    ${fn:substring(entity.decompressionCMD,0,10)}
                    <%--${entity.decompressionCMD} --%>
                    </td>
                    <td>
                     ${fn:substring(entity.shCMD,0,10)}
                    <%--${entity.shCMD} --%>
                    </td>
                    <td>
                    ${fn:substring(entity.shutdown,0,10)}
                    <%--${entity.shutdown}--%>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${entity.status==-1}">
                                未绑定服务器
                            </c:when>
                            <c:when test="${entity.status==0}">
                                等待启动
                            </c:when>
                            <c:when test="${entity.status==1}">
                                启动中
                            </c:when>
                            <c:when test="${entity.status==2}">
                                启动成功
                            </c:when>
                            <c:when test="${entity.status==3}">
                                启动失败
                            </c:when>
                            <c:when test="${entity.status==4}">
                                脚本停止
                            </c:when>
                        </c:choose>
                    </td>

                    <%--<td> ${entity.md5} </td>--%>
                    <td>
                        &nbsp;<a href="javascript:deletescript('${entity.code}','${entity.fileName}');" title="删除">[删除]</a>
                        &nbsp;<a href="javascript:view('${entity.code}');" title="查看基本信息">[查看]</a>
                        &nbsp;<a href="javascript:update('${entity.code}');" title="编辑基本信息">[编辑]</a>
                        &nbsp;<a href="javascript:binding('${entity.code}');" title="绑定服务器">[绑定]</a>
                        &nbsp;<a href="javascript:execute('${entity.code}','${entity.fileName}','${entity.status}');" title="执行脚本">[执行]</a>
                        &nbsp;<a href="javascript:shutdown('${entity.code}','${entity.fileName}','${entity.status}');" title="停止脚本">[停止]</a>
                        &nbsp;<a href="javascript:copy('${entity.code}','${entity.fileName}','${entity.status}');" title="复制脚本">[复制]</a>
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