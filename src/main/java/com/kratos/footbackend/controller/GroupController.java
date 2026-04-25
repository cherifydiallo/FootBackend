package com.kratos.footbackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratos.footbackend.dto.AddUserToGroupDto;
import com.kratos.footbackend.dto.CreateGroupDto;
import com.kratos.footbackend.dto.GroupPermissionDto;
import com.kratos.footbackend.model.Group;
import com.kratos.footbackend.model.RestPermissionConfig;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.service.GroupPermissionService;
import com.kratos.footbackend.service.GroupService;

@RequestMapping("/groups")
@RestController
public class GroupController {
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;
    private final GroupPermissionService groupPermissionService;

    @Autowired
    public GroupController(GroupService groupService, GroupPermissionService groupPermissionService) {
        this.groupService = groupService;
        this.groupPermissionService = groupPermissionService;
    }

    @PostMapping
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'group_create')")
    public ResponseEntity<Map<String, Object>> createGroup(@RequestBody CreateGroupDto createGroupDto) {
        try {
            if (createGroupDto.getName() == null || createGroupDto.getName().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Le nom du groupe est requis");
                return ResponseEntity.badRequest().body(response);
            }

            Group group = groupService.createGroup(createGroupDto);
            logger.info("Group created: {}", group.getName());

            Map<String, Object> groupInfo = buildGroupInfo(group);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Groupe créé avec succès");
            response.put("group", groupInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.warn("Group creation failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Group creation error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    @PreAuthorize("@restPermissionEvaluator.canExecute(authentication, 'group_view')")
    public ResponseEntity<Map<String, Object>> getAllGroups() {
        try {
            List<Group> groups = groupService.getAllGroups();
            List<Map<String, Object>> groupsList = new ArrayList<>();
            
            for (Group group : groups) {
                groupsList.add(buildGroupInfo(group));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", groupsList.size());
            response.put("groups", groupsList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching groups: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_view_group', #id)")
    public ResponseEntity<Map<String, Object>> getGroupById(@PathVariable Long id) {
        try {
            Optional<Group> groupOpt = groupService.getGroupById(id);
            if (groupOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Groupe non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> groupInfo = buildGroupInfo(groupOpt.get());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("group", groupInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching group: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_update_group', #id)")
    public ResponseEntity<Map<String, Object>> updateGroup(@PathVariable Long id, @RequestBody CreateGroupDto updateGroupDto) {
        try {
            if (updateGroupDto.getName() == null || updateGroupDto.getName().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Le nom du groupe est requis");
                return ResponseEntity.badRequest().body(response);
            }

            Group group = groupService.updateGroup(id, updateGroupDto);
            Map<String, Object> groupInfo = buildGroupInfo(group);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Groupe mis à jour avec succès");
            response.put("group", groupInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Group update failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Group update error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_delete_group', #id)")
    public ResponseEntity<Map<String, Object>> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Groupe supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Group deletion failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Group deletion error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/add-user")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_add_user', #addUserToGroupDto.groupId)")
    public ResponseEntity<Map<String, Object>> addUserToGroup(@RequestBody AddUserToGroupDto addUserToGroupDto) {
        try {
            if (addUserToGroupDto.getUserId() == null || addUserToGroupDto.getGroupId() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "userId et groupId sont requis");
                return ResponseEntity.badRequest().body(response);
            }

            Group group = groupService.addUserToGroup(addUserToGroupDto.getGroupId(), addUserToGroupDto.getUserId());
            Map<String, Object> groupInfo = buildGroupInfo(group);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur ajouté au groupe avec succès");
            response.put("group", groupInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Add user to group failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Add user to group error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/remove-user")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_remove_user', #removeUserFromGroupDto.groupId)")
    public ResponseEntity<Map<String, Object>> removeUserFromGroup(@RequestBody AddUserToGroupDto removeUserFromGroupDto) {
        try {
            if (removeUserFromGroupDto.getUserId() == null || removeUserFromGroupDto.getGroupId() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "userId et groupId sont requis");
                return ResponseEntity.badRequest().body(response);
            }

            Group group = groupService.removeUserFromGroup(removeUserFromGroupDto.getGroupId(), removeUserFromGroupDto.getUserId());
            Map<String, Object> groupInfo = buildGroupInfo(group);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur retiré du groupe avec succès");
            response.put("group", groupInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Remove user from group failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Remove user from group error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_view_members', #id)")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable Long id) {
        try {
            List<User> members = groupService.getGroupMembers(id);
            List<Map<String, Object>> membersList = new ArrayList<>();

            for (User user : members) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("identifiant", user.getIdentifiant());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole() != null ? user.getRole().name() : null);
                userInfo.put("status", user.getStatus());
                membersList.add(userInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", membersList.size());
            response.put("members", membersList);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Get group members failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Get group members error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_permission_view', #id)")
    public ResponseEntity<Map<String, Object>> getGroupPermissions(@PathVariable Long id) {
        try {
            List<RestPermissionConfig> permissions = groupPermissionService.getGroupPermissions(id);
            List<Map<String, Object>> list = new ArrayList<>();

            for (RestPermissionConfig permission : permissions) {
                Map<String, Object> permissionInfo = new HashMap<>();
                permissionInfo.put("id", permission.getId());
                permissionInfo.put("permissionKey", permission.getPermissionKey());
                permissionInfo.put("enabled", permission.isEnabled());
                permissionInfo.put("description", permission.getDescription());
                list.add(permissionInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", list.size());
            response.put("permissions", list);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Get group permissions failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Get group permissions error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_permission_manage', #id)")
    public ResponseEntity<Map<String, Object>> upsertGroupPermission(@PathVariable Long id, @RequestBody GroupPermissionDto dto) {
        try {
            RestPermissionConfig permission = groupPermissionService.upsertPermission(id, dto);

            Map<String, Object> permissionInfo = new HashMap<>();
            permissionInfo.put("id", permission.getId());
            permissionInfo.put("permissionKey", permission.getPermissionKey());
            permissionInfo.put("enabled", permission.isEnabled());
            permissionInfo.put("description", permission.getDescription());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission configurée avec succès");
            response.put("permission", permissionInfo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Upsert group permission failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Upsert group permission error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}/permissions/{permissionKey}")
    @PreAuthorize("@restPermissionEvaluator.canExecuteForGroup(authentication, 'group_permission_manage', #id)")
    public ResponseEntity<Map<String, Object>> deleteGroupPermission(
        @PathVariable Long id,
        @PathVariable String permissionKey
    ) {
        try {
            groupPermissionService.removePermission(id, permissionKey);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Delete group permission failed: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Delete group permission error: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Map<String, Object> buildGroupInfo(Group group) {
        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("id", group.getId());
        groupInfo.put("name", group.getName());
        groupInfo.put("description", group.getDescription());
        groupInfo.put("createdDate", group.getCreatedDate());
        groupInfo.put("updatedDate", group.getUpdatedDate());
        groupInfo.put("memberCount", group.getMembers().size());
        return groupInfo;
    }
}
