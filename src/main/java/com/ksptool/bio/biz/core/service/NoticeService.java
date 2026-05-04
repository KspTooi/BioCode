package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageQuery;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.notice.NoticePo;
import com.ksptool.bio.biz.core.model.notice.dto.AddNoticeDto;
import com.ksptool.bio.biz.core.model.notice.dto.EditNoticeDto;
import com.ksptool.bio.biz.core.model.notice.dto.GetNoticeListDto;
import com.ksptool.bio.biz.core.model.notice.vo.GetNoticeDetailsVo;
import com.ksptool.bio.biz.core.model.notice.vo.GetNoticeListVo;
import com.ksptool.bio.biz.core.model.noticercd.NoticeRcdPo;
import com.ksptool.bio.biz.core.repository.NoticeRcdRepository;
import com.ksptool.bio.biz.core.repository.NoticeRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * @author KspTooi
 * @since 1.2.8(H).1
 */
@Slf4j
@Service
public class NoticeService {

    @Autowired
    private NoticeRepository repository;

    @Autowired
    private NoticeRcdRepository noticeRcdRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查询消息列表
     *
     * @param dto 查询条件
     * @return 消息列表
     */
    public PageResult<GetNoticeListVo> getNoticeList(GetNoticeListDto dto) {
        NoticePo query = new NoticePo();
        assign(dto, query);

        Page<NoticePo> page = repository.getNoticeList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetNoticeListVo> vos = as(page.getContent(), GetNoticeListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增消息
     *
     * @param dto 新增消息DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void addNotice(AddNoticeDto dto) {

        //先保存消息，这样才能回填ID
        var insertPo = as(dto, NoticePo.class);

        //但是在保存消息之前，还需要获取当前是谁在发消息
        var aud = SessionService.sessionWithNullable();

        //如果是系统自动发 或者定时任务发 这里拿不到AUD
        if (aud == null) {
            insertPo.setSenderId(-1L);
            insertPo.setSenderName("系统");
        }

        //如果拿得到AUD 则设置发送人ID和发送人姓名
        if (aud != null) {
            insertPo.setSenderId(aud.getUserId());
            insertPo.setSenderName(aud.getUsername());
        }

        //保存消息
        insertPo = repository.save(insertPo);

        //获取回填ID
        var noticeId = insertPo.getId();

        var insertRcdPos = new ArrayList<NoticeRcdPo>();

        //处理消息RCD转发 - 全员
        if (dto.getTargetKind() == 0) {

            //分页批量发送消息RCD
            var page = new PageQuery();
            page.setPageNum(1);
            page.setPageSize(1000);

            //预计接收人数
            var targetCount = 0;

            while (true) {

                var userIds = userRepository.getUserIdsList(page.pageRequest());

                if (userIds.isEmpty()) {
                    break;
                }

                for (var userId : userIds) {
                    var insertRcdPo = new NoticeRcdPo();
                    insertRcdPo.setNoticeId(noticeId);
                    insertRcdPo.setUserId(userId);
                    insertRcdPos.add(insertRcdPo);
                }

                page.setPageNum(page.getPageNum() + 1);

                //保存本次批量插入的数据
                noticeRcdRepository.saveAll(insertRcdPos);
                targetCount += insertRcdPos.size();
                insertRcdPos.clear();
            }

            //更新消息预计接收人数
            insertPo.setTargetCount(targetCount);
            repository.save(insertPo);
        }

        //处理消息RCD转发 - 指定部门
        if (dto.getTargetKind() == 1) {

            //查询部门下全部用户列表
            var userPos = userRepository.getUserListByDeptIds(dto.getTargetIds());

            for (var userPo : userPos) {
                var insertRcdPo = new NoticeRcdPo();
                insertRcdPo.setNoticeId(noticeId);
                insertRcdPo.setUserId(userPo.getId());
                insertRcdPos.add(insertRcdPo);
            }

            //保存消息RCD转发
            noticeRcdRepository.saveAll(insertRcdPos);

            //更新消息预计接收人数
            insertPo.setTargetCount(userPos.size());
            repository.save(insertPo);
        }

        //处理消息RCD转发 - 指定用户
        if (dto.getTargetKind() == 2) {

            var userPos = userRepository.findAllById(dto.getTargetIds());

            for (var userPo : userPos) {
                var insertRcdPo = new NoticeRcdPo();
                insertRcdPo.setNoticeId(noticeId);
                insertRcdPo.setUserId(userPo.getId());
                insertRcdPos.add(insertRcdPo);
            }

            //保存消息RCD转发
            noticeRcdRepository.saveAll(insertRcdPos);

            //更新消息预计接收人数
            insertPo.setTargetCount(userPos.size());
            repository.save(insertPo);
        }


    }


    /**
     * 编辑消息
     *
     * @param dto 编辑消息DTO
     * @throws BizException 编辑消息失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void editNotice(EditNoticeDto dto) throws BizException {
        NoticePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询消息详情
     *
     * @param dto 查询消息详情DTO
     * @return 消息详情
     * @throws BizException 查询消息详情失败
     */
    public GetNoticeDetailsVo getNoticeDetails(CommonIdDto dto) throws BizException {
        NoticePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetNoticeDetailsVo.class);
    }

    /**
     * 删除消息
     *
     * @param dto 删除消息DTO
     * @throws BizException 删除消息失败
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeNotice(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 发送系统通知
     *
     * @param uid      接收人ID
     * @param title    标题
     * @param category 业务类型/分类
     * @param content  通知内容
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void sendSystemNotice(Long uid, String title, String category, String content) {

        var userPo = userRepository.findById(uid).orElse(null);

        if (userPo == null) {
            log.warn("发送系统通知失败,接收人不存在: uid={}", uid);
            return;
        }

        var dto = new AddNoticeDto();
        dto.setTitle(title);
        dto.setKind(1);
        dto.setContent(content);
        dto.setPriority(2);
        dto.setCategory(category);
        dto.setTargetKind(2);
        dto.setTargetIds(List.of(uid));
        addNotice(dto);
        log.info("发送系统通知成功: uid={} 通知标题:{}", uid, title);
    }

}