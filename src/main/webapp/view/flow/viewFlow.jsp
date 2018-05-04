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
<link rel="stylesheet" type="text/css" href="${base}/theme/common/gooflow/GooFlow.css"/>
<link rel="stylesheet" type="text/css" href="${base}/theme/common/gooflow/default.css"/>
<link rel="stylesheet" type="text/css" href="${base}/theme/default/css/base.css" />
<style>
.myForm{display:block;margin:0px;padding:0px;line-height:1.5;border:#ccc 1px solid;font: 12px Arial, Helvetica, sans-serif;border-radius:4px;}
.myForm .form_title{background:#428bca;padding:4px;color:#fff;border-radius:3px 3px 0px 0px;}
.myForm .form_content{padding:4px;background:#fff;}
.myForm .form_content table{border:0px}
.myForm .form_content table td{border:0px}
.myForm .form_content table .th{text-align:right;font-weight:bold}
.myForm .form_btn_div{text-align:center;border-top:#ccc 1px solid;background:#f5f5f5;padding:4px;border-radius:0px 0px 3px 3px;} 
.box {cursor: move; position: absolute; top: 30px; right: 30px;}
#propertyForm{width:300px}
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
	toolBtns:[],
	haveHead:false,
	headBtns:[],//如果haveHead=true，则定义HEAD区的按钮
	haveTool:true,
	haveGroup:false,
	useOperStack:true
};
var gooFlowRemark={
	cursor:"选择指针"
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
	
	//删除节点  只读的页面不能删除
	gooFlow.onItemDel=function(id,type){
		return false;
	}
	
	//不能变大小
	gooFlow.onItemResize = function(id,type,width,height){
		return false;
	}
	
	//不能变换线的样子
	gooFlow.onLineSetType = function(id,type){
		return false;
	}

	<c:if test="${not empty errMsg}">
			$.dialog({type: "warn", content: "获取数据失败：${errMsg}", modal: true, autoCloseTime: 3000});
	</c:if>
	<c:if test="${not empty message}">
		$.message({type: "success", content: "${message}"});
	</c:if>

});

//初始化页面显示
function initShow(){
	$("#propertyForm")[0].reset();
	$("#flowComProsTable").empty();
	$("#ele_from_tr").hide();
	$("#ele_to_tr").hide();
	$("#ele_task_tr").hide();
	$("#ele_name_tr").hide();
}

//显示节点的属性
function nodeShow(){
	initShow();
	$("#ele_task_tr").show();
}

//显示连接线的属性
function lineShow(){
	initShow();
	$("#ele_from_tr").show();
	$("#ele_to_tr").show();
	$("#ele_name_tr").show();
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

		//如果以前保存过该节点的属性列列，用旧属性；
		if(nodeVSCom[nodeID] && nodeVSCom[nodeID]==taskName && flowComPros[nodeID]){
			$.each(flowComPros[nodeID],function(name,value) {
					$("#flowComProsTable").append("<tr><td class='th'>"+name+":</td><td><input name='"+name+"'type='text' style='width:90%' readonly='readonly' value='"+value+"'/></td></tr>");
			});
		} else {  //如果以前没有保存过节点的属性，重新显示所有属性
			$.each(taskPros[taskName],function(index,pro) {
					$("#flowComProsTable").append("<tr><td class='th'>"+pro.cn+":</td><td><input name='"+pro.name+"'type='text' style='width:90%' readonly='readonly' value='"+pro.value+"'/></td></tr>");
			});
		}
  }
}

</script>
</head>
<body style="background:#EEEEEE">

<div style="margin-left:5px;margin-top:15px;width:100%;float:left">
流程名称：${flow.name}
&nbsp;&nbsp;&nbsp;流程编码：${flow.code}
&nbsp;&nbsp;&nbsp;<input id="submit" type="button" value='返  回' onclick="window.history.back(); return false;"/>
</div>

<div id="gooFlow" style="margin:5px;float:left"></div>
<form class="box myForm" id="propertyForm" action="${base}/flow/addFlow" method="POST">

<input type="hidden" id="code" name="code" value="${flow.code}" />

<div class="form_title main_tabletop">图形属性设置</div>
<div class="form_content">
  <table style="width:90%">
    <tr><td class="th">标识：</td><td><input type="text" style="width:90%" readonly="readonly" id="ele_id"/></td></tr>
    <tr id="ele_name_tr"><td class="th">名称：</td><td><input type="text" style="width:90%" readonly='readonly' id="ele_name"/></td></tr>
    <tr id="ele_task_tr"><td class="th">任务：</td><td>
    <select name="ele_task" id="ele_task" style="width:90%" disabled="disabled"  onchange="javascript:return false;">
    	<option value=''>虚任务</option>
    	<c:forEach var="com" items="${coms}">
    	<option value='${com.clz}'>${com.cn}</option>
    	</c:forEach>
	</select>
    </td></tr>
    <tr id="ele_from_tr"><td class="th">启点：</td><td><input type="text" style="width:90%" readonly="readonly"  id="ele_from"/></td></tr>
    <tr id="ele_to_tr"><td class="th">终点：</td><td><input type="text" style="width:90%" readonly="readonly"  id="ele_to"/></td></tr>
  </table>
</div>

<div class="form_title" id="taskPropertiesDiv1">任务属性设置</div>
<div class="form_content" id="taskPropertiesDiv2">
  <table id="flowComProsTable" style="width:90%"></table>
</div>

</form>

</body>
</html>
