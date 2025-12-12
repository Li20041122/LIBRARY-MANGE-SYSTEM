package edu.fosu.book.dao;

import edu.fosu.book.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(String userid);

    int insert(User record);

    User selectByPrimaryKey(String userid);

    List<User> selectAll();

    int updateByPrimaryKey(User record);

    User selectByUsername(String username);

    List<User> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    long countAll(@Param("keyword") String keyword);
}