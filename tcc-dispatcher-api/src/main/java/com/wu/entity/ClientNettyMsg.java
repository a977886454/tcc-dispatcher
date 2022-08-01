package com.wu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientNettyMsg implements Serializable {
    private static final long serialVersionUID = -132456;
    /**
     * @Description: 消息类型
     * @Author: wuzhouwei
     * @Date: 2022/3/10
     **/
    private Integer msgType;

    /**
     * @Description: 消息数据json格式
     * @Author: wuzhouwei
     * @Date: 2022/3/10
     **/
    private String data;

    // 0新增，1修改,3全局成功，4全部cancl回滚
    private Integer operationType;
}
