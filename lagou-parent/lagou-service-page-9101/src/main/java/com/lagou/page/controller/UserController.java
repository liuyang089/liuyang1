package com.lagou.page.controller;

import org.apache.ibatis.javassist.Loader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户注册接口-拉勾金融
     * @return
     */
    @GetMapping("/register")
    public String register() {
        String now = new SimpleDateFormat("yyyy/MM/dd mm:ss").format(new Date());
        System.out.println(now);
        System.out.println("Register success!");
        return "Register success!";
    }


    /**
     *  验证注册身份证接口（需要调用公安户籍资源）
     * @return
     */
    @GetMapping("/validateID")
    public String validateID() {
        System.out.println("validateID");
        return "ValidateID success!";
    }
}
