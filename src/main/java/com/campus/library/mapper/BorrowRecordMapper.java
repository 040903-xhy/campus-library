package com.campus.library.mapper;

import com.campus.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BorrowRecordMapper {

    @Insert("INSERT INTO borrow_record(user_id, book_id, borrow_date, due_date, status) " +
            "VALUES(#{userId}, #{bookId}, CURRENT_TIMESTAMP, #{dueDate}, 'BORROWED')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BorrowRecord record);

    @Select("SELECT r.*, u.username, u.nickname, b.title AS book_title FROM borrow_record r " +
            "JOIN user u ON r.user_id = u.id JOIN book b ON r.book_id = b.id WHERE r.id = #{id}")
    BorrowRecord findById(Long id);

    /** 读者当前未还的书有几本（限 5 本） */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND status = 'BORROWED'")
    int countBorrowing(Long userId);

    @Select("SELECT r.*, u.username, u.nickname, b.title AS book_title FROM borrow_record r " +
            "JOIN user u ON r.user_id = u.id JOIN book b ON r.book_id = b.id " +
            "WHERE r.user_id = #{userId} ORDER BY r.id DESC LIMIT #{size} OFFSET #{offset}")
    List<BorrowRecord> pageByUser(@Param("userId") Long userId,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId}")
    long countByUser(Long userId);

    /** 管理员看全量借阅记录 */
    @Select("<script>" +
            "SELECT r.*, u.username, u.nickname, b.title AS book_title FROM borrow_record r " +
            "JOIN user u ON r.user_id = u.id JOIN book b ON r.book_id = b.id " +
            "<where>" +
            "<if test='status != null and status != \"\"'> AND r.status = #{status}</if>" +
            "</where>" +
            " ORDER BY r.id DESC LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<BorrowRecord> pageAll(@Param("status") String status,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM borrow_record " +
            "<where><if test='status != null and status != \"\"'> AND status = #{status}</if></where>" +
            "</script>")
    long countAll(@Param("status") String status);

    /** 还书：只能把 BORROWED 更新掉，防止重复还书 */
    @Update("UPDATE borrow_record SET return_date = CURRENT_TIMESTAMP, status = #{status} " +
            "WHERE id = #{id} AND status = 'BORROWED'")
    int returnBook(@Param("id") Long id, @Param("status") String status);

    /** 查询当前逾期未还（管理端催还/统计） */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE status = 'BORROWED' AND due_date < CURRENT_TIMESTAMP")
    int countOverdue();
}
