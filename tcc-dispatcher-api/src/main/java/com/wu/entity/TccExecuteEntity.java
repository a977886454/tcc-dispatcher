package com.wu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TccExecuteEntity  implements Serializable {
    private static final long serialVersionUID = -132456;
    private String targetClass;
    private String targetMethod;
    private String tccId;
    private Object[] paramArgs;
}
