package com.onlinecourse.platform.course;

import com.onlinecourse.platform.course.dto.CategoryRequest;
import com.onlinecourse.platform.course.dto.CategoryResponse;
import com.onlinecourse.platform.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessException("Category with slug '" + request.slug() + "' already exists");
        }
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException("Category with name '" + request.name() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug());
    }
}
