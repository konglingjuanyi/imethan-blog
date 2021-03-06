package cn.imethan.service.security;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import cn.imethan.entity.security.Resource;
import cn.imethan.service.security.ResourceService;
import cn.imethan.utils.JsonUtils;

/**
 * ResourceServiceTest.java
 *
 * @author Ethan Wong
 * @time 2014年10月30日下午10:23:23
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/main/applicationContext.xml"})
@TransactionConfiguration(transactionManager="transactionManager",defaultRollback=false)  
public class ResourceServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private ResourceService resourceService;
	
	@Test
	public void testSave(){
		Resource parent = resourceService.getById(1l);
		System.out.println("parent:"+parent);
		
		Resource entity = new Resource();
		entity.setParent(parent);
		entity.setName("查看栏目");
		entity.setIntro("查看栏目");
		entity.setModule("channel:view");
		entity.setUrl("channel/view");
		entity.setRoot(false);
		
		resourceService.saveOrModify(entity);
		
	}
	
	@Test
	public void testDelete(){
		
		resourceService.deleteById(2l);
		
	}
	
	@Test
	public void testGetById(){
		Resource resource = resourceService.getById(1l);
		
		System.out.println("resource:"+resource);
		
		String string = JsonUtils.writeValueAsString(resource);
		System.out.println("string:"+string);
	}
	
	
	
	@Test
	public void testFindByIsRoot(){
		Set<Resource> list = resourceService.getRootResource();
		System.out.println(list);
	}

}


