package com.campus.library.mapper;

import com.campus.library.entity.Book;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BookMapper {

    @Select("<script>" +
            "SELECT b.*, c.name AS category_name FROM book b " +
            "LEFT JOIN category c ON b.category_id = c.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (b.title LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR b.author LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR b.isbn LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='categoryId != null'> AND b.category_id = #{categoryId}</if>" +
            "</where>" +
            " ORDER BY b.id DESC LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<Book> pageQuery(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("offset") int offset,
                         @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM book b " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (b.title LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR b.author LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR b.isbn LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='categoryId != null'> AND b.category_id = #{categoryId}</if>" +
            "</where>" +
            "</script>")
    long countQuery(@Param("keyword") String keyword,
                    @Param("categoryId") Long categoryId);

    @Select("SELECT b.*, c.name AS category_name FROM book b " +
            "LEFT JOIN category c ON b.category_id = c.id WHERE b.id = #{id}")
    Book findById(Long id);

    @Select("SELECT * FROM book WHERE isbn = #{isbn}")
    Book findByIsbn(String isbn);

    @Insert("INSERT INTO book(isbn, title, author, publisher, category_id, total, available, publish_date, created_at) " +
            "VALUES(#{isbn}, #{title}, #{author}, #{publisher}, #{categoryId}, #{total}, #{total}, #{publishDate}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    @Update("UPDATE book SET title = #{title}, author = #{author}, publisher = #{publisher}, " +
            "category_id = #{categoryId}, publish_date = #{publishDate} WHERE id = #{id}")
    int update(Book book);

    @Delete("DELETE FROM book WHERE id = #{id}")
    int delete(Long id);

    /**
     * 借书减库存：原子操作，available > 0 才允许减。
     * 并发下也不会减成负数，不需要额外加锁。
     */
    @Update("UPDATE book SET available = available - 1 WHERE id = #{id} AND available > 0")
    int decreaseAvailable(Long id);

    /** 还书加库存：不超过总库存 */
    @Update("UPDATE book SET available = available + 1 WHERE id = #{id} AND available < total")
    int increaseAvailable(Long id);
}
