package com.campus.library.service;

import com.campus.library.common.BusinessException;
import com.campus.library.entity.Category;
import com.campus.library.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> list() {
        return categoryMapper.findAll();
    }

    public Long create(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        categoryMapper.insert(category);
        return category.getId();
    }

    public void update(Category category) {
        if (categoryMapper.findById(category.getId()) == null) {
            throw new BusinessException("分类不存在");
        }
        categoryMapper.update(category);
    }

    public void delete(Long id) {
        if (categoryMapper.findById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        if (categoryMapper.countBooks(id) > 0) {
            throw new BusinessException("该分类下仍有图书，不能删除");
        }
        categoryMapper.delete(id);
    }
}
