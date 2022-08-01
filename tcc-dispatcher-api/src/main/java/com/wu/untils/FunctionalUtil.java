package com.wu.untils;

import com.google.common.collect.Maps;
import com.wu.enums.MergingOperation;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 二次封装jdk1.8流式
 *
 * @author wuzhouwei
 * @date 2021/9/4
 */
public class FunctionalUtil {
    private FunctionalUtil() {
    }

    /**
     * 基本排序操作
     *
     * @param list      集合
     * @param desc      是否降序
     * @param keySelect 排序字段选择函数
     * @param <T>       输入类型
     */
    private static <T, U extends Comparable<U>> void sortList(List<T> list,
                                                              boolean desc,
                                                              Function<? super T, ? extends U> keySelect) {
        if (desc) {
            list.sort(Comparator.comparing(keySelect).reversed());
        } else {
            list.sort(Comparator.comparing(keySelect));
        }
    }


    /**
     * 简单分组
     *
     * @param list       集合
     * @param classifier 分组字段选择函数
     * @param <T>        输入类型
     * @param <K>        Key的类型
     * @return 最后返回Map<K, List < T>
     */
    public static <T, K> Map<K, List<T>> simpleGroupingBy(List<T> list, Function<? super T, ? extends K> classifier) {
        return CollectionUtils.isNotEmpty(list) ? list.stream().collect(Collectors.groupingBy(classifier)) : Maps.newHashMap();
    }

    /**
     * 简单分组 + 排序字段的选择
     *
     * @param list       集合
     * @param classifier 分组字段
     * @param keySelect  排序字段选择
     * @param desc       是否降序
     * @param <T>        输入类型
     * @param <K>        key的类型
     * @return 最后返回Map<K, List < T> List为排序之后的List
     */
    public static <T, K, C extends Comparable<C>> Map<K, List<T>> simpleSortAndGroupingBy(List<T> list,
                                                                                          Function<? super T, ? extends K> classifier,
                                                                                          boolean desc,
                                                                                          Function<? super T, ? extends C> keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        sortList(list, desc, keySelect);
        return list.stream().collect(Collectors.groupingBy(classifier));
    }

    /**
     * 选择返回字段函数式分组
     *
     * @param list            列表
     * @param groupByKey      分组字段选择
     * @param mappingFunction 返回字段
     * @param <T>             输入类型
     * @param <K>             key类型
     * @param <U>             最后返回的元素类型
     * @return 最后返回Map<K, List < U>
     */
    public static <T, K, U> Map<K, List<U>> groupingByAndReturnValueList
    (
            List<T> list, Function<? super T, ? extends K> groupByKey,
            Function<? super T, ? extends U> mappingFunction
    ) {

        return CollectionUtils.isNotEmpty(list) ?
                list.stream().collect(Collectors.groupingBy(groupByKey, Collectors.mapping(mappingFunction, Collectors.toList()))) :
                Maps.newHashMap();
    }

    /**
     * 可选返回字段函数式分组 + 排序字段选择
     *
     * @param list            列表
     * @param groupByKey      分组字段选择
     * @param mappingFunction 返回字段
     * @param keySelect       排序字段选择
     * @param desc            是否降序
     * @param <T>             输入类型
     * @param <K>             key类型
     * @param <U>             最后返回的元素类型
     * @return 最后返回Map<K, List < U>，并对List排序
     */
    public static <T, K, U, C extends Comparable<C>> Map<K, List<U>> groupingByAndReturnSortValueList(List<T> list,
                                                                                                      Function<? super T, ? extends K> groupByKey,
                                                                                                      Function<? super T, ? extends U> mappingFunction,
                                                                                                      boolean desc,
                                                                                                      Function<? super T, ? extends C> keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        sortList(list, desc, keySelect);
        return list.stream()
                .collect(Collectors.groupingBy(groupByKey, Collectors.mapping(mappingFunction, Collectors.toList())));
    }

    /**
     * 分组后合并操作
     *
     * @param list              集合List
     * @param classifier        分组字段
     * @param mergingClassifier 合并字段选择, 支持多字段选择, 限制只能是数字 Integer/Long/Double
     * @param <T>               输入类型
     * @param <K>               key类型
     * @return Map<Long, ? extends Number> value是相同的key的value的求和或求平均分
     */
    @SafeVarargs
    public static <T, K> Map<K, Double> mergingResultGroupingBy(List<T> list, Function<? super T, ? extends K> classifier,
                                                                MergingOperation mergingOperation,
                                                                Function<? super T, ? extends Number>... mergingClassifier) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return mergingGroupingBy(list, classifier, mergingOperation, mergingClassifier);
    }

    /**
     * 合并操作, 分组后计算总值/平均值
     *
     * @param list              集合
     * @param classifier        分组字段选择
     * @param mergingClassifier 不定长参数, 选择多个合并字段.
     * @param mergingOperation  合并操作符
     * @param <T>               输入类型
     * @param <K>               key类型
     * @return 合并结果
     */
    @SafeVarargs
    private static <T, K> Map<K, Double> mergingGroupingBy(List<T> list,
                                                           Function<? super T, ? extends K> classifier,
                                                           MergingOperation mergingOperation,
                                                           Function<? super T, ? extends Number>... mergingClassifier) {
        switch (mergingOperation) {
            //求总和
            case SUMMING:
                return list.stream()
                        .collect(Collectors.groupingBy(classifier, Collectors.summingDouble(s -> {
                            //这里的逻辑是这样的, 循环多个函数相加出总和进行合并
                            double sum = 0;
                            for (Function<? super T, ? extends Number> function : mergingClassifier) {
                                sum += function.apply(s).doubleValue();
                            }
                            return sum;
                        })));
            //求平均值
            case AVERAGING:
                return list.stream().collect(Collectors.groupingBy(classifier, Collectors.averagingDouble(s -> {
                    //循环多个函数, 计算出总和后求平均值
                    double sum = 0;
                    for (Function<? super T, ? extends Number> function : mergingClassifier) {
                        sum += function.apply(s).doubleValue();
                    }
                    return sum;
                })));
            default:
                return null;
        }
    }

    /**
     * 分组后统计分组后的数量
     *
     * @param list       list集合
     * @param classifier 分组字段
     * @param <T>        输入类型
     * @param <K>        Key类型
     * @return Map<K, Long> Long为每组的数量
     */
    public static <T, K> Map<K, Long> groupingByAndCount(List<T> list, Function<? super T, ? extends K> classifier) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
    }

    /**
     * 根据多个字段进行排序, 排序字段的选择必须为数字, 排序方式为, 字段1 + 字段2 + 字段... 总和进行排序
     *
     * @param list      list集合
     * @param desc      是否降序
     * @param keySelect 字段选择
     * @param <T>       输入类型
     */
    @SafeVarargs
    public static <T> void sort(List<T> list, boolean desc, Function<? super T, ? extends Number>... keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        sortList(list, desc, keySelect);
    }

    /**
     * 精准排序, 限制数字类型, 支持多字段排序, 多字段排序为多个字段的总和后进行排序, 泛型约定了只能为数字.
     *
     * @param list      list集合
     * @param desc      是否降序
     * @param keySelect 排序字段选择, 不定长参数可以选择多个字段进行排序
     * @param <T>       输入类型
     */
    @SafeVarargs
    private static <T> void sortList(List<T> list, boolean desc, Function<? super T, ? extends Number>... keySelect) {
        list.sort(Comparator.comparingDouble(s -> {
            double sum = 0;
            for (Function<? super T, ? extends Number> function : keySelect) {
                sum += function.apply(s).doubleValue();
            }
            return desc ? -(sum) : sum;
        }));
    }

    /**
     * 从List中查找最大值
     *
     * @param list      集合
     * @param keySelect 字段选择
     * @param <T>       输入类型
     * @param <U>       比较字段类型
     * @return 结果
     */
    public static <T, U extends Comparable<U>> T findMaximum(List<T> list, Function<? super T, U> keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return (T) list;
        }
        return doFindMaxOrMin(list, keySelect, true);
    }

    /**
     * 从List中查找最小值
     *
     * @param list      集合
     * @param keySelect 字段选择
     * @param <T>       输入类型
     * @param <U>       比较字段类型
     * @return 结果
     */
    public static <T, U extends Comparable<U>> T findMinimum(List<T> list,
                                                             Function<? super T, U> keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return (T) list;
        }
        return doFindMaxOrMin(list, keySelect, false);
    }

    /**
     * 根据字段查询集合最大值/最小值
     *
     * @param list      list集合
     * @param keySelect 字段选择
     * @param max       true: 最大值, false: 最小值
     * @param <T>       输入类型
     * @param <U>       比较类型
     * @return 结果
     */
    private static <T, U extends Comparable<U>> T doFindMaxOrMin(List<T> list,
                                                                 Function<? super T, U> keySelect,
                                                                 boolean max) {

        return max ? list.stream().max(Comparator.comparing(keySelect)).orElseThrow(
                () -> new RuntimeException("该集合查询最大值的数据字段为空"))
                : list.stream().min(Comparator.comparing(keySelect)).orElseThrow(
                () -> new RuntimeException("该集合查询最小值的数据字段为空"));
    }

    /**
     * 将List某个字段合并, 总数/平均值
     *
     * @param list             集合
     * @param keySelect        字段选择, 不定长参数, 可以选择多个字段进行合并
     * @param mergingOperation 合并操作, 总数 / 平均数
     * @return 结果
     */
    @SafeVarargs
    public static <T> Double mergingListResult(List<T> list,
                                               MergingOperation mergingOperation,
                                               Function<? super T, ? extends Number>... keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return 0D;
        }
        return doMergingListResult(list, mergingOperation, keySelect);
    }

    /**
     * 线性表List, 根据字段计算总和/平均值
     *
     * @param list             list
     * @param keySelect        字段选择, 不定长参数, 可以选择多个字段进行合并
     * @param mergingOperation 合并操作, 总数 / 平均数
     * @return 结果
     */
    @SafeVarargs
    private static <T> Double doMergingListResult(List<T> list,
                                                  MergingOperation mergingOperation,
                                                  Function<? super T, ? extends Number>... keySelect) {
        //计算总数逻辑
        double sum = 0;
        for (Function<? super T, ? extends Number> keyFunction : keySelect) {
            sum += list.stream().mapToDouble(s -> keyFunction.apply(s).doubleValue()).sum();
        }
        switch (mergingOperation) {
            //如果是总数合并的话就直接返回
            case SUMMING:
                return sum;
            //要平均数的话就除以list.size()
            case AVERAGING:
                return sum / list.size();
            default:
                return sum;
        }
    }

    /**
     * List统计数据, 包括最小值, 最大值, 平均值等等, 详情查看类{@link java.util.DoubleSummaryStatistics}
     *
     * @param list      集合
     * @param keySelect
     * @param <T>
     * @return
     */
    public static <T> DoubleSummaryStatistics getDoubleSummaryStatistics(List<T> list,
                                                                         Function<? super T, ? extends Number> keySelect) {
        if (CollectionUtils.isEmpty(list)) {
            return new DoubleSummaryStatistics();
        }
        return list.stream().collect(Collectors.summarizingDouble(s -> keySelect.apply(s).doubleValue()));
    }

    /**
     * @param list:
     * @param keyExtractor:
     * @Description: List根据字段去重
     * @Author: wuzhouwei
     * @Date: 2021/9/4
     * @return:
     **/
    @SafeVarargs
    public static <T> List<T> distinctByField(List<T> list, Function<? super T, ?>... keyExtractor) {
        Stream<T> stream = Stream.empty();
        for (Function<? super T, ?> function : keyExtractor) {
            stream = list.stream().filter(distinctByKey(function));
        }
        return stream.collect(Collectors.toList());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static <T> List<T> asList(T... temps) {
        if (temps == null) {
            return new ArrayList<>();
        }

        int length = temps.length;
        List<T> results = new ArrayList<>(length);
        Collections.addAll(results, temps);
        return results;
    }

    /**
     * @Description:
     * @Author: wuzhouwei
     * @Date: 2021/9/23
     * @param list:
     * @param firstClassifier: 先以该分组字段选择函数
     * @param secondClassifier: 再根据该分组字段选择函数
     * @return:
     **/
    public static <T, K> Map<K, Map<K, List<T>>> simpleGroupingByInnerGroup(List<T> list,
                                                                  Function<? super T, ? extends K> firstClassifier,
                                                                  Function<? super T, ? extends K> secondClassifier) {
        return CollectionUtils.isNotEmpty(list) ? list.stream().collect(Collectors.groupingBy(firstClassifier,Collectors.groupingBy(secondClassifier))) : Maps.newHashMap();
    }

    public static <T, K, U> Map<K,U> simpleGroupingBy(List<T> list, Function<? super T, ? extends K> keyClassifier,Function<? super T, ? extends U> valueClassifier) {
        return CollectionUtils.isNotEmpty(list) ? list.stream().collect(Collectors.toMap(keyClassifier, valueClassifier)) : Maps.newHashMap();
    }

    public static <T> Map<T,T> collectionToMap(Collection<T> list){
        if(list == null){
            return Maps.newHashMap();
        }

        Map<T,T> map = Maps.newHashMapWithExpectedSize(list.size());
        for (T t : list) {
            map.put(t,t);
        }
        return map;
    }
}
