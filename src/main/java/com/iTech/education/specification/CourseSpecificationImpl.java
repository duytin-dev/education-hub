package com.iTech.education.specification;

import com.iTech.education.entity.Course;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import jakarta.persistence.criteria.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseSpecificationImpl  {
//    private final String keyword;
//    private final Level level;
//    private final CourseStatus status;
//    private final BigDecimal minPrice;
//    private final BigDecimal maxPrice;
//
//    public CourseSpecificationImpl(String keyword, Level level, CourseStatus status, BigDecimal minPrice, BigDecimal maxPrice) {
//        this.keyword = keyword;
//        this.level = level;
//        this.status = status;
//        this.minPrice = minPrice;
//        this.maxPrice = maxPrice;
//    }
    public static Specification<Course> filter(
            String keyword,
            Long categoryId,
            Level level,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (level != null) {
                predicates.add(cb.equal(root.get("level"), level));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
//    @Override
//    public @Nullable Predicate toPredicate(Root<Course> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//        List<Predicate> predicates = new ArrayList<>();
//
//        if (keyword != null && !keyword.isBlank()) {
//            String pattern = "%" + keyword.toLowerCase() + "%";
//            Expression<String> name = criteriaBuilder.lower(root.get("name"));
//            predicates.add(criteriaBuilder.like(name, pattern));
//        }
//
//        if (level != null) {
//            Expression<Level> levelExpr = root.get("level");
//            predicates.add(criteriaBuilder.equal(levelExpr, level));
//        }
//
//        if (status != null) {
//            Expression<CourseStatus> statusExpr = root.get("status");
//            predicates.add(criteriaBuilder.equal(statusExpr, status));
//        }
//
//        if (minPrice != null) {
//            Expression<BigDecimal> priceExpr = root.get("price");
//            predicates.add(criteriaBuilder.greaterThanOrEqualTo(priceExpr, minPrice));
//        }
//
//        if (maxPrice != null) {
//            Expression<BigDecimal> priceExpr = root.get("price");
//            predicates.add(criteriaBuilder.lessThanOrEqualTo(priceExpr, maxPrice));
//        }
//
//        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//    }
}
