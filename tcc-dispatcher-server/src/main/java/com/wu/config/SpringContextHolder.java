package com.wu.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Lazy(false)
@Component
public final class SpringContextHolder implements ApplicationContextAware,
		DisposableBean {

	private static ApplicationContext applicationContext = null;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SpringContextHolder.class);

	/**
	 *
	 * 取得存储在静态变量中的ApplicationContext.
	 */
	public static ApplicationContext getApplicationContext() {

		assertContextInjected();

		return applicationContext;

	}

	/**
	 *
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */

	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {

		assertContextInjected();

		return (T) applicationContext.getBean(name);

	}

	/**
	 * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
	 */
	public static <T> T getBean(Class<T> requiredType) {

		assertContextInjected();

		return applicationContext.getBean(requiredType);

	}

	/**
	 * 清除SpringContextHolder中的ApplicationContext为Null.
	 */

	public static void clearHolder() {

		LOGGER.debug("clear SpringContextHolder ApplicationContext:"
				+ applicationContext);

		applicationContext = null;

	}

	/**
	 * 实现ApplicationContextAware接口, 注入Context到静态变量中.
	 */
	@Override
    public void setApplicationContext(ApplicationContext applicationContext) {

		LOGGER.debug("Inject ApplicationContext to SpringContextHolder:{}",applicationContext);

		if (SpringContextHolder.applicationContext != null) {

			LOGGER.warn("SpringContextHolder ApplicationContext is overwrited, old ApplicationContext:"
					+ SpringContextHolder.applicationContext);

		}
		SpringContextHolder.applicationContext = applicationContext; // NOSONAR
	}

	/**
	 * 实现DisposableBean接口, 在Context关闭时清理静态变量.
	 */
	@Override
    public void destroy() throws Exception {

		SpringContextHolder.clearHolder();
	}

	/**
	 * 检查ApplicationContext不为空.
	 */
	private static void assertContextInjected() {
//
//		Validate.validState(applicationContext != null,
//				"applicaitonContext is null, please define applicationContext.xml.");
	}

	/**
     * 获取请求上下文
     *
     * @return
     * @return HttpServletRequest 返回类型
     * @author wuzhouwei
     * @date 2020年6月11日 下午7:16:30
     */
    public static HttpServletRequest getRequestContext() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (null == requestAttributes) {
            return null;
        }

        return ((ServletRequestAttributes)requestAttributes).getRequest();
    }

    /**
     * @Description: 当前环境，dev,test,pro
     * @Author: wuzhouwei
     * @Date: 2022/5/18
     * @return:
     **/
	public static String getActiveProfile() {
		return applicationContext.getEnvironment().getActiveProfiles()[0];
	}

	/**
	 * @Description: 是否生产环境
	 * @Author: wuzhouwei
	 * @Date: 2022/5/18
	 * @return:
	 **/
	public static boolean isProProfile(){
		return "pro".equals(getActiveProfile());
	}
}
