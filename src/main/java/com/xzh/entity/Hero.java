package com.xzh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author xzh
 * @since 2023-11-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("epic_hero")
public class Hero implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "code", type = IdType.ASSIGN_ID)
    private String code;

    /**
     * 初始星级
     */
    private Integer grade;

    /**
     * 英雄名称
     */
    private String name;

    /**
     * 职业
     */
    private String jobCd;

    /**
     * 属性
     */
    private String attributeCd;

    private String deName;

    private String koName;

    private String ptName;

    private String thName;

    private String twName;

    private String jaName;

    private String enName;

    private String frName;

    private String esName;

    /**
     * 别名逗号隔开
     */
    private String alias;

    private String image;


}
