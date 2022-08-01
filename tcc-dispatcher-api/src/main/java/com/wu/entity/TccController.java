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
@Data
@TableName("tcc_controller")
@AllArgsConstructor
@NoArgsConstructor
public class TccController  implements Serializable {
    private static final long serialVersionUID = -132456;
    @TableId(type = IdType.INPUT)
    private String tccId;
    private String targetClass;
    private String targetMethod;
    private String confirmMethod;
    private String cancelMethod;
    private Integer retriedCount;
    private LocalDateTime createTime;
    private LocalDateTime lastTime;
    private Integer status;
    private String applicationName;
    private String paramJson;
}
