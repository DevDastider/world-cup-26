package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.GroupDTO;
import org.sgd.worldcup.entity.Group;
import org.sgd.worldcup.enums.StageType;
import org.sgd.worldcup.exception.DuplicateResourceException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.GroupMapper;
import org.sgd.worldcup.repository.GroupRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMapper groupMapper;

    public GroupDTO createGroup(GroupDTO groupDTO) {
        log.info("Creating new group: {}", groupDTO.getName());

        if (groupRepository.findByName(groupDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Group with name '" + groupDTO.getName() + "' already exists");
        }

        Group group = groupMapper.toEntity(groupDTO);
        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with ID: {}", savedGroup.getId());
        return groupMapper.toDTO(savedGroup);
    }

    public GroupDTO getGroupById(Long id) {
        log.info("Fetching group with ID: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
        return groupMapper.toDTO(group);
    }

    public GroupDTO getGroupByName(String name) {
        log.info("Fetching group with name: {}", name);
        Group group = groupRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with name: " + name));
        return groupMapper.toDTO(group);
    }

    public List<GroupDTO> getAllGroups() {
        log.info("Fetching all groups");
        return groupRepository.findAll().stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

//    public List<GroupDTO> getGroupsByStage(StageType stage) {
//        log.info("Fetching groups with stage: {}", stage);
//        return groupRepository.findByStage(stage).stream()
//                .map(groupMapper::toDTO)
//                .collect(Collectors.toList());
//    }

    public GroupDTO updateGroup(Long id, GroupDTO groupDTO) {
        log.info("Updating group with ID: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));

        if (!group.getName().equals(groupDTO.getName()) &&
                groupRepository.existsByName(groupDTO.getName())) {
            throw new DuplicateResourceException("Group with name '" + groupDTO.getName() + "' already exists");
        }

        group.setName(groupDTO.getName());
//        group.setStage(groupDTO.getStage());

        Group updatedGroup = groupRepository.save(group);
        log.info("Group updated successfully with ID: {}", id);
        return groupMapper.toDTO(updatedGroup);
    }

    public void deleteGroup(Long id) {
        log.info("Deleting group with ID: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + id));
        groupRepository.delete(group);
        log.info("Group deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return groupRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return groupRepository.existsByName(name);
    }
}

