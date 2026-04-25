package com.kratos.footbackend.service;

import com.kratos.footbackend.dto.CreateGroupDto;
import com.kratos.footbackend.model.Group;
import com.kratos.footbackend.model.User;
import com.kratos.footbackend.repository.GroupRepository;
import com.kratos.footbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group createGroup(CreateGroupDto createGroupDto) {
        if (groupRepository.existsByName(createGroupDto.getName())) {
            throw new RuntimeException("Un groupe avec ce nom existe déjà");
        }

        Group group = new Group();
        group.setName(createGroupDto.getName());
        group.setDescription(createGroupDto.getDescription());
        group.setCreatedDate(LocalDateTime.now());
        group.setUpdatedDate(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);
        logger.info("Group created: {}", savedGroup.getName());
        return savedGroup;
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    public Optional<Group> getGroupByName(String name) {
        return groupRepository.findByName(name);
    }

    @Transactional
    public Group updateGroup(Long id, CreateGroupDto updateGroupDto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        if (!group.getName().equals(updateGroupDto.getName()) &&
                groupRepository.existsByName(updateGroupDto.getName())) {
            throw new RuntimeException("Un groupe avec ce nom existe déjà");
        }

        group.setName(updateGroupDto.getName());
        group.setDescription(updateGroupDto.getDescription());
        group.setUpdatedDate(LocalDateTime.now());

        Group updatedGroup = groupRepository.save(group);
        logger.info("Group updated: {}", updatedGroup.getName());
        return updatedGroup;
    }

    @Transactional
    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        // Remove all user associations
        for (User user : group.getMembers()) {
            user.getGroups().remove(group);
        }
        group.getMembers().clear();

        groupRepository.delete(group);
        logger.info("Group deleted: {}", group.getName());
    }

    @Transactional
    public Group addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getGroups().contains(group)) {
            throw new RuntimeException("L'utilisateur est déjà membre de ce groupe");
        }

        user.getGroups().add(group);
        group.getMembers().add(user);

        userRepository.save(user);
        groupRepository.save(group);

        logger.info("User {} added to group {}", user.getIdentifiant(), group.getName());
        return group;
    }

    @Transactional
    public Group removeUserFromGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getGroups().contains(group)) {
            throw new RuntimeException("L'utilisateur n'est pas membre de ce groupe");
        }

        user.getGroups().remove(group);
        group.getMembers().remove(user);

        userRepository.save(user);
        groupRepository.save(group);

        logger.info("User {} removed from group {}", user.getIdentifiant(), group.getName());
        return group;
    }

    public List<User> getGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));
        return group.getMembers().stream().toList();
    }
}
