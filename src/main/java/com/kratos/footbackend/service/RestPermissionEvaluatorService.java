package com.kratos.footbackend.service;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratos.footbackend.model.Role;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.repository.RestPermissionConfigRepository;
import com.kratos.footbackend.repository.UserRepository;

@Service("restPermissionEvaluator")
public class RestPermissionEvaluatorService {

    private final UserRepository userRepository;
    private final RestPermissionConfigRepository restPermissionConfigRepository;

    public RestPermissionEvaluatorService(
        UserRepository userRepository,
        RestPermissionConfigRepository restPermissionConfigRepository
    ) {
        this.userRepository = userRepository;
        this.restPermissionConfigRepository = restPermissionConfigRepository;
    }

    @Transactional(readOnly = true)
    public boolean canExecute(Authentication authentication, String permissionKey) {
        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (user.getRole() == Role.admin) {
            return true;
        }

        Set<Long> groupIds = extractGroupIds(user);
        if (groupIds.isEmpty()) {
            return false;
        }

        Set<String> candidateKeys = resolveCandidateKeys(permissionKey);
        return restPermissionConfigRepository.existsAnyPermissionInGroups(candidateKeys, groupIds);
    }

    @Transactional(readOnly = true)
    public boolean canExecuteForGroup(Authentication authentication, String permissionKey, Long groupId) {
        if (groupId == null) {
            return false;
        }

        Optional<User> userOpt = resolveUser(authentication);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (user.getRole() == Role.admin) {
            return true;
        }

        Set<Long> userGroupIds = extractGroupIds(user);
        if (!userGroupIds.contains(groupId)) {
            return false;
        }

        Set<String> candidateKeys = resolveCandidateKeys(permissionKey);
        return restPermissionConfigRepository.existsAnyPermissionInGroup(candidateKeys, groupId);
    }

    private Optional<User> resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String identifiant = authentication.getName();
        if (identifiant == null || identifiant.isBlank() || "anonymousUser".equals(identifiant)) {
            return Optional.empty();
        }

        return userRepository.findOneByIdentifiantWithGroups(identifiant);
    }

    private Set<Long> extractGroupIds(User user) {
        return user.getGroups()
            .stream()
            .map(g -> g.getId())
            .filter(id -> id != null)
            .collect(Collectors.toSet());
    }

    private Set<String> resolveCandidateKeys(String permissionKey) {
        String normalized = permissionKey == null ? "" : permissionKey.trim().toLowerCase();
        Set<String> keys = new LinkedHashSet<>();
        keys.add(normalized);

        if (normalized.endsWith("_read")) {
            String base = normalized.substring(0, normalized.length() - "_read".length());
            keys.add(base + "_write");
            keys.add(base + "_edit");
        } else if (normalized.endsWith("_write")) {
            String base = normalized.substring(0, normalized.length() - "_write".length());
            keys.add(base + "_edit");
        }

        return keys;
    }

}
