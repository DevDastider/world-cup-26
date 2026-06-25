package org.sgd.worldcup.mapper;

import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.GroupDTO;
import org.sgd.worldcup.entity.Group;

@Component
public class GroupMapper {
    public GroupDTO toDTO(Group group) {
        if (group == null) {
            return null;
        }
        return GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
//                .stage(group.getStage())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    public Group toEntity(GroupDTO groupDTO) {
        if (groupDTO == null) {
            return null;
        }
        return Group.builder()
                .id(groupDTO.getId())
                .name(groupDTO.getName())
//                .stage(groupDTO.getStage())
                .build();
    }
}

