package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.hnust.easyweibo.backend.domain.entity.CommentImageEntity;

@Mapper
public interface CommentImageMapper {

    @Select("SELECT * FROM comment_images WHERE comment_id = #{commentId} ORDER BY id ASC")
    List<CommentImageEntity> findByCommentId(Long commentId);

    @Insert("INSERT INTO comment_images (comment_id, image_url) VALUES (#{commentId}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CommentImageEntity image);
}
