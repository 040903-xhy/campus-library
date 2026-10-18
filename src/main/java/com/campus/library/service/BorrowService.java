package com.campus.library.service;

import com.campus.library.common.BusinessException;
import com.campus.library.common.PageResult;
import com.campus.library.entity.Book;
import com.campus.library.entity.BorrowRecord;
import com.campus.library.mapper.BookMapper;
import com.campus.library.mapper.BorrowRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BorrowService {

    /** 每个读者最多同时在借 5 本 */
    private static final int MAX_BORROWING = 5;
    /** 默认借期 30 天 */
    private static final int BORROW_DAYS = 30;

    private final BorrowRecordMapper recordMapper;
    private final BookMapper bookMapper;

    public BorrowService(BorrowRecordMapper recordMapper, BookMapper bookMapper) {
        this.recordMapper = recordMapper;
        this.bookMapper = bookMapper;
    }

    /**
     * 借书。整个方法在一个事务里：
     * 1. 校验图书存在  2. 校验借阅上限  3. 原子减库存  4. 写借阅记录
     * 任何一步失败都整体回滚，不会出现"扣了库存没记录"的脏数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public BorrowRecord borrow(Long userId, Long bookId) {
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (recordMapper.countBorrowing(userId) >= MAX_BORROWING) {
            throw new BusinessException("最多同时借 " + MAX_BORROWING + " 本，请先归还");
        }
        // 原子 SQL：available > 0 才减。返回 0 表示库存不足或并发下被抢完。
        if (bookMapper.decreaseAvailable(bookId) == 0) {
            throw new BusinessException("库存不足，该书已被借完");
        }
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setDueDate(LocalDateTime.now().plusDays(BORROW_DAYS));
        record.setStatus("BORROWED");
        recordMapper.insert(record);
        return recordMapper.findById(record.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long recordId, Long userId, boolean isAdmin) {
        BorrowRecord record = recordMapper.findById(recordId);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (!isAdmin && !record.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能归还自己的借阅记录");
        }
        if (!"BORROWED".equals(record.getStatus())) {
            throw new BusinessException("该记录已归还，请勿重复操作");
        }
        // 超过应还日期记为逾期归还
        String status = record.getDueDate() != null && LocalDateTime.now().isAfter(record.getDueDate())
                ? "OVERDUE" : "RETURNED";
        // 条件更新防并发重复还书；失败则整体回滚
        if (recordMapper.returnBook(recordId, status) == 0) {
            throw new BusinessException("归还失败，请刷新后重试");
        }
        bookMapper.increaseAvailable(record.getBookId());
    }

    public PageResult<BorrowRecord> myRecords(Long userId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        long total = recordMapper.countByUser(userId);
        return new PageResult<>(recordMapper.pageByUser(userId, (page - 1) * size, size), total, page, size);
    }

    public PageResult<BorrowRecord> allRecords(String status, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        long total = recordMapper.countAll(status);
        return new PageResult<>(recordMapper.pageAll(status, (page - 1) * size, size), total, page, size);
    }

    public int countOverdue() {
        return recordMapper.countOverdue();
    }
}
