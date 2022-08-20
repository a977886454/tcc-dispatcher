package com.wu.utils;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * @author wuzhouwei
 * @date 2022/8/11
 */
@Component
public class EmailUtils{
    @Value("${tcc-server.exception-notify.mail.host:}")
    private String host;
    @Value("${tcc-server.exception-notify.mail.port:}")
    private Integer port;
    @Value("${tcc-server.exception-notify.mail.ssl-enable:}")
    private Boolean sslEnable;
    @Value("${tcc-server.exception-notify.mail.ssl-enable:}")
    private Boolean auth;
    @Value("${tcc-server.exception-notify.mail.from:}")
    private String from;
    @Value("${tcc-server.exception-notify.mail.user:}")
    private String user;
    @Value("${tcc-server.exception-notify.mail.pass:}")
    private String pass;
    @Value("${tcc-server.exception-notify.mail.to-user:}")
    private String toUser;

    private final MailAccount mailAccount = new MailAccount();

    @PostConstruct
    public void init(){
        mailAccount.setHost(host);
        mailAccount.setPort(port);
        mailAccount.setSslEnable(sslEnable);
        mailAccount.setAuth(auth);
        mailAccount.setFrom(from);
        mailAccount.setUser(user);
        mailAccount.setPass(pass);
    }

    public void send(String title, String content){
        if(StringUtils.isNotBlank(host)){
            MailUtil.send(mailAccount,toUser, title, content, false);
        }
    }
}
