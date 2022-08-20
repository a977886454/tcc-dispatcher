package com.wu.config;

import com.wu.schedule.TryTransactionalSchedule;
import com.wu.utils.EmailUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wuzhouwei
 * @date 2022/7/30
 */
@Configuration
public class Configure {
    @Bean
    public TryTransactionalSchedule tryTransactionalSchedule(){
        return new TryTransactionalSchedule(TryTransactionalSchedule.class.getName());
    }
}
