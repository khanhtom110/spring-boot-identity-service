package com.khanheii.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Role {
    @Id//De la khoa chinh
    String name; //Ten role
    String description; //mieu ta role
    @ManyToMany//1 role co the co nh permission - 1 permission co the thuoc ve nhieu role
    Set<Permission> permissions; //Set kieu Permission
}
