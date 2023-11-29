package com.xzh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xzh.entity.Artifact;
import com.xzh.utils.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xzh
 * @since 2023-11-26
 */
public interface ArtifactService extends IService<Artifact> {

    public Result initOrUpdate();

}
