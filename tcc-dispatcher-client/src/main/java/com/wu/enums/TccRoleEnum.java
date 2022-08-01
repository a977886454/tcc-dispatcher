package com.wu.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Getter
@AllArgsConstructor
public enum TccRoleEnum {
    START(1,"发起者"),
    FEIGN(2,"feign参与者"),
    RPC(3,"rpc调用")
    ,;
    private int code;
    private String desc;
}
