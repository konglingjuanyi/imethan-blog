package com.the3.web.front;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.the3.base.web.SuperController;

/**
 * IndexController.java
 *
 * @author Ethan Wong
 * @time 2014年12月24日下午8:19:36
 */
@Controller
@RequestMapping("/index")
public class IndexController extends SuperController{
	
    @RequestMapping("")
    public String indexOne(Model model) {
    	return "front/index";
    }

}


