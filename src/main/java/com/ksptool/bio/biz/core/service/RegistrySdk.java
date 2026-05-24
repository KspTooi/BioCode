package com.ksptool.bio.biz.core.service;

import com.ksptool.bio.biz.core.model.registry.RegistryPo;
import com.ksptool.bio.biz.core.model.registry.dto.GetRegistryListDto;
import com.ksptool.bio.biz.core.repository.RegistryRepository;
import com.ksptool.bio.commons.dataprocess.Str;
import com.ksptool.bio.commons.utils.RegistryTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 注册表SDK
 * 这个类提供了注册表的常用操作，包括获取、设置、删除、创建等操作。
 * 这个类使用了Spring Cache来缓存注册表的值，以提高性能。
 *
 * @author KspTooi
 * @since 1.5.2(B).1
 */
@Service
public class RegistrySdk {

    @Autowired
    private RegistryRepository repository;

    /**
     * 清除所有缓存
     */
    @CacheEvict(value = "registry", allEntries = true)
    public void clearAllCache() {
    }

    /**
     * 获取整数条目值(这是一个带有缓存的函数)
     *
     * @param keyPath      全路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Cacheable(value = "registry", key = "#keyPath")
    public int getInt(String keyPath, int defaultValue) {

        if (Str.isBlank(keyPath)) {
            return defaultValue;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return defaultValue;
        }

        var nvalue = nodePo.getNvalue();

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 1 || Str.isBlank(nvalue)) {
            return defaultValue;
        }

        return Integer.parseInt(nodePo.getNvalue());
    }

    /**
     * 获取浮点条目值(这是一个带有缓存的函数)
     *
     * @param keyPath      全路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Cacheable(value = "registry", key = "#keyPath")
    public double getDouble(String keyPath, double defaultValue) {

        if (Str.isBlank(keyPath)) {
            return defaultValue;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return defaultValue;
        }

        var nvalue = nodePo.getNvalue();

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 2 || Str.isBlank(nvalue)) {
            return defaultValue;
        }

        return Double.parseDouble(nodePo.getNvalue());
    }

    /**
     * 获取字串条目值(这是一个带有缓存的函数)
     *
     * @param keyPath      全路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Cacheable(value = "registry", key = "#keyPath")
    public String getString(String keyPath, String defaultValue) {
        if (Str.isBlank(keyPath)) {
            return defaultValue;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return defaultValue;
        }

        var nvalue = nodePo.getNvalue();

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 0 || Str.isBlank(nvalue)) {
            return defaultValue;
        }

        return nodePo.getNvalue();
    }

    /**
     * 获取日期时间条目值(这是一个带有缓存的函数)
     *
     * @param keyPath      全路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Cacheable(value = "registry", key = "#keyPath")
    public LocalDateTime getDateTime(String keyPath, LocalDateTime defaultValue) {
        if (Str.isBlank(keyPath)) {
            return defaultValue;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return defaultValue;
        }

        var nvalue = nodePo.getNvalue();

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 3 || Str.isBlank(nvalue)) {
            return defaultValue;
        }

        return LocalDateTime.parse(nodePo.getNvalue());
    }

    /**
     * 设置整数条目值(这会清除指定keyPath的缓存)
     *
     * @param keyPath 全路径
     * @param value   值
     * @return 是否成功
     */
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean setInt(String keyPath, int value) {

        if (Str.isBlank(keyPath)) {
            return false;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return false;
        }

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 1) {
            return false;
        }

        nodePo.setNvalue(String.valueOf(value));
        repository.save(nodePo);
        return true;
    }

    /**
     * 设置浮点条目值(这会清除指定keyPath的缓存)
     *
     * @param keyPath 全路径
     * @param nvalue  值
     * @return 是否成功
     */
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean setDouble(String keyPath, double nvalue) {
        if (Str.isBlank(keyPath)) {
            return false;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return false;
        }

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 2) {
            return false;
        }

        nodePo.setNvalue(String.valueOf(nvalue));
        repository.save(nodePo);
        return true;

    }

    /**
     * 设置字串条目值(这会清除指定keyPath的缓存)
     *
     * @param keyPath 全路径
     * @param nvalue  值
     * @return 是否成功
     */
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean setString(String keyPath, String nvalue) {
        if (Str.isBlank(keyPath)) {
            return false;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return false;
        }

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 0) {
            return false;
        }

        nodePo.setNvalue(nvalue);
        repository.save(nodePo);
        return true;
    }

    /**
     * 设置日期时间条目值(这会清除指定keyPath的缓存)
     *
     * @param keyPath 全路径
     * @param nvalue  值
     * @return 是否成功
     */
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean setDateTime(String keyPath, LocalDateTime nvalue) {
        if (Str.isBlank(keyPath)) {
            return false;
        }

        //查找注册表条目
        RegistryPo nodePo = repository.getRegistryEntryByKeyPath(keyPath);

        if (nodePo == null) {
            return false;
        }

        //数据类型 0:字串 1:整数 2:浮点 3:日期(LDT)
        if (nodePo.getNvalueKind() != 3) {
            return false;
        }

        nodePo.setNvalue(nvalue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        repository.save(nodePo);
        return true;
    }

    /**
     * 获取注册表条目
     *
     * @param keyPath 全路径
     * @return 注册表条目
     */
    public RegistryPo getRegistryEntry(String keyPath) {
        return repository.getRegistryEntryByKeyPath(keyPath);
    }

    /**
     * 判断注册表条目是否存在
     *
     * @param keyPath 全路径
     * @return 是否存在
     */
    public boolean hasEntry(String keyPath) {
        return repository.countByKeyPathAndKind(keyPath, 1) > 0;
    }

    /**
     * 判断注册表节点是否存在
     *
     * @param keyPath 全路径
     * @return 是否存在
     */
    public boolean hasNode(String keyPath) {
        return repository.countByKeyPathAndKind(keyPath, 0) > 0;
    }


    /**
     * 获取注册表条目列表
     *
     * @param nodeKeyPath 节点全路径
     * @return 注册表条目列表
     */
    @Cacheable(value = "registry", key = "#nodeKeyPath")
    public List<RegistryPo> getEntries(String nodeKeyPath) {

        if (Str.isBlank(nodeKeyPath)) {
            return new ArrayList<>();
        }

        //查找注册表节点
        RegistryPo nodePo = repository.getRegistryNodeByKeyPath(nodeKeyPath);

        if (nodePo == null) {
            return new ArrayList<>();
        }

        //查询该节点下全部子项
        return repository.getRegistryEntryListNotPage(nodePo.getId(), new GetRegistryListDto());
    }

    /**
     * 创建注册表节点
     *
     * @param keyPath 全路径
     * @param label   标签(仅用于最下级节点)
     * @param seq     排序(仅用于最下级节点)
     * @param remark  说明(仅用于最下级节点)
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createNode(String keyPath, String label, int seq, String remark) {

        if (Str.isBlank(keyPath)) {
            return false;
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return false;
        }

        // 检查节点是否已存在
        if (repository.countByKeyPathAndKind(keyPath, 0) > 0) {
            return true;
        }

        // 分割keyPath获取各层级
        String[] keys = keyPath.split("\\.");

        // 递归创建各层级节点
        String currentPath = "";
        Long parentId = null;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            // 构建当前层级的完整路径
            if (i == 0) {
                currentPath = key;
            }

            if (i > 0) {
                currentPath = currentPath + "." + key;
            }

            // 检查当前层级节点是否已存在
            RegistryPo existNode = repository.getRegistryNodeByKeyPath(currentPath);

            if (existNode != null) {
                // 节点已存在,记录其ID作为下一层的父级ID
                parentId = existNode.getId();
                continue;
            }

            // 节点不存在,创建新节点
            RegistryPo newNode = new RegistryPo();
            newNode.setParentId(parentId);
            newNode.setKeyPath(currentPath);
            newNode.setKind(0); // 0:节点
            newNode.setNkey(key);
            newNode.setNvalueKind(null);
            newNode.setNvalue(null);
            newNode.setMetadata(null);
            newNode.setIsSystem(0);
            newNode.setStatus(0);

            // 判断是否是最下级节点
            boolean isLastLevel = (i == keys.length - 1);

            if (isLastLevel) {
                // 最下级节点使用传入的参数
                newNode.setLabel(label);
                newNode.setSeq(seq);
                newNode.setRemark(remark);
            }

            if (!isLastLevel) {
                // 非最下级节点使用默认值
                newNode.setLabel(null);
                newNode.setSeq(0);
                newNode.setRemark(null);
            }

            // 保存节点
            repository.save(newNode);

            // 记录新创建节点的ID作为下一层的父级ID
            parentId = newNode.getId();
        }

        return true;
    }

    /**
     * 删除注册表节点
     *
     * @param keyPath 全路径
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean removeNode(String keyPath) {

        // 空值校验
        if (Str.isBlank(keyPath)) {
            return false;
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return false;
        }

        // 查找节点
        RegistryPo nodePo = repository.getRegistryNodeByKeyPath(keyPath);

        if (nodePo == null) {
            return false;
        }

        // 检查节点类型
        if (nodePo.getKind() != 0) {
            return false;
        }

        // 检查是否有子节点
        long childCount = repository.countByParentId(nodePo.getId());

        if (childCount > 0) {
            return false;
        }

        // 删除节点
        repository.deleteById(nodePo.getId());
        return true;
    }

    /**
     * 创建注册表条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath    父节点全路径
     * @param nkey       条目Key
     * @param nvalueKind 数据类型 0:字串 1:整数 2:浮点 3:日期
     * @param nvalue     值
     * @param label      标签
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    protected boolean createEntry(String keyPath, String nkey, int nvalueKind, String nvalue, String label) {

        // 空值校验
        if (Str.isBlank(keyPath)) {
            return false;
        }

        if (Str.isBlank(nkey)) {
            return false;
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return false;
        }

        // 校验nkey合法性(不能包含点号等特殊字符)
        if (nkey.length() > 128) {
            return false;
        }

        if (!nkey.matches("^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$")) {
            return false;
        }

        // 校验数据类型 0:字串 1:整数 2:浮点 3:日期
        if (nvalueKind < 0 || nvalueKind > 3) {
            return false;
        }

        // 构建条目的完整keyPath
        String entryKeyPath = keyPath + "." + nkey;

        // 检查条目是否已存在
        if (repository.countByKeyPathAndKind(entryKeyPath, 1) > 0) {
            return false;
        }

        // 查找父节点
        RegistryPo parentNode = repository.getRegistryNodeByKeyPath(keyPath);

        // 如果父节点不存在,递归创建父节点
        if (parentNode == null) {
            boolean createResult = createNode(keyPath, null, 0, null);
            if (!createResult) {
                return false;
            }
            // 重新查询父节点
            parentNode = repository.getRegistryNodeByKeyPath(keyPath);
            if (parentNode == null) {
                return false;
            }
        }

        // 检查父节点类型
        if (parentNode.getKind() != 0) {
            return false;
        }

        // 创建条目
        RegistryPo entryPo = new RegistryPo();
        entryPo.setParentId(parentNode.getId());
        entryPo.setKeyPath(entryKeyPath);
        entryPo.setKind(1); // 1:条目
        entryPo.setNkey(nkey);
        entryPo.setNvalueKind(nvalueKind);
        entryPo.setNvalue(nvalue);
        entryPo.setLabel(label);
        entryPo.setRemark(null);
        entryPo.setMetadata(null);
        entryPo.setIsSystem(0);
        entryPo.setStatus(0);
        entryPo.setSeq(0);

        repository.save(entryPo);
        return true;
    }

    /**
     * 创建字串类型条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath 父节点全路径
     * @param nkey    条目Key
     * @param value   字串值
     * @param label   标签
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean createStringEntry(String keyPath, String nkey, String value, String label) {
        return createEntry(keyPath, nkey, 0, value, label);
    }

    /**
     * 创建整数类型条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath 父节点全路径
     * @param nkey    条目Key
     * @param value   整数值
     * @param label   标签
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean createIntEntry(String keyPath, String nkey, int value, String label) {
        return createEntry(keyPath, nkey, 1, String.valueOf(value), label);
    }

    /**
     * 创建浮点类型条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath 父节点全路径
     * @param nkey    条目Key
     * @param value   浮点值
     * @param label   标签
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean createDoubleEntry(String keyPath, String nkey, double value, String label) {
        return createEntry(keyPath, nkey, 2, String.valueOf(value), label);
    }

    /**
     * 创建日期时间类型条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath 父节点全路径
     * @param nkey    条目Key
     * @param value   日期时间值
     * @param label   标签
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean createDateTimeEntry(String keyPath, String nkey, LocalDateTime value, String label) {

        if (value == null) {
            return false;
        }

        return createEntry(keyPath, nkey, 3, value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), label);
    }

    /**
     * 删除注册表条目(这会清除指定keyPath的缓存)
     *
     * @param keyPath 条目全路径
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "registry", key = "#keyPath")
    public boolean removeEntry(String keyPath) {

        // 空值校验
        if (Str.isBlank(keyPath)) {
            return false;
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return false;
        }

        // 查找条目
        RegistryPo entryPo = repository.getRegistryEntryByKeyPath(keyPath);

        if (entryPo == null) {
            return false;
        }

        // 检查条目类型
        if (entryPo.getKind() != 1) {
            return false;
        }

        // 删除条目
        repository.deleteById(entryPo.getId());
        return true;
    }

    /**
     * 获取注册表节点
     *
     * @param keyPath 节点全路径
     * @return 注册表节点
     */
    public RegistryPo getNode(String keyPath) {

        // 空值校验
        if (Str.isBlank(keyPath)) {
            return null;
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return null;
        }

        return repository.getRegistryNodeByKeyPath(keyPath);
    }

    /**
     * 获取子节点列表
     *
     * @param keyPath 父节点全路径
     * @return 子节点列表
     */
    public List<RegistryPo> getChildNodes(String keyPath) {

        // 空值校验
        if (Str.isBlank(keyPath)) {
            return new ArrayList<>();
        }

        // 校验keyPath合法性
        if (!RegistryTool.allowKeyPath(keyPath)) {
            return new ArrayList<>();
        }

        // 查找父节点
        RegistryPo parentNode = repository.getRegistryNodeByKeyPath(keyPath);

        if (parentNode == null) {
            return new ArrayList<>();
        }

        // 查询子节点(kind=0表示节点)
        return repository.findAll().stream()
                .filter(item -> item.getKind() == 0 && parentNode.getId().equals(item.getParentId()))
                .sorted((a, b) -> {
                    // 先按seq排序,再按nkey排序
                    int seqCompare = Integer.compare(a.getSeq(), b.getSeq());
                    if (seqCompare != 0) {
                        return seqCompare;
                    }
                    return a.getNkey().compareTo(b.getNkey());
                })
                .toList();
    }
}
