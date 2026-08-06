package com.iTech.education.specification;

import com.iTech.education.entity.Category;
import jakarta.persistence.criteria.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CategorySpecificationImpl implements Specification<Category> {
    private final String keyword;
    private final String description;
    public CategorySpecificationImpl(String keyword,String description  ) {
        this.keyword = keyword;
        this.description = description;
    }


    @Override
    public @Nullable Predicate toPredicate(Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();


        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Expression<String> name = criteriaBuilder.lower(root.get("name"));
            predicates.add(criteriaBuilder.like(name, pattern));
        }
        if (description != null && !description.isBlank()) {
            String pattern = "%" + description.toLowerCase() + "%";
            Expression<String> descriptionExpr = criteriaBuilder.lower(root.get("description"));
            predicates.add(criteriaBuilder.like(descriptionExpr, pattern));
        }
     //criteriaBuilder.and(
        //    [p1, p2]
        //);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
