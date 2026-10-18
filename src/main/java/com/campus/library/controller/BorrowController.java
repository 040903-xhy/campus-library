package com.campus.library.controller;

import com.campus.library.common.BusinessException;
import com.campus.library.common.PageResult;
import com.campus.library.common.Result;
import com.campus.library.entity.BorrowRecord;
import com.campus.library.service.BorrowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /** 借书（登录读者），body 只需要 bookId */
    @PostMapping
    public Result<BorrowRecord> borrow(@RequestParam Long bookId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(borrowService.borrow(userId, bookId));
    }

    /** 还书（本人或管理员） */
    @PutMapping("/{id}/return")
    public Result<Void> returnBook(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        borrowService.returnBook(id, userId, isAdmin);
        return Result.ok();
    }

    /** 我的借阅记录 */
    @GetMapping("/my")
    public Result<PageResult<BorrowRecord>> my(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(borrowService.myRecords(userId, page, size));
    }

    /** 全部借阅记录（管理员） */
    @GetMapping
    public Result<PageResult<BorrowRecord>> all(@RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        checkAdmin(request);
        return Result.ok(borrowService.allRecords(status, page, size));
    }

    /** 逾期未还数量（管理员首页统计） */
    @GetMapping("/overdue-count")
    public Result<Map<String, Integer>> overdueCount(HttpServletRequest request) {
        checkAdmin(request);
        return Result.ok(Map.of("count", borrowService.countOverdue()));
    }

    private void checkAdmin(HttpServletRequest request) {
        if (!"ADMIN".equals(request.getAttribute("role"))) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
