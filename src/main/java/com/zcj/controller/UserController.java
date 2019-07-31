package com.zcj.controller;

import com.zcj.model.User;
import com.zcj.service.UserService;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * add user
     * <pre>
     *     {
     *         "name":"zhangsan",
     *         "age":19
     *     }
     * </pre>
     * @param user
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(@RequestBody User user) {
        userService.save(user);
        return "ok";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public List<User> all() {
        return userService.getAll();
    }
}

