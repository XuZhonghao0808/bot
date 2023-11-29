package com.xzh.service;

import com.xzh.entity.Hero;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xzh.utils.Result;
import org.springframework.stereotype.Component;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2023-11-24
 */
public interface HeroService extends IService<Hero> {

    public Result getHeroCodeByName(String name);

    public Result initOrUpdate();

}
