package com.wu.context;

import com.wu.enums.TccRoleEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wuzhouwei
 * @date 2022/7/30
 */
public class TccContextLocal {
    private static final TccContextLocal TCC_CONTEXT_LOCAL = new TccContextLocal();

    private static final ThreadLocal<TccContext> TCC_CONTEXT = new ThreadLocal<>();

    private static final Map<String,Thread> CONCURRENT_WAIT_THREAD_MAP = new ConcurrentHashMap<>(1024);

    public static TccContextLocal getInstance(){
        return TCC_CONTEXT_LOCAL;
    }


    public static Map<String,Thread> getConcurrentWaitThreadMap(){
        return CONCURRENT_WAIT_THREAD_MAP;
    }

    /**
     * set value.
     * @param context context
     */
    public void set(final TccContext context) {
        TCC_CONTEXT.set(context);
    }

    public void setTccContext(String tccId, Object[] args, TccRoleEnum tccRoleEnum){
        TccContext tccContext = new TccContext();
        tccContext.setParamArgs(args);
        tccContext.setTccId(tccId);
        tccContext.setTccRoleEnum(tccRoleEnum);
        TCC_CONTEXT.set(tccContext);
    }

    /**
     * get value.
     * @return TccTransactionContext
     */
    public TccContext get() {
        return TCC_CONTEXT.get();
    }

    /**
     * clean threadLocal for gc.
     */
    public void remove() {
        TCC_CONTEXT.remove();
    }
}
