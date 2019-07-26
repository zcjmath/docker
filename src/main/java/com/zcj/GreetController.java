package com.zcj;

import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zcj")
public class GreetController {

    @RequestMapping("/greet")
    public String greeting(){
        return "hello world.";
    }
}
