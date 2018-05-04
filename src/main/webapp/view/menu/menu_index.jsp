<%@page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>管理中心首页 - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${base}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${base}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${base}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>
<script type="text/javascript">
	$().ready(function() {
		$.ajax({
			url : "${base}/info/meta",
			data : "",
			type : "POST",
			dataType : "json",
			cache : false,
			success : function(data) {
				$("#flowExecConfig").html(data.flowExecConfig);
				$("#flow").html(data.flow);
				$("#com").html(data.com);
				$("#comPack").html(data.comPack);
				$("#tasks").html(data.tasks);
			}
		});

		$.ajax({
			url : "${base}/info/platform",
			data : "",
			type : "POST",
			dataType : "json",
			cache : false,
			success : function(data) {
				$("#memoryInfo").html(data.memoryInfo);
				$("#threadNumbers").html(data.threadNumbers);
				$("#pid").html(data.pid);
				$("#dir").html(data.dir);
				$("#runTime").html(data.runTime);
			}
		});

	});
</script>
</head>
<body class="index">
	<div class="bar">欢迎使用环球合一流程制作管理平台！</div>
	<div class="body">
		<div class="bodyLeft">
			<table class="listTable">
				<tr>
					<th colspan="2">平台信息</th>
				</tr>
				<tr>
					<td width="110">内存信息</td>
					<td id="memoryInfo"></td>
				</tr>
				<tr>
					<td>线程数</td>
					<td id="threadNumbers"></td>
				</tr>
				<tr>
					<td>进程号</td>
					<td id="pid"></td>
				</tr>
				<tr>
					<td>所在目录</td>
					<td id="dir"></td>
				</tr>
				<tr>
					<td>运行时长</td>
					<td id="runTime"></td>
				</tr>
			</table>
			<div class="blank"></div>
			<table class="listTable">
				<tr>
					<th colspan="2">软件信息</th>
				</tr>
				<tr>
					<td width="110">系统名称:</td>
					<td>环球合一流程制作管理平台</td>
				</tr>
				<tr>
					<td>系统版本:</td>
					<td>Q1 测试版</td>
				</tr>
				<tr>
					<td>官方网站:</td>
					<td><a href="http://www.guttv.cn/" target="blank">http://www.guttv.cn/</a>
					</td>
				</tr>
				<tr>
					<td>交流论坛:</td>
					<td><a href="http://www.bbs.guttv.cn/" target="blank">http://www.bbs.guttv.cn/</a>
					</td>
				</tr>
				<tr>
					<td>BUG反馈邮箱:</td>
					<td><a href="mailto:bug@guttv.cn">bug@guttv.cn</a></td>
				</tr>
				<tr>
					<td>商业授权:</td>
					<td>未取得商业授权之前,您不能将本软件应用于商业用途 <a class="red"
						href="http://www.guttv.cn/list/?id=27" target="_blank">[授权查询]</a>
					</td>
				</tr>
			</table>

		</div>
		<div class="bodyRight">
			<table class="listTable">
				<tr>
					<th colspan="2">元件信息</th>
				</tr>
				<tr>
					<td width="110">组件包数量：</td>
					<td id="comPack"></td>
				</tr>
				<tr>
					<td width="110">组件数量：</td>
					<td id="com"></td>
				</tr>
				<tr>
					<td>流程数量：</td>
					<td id="flow"></td>
				</tr>
				<tr>
					<td>流程执行配置数量：</td>
					<td id="flowExecConfig"></td>
				</tr>
				<tr>
					<td>任务数量：</td>
					<td id="tasks"></td>
				</tr>
			</table>
			<div class="blank"></div>
			<table class="listTable">
				<tr>
					<th colspan="2">系统信息</th>
				</tr>
				<tr>
					<td width="110">Java版本:</td>
					<td><%=System.getProperty("java.version")%></td>
				</tr>
				<tr>
					<td>操作系统名称:</td>
					<td><%=System.getProperty("os.name")%></td>
				</tr>
				<tr>
					<td>操作系统构架:</td>
					<td><%=System.getProperty("os.arch")%></td>
				</tr>
				<tr>
					<td>操作系统版本:</td>
					<td><%=System.getProperty("os.version")%></td>
				</tr>
				<tr>
					<td>本地IP:</td>
					<td><%=request.getRemoteAddr()%></td>
				</tr>
				<tr>
					<td>访问会话数:</td>
					<td>${activeSessionNum}</td>
				</tr>
			</table>
		</div>
		<p class="copyright">COPYRIGHT © 2017-2019 GUTTV.CN ALL RIGHTS RESERVED.</p>
	</div>
</body>
</html>