<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/content/base/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>ImEthan</title>
<script type="text/javascript">
	//页面加载时初始化脚本
	$(document).ready(function () {
		loadTodoItem();
	});
	
	//加载todo item
	function loadTodoItem(){
		$("#todo-item-list").html("");
		$.ajax({
			url:"${root}/todoitem/json",
			type:"POST",
			dateType:"json",
			success:function(data){
				var result = eval("(" + data + ")");
				
				$.each(result, function(i, item) {
					var flag = "<span class='glyphicon glyphicon-flag' onclick='changeFlag(this,"+item.id+")'></span>";
					if(item.publish == true){
						flag = "<span class='glyphicon glyphicon-flag' style='color:#357ebd' onclick='changeFlag(this,"+item.id+")'></span>";
					};
					
					var item = ""+		
								"<tr>"+
								"   <td width='20px;'>"+flag+"</td>"+
								"	<td id='"+item.id+"' "+
								"	onmouseenter='showDeleteMenu(this)' onmouseleave='hiddenDeleteMenu(this)'>"+
								"	<span id='itemName'>"+item.name+"</span>"+
								"	</td>"+
								"	<td width='80px;'>"+item.createTime+"</td>"+
								"</tr>";
					
					$("#todo-item-list").append(item);
				});
			}
		});
	};
	
	//更新是否发布
	function changeFlag(object,id){
		$.ajax({
			url:"${root}/todoitem/publish/"+id,
			type:"POST",
			dateType:"json",
			success:function(data){
				var result = eval("(" + data + ")");
				
				var color = $(object).css("color");
				if(color =="rgb(53, 126, 189)"){
					$(object).css("color","");
				}else{
					$(object).css("color","rgb(53, 126, 189)");
				}
				
				addWarm(result.success,result.message);
				setTimeout(function(){
					$(".addWarm").html("");
				},2000);
			}
		});
	};
	
	//展现item删除按钮
	function showDeleteMenu(object){
		if($(object).find("#todoItemName").length == 0){
			var id = $(object).attr("id");
			var deleteMenu = ""+
			"<span id='deleteIco' style='float:right;'>"+
				"<a href='#' onclick='editTodoItem(this)'><span class='glyphicon glyphicon-edit' style='color:#5bc0de;'></span></a>&nbsp;&nbsp;"+
				"<a href='#' onclick='deleteItem("+id+")'><span class='glyphicon glyphicon-trash'  style='color:#d9534f;'></span></a>"+
			"</span>";
			
			$(object).append(deleteMenu);
		}
	};
	
	//移除item删除按钮
	function hiddenDeleteMenu(object){
		$(object).find("#deleteIco").remove();
	};
	
	//编辑todo item
	function editTodoItem(object){
		var item = $(object).parent().parent().find("#itemName").html();
		var input = ""+
					"<form id='todoItemEditorForm'  role='form'>"+
					"<div class='input-group'>"+
					"<input name='' id='todoItemName' class='form-control required'  value='"+$.trim(item)+"'>"+
				     " <span class='input-group-btn'>"+
				      "  <button class='btn btn-default' type='button' onclick='saveTodoItemEdit(this)'><span class='glyphicon glyphicon-ok' style='color:#5cb85c;'></span></button>"+
				      "  <button class='btn btn-default' type='button' onclick='removeTodoItemEdit(this)'><span class='glyphicon glyphicon-remove' style='color:#d9534f'></span></button>"+
				      "</span>"+
				    "</div>"+
				    "</form>";
		$(object).parent().parent().html(input);
	};
	
	//取消编辑
	function removeTodoItemEdit(object){
		loadTodoItem();
	};
	
	//保存todo item 编辑
	function saveTodoItemEdit(object){
		if($("#todoItemEditorForm").valid()){
			var todoItem = $(object).parent().parent().find("#todoItemName").val();
			$.ajax({
				type:"POST",
				url:"${root}/todoitem/save",
				data: "name="+encodeURIComponent(todoItem)+"&id="+$(object).parent().parent().parent().parent().attr("id"),
				dateType:"json",
				success:function(data){
					var result = eval("(" + data + ")");
					$(object).parent().parent().parent().parent().html("<span id='itemName'>"+todoItem+"</span>");
					
					addWarm(result.success,result.message);
					setTimeout(function(){
						$(".addWarm").html("");
					},2000);
					
				}
			});
		}
	};
	
	//删除item
	function deleteItem(id){
		$('#deleteTodoItemConfirmModal').modal({
		 	 keyboard: true
		}, $("#deleteTodoItemConfirmModal").find("#id").val(id));
	};
	
	//执行删除item
	function deleteItemOne(){
		var id = $("#deleteTodoItemConfirmModal").find("#id").val();
		$.ajax({
			url:"${root}/todoitem/delete/"+id,
			type:"POST",
			dateType:"json",
			success:function(data){
				var result = eval("(" + data + ")");
				
				$('#deleteTodoItemConfirmModal').modal('toggle');
				
				addWarm(result.success,result.message);
				setTimeout(function(){
					$(".addWarm").html("");
					loadTodoItem()
				},2000);
			}
		});
	};
	
	//添加提醒
	function addWarm(isSuccess,message){
		var messageType = "info";
		if(isSuccess == 'false'){
			messageType = "danger";
		};
		$(".addWarm").html("<p class='bg-"+messageType+"' style='padding: 8px;display: inline;float: right;margin:0px;width:100%;'>"+message+"</p>");
// 		$(".addWarm").html("<div class='alert alert-"+messageType+"' role='alert'  style='padding: 8px;display: inline;float: right;margin:0px;width:100%;'>"+message+"</div>");
	};
	
	//保存item
	function saveTodoItem(){
		if($("#todoItemForm").valid()){
			var todoItem = $("#todoItem").val();
			$.ajax({
				type:"POST",
				url:"${root}/todoitem/save",
				data: "name="+encodeURIComponent(todoItem),
				dateType:"json",
				success:function(data){
					var result = eval("(" + data + ")");
					
					$("#todoItem").val("");
					
					addWarm(result.success,result.message);
					setTimeout(function(){
						location.href = "${root}/todoitem";
					},2000);
					
				}
			});
		}
	};

</script>
</head>
<body>
	<div class="row">
		<div class="col-md-12" >
			<div class="panel panel-default contact" >
				<div class="panel-body">
					<div class="row" style="padding-top: 0px;">
						<div class="col-md-3">
							<h4><span class="glyphicon glyphicon-list"></span>&nbsp;&nbsp;Todo item</h4>
						</div>
						<div class="col-md-9 addWarm"></div>
					</div>
					<div class="row">
						<div class="col-md-12" >
							 <a class="btn btn-info" type="button" href="${root}/todoitem/input"><span class="glyphicon glyphicon-plus"></span>&nbsp;New todo item</a>
						 </div>
					</div>
					<br>
					<table class="table table-bordered todo-list table-hover" >
						<tbody id="todo-item-list">
							
						</tbody>
					</table>
					<span class='glyphicon glyphicon-flag' style='color:#357ebd'></span> Show
					&nbsp;
					<span class='glyphicon glyphicon-flag'></span> Hide
				</div>

			</div>
		</div>
	</div>
	
	
	
	<!-- 删除item确定框 -->
	<div class="modal fade" id="deleteTodoItemConfirmModal" tabindex="-1" role="dialog" aria-labelledby="deleteTodoItemConfirmModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					<h4 class="modal-title" id="myModalLabel">Warning</h4>
				</div>
				<div class="modal-body">
					Sure you want to delete?
					<input type="hidden" id="id" name="" value="">
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-primary" onclick="deleteItemOne()">Delete</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				</div>
			</div>
		</div>
	</div>
</body>
</html>