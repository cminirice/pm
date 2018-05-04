(function ($)
{

    //全局系统对象
    window['CM'] = {};

    CM.cookies = (function ()
    {
        var fn = function ()
        {
        };
        fn.prototype.get = function (name)
        {
            var cookieValue = "";
            var search = name + "=";
            if (document.cookie.length > 0)
            {
                offset = document.cookie.indexOf(search);
                if (offset != -1)
                {
                    offset += search.length;
                    end = document.cookie.indexOf(";", offset);
                    if (end == -1) end = document.cookie.length;
                    cookieValue = decodeURIComponent(document.cookie.substring(offset, end))
                }
            }
            return cookieValue;
        };
        fn.prototype.set = function (cookieName, cookieValue, DayValue)
        {
            var expire = "";
            var day_value = 1;
            if (DayValue != null)
            {
                day_value = DayValue;
            }
            expire = new Date((new Date()).getTime() + day_value * 86400000);
            expire = "; expires=" + expire.toGMTString();
            document.cookie = cookieName + "=" + encodeURIComponent(cookieValue) + ";path=/" + expire;
        }
        fn.prototype.remvoe = function (cookieName)
        {
            var expire = "";
            expire = new Date((new Date()).getTime() - 1);
            expire = "; expires=" + expire.toGMTString();
            document.cookie = cookieName + "=" + escape("") + ";path=/" + expire;
            /*path=/*/
        };

        return new fn();
    })();

    /* 根据QueryString参数名称获取值 */
    CM.getQueryStringByName = function (name) {
        var result = location.search.match(new RegExp("[\?\&]" + name + "=([^\&]+)", "i"));
        if (result == null || result.length < 1)
        {
            return "";
        }
        return result[1];
    }

    //显示loading
    CM.showLoading = function (message)
    {
    	if(typeof(window.parent.parent.showLoading)=="function"&&"..."!=message){
    		window.parent.parent.showLoading(message);
    	}
    };
    //隐藏loading
    CM.hideLoading = function (message)
    {
    	if(typeof(window.parent.parent.hideLoading)=="function"){
    		window.parent.parent.hideLoading(message);
    	}
    };
    
    //右下角的提示框
    CM.tip = function (message)
    {
        alert(message);
    };
    
    //获取cell对应的值
    CM.getCellDisplayValue = function (cellvalue,allstr)
    {
    	var tmpdisplay="";
    	var tmpstr = ""+allstr;
	    var tmpvalue=""+cellvalue;
		tmpstr=tmpstr.replace('{','').replace('}','');
		var tmpvalues=tmpstr.split(/[;,]/);
		if(tmpvalues.length>1){
		   for(var i=0;i<tmpvalues.length;i++){
		      
		      var tmps=tmpvalues[i].split(":");
			  if(tmps.length==2&&tmps[0]==tmpvalue){

			     tmpdisplay=tmps[1].replace('{','').replace('}','').replace('\'','').replace('\'','');
			  }
		   }
		}
		tmpdisplay+="&nbsp;";
		//tmpdisplay="<a href='http://www.google.com'>删除</a>&nbsp;<input type='button' value='删除' />";
		return tmpdisplay;
	};
	//获取Grid数据行的ID集合
	CM.getGridIDs = function (data)
    {
        var ids='';
		for(var i=0;i<data.length;i++) {
			if(i==0) {
               	ids += data[i];  
            } else {
       		  	ids += (',' + data[i]);
       		}
		}
        return ids;
    };
    
    
    //创建过滤规则(查询Array)
    CM.bulidFilterGroup = function (objarr)
    {
		var group = { op: "and", rules: [] };
		$.each(objarr, function(key, val) {
		    //alert('index in arr:' + key + ", corresponding value:" + val);
		    // 如果想退出循环
		    // return false;
			var obj = $("#"+val);//$("input[name='']").each()
			if (!obj.attr("name")) return;
			if (obj.val() == null || obj.val() == "") return;

            var op = obj.attr("op") || "like";
            //get the value type(number or date)
            var type = obj.attr("vt") || "string";
            var value = obj.val();
            var name = obj.attr("name");
			group.rules.push({
	               op: op,
	               field: name,
	               value: value,
	               type: type
	        });		    
		});
		return group;
	}
    
    //重置搜索对象
    CM.resetSearchObj = function (objarr)
    {
    	$.each(objarr, function(key, val) {
		    //alert('index in arr:' + key + ", corresponding value:" + val);
		    // 如果想退出循环
		    // return false;
			var obj = $("#"+val);
			if (!obj.attr("name")) return;
			obj.val('');
		});
	}
    
    //提交服务器请求
    //返回json格式
    //1,提交给类 options.type  方法 options.method 处理
    //2,并返回 AjaxResult(这也是一个类)类型的的序列化好的字符串
    CM.ajax = function (options)
    {
        var p = options || {};
        var ashxUrl = options.ashxUrl || "Action/System/ajaxhandle.do?";
        var url = p.url || ashxUrl + $.param({ type: p.type, method: p.method });
        $.ajax({
            cache: false,
            async: true,
            url: url,
            data: p.data,
            dataType: 'json', type: 'post',
            beforeSend: function ()
            {
        	CM.loading = true;
                if (p.beforeSend)
                    p.beforeSend();
                else
                	CM.showLoading(p.loading);
            },
            complete: function ()
            {
            	CM.loading = false;
                if (p.complete)
                    p.complete();
                else
                	CM.hideLoading();
            },
            success: function (result)
            {
            	CM.hideLoading();
            	if (!result) return;
                if (!result.iserror)
                {
                    if (p.success)
                        p.success(result.data, result.message);
                }
                else
                {
                    if (p.error)
                        p.error(result.message);
                }
            },
            error: function (result, b)
            {
            	CM.hideLoading();
            	CM.tip('System Error <BR>Error Message：' + result.status);
            }
        });
    };
    
    //带loading的提交
    CM.submitForm = function (mainform, success, error)
    {
        if (!mainform)
            mainform = $("form:first");
        mainform.ajaxSubmit({
            dataType: 'json',
            success: success,
            beforeSubmit: function (formData, jqForm, options)
            {
                //针对复选框和单选框 处理
                $(":checkbox,:radio", jqForm).each(function ()
                {
                    if (!existInFormData(formData, this.name))
                    {
                        formData.push({ name: this.name, type: this.type, value: this.checked });
                    }
                });
                for (var i = 0, l = formData.length; i < l; i++)
                {
                    var o = formData[i];
                    if (o.type == "checkbox" || o.type == "radio")
                    {
                        o.value = $("[name=" + o.name + "]", jqForm)[0].checked ? "true" : "false";
                    }
                }
            },
            beforeSend: function (a, b, c)
            {
                CM.showLoading('保存中...');

            },
            complete: function ()
            {
                CM.hideLoading();
            },
            error: function (result)
            {	
                CM.tip('System error <BR>Error code：' + result.status);
            }
        });
        
        function existInFormData(formData, name)
        {
            for (var i = 0, l = formData.length; i < l; i++)
            {
                var o = formData[i];
                if (o.name == name) return true;
            }
            return false;
        }
    };
    /**
     * 格式华时间 hh:mm:ss
     */
    CM.TIME_TO_HHMMSS=function(seconds){
    	   if(!isNum(seconds))
    	   return "&nbsp;";
		   var hh; var mm; var ss;
		   if(seconds==null||seconds<0){
		       return;
		   }
		   hh=seconds/3600|0;
		   seconds=parseInt(seconds)-hh*3600;
		   if(parseInt(hh)<10){
		          hh="0"+hh;
		   }
		   mm=seconds/60|0;
		   ss=parseInt(seconds)-mm*60;
		   if(parseInt(mm)<10){
		         mm="0"+mm;    
		   }
		   if(ss<10){
		       ss="0"+ss;      
		   }
		   return hh+":"+mm+":"+ss;
	}
	/**
	 * 右下角显示选中条数与总时长
	 * proName
	 */
	CM.INIT_SHOW_DURATION=function(proName)
    {
        $("#gridpager_left").append("<div>&nbsp;&nbsp;&nbsp;&nbsp;当前选中 <span id='selectTotal'>0</span>条&nbsp; 时长:<span id='sTDcuration' style='color:red'>00:00:00</span></div>");

        $('#gridlist').jqGrid('setGridParam', {selectDuration:0} );
        $('#gridlist').jqGrid('setGridParam', {selectTotal:0} );
        
    	$('#gridlist').jqGrid('setGridParam', { onSelectRow: function(rowID,status){
    		var selectTotal=$('#gridlist').jqGrid('getGridParam','selectTotal');
    		var selectDuration=$('#gridlist').jqGrid('getGridParam','selectDuration');
    		var rowData = $("#gridlist").jqGrid("getRowData",rowID);
    		if(status)
             {
             	selectTotal=selectTotal+1;
             	var _tempDuration=0;
             	if(isNum(rowData[proName]))
             	{
             		_tempDuration=parseInt(rowData[proName]);
             	}
             	selectDuration=selectDuration+_tempDuration;
             }else
             {
             	selectTotal=selectTotal-1;
             	var _tempDuration=0;
             	if(isNum(rowData[proName]))
             	{
             		_tempDuration=parseInt(rowData[proName]);
             	}
             	selectDuration=selectDuration-_tempDuration;
             }
    		_handlerDisplay(selectTotal,selectDuration)
          } } );
    	$('#gridlist').jqGrid('setGridParam', { onSelectAll: function(aRowids,status){
    		var selectTotal=$('#gridlist').jqGrid('getGridParam','selectTotal');
    		var selectDuration=$('#gridlist').jqGrid('getGridParam','selectDuration');
    		  if(status)
              {
              	selectTotal=aRowids.length;
                  for(var i=0;i<aRowids.length;i++)
                  {
                  	var rowData = $("#gridlist").getRowData(aRowids[i]);
	                var _tempDuration=0;
	             	if(isNum(rowData[proName]))
	             	{
	             		_tempDuration=parseInt(rowData[proName]);
	             	}
                  	selectDuration=selectDuration+_tempDuration;
                  }
              }else
              {
              	selectTotal=0;
              	selectDuration=0;  
              }
    		  _handlerDisplay(selectTotal,selectDuration)
        	 } } );
    	$('#gridlist').jqGrid('setGridParam', { gridComplete: function(){ 
    		  _handlerDisplay(0,0)
        }});
 
        
        
        
    	function  _handlerDisplay(_selectTotal,_selectDuration)
    	{
    		$("#selectTotal").html(_selectTotal);
           	$("#sTDcuration").html(CM.TIME_TO_HHMMSS(_selectDuration));

           	$('#gridlist').jqGrid('setGridParam', {selectDuration:_selectDuration} );
            $('#gridlist').jqGrid('setGridParam', {selectTotal:_selectTotal} );
    	}
    	
    }
    
    function isNum(s)
	{
	 if (s!=null && s!="")
	 {
		return !isNaN(s);
	 }
	 return false;
	}
	
	CM.ShowLargePoster=function(address)
	{
		var imgAddress;
		if(address){
			imgAddress = address;
		}else{
			imgAddress = $("#largePoster").text();
		}
		var background = $("<div></div>");
		$(background).attr("id","overlaybackground").animate({'opacity':'.5'},6).css({
		  "width"  : '100%','height' : '100%','background' : '#d9eaff','z-index' : '5000',
		  'position': 'absolute','top' : '0px','left' : '0px'
		});
		$("body").append(background);
		var newimage = $("<img style='cursor: pointer;' src='"+imgAddress+"'/>");
		var width = $('body').width();
		$(newimage).attr("id","largeimage").css({
		'left' : '40%','top' : '20%','position': 'absolute','z-index' : '6000','display' : 'none',
		'border' : '1px solid #fff'
		});
		$("body").append(newimage);
		$("#largeimage").fadeIn(300,function(){
		  $(this).click(function(){
		       $(this).fadeOut(300);
		            $("div#overlaybackground").fadeOut(300,function(){
		            $("#overlaybackground").remove();
		            $("#largeimage").remove();
		         })
		     })
		})
	}
	
	/**
	 *日期相减，返回天数
     * CM.getDays("2012-12-08","2012-12-25")
     */
    CM.getDays=function(strDateStart){
    	   var strSeparator = "-"; //日期分隔符
    	   var oDate1;
    	   var oDate2;
    	   var iDays;
    	   var tmpDate=new Date();
    	   var strDateEnd=tmpDate.format("yyyy-MM-dd"); 
    	   oDate1= strDateStart.split(strSeparator);
    	   oDate2= strDateEnd.split(strSeparator);
    	   var strDateS = new Date(oDate1[0], oDate1[1]-1, oDate1[2]);
    	   var strDateE = new Date(oDate2[0], oDate2[1]-1, oDate2[2]);
    	   iDays = parseInt(Math.abs(strDateS - strDateE ) / 1000 / 60 / 60 /24)//把相差的毫秒数转换为天数 
    	   return iDays ;
    }
    
    Date.prototype.format = function(format){
		var o = {
		            "M+" : this.getMonth()+1, //month
		            "d+" : this.getDate(), //day
		            "h+" : this.getHours(), //hour
		            "m+" : this.getMinutes(), //minute
		            "s+" : this.getSeconds(), //second
		            "q+" : Math.floor((this.getMonth()+3)/3), //quarter
		            "S" : this.getMilliseconds() //millisecond
		        }
		    if(/(y+)/.test(format))
		    format=format.replace(RegExp.$1,(this.getFullYear()+"").substr(4 - RegExp.$1.length));
		    for(var k in o)
		    if(new RegExp("("+ k +")").test(format))
		    format = format.replace(RegExp.$1,RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length));
		    return format;	
	}
    
})(jQuery);


