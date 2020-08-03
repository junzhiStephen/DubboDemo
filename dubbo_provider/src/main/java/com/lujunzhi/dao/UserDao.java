package com.lujunzhi.dao;

import com.lujunzhi.pojo.User;

public interface UserDao {
    User findById(Integer id);
}
