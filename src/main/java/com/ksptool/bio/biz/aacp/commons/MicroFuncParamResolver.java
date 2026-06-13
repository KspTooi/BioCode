package com.ksptool.bio.biz.aacp.commons;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import static com.ksptool.entities.Entities.as;

/**
 * 微函数参数解析器
 * 将输入JSON字符串解析为参数列表
 */
@Getter
public class MicroFuncParamResolver {

    //输入JSON字符串
    private final Map<String, Object> inputParams;

    //参数定义列表
    private final List<MicroFuncParamDefinition> defParams;


    /**
     * 构造函数
     *
     * @param inputParams 输入参数
     * @param defParams   参数定义
     */
    public MicroFuncParamResolver(Map<String, Object> inputParams, List<MicroFuncParamDefinition> defParams) {
        this.inputParams = inputParams;
        this.defParams = defParams;
    }

    public static MicroFuncParamResolver of(Map<String, Object> inputParams, List<MicroFuncParamDefinition> defParams) {
        return new MicroFuncParamResolver(inputParams, defParams);
    }

    
    /**
     * 解析参数
     * @return 参数数组
     */
    public Object[] resolve() {

        Object[] params = new Object[defParams.size()];

        for (var def : defParams) {

            var idx = def.getIndex();

            //如果输入中没有参数定义的参数名，则跳过
            if (!inputParams.containsKey(def.getName())) {
                continue;
            }

            var pFip = inputParams.get(def.getName());

            // 如果值已经是目标类型实例，直接透传
            if (def.getType().isInstance(pFip)) {
                params[idx] = pFip;
                continue;
            }

            // 否则尝试 as 映射（适用于 DTO 等复杂类型）
            params[idx] = as(pFip, def.getType());
        }

        return params;
    }


    /**
     * 获取参数
     *
     * @param name 参数名称
     * @return 参数值 无返回值则返回null
     */
    public Object getParam(String name) {
        return inputParams.get(name);
    }

    /**
     * 判断参数是否存在
     *
     * @param name 参数名称
     * @return 是否存在
     */
    public boolean hasParam(String name) {
        return inputParams.containsKey(name);
    }

}
