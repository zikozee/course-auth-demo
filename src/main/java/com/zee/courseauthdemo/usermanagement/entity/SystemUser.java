package com.zee.courseauthdemo.usermanagement.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @dev : Ezekiel Eromosei
 * @date : 22 Sep, 2026
 */

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class SystemUser {

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

    private String email;

    @Column(length = 150, nullable = false)
    private String fullName;

}
