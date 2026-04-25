package com.kratos.footbackend.controller;

import com.kratos.footbackend.dto.AddUserToGroupDto;
import com.kratos.footbackend.dto.CreateGroupDto;
import com.kratos.footbackend.model.Group;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.service.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/groups")
@RestController
public class GroupController {
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
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
