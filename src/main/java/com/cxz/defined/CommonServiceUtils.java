package com.cxz.defined;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用 MyBatis-Plus 服务工具类，所有方法均为静态调用，无需实例化
 */
public final class CommonServiceUtils {

    // 私有构造，避免实例化
    private CommonServiceUtils() { }

    /**
     * 从源集合中提取指定属性并作为查询条件查询目标集合
     *
     * @param sourceList  源集合（List<T>），包含需要提取属性的元素
     * @param service     执行查询的 MyBatis-Plus IService<R>
     * @param sourceField 提取源集合中每个元素的属性值的函数（如 T::getId）
     * @param targetField 目标集合中查询字段的函数引用（如 R::getForeignId）
     * @param <T>         源集合元素类型
     * @param <R>         目标集合中元素类型
     * @return 查询结果集合
     */
    public static <T, R> List<R> findByFieldInTargetField(
            List<T> sourceList,
            IService<R> service,
            Function<T, ?> sourceField,
            SFunction<R, ?> targetField
    ) {
        List<Object> fieldValues = sourceList.stream()
                .map(sourceField)
                .collect(Collectors.toList());

        if (fieldValues.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<R> query = new LambdaQueryWrapper<>();
        query.in(targetField, fieldValues);
        return service.list(query);
    }

    /**
     * 根据指定字段和值，使用给定的服务对象构建查询条件。
     *
     * @param <T>        实体类类型
     * @param field      目标查询字段的函数引用（如 User::getUsername）
     * @param fieldValue 查询字段的值
     * @param service    用于执行查询的服务层对象（可选，用于后续执行 list/query 等）
     * @return 返回构建的 LambdaQueryWrapper 对象，用于进一步查询或修改
     */
    public static <T> LambdaQueryWrapper<T> buildQueryWrapperByField(
            SFunction<T, ?> field,
            Object fieldValue,
            IService<T> service
    ) {
        LambdaQueryWrapper<T> query = new LambdaQueryWrapper<>();
        query.eq(field, fieldValue);
        return query;
    }

    /**
     * 根据指定字段和值，使用给定的服务对象查询对应的实体类列表。
     *
     * @param <T>        实体类类型
     * @param field      目标查询字段的函数引用（如 User::getUsername）
     * @param fieldValue 查询字段的值
     * @param service    用于执行查询的服务层对象
     * @return 返回符合条件的实体类列表（List<T>）。如果没有符合条件的记录，返回空列表。
     */
    public static <T> List<T> findByFieldEqTargetField(
            SFunction<T, ?> field,
            Object fieldValue,
            IService<T> service
    ) {
        LambdaQueryWrapper<T> query = new LambdaQueryWrapper<>();
        query.eq(field, fieldValue);
        return service.list(query);
    }

    /**
     * 根据多个字段和对应的值进行查询。
     * 该方法可以动态构建查询条件并执行查询。
     *
     * @param <T>             实体类类型
     * @param fieldConditions 查询条件的字段引用和值，使用 Map 存储（key：R::getXxx，value：对应的查询值）
     * @param service         执行查询操作的服务对象（MyBatis-Plus 的 IService）
     * @return 返回查询结果的列表
     */
    public static <T> List<T> findByFieldEqTargetFields(
            Map<SFunction<T, ?>, Object> fieldConditions,
            IService<T> service
    ) {
        LambdaQueryWrapper<T> query = new LambdaQueryWrapper<>();
        fieldConditions.forEach(query::eq);
        return service.list(query);
    }

    /**
     * 将一个类型的 List 转换为另一个类型的 List。
     *
     * @param sourceList  源列表，类型为 T
     * @param targetClass 目标类型的 Class 对象
     * @param <T>         源类型
     * @param <R>         目标类型
     * @return 转换后的目标类型列表
     */
    public static <T, R> List<R> convertList(
            List<T> sourceList,
            Class<R> targetClass
    ) {
        return sourceList.stream()
                .map(source -> {
                    try {
                        R target = targetClass.getDeclaredConstructor().newInstance();
                        BeanUtils.copyProperties(source, target);
                        return target;
                    } catch (Exception e) {
                        throw new RuntimeException("Error copying properties", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 复制属性并返回新的目标对象
     *
     * @param source      源对象
     * @param targetClass 目标对象的类型
     * @param <S>         源对象类型
     * @param <T>         目标对象类型
     * @return 目标对象
     */
    public static <S, T> T copyProperties(
            S source,
            Class<T> targetClass
    ) {
        if (source == null || targetClass == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Error copying properties", e);
        }
    }
}
