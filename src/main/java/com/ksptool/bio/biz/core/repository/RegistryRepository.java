package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.registry.RegistryPo;
import com.ksptool.bio.biz.core.model.registry.dto.GetRegistryListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistryRepository extends JpaRepository<RegistryPo, Long> {


    /**
     * 获取注册表全部节点
     *
     * @return 注册表全部节点
     */
    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.kind = 0
            ORDER BY u.seq ASC,u.nkey ASC
            """)
    List<RegistryPo> getRegistryAllNodes();


    /**
     * 根据父级ID统计注册表数量
     *
     * @param parentId 父级ID
     * @return 注册表数量
     */
    @Query("""
            SELECT COUNT(u) FROM RegistryPo u
            WHERE u.parentId = :parentId
            """)
    int countByParentId(@Param("parentId") Long parentId);


    /**
     * 根据KEY的全路径统计注册表数量
     *
     * @param keyPath KEY的全路径
     * @return 注册表数量
     */
    @Query("""
            SELECT COUNT(u) FROM RegistryPo u
            WHERE u.keyPath = :keyPath
            """)
    int countByKeyPath(@Param("keyPath") String keyPath);


    /**
     * 根据KEY的全路径查询注册表节点
     *
     * @param keyPath KEY的全路径
     * @return 注册表节点
     */
    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.keyPath = :keyPath AND u.kind = 0
            """)
    RegistryPo getRegistryNodeByKeyPath(@Param("keyPath") String keyPath);

    /**
     * 根据KEY的全路径查询注册表条目
     *
     * @param keyPath KEY的全路径
     * @return 注册表条目
     */
    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.keyPath = :keyPath AND u.kind = 1
            """)
    RegistryPo getRegistryEntryByKeyPath(@Param("keyPath") String keyPath);


    /**
     * 根据KEY的全路径统计注册表条目数量
     *
     * @param keyPath KEY的全路径
     * @param kind    类型 0:节点 1:条目
     * @return 注册表条目数量
     */
    @Query("""
            SELECT COUNT(u) FROM RegistryPo u
            WHERE u.keyPath = :keyPath AND u.kind = :kind
            """)
    Integer countByKeyPathAndKind(@Param("keyPath") String keyPath, @Param("kind") Integer kind);


    /**
     * 根据父级ID查询注册表条目列表
     *
     * @param parentId 父级ID
     * @return 注册表条目列表
     */
    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.parentId = :parentId AND u.kind = 1
            AND (:#{#dto.nkey} IS NULL OR u.nkey  LIKE CONCAT('%', :#{#dto.nkey}, '%') )
            AND (:#{#dto.label} IS NULL OR u.label  LIKE CONCAT('%', :#{#dto.label}, '%') )
            AND (:#{#dto.nvalueKind} IS NULL OR u.nvalueKind  = :#{#dto.nvalueKind} )
            ORDER BY u.seq ASC
            """)
    Page<RegistryPo> getRegistryEntryList(@Param("parentId") Long parentId, @Param("dto") GetRegistryListDto dto, Pageable pageable);

    /**
     * 根据父级ID查询注册表条目列表(不分页)
     *
     * @param parentId 父级ID
     * @return 注册表条目列表
     */
    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.parentId = :parentId AND u.kind = 1
            AND (:#{#dto.nkey} IS NULL OR u.nkey  LIKE CONCAT('%', :#{#dto.nkey}, '%') )
            AND (:#{#dto.label} IS NULL OR u.label  LIKE CONCAT('%', :#{#dto.label}, '%') )
            AND (:#{#dto.nvalueKind} IS NULL OR u.nvalueKind  = :#{#dto.nvalueKind} )
            ORDER BY u.seq ASC
            """)
    List<RegistryPo> getRegistryEntryListNotPage(@Param("parentId") Long parentId, @Param("dto") GetRegistryListDto dto);

    @Query("""
            SELECT u FROM RegistryPo u
            WHERE u.keyPath = :keyPath AND u.kind = 1
            """)
    RegistryPo getRegistryByKeyPath(@Param("keyPath") String keyPath);
}
