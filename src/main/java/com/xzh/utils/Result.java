package com.xzh.utils;

import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

//设置统一资源返回结果集
@Data
@ApiModel(value = "全局统一返回结果")
public class Result {
    @ApiModelProperty(value = "返回码")
    private  Integer code;
    @ApiModelProperty(value = "返回消息")
    private  String message;
    @ApiModelProperty(value = "是否成功")
    private  Boolean success;
    @ApiModelProperty(value = "返回数据")
    private Object data;
    private Result() {}
    //返回成功的结果集
    public static Result success(){
        Result r = new Result();
        r.setSuccess(ResultCodeEnum.SUCCESS.isSuccess());
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        return r;
    }
    //返回带参的成功结果集
    public static Result success(Object data) {
        Result r = new Result();
        r.setSuccess(ResultCodeEnum.SUCCESS.isSuccess());
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }
    //返回失败的结果集
    public static Result error(){
        Result r = new Result();
        r.setSuccess(ResultCodeEnum.UNKNOWN_REASON.isSuccess());
        r.setCode(ResultCodeEnum.UNKNOWN_REASON.getCode());
        r.setMessage(ResultCodeEnum.UNKNOWN_REASON.getMessage());
        return r;
    }
    /**
     *
     * @param resultCodeEnum
     * @return
     */
    public static Result setResult(ResultCodeEnum resultCodeEnum){
        Result r = new Result();
        r.setSuccess(resultCodeEnum.isSuccess());
        r.setCode(resultCodeEnum.getCode());
        r.setMessage(resultCodeEnum.getMessage());
        return r;
    }

    public Result success(Boolean success){
        this.setSuccess(success);
        return this;
    }

    public Result message(String message){
        this.setMessage(message);
        return this;
    }

    public Result code(Integer code){
        this.setCode(code);
        return this;
    }
    public Result data(Object data) {
        this.data = data;
        return this;

    }

    public Result data(String key,Object value) {
        Map<String, Object> map = Maps.newHashMap();
        map.put(key,value);
        this.data = map;
        return this;

    }

}