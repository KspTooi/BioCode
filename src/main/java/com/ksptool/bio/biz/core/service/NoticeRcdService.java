package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.noticercd.dto.GetUserNoticeRcdListDto;
import com.ksptool.bio.biz.core.model.noticercd.vo.GetNoticeRcdDetailsVo;
import com.ksptool.bio.biz.core.model.noticercd.vo.GetUserNoticeRcdListVo;
import com.ksptool.bio.biz.core.repository.NoticeRcdRepository;
import com.ksptool.bio.biz.core.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;

/**
 * @author KspTooi
 * @since 1.2.8(H).1
 */
@Service
public class NoticeRcdService {

    @Autowired
    private NoticeRcdRepository repository;

    @Autowired
    private NoticeRepository noticeRepository;

    /**
     * 获取当前用户未读通知数量
     *
     * @return
     */
    public Integer getUserNoticeCount() throws AuthException {
        var userId = session().getUserId();
        return repository.getUserNoticeCount(userId);
    }

    /**
     * 阅读全部用户通知记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void readAllUserNoticeRcd() throws Exception {
        var userId = session().getUserId();
        repository.readAllUserNoticeRcd(userId);
    }

    /**
     * 查询消息接收记录列表
     *
     * @param dto
     * @return
     */
    public PageResult<GetUserNoticeRcdListVo> getUserNoticeRcdList(GetUserNoticeRcdListDto dto) throws Exception {

        // 分页查询当前用户消息列表（VO投影）
        var voPage = repository.getNoticeRcdsByUserId(session().getUserId(), dto, dto.pageRequest());

        if (voPage.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        //是否要在查询时设置已读 0:是 1:否
        if (dto.getSetRead() == 1) {

            // 从分页结果中提取RCD ID
            var rcdIds = voPage.getContent().stream().map(GetUserNoticeRcdListVo::getId).toList();

            if (rcdIds.isEmpty()) {
                return PageResult.successWithEmpty();
            }

            // 把这些已经查询出来的RcdPo设为已读
            var rcdPos = repository.getNotifyRcdByIdsAndUserId(rcdIds, session().getUserId());
            var needUpdate = false;

            for (var rcdPo : rcdPos) {
                if (rcdPo.getReadTime() == null) {
                    needUpdate = true;
                    rcdPo.setReadTime(LocalDateTime.now());
                }
            }

            if (needUpdate) {
                repository.saveAll(rcdPos);
            }

        }

        return PageResult.success(voPage.getContent(), voPage.getTotalElements());
    }

    /**
     * 查询消息接收记录详情
     *
     * @param dto
     * @return
     * @throws BizException
     */
    public GetNoticeRcdDetailsVo getUserNoticeRcdDetails(CommonIdDto dto) throws Exception {

        var noticePo = noticeRepository.getNoticeByRcdIdAndUserId(dto.getId(), session().getUserId());

        if (noticePo == null) {
            throw new BizException("查询详情失败,数据不存在或无权限访问.");
        }

        return as(noticePo, GetNoticeRcdDetailsVo.class);
    }

    /**
     * 删除消息接收记录
     *
     * @param dto
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeNoticeRcd(CommonIdDto dto) throws Exception {

        var ids = dto.getIds();

        if (ids.isEmpty()) {
            throw new BizException("删除失败,参数错误");
        }

        // 根据RCDID + UID查询RCD
        var noticeRcdPos = repository.getNotifyRcdByIdsAndUserId(ids, session().getUserId());

        if (noticeRcdPos.isEmpty()) {
            throw new BizException("删除失败,数据不存在或无权限访问.");
        }

        // 删除RCD
        repository.deleteAll(noticeRcdPos);
    }

}