package com.project.DuAnTotNghiep.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_day")
    private Date birthDay;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "tinh_thanh")
    private String tinhThanh;

    @Column(name = "quan_huyen")
    private String quanHuyen;

    @Column(name = "xa_phuong")
    private String xaPhuong;

    @Column(name = "so_nha")
    private String soNha;

    @Column(name = "status", nullable = false)
    private Integer status;

    @OneToOne(cascade = CascadeType.ALL,
            mappedBy = "employee",
            fetch = FetchType.LAZY,
            optional = true)
    private Account account;
}