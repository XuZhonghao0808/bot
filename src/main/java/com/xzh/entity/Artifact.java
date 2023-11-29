package com.xzh.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
@TableName("epic_artifact")
public class Artifact implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "code", type = IdType.ASSIGN_ID)
    private String code;

    private String name;

    private String alias;

    private Date updateTime;

    private Integer status;

    private String deName;

    private String enName;

    private String esName;

    private String frName;

    private String jaName;

    private String koName;

    private String ptName;

    private String thName;

    private String twName;


}
