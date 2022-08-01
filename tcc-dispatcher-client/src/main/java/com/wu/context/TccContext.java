package com.wu.context;

import com.wu.enums.TccRoleEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
@Data
public class TccContext implements Serializable {

    private static final long serialVersionUID = -5289080166922118073L;
    private String tccId;
    private Object[] paramArgs;
    // 用来feign请求拦截
    private Boolean isFeign;
    private TccRoleEnum tccRoleEnum;
}
