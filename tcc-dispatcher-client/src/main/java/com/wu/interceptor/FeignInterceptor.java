package com.wu.interceptor;

import com.wu.context.TccContext;
import com.wu.context.TccContextLocal;
import com.wu.init.InitTcc;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        TccContext tccContext = TccContextLocal.getInstance().get();
        if(tccContext != null && tccContext.getIsFeign() != null && tccContext.getIsFeign()){
            template.header("tccId",tccContext.getTccId());
        }
    }
}
