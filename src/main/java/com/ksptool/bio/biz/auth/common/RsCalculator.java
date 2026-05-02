package com.ksptool.bio.biz.auth.common;

import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.repository.GroupDeptRepository;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.OrgRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * RS数据权限计算器
 * 用于计算用户拥有的行级数据权限
 * <p>
 * RS数据权限一种7级别的数据权限，完美支持大型集团型企业的数据权限管理,比传统的6级别数据权限更灵活更强大
 * <p>
 * 当用户登录时，本计算器会计算用户所有的RS等级，并以其中的最小值作为用户的RS等级
 * </p>
 * RS权限等级说明
 * rsMax=0 全集团数据
 * rsMax=10 本公司+下级公司
 * rsMax=20 仅本公司
 * rsMax=30 本部门+下级部门
 * rsMax=40 仅本部门
 * rsMax=50 仅本人
 * rsMax=60 指定组织
 * <p>
 * 关于业务表支持RS数据权限的说明
 * 在MRMO(多租户-多集团)数据权限设计中业务表有2ID或3ID方案
 * <p>
 * ####2ID方案:
 * RootId + OrgId(此ID为最细粒度机构 可公司可部门)
 * 优势汇总:
 * 1.2ID直接表示“归属组织节点”，公司、子公司、部门、班组都能统一表达
 * 2.不会出现org/dept一致性问题，只有一个 org_id，不会出现公司是A 部门是B的脏数据
 * 3.以后有班组、小组、事业部、区域中心，都还是同一个 org_id，不用继续加字段
 * 4.RS预计算权限更好做，RS计算器不需要输出两个ID集合
 * <p>
 * <p>
 * 问题汇总:
 * 1.RS=20更难做，需要预计算“本公司下所有部门但排除子公司”的节点集合
 * 2.allowIds可能包含公司、部门、班组所有可见节点，集团组织树很大时 SQL 变长
 * 3.报表不好写，公司维度统计麻烦 要回查组织树表
 * 4.历史归属会跟着组织树变化 部门从 A 公司迁到 B 公司后，历史数据也会被当前组织树解释为 B 公司数据；如果不能接受，需要快照字段
 * 5.权限排查稍绕：看到一条数据的 org_id，要查组织表才知道它到底是公司、部门还是班组
 * 6.强依赖组织树数据质量：kind、parent_id、org_path_ids 一旦错，权限结果会错
 * <p>
 * <p>
 * <p>
 * ####3ID方案:
 * RootId + OrgId + DeptId(此ID可空,用户可能无部门)
 * 优势汇总:
 * 1.RS=20更好做，仅本公司很简单：不用预计算“本公司下所有部门但排除子公司”的节点集合
 * 2.报表好写，公司维度统计方便 不用回查组织树表
 * 3.RS=10 本公司+下级公司时，只需要公司 ID 集合，不一定要包含所有部门节点
 * 4.一行数据同时能看出“属于哪个公司”和“属于哪个部门”，排查问题更快
 * <p>
 * 问题汇总:
 * 1.org、dept一致性难保证，组织调整时双字段同步成本翻倍，若部门从A公司挂到B公司，所有业务表的orgId都需要同步，工作量与风险巨大。
 * 2.RS预计算需要双集合，SQL 拼接更复杂，若选择3ID方案 RS计算器需要同时输出allowOrgIds  + allowDeptIds
 * 3.项目里如果有很多企业直属数据，deptId为空的情况会非常多
 * 4.我设计的项目中core_org是一棵任意深的树，3ID如果遇到加组织结构加kind，有改4ID的风险！
 * 5.改造成本高，业务表都要加字段
 * <p>
 * 其中两套方案都可实现完整的7级RS数据权限，本项目依据"最致命缺陷"原则选择2ID方案
 * <p>
 * 3ID方案最致命缺陷:
 * org_id和dept_id必须永远匹配，一旦匹配失败，权限结果会错
 * <p>
 * 2ID方案最致命缺陷:
 * 历史归属依赖当前组织树，如果部门从A公司迁进B公司，历史数据也会归属给B公司
 * <p>
 * 目前来说2ID已满足，3ID 或者以后4ID,还需根据不同的业务来进行扩展。
 * <p>
 * 综上所述本项目：标版选择2ID方案，扩展版选择3ID或4ID方案(由业务表自由扩展)
 *
 * @author KspTool
 * @since 2026-04-28
 */
public class RsCalculator {

    //用户
    private final UserPo user;

    //用户拥有的用户组
    private final List<GroupPo> groups;

    //部门与用户组关联关系仓库
    private final GroupDeptRepository gdRepository;

    //组织机构仓库
    private final OrgRepository coRepository;

    //用户最大RS等级
    private final int rsMax;

    /**
     * 构造函数
     *
     * @param groups 用户拥有的用户组
     */
    public RsCalculator(UserPo user, List<GroupPo> groups, GroupDeptRepository gdRepository, OrgRepository coRepository) {
        this.user = user;
        this.groups = groups;
        this.gdRepository = gdRepository;
        this.coRepository = coRepository;

        /*
         * A:权限优先级
         * 当用户存在多个组时,提取所有组的RowScope等级,取最小值作为用户的RowScope等级
         * RS等级: 0:全部 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:本部门 50:仅本人 60:指定部门
         *
         * B:允许访问的企业IDS
         * 当用户存在多个组时,提取所有组的允许访问的企业IDS,取并集作为用户的允许访问的企业IDS
         *
         * C:允许访问的部门IDS
         * 当用户存在多个组时,提取所有组的允许访问的部门IDS,取并集作为用户的允许访问的部门IDS
         */
        //计算用户的最大RS等级
        int rsMax = 100;

        //提取所有组的RowScope等级,取最小值作为用户的RowScope等级
        for (GroupPo group : groups) {
            if (group.getRowScope() < rsMax) {
                rsMax = group.getRowScope();
            }
        }

        this.rsMax = rsMax;
    }

    /**
     * 进行数据权限预计算
     *
     * @return 数据权限预计算结果
     */
    public RsCalculated calculate() {

        //允许访问的组织IDS
        var allowOrgIds = new HashSet<Long>();

        //如果RS等级为100,则允许访问的组织IDS为空
        if (rsMax == 100) {
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为60 只能访问角色上选择的组织机构
        if (rsMax == 60) {

            var allowDeptGroupIds = new HashSet<Long>();

            //提取哪些角色上面配了指定组织RS
            for (var g : groups) {
                if (g.getRowScope() == 60) {
                    allowDeptGroupIds.add(g.getId());
                }
            }

            //获取这些组关联的部门IDS TODO:这里会优化重构、因为企业+部门都属于组织IDS 不应该获取部门IDS
            var orgIds = gdRepository.getDeptIdsByGroupIds(new ArrayList<>(allowDeptGroupIds));
            allowOrgIds.addAll(orgIds);
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为50 只能访问本人数据 允许访问的企业、部门IDS为空
        if (rsMax == 50) {
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS < 50以下的值至少需要有公司或者部门 如果用户既没有公司 也没有部门 不再计算RS允许组织IDS 直接返回空集合
        if (user.getOrgId() == null && user.getDeptId() == null) {
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为40 只能访问本部门数据 直接把用户当前的部门ID加入到允许访问的部门IDS中
        if (rsMax == 40) {

            //如果用户不挂部门，不计算RS权限 直接返回空集合
            if (user.getDeptId() == null) {
                return RsCalculated.of(rsMax, allowOrgIds);
            }

            //把用户所属部门ID加入到允许访问的组织IDS中
            allowOrgIds.add(user.getDeptId());
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为30 可以访问本部门+下级部门(直接把用户所属部门+子部门都添加进允许访问的部门IDS中)
        if (rsMax == 30) {

            //如果用户不挂部门，不计算RS权限 直接返回空集合
            if (user.getDeptId() == null) {
                return RsCalculated.of(rsMax, allowOrgIds);
            }

            //获取用户所属部门+子部门
            var orgs = coRepository.getChildByOrgId(user.getDeptId());
            for (var org : orgs) {
                allowOrgIds.add(org.getId());
            }
            //把用户当前的部门ID也加进去
            allowOrgIds.add(user.getDeptId());
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为20 仅本公司以及下属部门的数据，不能访问下级子公司以及部门的数据
        if (rsMax == 20) {

            //获取用户的公司ID
            var orgId = user.getOrgId();

            //如果用户不挂公司，不计算RS权限 直接返回空集合
            if (orgId == null) {
                return RsCalculated.of(rsMax, allowOrgIds);
            }

            //查询用户所属公司下面的直属部门(排除子公司以及子公司下面的部门)
            var orgs = coRepository.getRowScope20OrgScopeListByOrgId(orgId);

            for (var org : orgs) {
                allowOrgIds.add(org.getId());
            }

            //把用户所属公司ID也加进去
            allowOrgIds.add(orgId);
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为10 可以访问本公司下面所有子公司以及部门的数据
        if (rsMax == 10) {

            //获取用户的公司ID
            var orgId = user.getOrgId();

            //如果用户不挂公司，不计算RS权限 直接返回空集合
            if (orgId == null) {
                return RsCalculated.of(rsMax, allowOrgIds);
            }

            //获取用户所属公司下面的全部子组织+全部部门
            var orgs = coRepository.getChildByOrgId(orgId);
            for (var org : orgs) {
                allowOrgIds.add(org.getId());
            }

            //把用户所属公司ID也加进去
            allowOrgIds.add(orgId);
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        //RS等级为0 全集团数据 允许访问所有企业以及部门的数据
        if (rsMax == 0) {
            return RsCalculated.of(rsMax, allowOrgIds);
        }

        return RsCalculated.of(rsMax, allowOrgIds);
    }


}

