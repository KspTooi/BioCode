package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.post.PostPo;
import com.ksptool.bio.biz.core.model.post.dto.AddPostDto;
import com.ksptool.bio.biz.core.model.post.dto.EditPostDto;
import com.ksptool.bio.biz.core.model.post.dto.GetPostListDto;
import com.ksptool.bio.biz.core.model.post.vo.GetPostDetailsVo;
import com.ksptool.bio.biz.core.model.post.vo.GetPostListVo;
import com.ksptool.bio.biz.core.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * @author KspTooi
 * @since 1.5.2(B).1
 */
@Service
public class PostService {

    @Autowired
    private PostRepository repository;

    /**
     * 查询岗位列表
     *
     * @param dto 查询条件
     * @return 岗位列表
     */
    public PageResult<GetPostListVo> getPostList(GetPostListDto dto) {
        PostPo query = new PostPo();
        assign(dto, query);

        Page<PostPo> page = repository.getPostList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPostListVo> vos = as(page.getContent(), GetPostListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增岗位
     *
     * @param dto 新增岗位信息
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPost(AddPostDto dto) throws BizException {

        //查询编码是否被占用
        var existPo = repository.countByCode(dto.getCode());
        if (existPo > 0) {
            throw new BizException("岗位编码已存在:[" + dto.getCode() + "]");
        }


        PostPo insertPo = as(dto, PostPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑岗位
     *
     * @param dto 编辑岗位信息
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPost(EditPostDto dto) throws BizException {
        PostPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));


        //查询编码是否被占用
        var existPo = repository.countByCodeExcludeId(dto.getCode(), updatePo.getId());
        if (existPo > 0) {
            throw new BizException("岗位编码已存在:[" + dto.getCode() + "]");
        }

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询岗位详情
     *
     * @param dto 查询条件
     * @return 岗位详情
     * @throws BizException 业务异常
     */
    public GetPostDetailsVo getPostDetails(CommonIdDto dto) throws BizException {
        PostPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetPostDetailsVo.class);
    }

    /**
     * 删除岗位
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePost(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}