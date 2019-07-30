package com.zcj.dao;

import com.zcj.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDao {

    void add(User user);

    List<User> getAll();
}
