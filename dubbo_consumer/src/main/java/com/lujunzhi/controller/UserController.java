package com.lujunzhi.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lujunzhi.pojo.User;
import com.lujunzhi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequestMapping("/user")
public class UserController {
   /* @Autowired*/
    @Reference(loadbalance = "roundrobin")// 表示轮询
  //@Reference(loadbalance = "random")       	// 表示随机（默认）
    UserService userService;


    @RequestMapping("/findById")
    public User findById(Integer id){
        User user = userService.findById(id);
        return user;
    }
}
