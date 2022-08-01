package com.wu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wu.entity.TccParticipator;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author wuzhouwei
 * @date 2022/7/28
 */
public interface TccParticipatorMapper extends BaseMapper<TccParticipator> {
    int accumulateRetryCount(@Param("lastTime") LocalDateTime plusSeconds,@Param("maxRetryCount") Integer maxRetryCount,
                             @Param("id")Integer id,@Param("status")Integer status);
}
