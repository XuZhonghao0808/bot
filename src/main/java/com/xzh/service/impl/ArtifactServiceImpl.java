package com.xzh.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzh.entity.Artifact;
import com.xzh.entity.Hero;
import com.xzh.mapper.ArtifactMapper;
import com.xzh.service.ArtifactService;
import com.xzh.utils.HttpClientUtils;
import com.xzh.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xzh
 * @since 2023-11-26
 */
@Service
public class ArtifactServiceImpl extends ServiceImpl<ArtifactMapper, Artifact> implements ArtifactService {

    @Override
    public Result initOrUpdate() {
        String url = "https://static.smilegatemegaport.com/gameRecord/epic7/epic7_artifact.json?_=17009627";
        try {
            String result = HttpClientUtils.get(url);
            if(StringUtils.isBlank(result)){
                return Result.error().message("接口返回null");
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("zh-CN");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject heroMessage = jsonArray.getJSONObject(i);
                String code = heroMessage.getString("code");
                Artifact byId = getById(code);
                if(byId !=null){
                    String enName = updateOtherName(jsonObject,"en", code);
                    byId.setEnName(enName);
                    String koName = updateOtherName(jsonObject,"ko", code);
                    byId.setKoName(koName);
                    String twName = updateOtherName(jsonObject,"zh-TW", code);
                    byId.setTwName(twName);
                    String esName = updateOtherName(jsonObject,"es", code);
                    byId.setEsName(esName);
                    String frName = updateOtherName(jsonObject,"fr", code);
                    byId.setFrName(frName);
                    String jaName = updateOtherName(jsonObject,"ja", code);
                    byId.setJaName(jaName);
                    String deName = updateOtherName(jsonObject,"de", code);
                    byId.setDeName(deName);
                    String ptName = updateOtherName(jsonObject,"pt", code);
                    byId.setPtName(ptName);
                    String thName = updateOtherName(jsonObject,"th", code);
                    byId.setThName(thName);

                    updateById(byId);
                    continue;
                }
                String name = heroMessage.getString("name");

                Artifact artifact = new Artifact();
                artifact.setCode(code);
                artifact.setName(name);
                artifact.setStatus(0);
                artifact.setUpdateTime(new java.util.Date());
                save(artifact);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error().message(e.getMessage());
        }
        return Result.success();
    }


    private String updateOtherName(JSONObject jsonObject, String en, String code) {
        JSONArray enJsonArray = jsonObject.getJSONArray(en);
        for (int i1 = 0; i1 < enJsonArray.size(); i1++) {
            JSONObject heroMessage1 = enJsonArray.getJSONObject(i1);
            String code1 = heroMessage1.getString("code");
            if(!code1.equals(code)){
                continue;
            }
            return heroMessage1.getString("name");
        }
        return null;
    }

}
