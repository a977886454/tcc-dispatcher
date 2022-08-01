package com.wu.constant;

/**
 * @author wuzhouwei
 * @date 2021/3/26
 */
public class NettyConstant {
    /**
     * 读空闲超时时间
     */
    public static final int READ_IDLE_TIMEOUT = 5;

    /**
     * 写闲超时时间
     */
    public static final int WRITE_IDLE_TIMEOUT = 5;


    /**
     * 读写空闲超时时间
     */
    public static final int READ_WRITE_IDLE_TIMEOUT = 10;

    /**
     * netty服务器绑定监听端口
     */
    public static final int NETTY_SERVER_BIND_PORT = 9001;


    /**
     * 最大长度
     */
    public static final int MAX_FRAME_LENGTH = 1024 * 1024 * 600;
    /**
     * 长度字段所占的字节数
     */
    public static final int LENGTH_FIELD_LENGTH = 4;
    /**
     * 长度偏移
     */
    public static final int LENGTH_FIELD_OFFSET = 0;
    public static final int LENGTH_ADJUSTMENT = 0;
    /**
     * 丢弃几位(从头开始算,第一个下标为0)
     */
    public static final int INITIAL_BYTES_TO_STRIP = 4;

    /**
     * 客户端mac地址key
     */
//    AttributeKey<String> CLIENT_MAC_KEY = AttributeKey.valueOf("clientMac");
}

