package com.zee.courseauthdemo.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @dev : Ezekiel Eromosei
 * @date : 05 Jun, 2026
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SystemUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String hashPassword;

    @Column(length = 50, nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    private boolean enabled;

    private String fullName;

    private String email;

}
