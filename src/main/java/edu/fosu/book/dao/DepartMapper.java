package edu.fosu.book.dao;

import edu.fosu.book.entity.Depart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DepartMapper {
    int deleteByPrimaryKey(String departid);

    int insert(Depart record);

    Depart selectByPrimaryKey(String departid);

    List<Depart> selectAll();

    int updateByPrimaryKey(Depart record);

    List<Depart> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    long countAll(@Param("keyword") String keyword);
}