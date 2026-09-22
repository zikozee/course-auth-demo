package com.zee.courseauthdemo.usermanagement.entity;

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
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long roleId;

    @Column(name="perm_id", nullable = false)
    private long permissionId;
}
