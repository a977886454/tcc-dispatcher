package com.wu.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author wuzhouwei
 * @Description 随机数工具类
 * @Date 2021/3/25
 */
public class RandomUtil {

    /**
     * 获取指定范围内随机数
     * @param start 开始值
     * @param end 结束值
     * @return start <= random <= end
     */
    public static int getSpecifiedRangeRandom(Integer start, Integer end){
        int random = ThreadLocalRandom.current().nextInt(start, end + 1);
        return random;
    }
}
