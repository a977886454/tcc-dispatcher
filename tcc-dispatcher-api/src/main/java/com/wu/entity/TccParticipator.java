package com.wu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@TableName("tcc_participator")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TccParticipator implements Serializable {
    private static final long serialVersionUID = -132456;
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String tccId;
    private String targetClass;
    private String targetMethod;
    private String confirmMethod;
    private Integer retriedCount;
    private String cancelMethod;
    private LocalDateTime createTime;
    private Integer status;
    private String applicationName;
    private LocalDateTime lastTime;
    private String paramJson;
}
