package com.ksptool.bio.biz.core.common;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tuple -> Bean 映射工具。
 * <p>
 * 风格与 {@code Entities.as} 保持一致，通过 static import 使用：
 * <pre>
 *     import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
 *
 *     Page&lt;Tuple&gt; page = repository.getXxxList(dto, dto.pageRequest());
 *     List&lt;GetXxxListVo&gt; vos = tupleAs(page.getContent(), GetXxxListVo.class);
 * </pre>
 * <p>
 * 要求 JPQL 中每一列都使用 {@code AS 驼峰别名}，别名与目标 VO 字段名一致。
 *
 * @author WangQingHua(603484930@qq.com)
 * @license Apache-2.0
 * @since 2026-04-17
 */
public final class TupleMapper {

    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private TupleMapper() {
    }

    /**
     * 将单个 Tuple 映射为目标类型实例。
     *
     * @param tuple       待映射的Tuple
     * @param targetClass 目标类型
     * @return 映射后的对象
     * @throws IllegalStateException 如果无法实例化目标类型
     */
    public static <T> T tupleAs(Tuple tuple, Class<T> targetClass) {
        if (tuple == null || targetClass == null) {
            return null;
        }

        T target;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法实例化 " + targetClass.getName() + "：需要一个无参构造函数.", e);
        }

        Map<String, Field> fields = resolveFields(targetClass);

        for (TupleElement<?> elem : tuple.getElements()) {
            String alias = elem.getAlias();

            if (StringUtils.isBlank(alias)) {
                continue;
            }

            Field field = fields.get(alias);
            if (field == null) {
                continue;
            }

            Object value = tuple.get(alias);
            if (value == null) {
                continue;
            }

            Object converted = convert(value, field.getType());
            try {
                field.set(target, converted);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("无法写入字段 " + targetClass.getName() + "#" + field.getName(), e);
            }
        }

        return target;
    }

    /**
     * 将 Tuple 列表映射为目标类型列表。
     *
     * @param tuples      待映射的Tuple列表
     * @param targetClass 目标类型
     * @return 映射后的对象列表
     */
    public static <T> List<T> tupleAs(List<Tuple> tuples, Class<T> targetClass) {
        if (tuples == null || tuples.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>(tuples.size());
        for (Tuple t : tuples) {
            list.add(tupleAs(t, targetClass));
        }
        return list;
    }

    /**
     * 将 Page&lt;Tuple&gt; 映射为 Page&lt;T&gt;，保留分页信息与总数。
     *
     * @param page        待映射的Tuple分页
     * @param targetClass 目标类型
     * @return 映射后的对象分页
     */
    public static <T> Page<T> tupleAs(Page<Tuple> page, Class<T> targetClass) {
        if (page == null) {
            return new PageImpl<>(Collections.emptyList());
        }

        List<T> content = tupleAs(page.getContent(), targetClass);
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    /**
     * 读取目标类所有可写字段（含父类），按字段名建立索引并缓存。
     *
     * @param cls 目标类
     * @return 字段索引
     */
    private static Map<String, Field> resolveFields(Class<?> cls) {
        Map<String, Field> cached = FIELD_CACHE.get(cls);
        if (cached != null) {
            return cached;
        }

        Map<String, Field> map = new HashMap<>();
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if (Modifier.isFinal(f.getModifiers())) {
                    continue;
                }
                if (map.containsKey(f.getName())) {
                    continue;
                }
                f.setAccessible(true);
                map.put(f.getName(), f);
            }
            c = c.getSuperclass();
        }

        FIELD_CACHE.put(cls, map);
        return map;
    }

    /**
     * 类型适配：覆盖常见的 数值互转 / 日期时间 / 字符串 等场景，其余类型原样返回。
     *
     * @param value      待转换的值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convert(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }

        if (value instanceof Number n) {
            Object num = convertNumber(n, targetType);
            if (num != null) {
                return num;
            }
        }

        if (value instanceof Timestamp ts) {
            if (targetType == LocalDateTime.class) {
                return ts.toLocalDateTime();
            }
            if (targetType == LocalDate.class) {
                return ts.toLocalDateTime().toLocalDate();
            }
            if (targetType == Date.class) {
                return new Date(ts.getTime());
            }
        }

        if (value instanceof java.sql.Date sqlDate) {
            if (targetType == LocalDate.class) {
                return sqlDate.toLocalDate();
            }
            if (targetType == LocalDateTime.class) {
                return sqlDate.toLocalDate().atStartOfDay();
            }
        }

        if (value instanceof Date d && !(value instanceof Timestamp) && !(value instanceof java.sql.Date)) {
            Instant instant = d.toInstant();
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
            if (targetType == LocalDate.class) {
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
            }
        }

        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    /**
     * 数字类型互转。命中返回转换结果，未命中返回 null。
     *
     * @param n          待转换的数字
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convertNumber(Number n, Class<?> targetType) {
        if (targetType == Long.class || targetType == long.class) {
            return n.longValue();
        }
        if (targetType == Integer.class || targetType == int.class) {
            return n.intValue();
        }
        if (targetType == Short.class || targetType == short.class) {
            return n.shortValue();
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return n.byteValue();
        }
        if (targetType == Double.class || targetType == double.class) {
            return n.doubleValue();
        }
        if (targetType == Float.class || targetType == float.class) {
            return n.floatValue();
        }
        if (targetType == BigDecimal.class) {
            if (n instanceof BigDecimal bd) {
                return bd;
            }
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (targetType == BigInteger.class) {
            return BigInteger.valueOf(n.longValue());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return n.intValue() != 0;
        }
        return null;
    }
}
