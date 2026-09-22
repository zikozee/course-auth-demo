package com.zee.courseauthdemo.usermanagement.entity;

import com.zee.courseauthdemo.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author : Ezekiel Eromosei
 * @code @created : 06 Apr, 2025
 */

@Entity
@Table
@Getter
@Setter
@ToString
public class Role extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String roleName;

    @Column(length = 100)
    private String description;
}
