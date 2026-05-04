package com.ksptool.bio.biz.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;



@Getter
@Setter
@IdClass(GroupPermissionPo.Pk.class)
@Entity
@Table(name = "auth_group_permission", comment = "权限组关联表")
@AllArgsConstructor
@NoArgsConstructor
public class GroupPermissionPo {

    @Id
    @Column(name = "group_id", nullable = false, comment = "用户组ID")
    private Long groupId;

    @Id
    @Column(name = "permission_id", nullable = false, comment = "权限ID")
    private Long permissionId;

    /**
     * 用于复合主键的类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long groupId;
        private Long permissionId;
    }

}
