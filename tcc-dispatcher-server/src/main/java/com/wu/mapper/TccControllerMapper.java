package com.wu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wu.entity.TccController;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
public interface TccControllerMapper extends BaseMapper<TccController> {
    int accumulateRetryCount(@Param("lastTime") LocalDateTime plusSeconds, @Param("maxRetryCount") Integer maxRetryCount,
                             @Param("tccId") String tccId,@Param("status")Integer status);
}
