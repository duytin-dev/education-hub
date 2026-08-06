package com.iTech.education.service;

import com.iTech.education.dto.request.CategoryRequest;
import com.iTech.education.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
    CategoryResponse getById(Long id);
    Page<CategoryResponse> getAll(String keyword,String description, Pageable pageable);
}
