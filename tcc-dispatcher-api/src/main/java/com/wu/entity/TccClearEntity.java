package com.wu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TccClearEntity implements Serializable {
    private static final long serialVersionUID = -132456;
    private String applicationName;
    private String tccId;
    private String targetClass;
}
