package com.wu.enums;

/**
 * @author wuzhouwei
 * @date 2022/3/10
 * netty消息状态枚举
 */
public enum NettyMsgTypeEnum {
    /**
     * tcc事务首次发起
     */
    TCC_START(0, "transactionalInitiatorStrategy"),
    PING(1,"pongHandleStrategy"),
    // tcc链路成功结束
    TCC_SUCCESS(2,"transactionalFinalStrategy"),
    TCC_ROLLBACK(3,"transactionalFinalStrategy"),

    BUILD_CONNECTION(4,"buildConnectionStrategy"),

    CLIENT_LOCAL_EXECUTE(5,"clientLocalExecuteTryStrategy"),
    SERVER_CLEAR_TCC(6,"transactionalClearStrategy"),
    TCC_PARTICIPATOR(7,"transactionalParticipatorStrategy"),
    NOTIFY_WAIT_SEND_MSG_THREAD(8,"notifyWaitSendMsgThreadStrategy"),
    ;
    /**
     * 消息类型
     */
    private int msgType;
    /**
     * 处理类beanName
     */
    private String beanName;

    NettyMsgTypeEnum(int msgType,String beanName){
        this.msgType = msgType;
        this.beanName = beanName;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public static NettyMsgTypeEnum getEnumByMsgType(int msgType) {
        for (NettyMsgTypeEnum nettyMsgTypeEnum : values()) {
            if (nettyMsgTypeEnum.msgType == msgType) {
                return nettyMsgTypeEnum;
            }
        }
        return null;
    }
}
