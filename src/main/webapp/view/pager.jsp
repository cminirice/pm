<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="UTF-8"%>
<script type="text/javascript">
$().ready( function() {
	
	var $pager = $("#pager");
	
	$pager.pager({
		pagenumber: ${pager.pageNumber},
		pagecount: ${pager.pageCount},
		buttonClickCallback: $.gotoPage
	});

})
</script>
<span id="pager"></span>
<input type="hidden" name="pageNumber" id="pageNumber" value="${pager.pageNumber}" />
<input type="hidden" name="orderBy" id="orderBy" value="${pager.orderBy}" />
<input type="hidden" name="order" id="order" value="${pager.order}" />