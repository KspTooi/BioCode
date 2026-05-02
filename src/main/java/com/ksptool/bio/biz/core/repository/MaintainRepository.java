package com.ksptool.bio.biz.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ksptool.bio.biz.core.model.root.CoreRootPo;
import com.ksptool.bio.biz.core.model.user.UserPo;

public interface MaintainRepository extends JpaRepository<CoreRootPo, Long> {


    /**
     * 创建系统超级租户(超级租户是ID为0的系统内置租户)
     *
     * @return 创建的超级租户ID
     */
    @Query("""
            INSERT INTO CoreRootPo (id, name, status, adminUserId, createTime, creatorId, updateTime, updaterId) VALUES (:id, '超级租户', 0, 0, NOW(), 0, NOW(), 0)
            """)
    @Modifying
    int createDefaultRoot(@Param("id") Long id);


    /**
     * 创建系统超级用户(超级用户是代码为"admin"的系统内置用户 且租户ID必须为0)
     *
     * @param password 加密后的密码
     * @return 受影响的行数
     */
    @Query("""
            INSERT INTO UserPo (
                id, rootId, username, password, nickname, gender,
                loginCount, status, isSystem, dataVersion,
                createTime, creatorId, updateTime, updaterId
            ) VALUES (
                :id, :rootId, :username, :password, '超级用户', 2,
                0, 0, 1, 0,
                NOW(), -1, NOW(), -1
            )
            """)
    @Modifying
    int createDefaultUser(@Param("id") Long id, @Param("rootId") Long rootId, @Param("username") String username, @Param("password") String password);

    /**
     * 创建系统超级组(超级组是代码为"admin"的系统内置组 且租户ID必须为-1)
     *
     * @return 创建的超级组ID
     */
    @Query("""
            INSERT INTO GroupPo (id, rootId, code, name, remark, status, seq, rowScope, isSystem, createTime, creatorId, updateTime, updaterId) 
            VALUES (:id, :rootId, :code, :name, :remark, 1, 0, 0, 1, NOW(), -1, NOW(), -1)
            """)
    @Modifying
    int createDefaultGroup(@Param("id") Long id, @Param("rootId") Long rootId, @Param("code") String code, @Param("name") String name, @Param("remark") String remark);
}
