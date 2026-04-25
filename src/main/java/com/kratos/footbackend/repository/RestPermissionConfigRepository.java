package com.kratos.footbackend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kratos.footbackend.model.RestPermissionConfig;

public interface RestPermissionConfigRepository extends JpaRepository<RestPermissionConfig, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END
        FROM RestPermissionConfig rpc
        WHERE rpc.enabled = true
          AND rpc.permissionKey = :permissionKey
          AND rpc.group.id IN :groupIds
    """)
    boolean existsPermissionInGroups(String permissionKey, Collection<Long> groupIds);

    @Query("""
        SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END
        FROM RestPermissionConfig rpc
        WHERE rpc.enabled = true
          AND rpc.permissionKey IN :permissionKeys
          AND rpc.group.id IN :groupIds
    """)
    boolean existsAnyPermissionInGroups(Collection<String> permissionKeys, Collection<Long> groupIds);

    @Query("""
        SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END
        FROM RestPermissionConfig rpc
        WHERE rpc.enabled = true
          AND rpc.permissionKey = :permissionKey
          AND rpc.group.id = :groupId
    """)
    boolean existsPermissionInGroup(String permissionKey, Long groupId);

    @Query("""
        SELECT CASE WHEN COUNT(rpc) > 0 THEN true ELSE false END
        FROM RestPermissionConfig rpc
        WHERE rpc.enabled = true
          AND rpc.permissionKey IN :permissionKeys
          AND rpc.group.id = :groupId
    """)
    boolean existsAnyPermissionInGroup(Collection<String> permissionKeys, Long groupId);

    boolean existsByPermissionKeyAndGroupId(String permissionKey, Long groupId);

    Optional<RestPermissionConfig> findByPermissionKeyAndGroupId(String permissionKey, Long groupId);

    List<RestPermissionConfig> findByGroupIdOrderByPermissionKeyAsc(Long groupId);

    long deleteByPermissionKeyAndGroupId(String permissionKey, Long groupId);
}
