package com.campus.library.service;

import com.campus.library.common.BusinessException;
import com.campus.library.common.PageResult;
import com.campus.library.dto.BookRequest;
import com.campus.library.entity.Book;
import com.campus.library.mapper.BookMapper;
import com.campus.library.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;

    public BookService(BookMapper bookMapper, CategoryMapper categoryMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
    }

    public PageResult<Book> page(String keyword, Long categoryId, int page, int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        long total = bookMapper.countQuery(keyword, categoryId);
        return new PageResult<>(bookMapper.pageQuery(keyword, categoryId, (page - 1) * size, size),
                total, page, size);
    }

    public Book detail(Long id) {
        Book book = bookMapper.findById(id);
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        return book;
    }

    public Long create(BookRequest request) {
        if (categoryMapper.findById(request.getCategoryId()) == null) {
            throw new BusinessException("分类不存在");
        }
        if (bookMapper.findByIsbn(request.getIsbn()) != null) {
            throw new BusinessException("ISBN 已存在，请勿重复录入");
        }
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setCategoryId(request.getCategoryId());
        book.setTotal(request.getTotal());
        book.setPublishDate(request.getPublishDate());
        bookMapper.insert(book);
        return book.getId();
    }

    public void update(Long id, BookRequest request) {
        if (bookMapper.findById(id) == null) {
            throw new BusinessException("图书不存在");
        }
        if (categoryMapper.findById(request.getCategoryId()) == null) {
            throw new BusinessException("分类不存在");
        }
        Book existed = bookMapper.findByIsbn(request.getIsbn());
        if (existed != null && !existed.getId().equals(id)) {
            throw new BusinessException("ISBN 已被其他图书使用");
        }
        Book book = new Book();
        book.setId(id);
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setCategoryId(request.getCategoryId());
        book.setPublishDate(request.getPublishDate());
        bookMapper.update(book);
    }

    public void delete(Long id) {
        if (bookMapper.delete(id) == 0) {
            throw new BusinessException("图书不存在");
        }
    }
}
