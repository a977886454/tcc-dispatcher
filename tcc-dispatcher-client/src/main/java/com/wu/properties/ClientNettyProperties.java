package com.wu.properties;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuzhouwei
 * @date 2022/8/8
 */
public class ClientNettyProperties {
    @Value("${netty.server.port}")
    private Integer port;

    @Value("${netty.server.ips}")
    private String[] ips;

    @Value("${netty.connection.timeout:5000}")
    private Integer connectionTimeout;

    @Value("${netty.sync.send.timeout:5000}")
    private Integer syncSendTimeout;

    public Integer getPort() {
        return port;
    }

    public String[] getIps() {
        return ips;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getSyncSendTimeout() {
        return syncSendTimeout;
    }
}
