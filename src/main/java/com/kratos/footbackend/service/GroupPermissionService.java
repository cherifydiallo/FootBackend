package com.kratos.footbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratos.footbackend.dto.GroupPermissionDto;
import com.kratos.footbackend.model.Group;
import com.kratos.footbackend.model.RestPermissionConfig;
import com.kratos.footbackend.repository.GroupRepository;
import com.kratos.footbackend.repository.RestPermissionConfigRepository;

@Service
public class GroupPermissionService {

    private final GroupRepository groupRepository;
    private final RestPermissionConfigRepository restPermissionConfigRepository;

    public GroupPermissionService(
        GroupRepository groupRepository,
        RestPermissionConfigRepository restPermissionConfigRepository
    ) {
        this.groupRepository = groupRepository;
        this.restPermissionConfigRepository = restPermissionConfigRepository;
    }

    @Transactional(readOnly = true)
    public List<RestPermissionConfig> getGroupPermissions(Long groupId) {
        assertGroupExists(groupId);
        return restPermissionConfigRepository.findByGroupIdOrderByPermissionKeyAsc(groupId);
    }

    @Transactional
    public RestPermissionConfig upsertPermission(Long groupId, GroupPermissionDto dto) {
        if (dto == null || dto.getPermissionKey() == null || dto.getPermissionKey().trim().isEmpty()) {
            throw new RuntimeException("permissionKey est requis");
        }

        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        String normalizedPermissionKey = dto.getPermissionKey().trim().toLowerCase();
        RestPermissionConfig config = restPermissionConfigRepository
            .findByPermissionKeyAndGroupId(normalizedPermissionKey, groupId)
            .orElseGet(RestPermissionConfig::new);

        config.setPermissionKey(normalizedPermissionKey);
        config.setGroup(group);
        config.setEnabled(dto.getEnabled() == null || dto.getEnabled());
        config.setDescription(dto.getDescription());

        RestPermissionConfig saved = restPermissionConfigRepository.save(config);

        if (saved.isEnabled()) {
            grantImpliedPermissions(group, normalizedPermissionKey, dto.getDescription());
        }

        return saved;
    }

    @Transactional
    public void removePermission(Long groupId, String permissionKey) {
        if (permissionKey == null || permissionKey.trim().isEmpty()) {
            throw new RuntimeException("permissionKey est requis");
        }

        assertGroupExists(groupId);
        long deleted = restPermissionConfigRepository.deleteByPermissionKeyAndGroupId(
            permissionKey.trim().toLowerCase(),
            groupId
        );

        if (deleted == 0) {
            throw new RuntimeException("Permission non trouvée pour ce groupe");
        }
    }

    private void assertGroupExists(Long groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new RuntimeException("Groupe non trouvé");
        }
    }

    private void grantImpliedPermissions(Group group, String permissionKey, String description) {
        if (permissionKey.endsWith("_edit")) {
            String base = permissionKey.substring(0, permissionKey.length() - "_edit".length());
            ensurePermissionEnabled(group, base + "_write", description);
            ensurePermissionEnabled(group, base + "_read", description);
            return;
        }

        if (permissionKey.endsWith("_write")) {
            String base = permissionKey.substring(0, permissionKey.length() - "_write".length());
            ensurePermissionEnabled(group, base + "_read", description);
        }
    }

    private void ensurePermissionEnabled(Group group, String permissionKey, String description) {
        RestPermissionConfig implied = restPermissionConfigRepository
            .findByPermissionKeyAndGroupId(permissionKey, group.getId())
            .orElseGet(RestPermissionConfig::new);

        implied.setPermissionKey(permissionKey);
        implied.setGroup(group);
        implied.setEnabled(true);

        if (implied.getDescription() == null || implied.getDescription().isBlank()) {
            implied.setDescription(description);
        }

        restPermissionConfigRepository.save(implied);
    }
}
