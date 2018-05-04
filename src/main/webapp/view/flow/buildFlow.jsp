<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>流程制作</title>
<!--[if lt IE 9]>
<?import namespace="v" implementation="#default#VML" ?>
<![endif]-->
<link rel="stylesheet" type="text/css" href="${base}/theme/common/gooflow/GooFlow.css" />
<link rel="stylesheet" type="text/css" href="${base}/theme/common/gooflow/default.css" />
<link rel="stylesheet" type="text/css" href="${base}/theme/default/css/base.css" />
<style>
.myForm {
	display: block;
	margin: 0px;
	padding: 0px;
	line-height: 1.5;
	border: #ccc 1px solid;
	font: 12px Arial, Helvetica, sans-serif;
	border-radius: 4px;
}

.myForm .form_title {
	background: #428bca;
	padding: 4px;
	color: #fff;
	border-radius: 3px 3px 0px 0px;
}

.myForm .form_content {
	padding: 4px;
	background: #fff;
}

.myForm .form_content table {
	border: 0px
}

.myForm .form_content table td {
	border: 0px
}

.myForm .form_content table .th {
	text-align: right;
	font-weight: bold
}

.myForm .form_btn_div {
	text-align: center;
	border-top: #ccc 1px solid;
	background: #f5f5f5;
	padding: 4px;
	border-radius: 0px 0px 3px 3px;
}

.box {
	cursor: move;
	position: absolute;
	top: 30px;
	right: 30px;
}

#propertyForm {
	width: 300px
}
</style>

<script type="text/javascript" src="${base}/theme/common/jquery/jquery.min.js"></script>
<script type="text/javascript" src="${base}/theme/common/gooflow/GooFunc.js"></script>
<script type="text/javascript" src="${base}/theme/common/gooflow/GooFlow.js"></script>
<script type="text/javascript" src="${base}/theme/common/gooflow/GooFlow.color.js"></script>
<script type="text/javascript" src="${base}/theme/common/gooflow/drag.js"></script>
<script type="text/javascript" src="${base}/theme/default/js/base.js"></script>

<script type="text/javascript">
var width = ($(window).width())*0.99;
var height = ($(window).height())*0.90;

var gooFlowProperty={
	width:width,
	height:height,
	toolBtns:["node"],
	haveHead:false,
	headBtns:["new","open","save","undo","redo","reload"],//如果haveHead=true，则定义HEAD区的按钮
	haveTool:true,
	haveGroup:false,
	useOperStack:true
};
var gooFlowRemark={
	cursor:"选择指针",
	direct:"结点连线",
	node:"自动结点"
};


var gooFlow;
var flowComPros = {};
var nodeVSCom = {};
var taskPros;

$(document).ready(function(){
	//初始化
	gooFlow=$.createGooFlow($("#gooFlow"),gooFlowProperty);
	gooFlow.setNodeRemarks(gooFlowRemark);
	

	//加载数据
	if(eval(${flow.flowContent})){
		gooFlow.loadData(eval(${flow.flowContent}));
		flowComPros = eval(${flow.flowComPros});
		nodeVSCom = eval(${flow.nodeVSCom});
	}
	
	taskPros = eval( ${comProsMap} );
	
	
	//初始化名称
	if("${flow.name}"!=""){
		$("#flowName").val("${flow.name}");
	}else{
		$("#flowName").val("流程名称");
	}
	gooFlow.$title=$("#flowName").val();
	
	
	//初始化页面显示
	initShow();

	//注册事件
	gooFlow.onItemFocus=function(id,model){
      var obj;
      if(model=="line"){
        obj=this.$lineData[id];
        lineShow();
        $("#ele_from").val(obj.from);
        $("#ele_to").val(obj.to);
      }else if(model=="node"){
        obj=this.$nodeData[id];
        var type = obj.type;
        if(type=="node"){
					//显示node节点的属性
        	nodeShow();

			//初始化节点值
			if(nodeVSCom[id]){
				changeTaskPros(nodeVSCom[id],id);
			}
        }
      }
      
      $("#ele_name").val(obj.name);
      $("#ele_id").val(id);
      return true;
	};
	
	//失去焦点
	gooFlow.onItemBlur=function(id,model){
    initShow();
    return true;
	};
	
	//删除节点
	gooFlow.onItemDel=function(id,type){
		if(document.getSelection()==""){
			if(confirm("确定要删除该单元吗?")){
		    	initShow(); //初始化显示属性
		
					//从缓存中删除
				delete flowComPros[id];
		    	delete nodeVSCom[id];
		      
		    	return true;
			}else{
	    		return false;
			}
		}else{
			 //此处是选中了页面的其它元素
			 //此处必须返回false,否则会把当前选中的流程元素删除
			return false;
		}
	}
	
	 $("#ele_task option").each(function (){
 		if($(this).val()!=""){
 			$(this).hide();
 		}
	 });
	 changeGroup($("#ele_group").val());
	
	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "保存失败：${errMsg}", modal: true, autoCloseTime: 10000});
	</c:if>
});

//初始化页面显示
function initShow(){
	$("#propertyForm")[0].reset();
	$("#flowComProsTable").empty();
	$("#ele_from_tr").hide();
	$("#ele_to_tr").hide();
	$("#ele_task_tr").hide();
	$("#ele_group_tr").hide();
	$("#ele_name_tr").hide();
}

//显示节点的属性
function nodeShow(){
	initShow();
	$("#ele_task_tr").show();
	$("#ele_group_tr").show();
}

//显示连接线的属性
function lineShow(){
	initShow();
	$("#ele_from_tr").show();
	$("#ele_to_tr").show();
	$("#ele_name_tr").show();
}


function add(){
	$.dialog({type: "warn", content: "确定设计好了V_V", ok: "先保存吧", cancel: "再改改~~", modal: true, okCallback: function () {
		$("#name").val($("#flowName").val());
		$("#flowContent").val(JSON.stringify(gooFlow.exportData()));
		$("#flowComPros").val(JSON.stringify(flowComPros));
		$("#nodeVSCom").val(JSON.stringify(nodeVSCom));
		$("#propertyForm").submit();
	}});
}

function changeGroup(group){
	$("#ele_group").val(group);
	$("#ele_task option").each(function (){
 		var clz = $(this).attr("class");
 		if($(this).val() == ""){
 			$(this).show();
 		} else if(clz==group){
			$(this).show();
		}else{
			$(this).hide();
		}
	 });
}

// 任务的名称改变时，设置属性
function changeTaskPros(taskName,nodeID){

	if(!nodeID) return false;
	
	if(taskName==""){
		//把属性清空
		$("#flowComProsTable").empty();
		return;
	}
	
	//找到对应的任务属性集合
	if(taskPros[taskName]){
		//先把原属性清空
		$("#flowComProsTable").empty();
		
		$("#ele_task").val(taskName);
		
		changeGroup($("#ele_task").find('option:selected').attr("class"));

		//如果以前保存过该节点的属性列列，用旧属性；
		if(nodeVSCom[nodeID] && nodeVSCom[nodeID]==taskName && flowComPros[nodeID]){
			$.each(taskPros[taskName],function(index,pro) {
				var proValue = pro.value;
				if(proValue=='undefined'){
					proValue = "";
				}
				$.each(flowComPros[nodeID],function(name,value) {
						//$("#flowComProsTable").append("<tr><td class='th'>"+name+":</td><td><input name='"+name+"'type='text' style='width:90%' value='"+value+"'/></td></tr>");
					if(pro.name==name){
						proValue = value;
					}
				});
				$("#flowComProsTable").append("<tr><td class='th'>"+pro.cn+":</td><td><input name='"+pro.name+"'type='text' style='width:90%' value='"+proValue+"' autocomplete='off'/></td></tr>");
			});
			
		} else {  //如果以前没有保存过节点的属性，重新显示所有属性
			$.each(taskPros[taskName],function(index,pro) {
				var proValue = pro.value;
				if(!proValue || proValue=='undefined'){
					proValue = "";
				}
				$("#flowComProsTable").append("<tr><td class='th'>"+pro.cn+":</td><td><input name='"+pro.name+"'type='text' style='width:90%' value='"+proValue+"' autocomplete='off'/></td></tr>");
			});
		}
  }
}

//保存任务的属性
function saveTaskPros(){
	var selectedNodeID = gooFlow.$focus;
	if(!selectedNodeID || selectedNodeID==""){return false;}
	
	if(gooFlow.$nodeData[selectedNodeID]){
		nodeVSCom[selectedNodeID] = $("#ele_task").val();
	
		//每次都重新定义
		flowComPros[selectedNodeID] = {};
		//遍历所有的属性并保存
		$("#flowComProsTable :input[type='text']").each(function(i){
		    flowComPros[selectedNodeID][this.name]=this.value;
		});
		
		//修改node节点的名称
		var taskName = $("#ele_task").find('option:selected').text();
		gooFlow.setName(selectedNodeID,taskName,'node');
		
		$("#ele_name").val(taskName);
	} else if (gooFlow.$lineData[selectedNodeID]){
		//修改line节点的名称
		gooFlow.setName(selectedNodeID,$("#ele_name").val(),'line');
	}
}

</script>
</head>
<body style="background:#EEEEEE">

	<div style="margin-left:5px;margin-top:15px;width:100%;float:left"> 流程名称：
		<input type="text" id="flowName" name="flowName" value="" onBlur="javascritp:gooFlow.$title=$('#flowName').val();" />
		&nbsp;&nbsp;&nbsp;
		<input id="submit" type="button" value='保存流程' onclick="add()" />
	</div>

	<div id="gooFlow" style="margin:5px;float:left"></div>
	<form class="box myForm" id="propertyForm" action="${base}/flow/addFlow" method="POST">

		<input type="hidden" id="code" name="code" value="${flow.code}" />
		<input type="hidden" id="name" name="name" value="" />
		<input type="hidden" id="flowContent" name="flowContent" value="" />
		<input type="hidden" id="flowComPros" name="flowComPros" value="" />
		<input type="hidden" id="nodeVSCom" name="nodeVSCom" value="" />

		<div class="form_title main_tabletop">图形属性设置</div>
		<div class="form_content">
			<table style="width:90%">
				<tr>
					<td class="th">标识：</td>
					<td>
						<input type="text" style="width:90%" readonly="true" id="ele_id" />
					</td>
				</tr>
				<tr id="ele_name_tr">
					<td class="th">名称：</td>
					<td><input type="text" style="width:90%" id="ele_name" /></td>
				</tr>
				<tr id="ele_group_tr">
					<td class="th">组：</td>
					<td><select name="ele_group" id="ele_group" style="width:90%" onchange="javascript:changeGroup(this.value)">
							<c:forEach var="group" items="${groups}">
								<option value='${group}'>${group}</option>
							</c:forEach>
					</select></td>
				</tr>
				<tr id="ele_task_tr">
					<td class="th">任务：</td>
					<td><select name="ele_task" id="ele_task" style="width:90%" onchange="javascript:changeTaskPros(this.value,gooFlow.$focus)">
							<option value=''>虚任务</option>
							<c:forEach var="com" items="${coms}">
								<option value='${com.clz}' class='${com.group}'>${com.cn}</option>
							</c:forEach>
					</select></td>
				</tr>
				<tr id="ele_from_tr">
					<td class="th">启点：</td>
					<td><input type="text" style="width:90%" readonly="true" id="ele_from" /></td>
				</tr>
				<tr id="ele_to_tr">
					<td class="th">终点：</td>
					<td><input type="text" style="width:90%" readonly="true" id="ele_to" /></td>
				</tr>
			</table>
		</div>

		<div class="form_title" id="taskPropertiesDiv1">任务属性设置</div>
		<div class="form_content" id="taskPropertiesDiv2">
			<table id="flowComProsTable" style="width:90%"></table>
		</div>
		<div class="form_btn_div" id="form_btn_div">
			<input type="button" value="暂存" onclick="javascript:saveTaskPros();" />
		</div>
	</form>

</body>
</html>
