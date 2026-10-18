package com.campus.library.controller;

import com.campus.library.common.BusinessException;
import com.campus.library.common.Result;
import com.campus.library.entity.Category;
import com.campus.library.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.ok(categoryService.list());
    }

    @PostMapping
    public Result<Long> create(@RequestBody Category category, HttpServletRequest http) {
        checkAdmin(http);
        return Result.ok(categoryService.create(category));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category, HttpServletRequest http) {
        checkAdmin(http);
        category.setId(id);
        categoryService.update(category);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        checkAdmin(http);
        categoryService.delete(id);
        return Result.ok();
    }

    private void checkAdmin(HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
