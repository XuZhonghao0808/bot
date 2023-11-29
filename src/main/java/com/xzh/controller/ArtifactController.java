package com.xzh.controller;


import com.xzh.service.ArtifactService;
import com.xzh.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xzh
 * @since 2023-11-26
 */
@RestController
@RequestMapping("/artifact")
public class ArtifactController {

    @Autowired
    private ArtifactService artifactService;


    @GetMapping("/initOrUpdate")
    public Result initOrUpdate(){
        System.out.println(artifactService.count());
        artifactService.initOrUpdate();
        System.out.println(artifactService.count());
        return Result.success();
    }

}

