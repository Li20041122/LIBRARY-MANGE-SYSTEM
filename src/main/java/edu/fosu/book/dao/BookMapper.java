package edu.fosu.book.dao;

import edu.fosu.book.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    int deleteByPrimaryKey(String bookid);

    int insert(Book record);

    Book selectByPrimaryKey(String bookid);

    List<Book> selectAll();

    int updateByPrimaryKey(Book record);

    List<Book> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    long countAll(@Param("keyword") String keyword);
}