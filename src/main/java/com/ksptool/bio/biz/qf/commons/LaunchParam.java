package com.ksptool.bio.biz.qf.commons;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ksptool.bio.biz.core.common.aop.DtoCustomValidator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 启动流程参数
 * @author KspTool(ksptool@outlook.com)
 * @since 1.7.2(B).12
 */
@Getter
@Builder
public class LaunchParam implements DtoCustomValidator{

    //模型编码
    private String modelCode;

    //业务数据ID
    private Long dataId;

    //传递给摘要模板的参数
    private final Map<String, String> sParams = new HashMap<>();

    //启动成员参数列表
    private final Set<LaunchMemberParam> members = new HashSet<>();


    /**
     * 启动成员参数，流程中的某个节点配置为"发起时选人"，则必须要在发起时在这个节点指定处理人，否则无法启动流程。
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public class LaunchMemberParam {
        //节点ID
        private String nodeId;

        //成员ID
        private Long memberId;

        @Override
        public String toString() {
            return "LaunchMemberParam [nodeId=" + nodeId + ", memberId=" + memberId + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            LaunchMemberParam that = (LaunchMemberParam) obj;
            return Objects.equals(nodeId, that.nodeId) && Objects.equals(memberId, that.memberId);
        }
    }

    /**
     * 添加启动成员参数,重复添加会覆盖之前的参数
     * @param nodeId 节点ID
     * @param memberId 成员ID
     */
    public void addMember(String nodeId, Long memberId) {
        members.add(new LaunchMemberParam(nodeId, memberId));
    }

    /**
     * 根据节点ID获取指定处理人
     * @param nodeId 节点ID
     * @return 指定处理人ID 未找到返回null
     */
    public Long getMemberId(String nodeId) {
        return members.stream().filter(n -> n.getNodeId().equals(nodeId)).findFirst().map(n -> n.getMemberId()).orElse(null);
    }

    /**
     * 验证入参
     * @return 错误信息 为空则验证通过
     */
    @Override
    public String validate() {

        if (StringUtils.isBlank(modelCode) || dataId == null) {
            return "模型编码或业务数据ID不能为空";
        }

        return null;
    }
}
