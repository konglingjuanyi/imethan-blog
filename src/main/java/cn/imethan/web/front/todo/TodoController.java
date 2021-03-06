package cn.imethan.web.front.todo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.imethan.common.repository.SearchFilter;
import cn.imethan.common.web.SuperController;
import cn.imethan.dto.common.ReturnDto;
import cn.imethan.dto.page.GridPageDto;
import cn.imethan.entity.todo.Todo;
import cn.imethan.entity.todo.TodoItem;
import cn.imethan.service.todo.TodoItemService;
import cn.imethan.service.todo.TodoService;
import cn.imethan.utils.DateUtils;

/**
 * TodoController.java
 *
 * @author Ethan Wong
 * @time 2014年12月16日下午10:58:03
 */
@Controller
@RequestMapping("/todo")
public class TodoController extends SuperController{
	
	@Autowired
	private TodoService todoService;
	@Autowired
	private TodoItemService todoItemService;
	
	
	/**
	 * 进到首页
	 * @param model
	 * @return
	 */
    @RequestMapping("")
    public String todo(Model model) {
		List<SearchFilter> filters = new ArrayList<SearchFilter>();//检索条件列表
		
    	model.addAttribute("todoItems", todoItemService.getAll(filters));
    	
        return "front/todo/todo";
    }
    
    @RequestMapping("/input/{itemId}")
    @RequiresUser//当前用户需为已认证用户或已记住用户 
    public String input(Model model,@PathVariable Long itemId,ServletRequest request) {
    	
		List<SearchFilter> filters = new ArrayList<SearchFilter>();//检索条件列表
		
		List<TodoItem> list = todoItemService.getAll(filters);
		model.addAttribute("todoItems", list);
		model.addAttribute("itemId", itemId);
		
		String id = request.getParameter("id");
		if(!StringUtils.isEmpty(id)){
			Todo todo = todoService.getById(Long.valueOf(id.trim()));
			model.addAttribute("todo", todo);
		}
		
    	return "front/todo/todo-input";
    }
    
    @RequiresUser//当前用户需为已认证用户或已记住用户 
    @RequestMapping("/edit/{id}")
    public String edit(Model model,@PathVariable Long id,ServletRequest request) {
    	
    	List<SearchFilter> filters = new ArrayList<SearchFilter>();//检索条件列表
    	
    	List<TodoItem> list = todoItemService.getAll(filters);
    	model.addAttribute("todoItems", list);
    	Todo todo = todoService.getById(id);
    	model.addAttribute("itemId", todo.getTodoItem().getId());
    	
		model.addAttribute("todo", todo);
    	
    	return "front/todo/todo-edit";
    }
    
    
    /**
     * 保存
     * @param todo
     * @param result
     * @param request
     * @return
     */
    @RequiresUser//当前用户需为已认证用户或已记住用户 
	@ResponseBody
	@RequestMapping(value = "save" , method = {RequestMethod.POST})
    public ReturnDto save(@Valid @ModelAttribute("Todo") Todo todo, BindingResult result,ServletRequest request){
		ReturnDto returnDto = new ReturnDto();
		if(result.hasFieldErrors()){
			returnDto.setMessage("参数验证出现异常:"+result.getFieldError().getDefaultMessage());
			returnDto.setSuccess(false);
		}else{
			returnDto = todoService.save(todo);
		}
		return returnDto;
	}
	
    /**
     * 获取列表
     * @param page
     * @param model
     * @param request
     * @return
     */
	@RequestMapping(value = "json/{page}",method = {RequestMethod.POST})
	@ResponseBody
	public GridPageDto<Todo> json(@PathVariable Integer page,Model model,ServletRequest request){
		
		//设置排序信息
		List<Order> orders=new ArrayList<Order>();
		orders.add(new Order(Direction.ASC, "finish"));
		orders.add(new Order(Direction.DESC, "orderNo"));
		orders.add(new Order(Direction.DESC, "id"));
		PageRequest pageable = new PageRequest(page-1, size, new Sort(orders));
		
		List<SearchFilter> filters = new ArrayList<SearchFilter>();//检索条件列表
		
		String beginTime = request.getParameter("beginTime");//开始时间
		String endTime = request.getParameter("endTime");//结束时间
		String finish = request.getParameter("finish");//是否完成
		String itemId = request.getParameter("itemId");//todo分类
		
		if(!StringUtils.isEmpty(itemId)){
			SearchFilter itemSearchFilter = new SearchFilter("todoItem.id",SearchFilter.Operator.EQ,itemId);
			filters.add(itemSearchFilter);
		}
		
		//设置开始时间和结束时间参数
		if(!StringUtils.isEmpty(beginTime) && !StringUtils.isEmpty(endTime)){
			SearchFilter beginTimeSearchFilter = new SearchFilter("createTime",SearchFilter.Operator.GTE,DateUtils.StringToDate(beginTime, DateUtils.DATE_PATTERN_09));
			filters.add(beginTimeSearchFilter);
			SearchFilter endTimeSearchFilter = new SearchFilter("createTime",SearchFilter.Operator.LTE,DateUtils.StringToDate(endTime, DateUtils.DATE_PATTERN_09));
			filters.add(endTimeSearchFilter);
		}
		
		//设置是否完成参数
		if(!StringUtils.isEmpty(finish)){
			if(finish.trim().equals("finish")){
				SearchFilter searchFilter = new SearchFilter("finish",SearchFilter.Operator.EQ,true);
				filters.add(searchFilter);
			}
			if(finish.trim().equals("unfinished")){
				SearchFilter searchFilter = new SearchFilter("finish",SearchFilter.Operator.EQ,false);
				filters.add(searchFilter);
			}
		}

		Page<Todo> result = todoService.findPage(filters, pageable);
		return new GridPageDto<Todo>(result);
	}
	
	/**
	 * 更新完成状态
	 * @param id
	 * @param finish
	 * @return
	 */
	@RequiresUser//当前用户需为已认证用户或已记住用户 
	@ResponseBody
	@RequestMapping(value = "finish/{id}" , method = {RequestMethod.POST})
	public ReturnDto finish(@PathVariable long id){
		return todoService.finish(id);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@RequiresUser//当前用户需为已认证用户或已记住用户 
	@ResponseBody
	@RequestMapping(value = "delete/{id}" , method = {RequestMethod.POST})
	public ReturnDto delete(@PathVariable long id){
		return todoService.delete(id);
		
	}
	
	/**
	 * 置顶
	 * @param id
	 * @param nextOrderNo
	 * @param previousOrderNo
	 * @return
	 */
	@RequiresUser//当前用户需为已认证用户或已记住用户 
	@ResponseBody
	@RequestMapping(value = "up" , method = {RequestMethod.POST})
	public ReturnDto up(@RequestParam Long id,@RequestParam int nextOrderNo,@RequestParam int previousOrderNo){
		
		return todoService.upTodo(id,nextOrderNo,previousOrderNo);
		
	}
	
	/**
	 * 置底
	 * @param id
	 * @param nextOrderNo
	 * @param previousOrderNo
	 * @return
	 */
	@RequiresUser//当前用户需为已认证用户或已记住用户 
	@ResponseBody
	@RequestMapping(value = "down" , method = {RequestMethod.POST})
	public ReturnDto down(@RequestParam Long id,@RequestParam int nextOrderNo,@RequestParam int previousOrderNo){
		
		return todoService.downTodo(id,nextOrderNo,previousOrderNo);
		
	}
	
}