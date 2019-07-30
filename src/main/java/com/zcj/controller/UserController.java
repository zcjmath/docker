package com.zcj.controller;

import com.zcj.model.User;
import com.zcj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/save", method = RequestMethod.GET)
    public String save() {
        userService.save();
        return "ok";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<User> all() {
        return userService.getAll();
    }
}

