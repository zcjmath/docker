package com.zcj.service;

import com.zcj.dao.UserDao;
import com.zcj.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    public void save() {
        User user = new User();
        user.setAge(12);
        user.setName("zcj");
        userDao.add(user);
    }

    public List<User> getAll() {
        return userDao.getAll();
    }

}
