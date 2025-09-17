package com.project.DuAnTotNghiep.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter

@Entity
@Table(name = "product_discount")
public class ProductDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Nationalized
    private String name;
    private Double discountedAmount;
    private Date startDate;
    private Date endDate;
    private boolean closed;

    @OneToOne
    @JoinColumn(name = "product_detail_id")
    private ProductDetail productDetail;
}
