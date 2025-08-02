package com.project.DuAnTotNghiep.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "Customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Nationalized
    private String name;
    private String phoneNumber;
    private String email;
    private Date birthDay;
    private Boolean gender;
    private String cccd;
    private String tinhThanh;
    private String quanHuyen;
    private String xaPhuong;
    private String soNha;
    private Boolean isGuest;
    private Integer status;
    private String address;


    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressShipping> addressShippings;

    @OneToOne(cascade = CascadeType.ALL,
            mappedBy = "customer",
            fetch = FetchType.LAZY,
            optional = true)
    private Account account;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bill> bills = new ArrayList<>();



}