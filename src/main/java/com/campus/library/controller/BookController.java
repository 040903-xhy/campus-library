package com.campus.library.controller;

import com.campus.library.common.BusinessException;
import com.campus.library.common.PageResult;
import com.campus.library.common.Result;
import com.campus.library.dto.BookRequest;
import com.campus.library.entity.Book;
import com.campus.library.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** 图书检索（公开）：支持书名/作者/ISBN 模糊 + 分类 + 分页 */
    @GetMapping
    public Result<PageResult<Book>> page(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.ok(bookService.page(keyword, categoryId, page, size));
    }

    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody BookRequest request, HttpServletRequest http) {
        checkAdmin(http);
        return Result.ok(bookService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BookRequest request,
                               HttpServletRequest http) {
        checkAdmin(http);
        bookService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest http) {
        checkAdmin(http);
        bookService.delete(id);
        return Result.ok();
    }

    private void checkAdmin(HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
