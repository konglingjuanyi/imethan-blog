<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/content/base/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Blog:ImEthan独立博客:Full Stack Engineer</title>
<%@ include file="/WEB-INF/content/base/umeditor.jsp"%>
<script type="text/javascript">
	//页面加载时初始化脚本
	$(document).ready(function () {
		//实例化编辑器
		var um = UM.getEditor('editor');
		um.addListener('blur',function(){
			saveArticleDraft();
	    });
	    um.addListener('focus',function(){
	        $('#editorWarn').html('')
	    });
	    
	    //checkbox选中设置
	    $(".articlelabel").click(function(){
	    	var checked = $(this).attr("checked")
	    	console.log("checked:"+checked);
	    	if(checked == undefined){
	    		$(this).attr("checked",true);
	    	}else{
	    		$(this).attr("checked",false);
	    	}
	    });
		
	});
	
	//保存草稿
	function saveArticleDraft(){
		
		if(!UM.getEditor('editor').hasContents()){
			$("#editorContentError").html("This field is required.");
		};

		if($("#inputForm").valid()){
			//保存草稿开始时禁用save按钮
			$("#saveButtonOne").attr("class","btn btn-primary disabled")
			$("#saveButtonTwo").attr("class","btn btn-primary disabled")
			
			var id = $("#id").val();
			var title = $("#title").val();
			var channelId = $("#channelId").val();
			var isPublish = $("#isPublish:checked").val();
			var content = UM.getEditor('editor').getContent();
	        var labels="";
	        $("input[name='articlelabel']:checkbox").each(function(){ 
	            if($(this).attr("checked")){
	            	labels += $(this).val()+","
	            }
	        })
	        
			$.ajax({
				type:"POST",
				url:"${root}/cms/article/save",
				data: "id="+id+"&title="+title+"&channel.id="+channelId+"&content="+encodeURIComponent(content)+"&publish="+isPublish+"&labelIds="+labels,
			    dateType:"json",
				success:function(data){
					var result = eval("(" + data + ")");
					$("#editorContentError").html("");
					$('#editorWarn').html('草稿'+result.message);
					var id = result.entity.id;
					$('#id').val(id);
				}
			});
	        
	        
	        
		      //保存草稿结束时开启save按钮
		      $("#saveButtonOne").attr("class","btn btn-primary")
		      $("#saveButtonTwo").attr("class","btn btn-primary")
		};
	};
	

	
	//提交保存
	function saveArticle(){
		
		if(!UM.getEditor('editor').hasContents()){
			$("#editorContentError").html("This field is required.");
		};

		if($("#inputForm").valid()){
			var id = $("#id").val();
			var title = $("#title").val();
			var channelId = $("#channelId").val();
			var isPublish = $("#isPublish:checked").val();
			var content = UM.getEditor('editor').getContent();
			
	        var labels="";
	        $("input[name='articlelabel']:checkbox").each(function(){ 
	            if($(this).attr("checked")){
	            	labels += $(this).val()+","
	            }
	        })
			
			$.ajax({
				type:"POST",
				url:"${root}/cms/article/save",
				data: "id="+id+"&title="+title+"&channel.id="+channelId+"&content="+encodeURIComponent(content)+"&publish="+isPublish+"&locate=${locate}"+"&labelIds="+labels,
			    dateType:"json",
				success:function(data){
					
					var result = eval("(" + data + ")");
					var messageType = "success";
					if(result.success == false){
						messageType = "error";
					};
					
					showMsg(messageType,result.message);
					
					
					$(".addWarm").html("<p class='bg-primary' style='padding: 9px;display: inline;'>"+result.message+"</p>");
					
					var locate = '${locate}';
					var href = "${root}/blog/${channelId}";
					if(locate == 'about'){
						href = "${root}/about";
					}
					
					if(result.success == true){
						setTimeout(function(){
							location.href = href;
						},1500);
					};
	
					
				}
			});
		};
	};

</script>
</head>
<body>
<div class="container main">
	<div class="row">
		<div class="col-md-12" >
			<div class="panel panel-default">
				  <div class="panel-heading">
				    <h4 class="panel-title">Publish article</h4>
				  </div>
				  <div class="panel-body">
					  	<div class="row" style="padding-top: 0px;">
							<div class="col-md-12">
								 <button id="saveButtonOne" type="button" class="btn btn-primary" onclick="saveArticle()">Save</button>
<%-- 								 <a type="button" class="btn btn-default" href="${root}/blog/article/input/${channelId}/${articleId}">Switch UE</a> --%>
								 <button type="button" class="btn btn-default" onclick="javascript:history.back(-1);">Back</button>
							</div>
						</div>
						<br>
					  	<form role="form" method="post" action="" id="inputForm">
					  	  <input id="id"  type="hidden" name="id" value="${article.id}">
					  	  <div class="form-group">
						    <label for="title">Title</label>
						    <input id="title"  type="text" class="form-control required" placeholder="Enter title" value="${article.title}">
						  </div>
						  
						  <div class="form-group">
						    <label for="title">Channel</label>
						    <select id="channelId" name="channelId" class="form-control required">
						    	<c:forEach items="${channelList}" var="channel" varStatus="status">
						    		<option value="${channel.id}" <c:if test="${channelId eq  channel.id}">selected="selected"</c:if>>${channel.name}</option>
						    	</c:forEach>
							</select>
						  </div>
						  <div class="radio">
							  <label>
								    <input type="radio" name="isPublish" id="isPublish" value="true" <c:if test="${article.publish eq true }">checked="checked"</c:if>/>
								    Publish
							  </label>
							  <label>
								    <input type="radio" name="isPublish" id="isPublish" value="false" <c:if test="${article.publish eq false }">checked="checked"</c:if> <c:if test="${article.publish == null }">checked="checked"</c:if> />
								    Hidden
							  </label>
						  </div>
						  
						  <div class="form-group">
						  	 <label class="control-label">Label</label>
					          <div class="controls">
					          	<c:forEach var="label" items="${allLabel}">
					          		<input  class="articlelabel" name="articlelabel" type="checkbox" value="${label.id}"  <c:if test="${label.check eq true}">checked="checked"</c:if>> ${label.name}
					          	</c:forEach>
					          </div>
						  </div>
						  <hr>
						  <div class="form-group">
						    <label for="content">Content</label>
						    <p style="float: right;"><a href="${root}/blog/article/input/${channelId}/${articleId}">Switch UEditor</a></p>
						    <script type="text/plain" id="editor"  style="width:100%!important;height: 300px;">${article.content}</script>
							<label id="editorContentError" ></label>
							<p id="editorWarn"></p>
						  </div>
						  
						  <button id="saveButtonTwo" type="button" class="btn btn-primary" onclick="saveArticle()">Save</button>
						  <button type="button" class="btn btn-default" onclick="javascript:history.back(-1);">Back</button>
						  <span class="addWarm"></span>
						</form>
				  </div>
			</div>
		</div>
	</div>
</div>
</body>
</html>