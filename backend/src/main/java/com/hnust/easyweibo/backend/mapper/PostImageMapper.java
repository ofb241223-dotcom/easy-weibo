package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.hnust.easyweibo.backend.domain.entity.PostImageEntity;

@Mapper
public interface PostImageMapper {

    @Select("SELECT * FROM post_images WHERE post_id = #{postId} ORDER BY id ASC")
    List<PostImageEntity> findByPostId(Long postId);

    @Insert("INSERT INTO post_images (post_id, image_url) VALUES (#{postId}, #{imageUrl})")
    void insert(PostImageEntity image);
}
