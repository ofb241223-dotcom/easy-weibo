package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import com.hnust.easyweibo.backend.domain.entity.CommentEntity;

@Mapper
public interface CommentMapper {

    @Select("SELECT * FROM comments WHERE post_id = #{postId} ORDER BY created_at ASC")
    List<CommentEntity> findByPostId(Long postId);

    @Select("SELECT * FROM comments WHERE author_id = #{authorId} ORDER BY created_at DESC")
    List<CommentEntity> findByAuthorId(Long authorId);

    @Select("""
        <script>
        SELECT * FROM comments
        <where>
          <if test="query != null and query != ''">
            AND LOWER(content) LIKE CONCAT('%', LOWER(#{query}), '%')
          </if>
        </where>
        ORDER BY created_at DESC
        </script>
        """)
    List<CommentEntity> findForAdmin(@Param("query") String query);

    @Select("SELECT * FROM comments WHERE id = #{id}")
    CommentEntity findById(Long id);

    @Insert("INSERT INTO comments (post_id, author_id, content, likes_count, created_at) VALUES (#{postId}, #{authorId}, #{content}, #{likesCount}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CommentEntity comment);

    @Delete("DELETE FROM comments WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM comments")
    int countAll();
}
