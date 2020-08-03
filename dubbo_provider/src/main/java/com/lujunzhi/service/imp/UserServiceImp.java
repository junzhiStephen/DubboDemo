package com.lujunzhi.service.imp;

import com.alibaba.dubbo.config.annotation.Service;
import com.lujunzhi.dao.UserDao;
import com.lujunzhi.pojo.User;
import com.lujunzhi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
@Service/*(protocol = "rmi")*/
public class UserServiceImp implements UserService {
    @Autowired
    UserDao userDao;
    @Override
    public User findById(Integer id) {
        return userDao.findById(id);
    }
}
