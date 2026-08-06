package com.iTech.education.service.impl;

import com.iTech.education.dto.request.CategoryRequest;
import com.iTech.education.dto.response.CategoryResponse;
import com.iTech.education.entity.Category;
import com.iTech.education.exception.ResourceNotFoundException;
import com.iTech.education.repository.CategoryRepository;
import com.iTech.education.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Danh mục '" + request.getName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + id));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Danh mục '" + request.getName() + "' đã tồn tại");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + id));

        // Lưu ý: nếu category đang có Course liên kết, xóa sẽ lỗi FK constraint
        // Có thể bắt riêng lỗi này ở GlobalExceptionHandler để trả message rõ ràng hơn
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với id: " + id));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public Page<CategoryResponse> getAll(String keyword, Pageable pageable) {
        Page<Category> categoryPage;

        if (StringUtils.hasText(keyword)) {
            categoryPage = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }

        // map Page<Category> -> Page<CategoryResponse>, giữ nguyên thông tin phân trang (total, pageNumber...)
        return categoryPage.map(CategoryResponse::fromEntity);
    }
}
