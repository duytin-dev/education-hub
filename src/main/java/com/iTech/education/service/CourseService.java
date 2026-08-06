package com.iTech.education.service;

import com.iTech.education.dto.request.CourseRequest;
import com.iTech.education.dto.response.CourseResponse;
import com.iTech.education.utils.CourseStatus;
import com.iTech.education.utils.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CourseService {
    CourseResponse create(CourseRequest request, String currentUserEmail);

    CourseResponse update(Long id, CourseRequest request, String currentUserEmail);

    void delete(Long id, String currentUserEmail);

    CourseResponse getById(Long id);

    Page<CourseResponse> search(
            String keyword,
            Long categoryId,
            Level level,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseStatus status,
            Pageable pageable
    );

    CourseResponse changeStatus(Long id, CourseStatus newStatus, String currentUserEmail);
}
