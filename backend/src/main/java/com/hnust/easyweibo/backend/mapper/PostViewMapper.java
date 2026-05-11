package com.hnust.easyweibo.backend.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.hnust.easyweibo.backend.domain.entity.PostViewEntity;

@Mapper
public interface PostViewMapper {

    @Insert("""
        INSERT INTO post_views (post_id, viewer_id, viewed_at)
        VALUES (#{postId}, #{viewerId}, #{viewedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PostViewEntity entity);

    @Select("""
        SELECT * FROM post_views
        WHERE post_id = #{postId}
        ORDER BY viewed_at DESC, id DESC
        """)
    List<PostViewEntity> findByPostId(@Param("postId") Long postId);

    @Select("""
        SELECT COUNT(*) FROM post_views
        WHERE post_id = #{postId}
        """)
    long countByPostId(@Param("postId") Long postId);

    @Select("""
        SELECT * FROM post_views
        WHERE viewer_id = #{viewerId} AND post_id = #{postId}
        ORDER BY viewed_at DESC, id DESC
        LIMIT 1
        """)
    PostViewEntity findLatestByViewerAndPost(@Param("viewerId") Long viewerId, @Param("postId") Long postId);
}
