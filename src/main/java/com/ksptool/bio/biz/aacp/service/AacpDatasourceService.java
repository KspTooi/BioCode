package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import com.ksptool.bio.biz.aacp.model.datasource.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacp.repository.AacpDatasourceRepository;
import com.ksptool.bio.biz.aacp.repository.CapDatasourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpDatasourceService {

    @Autowired
    private AacpDatasourceRepository repository;

    @Autowired
    private CapDatasourceRepository capDatasourceRepository;

    /**
     * 查询AACP数据源列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(GetAacpDatasourceListDto dto) {
        AacpDatasourcePo query = new AacpDatasourcePo();
        assign(dto, query);

        Page<AacpDatasourcePo> page = repository.getAacpDatasourceList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpDatasourceListVo> vos = as(page.getContent(), GetAacpDatasourceListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增AACP数据源
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpDatasource(AddAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpDatasourcePo insertPo = as(dto, AacpDatasourcePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑AACP数据源
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpDatasource(EditAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpDatasourcePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询AACP数据源详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpDatasourceDetailsVo getAacpDatasourceDetails(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpDatasourceDetailsVo.class);
    }

    /**
     * 删除AACP数据源
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpDatasource(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("数据源不支持批量删除");
        }
        long refCount = capDatasourceRepository.countByDatasourceId(dto.getId());
        if (refCount > 0) {
            throw new BizException("该数据源已被" + refCount + "个能力包使用，无法删除");
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 测试AACP数据源连接
     *
     * @param dto 查询参数
     * @return 测试结果
     * @throws BizException 业务异常
     */
    public Result<String> testAacpDatasourceConnection(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("测试失败,数据不存在或无权限访问."));

        long startTime = System.currentTimeMillis();
        try {
            Class.forName(po.getDrive());
        } catch (ClassNotFoundException e) {
            return Result.error("测试失败,JDBC驱动不存在.");
        }

        try {
            Connection connection = DriverManager.getConnection(po.getUrl(), po.getUsername(), po.getPassword());
            connection.close();
        } catch (SQLException e) {
            return Result.error("测试失败,连接失败: " + e.getMessage());
        }

        return Result.success("成功连接数据库 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
