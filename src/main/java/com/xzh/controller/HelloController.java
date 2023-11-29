package com.xzh.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "nice to meet you";
    }
}
