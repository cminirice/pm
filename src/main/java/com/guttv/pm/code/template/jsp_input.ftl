<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>添加/编辑${codeEntity.cn} - Powered By GUTTV</title>
<meta name="Author" content="GUTTV Team" />
<meta name="Copyright" content="GUTTV" />
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link href="${r"${base}"}/theme/default/css/base.css" rel="stylesheet" type="text/css" />
<link href="${r"${base}"}/theme/default/css/admin.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/jquery.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/default/js/base.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/jquery.validate.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/common/jquery/jquery.validate.methods.js"></script>
<script type="text/javascript" src="${r"${base}"}/theme/common/datePicker/WdatePicker.js"></script>
<script type="text/javascript">

$().ready(function() {

	var $validateErrorContainer = $("#validateErrorContainer");
	var $validateErrorLabelContainer = $("#validateErrorContainer ul");
	var $validateForm = $("#validateForm");
	
	// 表单验证
	$validateForm.validate({
		errorContainer: $validateErrorContainer,
		errorLabelContainer: $validateErrorLabelContainer,
		wrapper: "li",
		errorClass: "validateError",
		ignoreTitle: true,
		rules: {
			<#list codeEntity.editFields as field>
			<#if codeEntity.fields[field].type='Integer'>
			"${codeEntity.fsName}.${field}": "digits",
			<#elseif codeEntity.fields[field].required>
			"${codeEntity.fsName}.${field}": "required",
			</#if>
			</#list>
		},
		messages: {
			<#list codeEntity.editFields as field>
			<#if codeEntity.fields[field].type='Integer'>
			"${codeEntity.fsName}.${field}": "${codeEntity.fields[field].cn}为整型值",
			<#elseif codeEntity.fields[field].required>
			"${codeEntity.fsName}.${field}": "请填写${codeEntity.fields[field].cn}",
			</#if>
			</#list>
		},
		submitHandler: function(form) {
			$(form).find(":submit").attr("disabled", true);
			$("input:text").each(function(){
				var name = $(this).attr("name");
				if(name){
					var spi = name.split(".");
					if(spi.length==2){
						$(this).attr("name",spi[1]);
					}
				}
			});
			$("textarea").each(function(){
				var name = $(this).attr("name");
				if(name){
					var spi = name.split(".");
					if(spi.length==2){
						$(this).attr("name",spi[1]);
					}
				}
			});
			form.submit();
		}
	});
	
})
</script>
</head>
<body class="input">
	<div class="bar">
		${codeEntity.cn}管理 / 
		<c:choose>
			<c:when test="${r"${empty param.id}"}">
				添加${codeEntity.cn}
			</c:when>
			<c:otherwise>
				编辑${codeEntity.cn}
			</c:otherwise>
		</c:choose>
	</div>
	<div id="validateErrorContainer" class="validateErrorContainer">
		<div class="validateErrorTitle">以下信息填写有误,请重新填写</div>
		<ul></ul>
	</div>
	<div class="body">
		<form id="validateForm" action="${r"${base}"}/${codeEntity.tableMeta.pkg}/saveOrUpdate" method="post">
			<input type="hidden" name="id" value="${r"${param.id}"}" />
			
			<table class="inputTable">
			<#assign sign=0/>
			<#list codeEntity.editFields as field>
			<#if sign=0>
				<tr>
			</#if>
					<th>
						${codeEntity.fields[field].cn}: 
					</th>
					<td>
						<#if codeEntity.fields[field].type=='Date'>
						<input type="text" id="${codeEntity.fsName}${field}" name="${codeEntity.fsName}.${field}" class="formText" readOnly="true" value="<s:date name="%{${codeEntity.fsName}.${field}}" format="yyyy-MM-dd HH:mm:ss" />" onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss',readOnly:true})"/>
						<img onclick="WdatePicker({el:'${codeEntity.fsName}${field}',dateFmt:'yyyy-MM-dd HH:mm:ss'});" src="${r"${base}"}/theme/common/datePicker/skin/datePicker.gif" width="16" height="22" align="absmiddle" />
						<#elseif codeEntity.fields[field].type=='Integer'>
						<input type="text" name="${codeEntity.fsName}.${field}" class="formText" value="${r"${"}(${codeEntity.fsName}.${field})}"  maxLength="8" />
						<#elseif codeEntity.fields[field].type=='String'>
						<#if ((codeEntity.fields[field].length) > 100?int)>
						<textarea name="${codeEntity.fsName}.${field}" class="formText" isMultiLine="true" cols="50" rows="5" maxLength="${codeEntity.fields[field].length}">${r"${"}(${codeEntity.fsName}.${field})}</textarea>
						<#else>
						<input type="text" name="${codeEntity.fsName}.${field}" class="formText" value="${r"${"}(${codeEntity.fsName}.${field})}"  maxLength="${codeEntity.fields[field].length}"/>
						</#if>
						<#else>
						<input type="text" name="${codeEntity.fsName}.${field}" class="formText" value="${r"${"}(${codeEntity.fsName}.${field})}"  maxLength="${codeEntity.fields[field].length}"/>
						</#if>
						<#if codeEntity.fields[field].required>
						<label class="requireField"><font color="red">*</font></label>
						</#if>
					</td>
			<#if sign=1>
				</tr>
			</#if>
			<#if sign=0>
			<#assign sign=1/>
			<#else>
			<#assign sign=0/>
			</#if>
			</#list>
			<#if sign=1>
					<th>
						&nbsp;
					</th>
					<td>
						&nbsp;
					</td>
				</tr>
			</#if>
			</table>
			
			<div class="buttonArea">
				<input type="submit" class="formButton" value="确  定" hidefocus />&nbsp;&nbsp;
				<input type="button" class="formButton" onclick="window.history.back(); return false;" value="返  回" hidefocus />
			</div>
		</form>
	</div>
</body>
</html>