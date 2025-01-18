package ru.students.forumservicediplomproject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "User_Table")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "user_id")
    private long userId;
    @NotNull
    private String userName;
    @NotNull
    private String password;
    @NotNull
    private String email;
    @ManyToMany
    @JoinTable(name = "user_table_roles",
    joinColumns = {@JoinColumn(name = "user_user_id", referencedColumnName = "user_id")},
    inverseJoinColumns = {@JoinColumn(name = "roles_role_id", referencedColumnName = "role_id")})
    @NotNull
    private List<Role> roles;
    @NotNull
    private Timestamp registrationDate;
}
