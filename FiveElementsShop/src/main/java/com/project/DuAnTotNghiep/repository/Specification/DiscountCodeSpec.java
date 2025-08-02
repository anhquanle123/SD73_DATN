package com.project.DuAnTotNghiep.repository.Specification;

import com.project.DuAnTotNghiep.dto.DiscountCode.SearchDiscountCodeDto;
import com.project.DuAnTotNghiep.dto.Product.SearchProductDto;
import com.project.DuAnTotNghiep.entity.DiscountCode;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscountCodeSpec implements Specification<DiscountCode> {
    private SearchDiscountCodeDto searchDiscountCodeDto;

    public DiscountCodeSpec(SearchDiscountCodeDto searchRequest) {
        this.searchDiscountCodeDto = searchRequest;
    }

    @Override
    public Predicate toPredicate(Root<DiscountCode> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (searchDiscountCodeDto.getKeyword() != null) {
            String keyword = searchDiscountCodeDto.getKeyword();

            Predicate productNamePredicate = criteriaBuilder.like(root.get("code"), "%" + keyword + "%");
            Predicate productCodePredicate = criteriaBuilder.like(root.get("detail"), "%" + keyword + "%");
            Predicate combinedPredicate = criteriaBuilder.or(productNamePredicate, productCodePredicate);

            predicates.add(combinedPredicate);
        }
        if(searchDiscountCodeDto.getCode() != null) {
            Predicate predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), "%" + searchDiscountCodeDto.getCode() + "%" );
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getDetail() != null) {
            Predicate predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("detail")), "%" + searchDiscountCodeDto.getDetail() + "%" );
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getStartDate() != null) {
            Predicate predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"),  searchDiscountCodeDto.getStartDate());
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getEndDate() != null) {
            Predicate predicate = criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), searchDiscountCodeDto.getEndDate() );
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getDiscountCodeType() != null) {
            Predicate predicate = criteriaBuilder.equal(root.get("type"), searchDiscountCodeDto.getDiscountCodeType());
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getMaximumUsage() != null) {
            Predicate predicate = criteriaBuilder.equal(root.get("maximumUsage"), searchDiscountCodeDto.getMaximumUsage() );
            predicates.add(predicate);
        }

        if(searchDiscountCodeDto.getStatus() != null) {
            Integer status = searchDiscountCodeDto.getStatus();
            LocalDateTime now = LocalDateTime.now();
            Date currentDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

            switch (status) {
                case 0: // Ngừng kích hoạt
                    predicates.add(criteriaBuilder.equal(root.get("status"), 0));
                    break;
                case 1: // Đang kích hoạt: status = 1 và ngày bắt đầu <= now <= ngày kết thúc
                    predicates.add(criteriaBuilder.equal(root.get("status"), 1));
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), currentDate));
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), currentDate));
                    break;
                case 2: // Sắp đến: status = 1 nhưng ngày bắt đầu > now
                    predicates.add(criteriaBuilder.equal(root.get("status"), 1));
                    predicates.add(criteriaBuilder.greaterThan(root.get("startDate"), currentDate));
                    break;
            }
        }

//        predicates.add(criteriaBuilder.equal(root.get("status"), 1));
        predicates.add(criteriaBuilder.equal(root.get("deleteFlag"), false));
        return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
