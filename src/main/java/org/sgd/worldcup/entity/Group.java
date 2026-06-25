package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sgd.worldcup.enums.StageType;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "`groups`", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group name is required")
    @Size(min = 1, max = 50, message = "Group name must be between 1 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

//    @NotNull(message = "Stage type is required")
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 50)
//    private StageType stage;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<GroupTeam> groupTeams;

    @OneToMany(mappedBy = "group")
    private Set<Match> matches;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

