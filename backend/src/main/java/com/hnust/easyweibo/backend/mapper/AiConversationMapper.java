package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.hnust.easyweibo.backend.domain.entity.AiConversationEntity;

@Mapper
public interface AiConversationMapper {

    @Insert("""
        INSERT INTO ai_conversations (user_id, title, created_at, updated_at)
        VALUES (#{userId}, #{title}, #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiConversationEntity conversation);

    @Select("""
        SELECT * FROM ai_conversations
        WHERE user_id = #{userId}
        ORDER BY updated_at DESC, id DESC
        """)
    List<AiConversationEntity> findByUserId(@Param("userId") Long userId);

    @Select("""
        SELECT * FROM ai_conversations
        WHERE id = #{id} AND user_id = #{userId}
        """)
    AiConversationEntity findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Update("""
        UPDATE ai_conversations
        SET title = #{title},
            updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void updateTitleAndTouched(AiConversationEntity conversation);

    @Update("""
        UPDATE ai_conversations
        SET updated_at = #{updatedAt}
        WHERE id = #{id}
        """)
    void touchUpdatedAt(AiConversationEntity conversation);

    @org.apache.ibatis.annotations.Delete("""
        DELETE FROM ai_conversations
        WHERE id = #{id} AND user_id = #{userId}
        """)
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
