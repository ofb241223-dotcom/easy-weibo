package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import com.hnust.easyweibo.backend.domain.entity.AiMessageEntity;

@Mapper
public interface AiMessageMapper {

    @Insert("""
        INSERT INTO ai_messages (conversation_id, role, content, model, created_at)
        VALUES (#{conversationId}, #{role}, #{content}, #{model}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiMessageEntity message);

    @Select("""
        SELECT * FROM ai_messages
        WHERE conversation_id = #{conversationId}
        ORDER BY created_at ASC, id ASC
        """)
    List<AiMessageEntity> findByConversationId(Long conversationId);

    @Select("""
        SELECT * FROM ai_messages
        WHERE conversation_id = #{conversationId}
        ORDER BY id DESC
        LIMIT 1
        """)
    AiMessageEntity findLatestByConversationId(Long conversationId);

    @Delete("DELETE FROM ai_messages WHERE id = #{id}")
    void deleteById(Long id);
}
