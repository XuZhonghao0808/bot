package com.xzh.controller;


import com.xzh.entity.Hero;
import com.xzh.service.HeroService;
import com.xzh.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xzh
 * @since 2023-11-24
 */
@RestController
@RequestMapping("/hero")
public class HeroController {

    @Autowired
    private HeroService heroService;

    @GetMapping("/getById")
    public Result getById(@RequestBody String code){
        if(StringUtils.isEmpty(code)){
            return Result.error().message("code不能为空");
        }
        Hero hero = heroService.getById(code);
        return Result.success(hero);
    }


    @GetMapping("/initOrUpdate")
    public Result initOrUpdate(){
        System.out.println(heroService.count());
        heroService.initOrUpdate();
        System.out.println(heroService.count());
        return Result.success();
    }

}

