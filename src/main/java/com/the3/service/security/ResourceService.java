package com.the3.service.security;

import java.util.List;
import java.util.Set;

import com.the3.dto.service.ServiceReturnDto;
import com.the3.entity.security.Resource;


/**
 * ResourceService.java
 *
 * @author Ethan Wong
 * @time 2014年3月17日下午10:09:38
 */
public interface ResourceService {
	
	/**
	 * 保存或者修改
	 * @param entity
	 * @return
	 */
	public ServiceReturnDto saveOrModify(Resource entity);
	
	/**
	 * 根据ID获取
	 * @param id
	 * @return
	 */
	public Resource getById(Long id);
	
	/**
	 * 根据ID删除
	 * @param id
	 * @return
	 */
	public ServiceReturnDto deleteById(Long id);
	
	/**
	 * 获取根节点
	 * @return
	 */
	Set<Resource> getRootResource();
	
	/**
	 * 获取资源和授权关联列表
	 * @param roleId
	 * @return
	 */
	public Set<Resource> getResourcePermissionForRoleInput(Long roleId);
	
	
}


