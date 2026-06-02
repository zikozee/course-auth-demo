package com.zee.courseauthdemo.entity;



import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author : Ezekiel Eromosei
 * @code @created : 01 Jun, 2026
 */

@MappedSuperclass
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    protected Instant createdDate= Instant.now();

    @CreatedBy
    @Column(name = "created_by", length = 50)
    protected String createdBy;

    @Column(name = "updated_date")
    @LastModifiedDate
    protected Instant updatedDate;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    protected String updatedBy;

    @Column(name = "deleted_date")
    protected Instant deletedDate;

    @Column(name = "deleted")
    protected boolean deleted = false;

}
