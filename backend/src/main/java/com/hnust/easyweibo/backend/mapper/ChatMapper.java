package com.hnust.easyweibo.backend.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.hnust.easyweibo.backend.domain.entity.ConversationEntity;
import com.hnust.easyweibo.backend.domain.entity.MessageEntity;

@Mapper
public interface ChatMapper {

    @Select("""
        SELECT * FROM conversations
        WHERE id = #{id}
        """)
    ConversationEntity findConversationById(@Param("id") Long id);

    @Select("""
        SELECT * FROM conversations
        WHERE user_a_id = #{userAId} AND user_b_id = #{userBId}
        LIMIT 1
        """)
    ConversationEntity findConversationBetween(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    @Select("""
        SELECT * FROM conversations
        WHERE user_a_id = #{userId} OR user_b_id = #{userId}
        ORDER BY updated_at DESC, id DESC
        """)
    List<ConversationEntity> findConversationsByUserId(@Param("userId") Long userId);

    @Insert("""
        INSERT INTO conversations (user_a_id, user_b_id, created_at, updated_at)
        VALUES (#{userAId}, #{userBId}, #{createdAt}, #{updatedAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertConversation(ConversationEntity entity);

    @Update("""
        UPDATE conversations
        SET updated_at = #{updatedAt}
        WHERE id = #{conversationId}
        """)
    void touchConversation(@Param("conversationId") Long conversationId, @Param("updatedAt") LocalDateTime updatedAt);

    @Select("""
        SELECT * FROM messages
        WHERE conversation_id = #{conversationId}
        ORDER BY created_at ASC, id ASC
        """)
    List<MessageEntity> findMessagesByConversationId(@Param("conversationId") Long conversationId);

    @Select("""
        SELECT * FROM messages
        WHERE conversation_id = #{conversationId}
        ORDER BY created_at DESC, id DESC
        LIMIT 1
        """)
    MessageEntity findLastMessage(@Param("conversationId") Long conversationId);

    @Insert("""
        INSERT INTO messages (
            conversation_id,
            sender_id,
            content,
            message_type,
            file_url,
            file_name,
            mime_type,
            created_at,
            read_at,
            recalled,
            recalled_at
        )
        VALUES (
            #{conversationId},
            #{senderId},
            #{content},
            #{messageType},
            #{fileUrl},
            #{fileName},
            #{mimeType},
            #{createdAt},
            #{readAt},
            #{recalled},
            #{recalledAt}
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMessage(MessageEntity entity);

    @Select("""
        SELECT * FROM messages
        WHERE id = #{id}
        LIMIT 1
        """)
    MessageEntity findMessageById(@Param("id") Long id);

    @Update("""
        UPDATE messages
        SET read_at = #{readAt}
        WHERE conversation_id = #{conversationId}
          AND sender_id <> #{currentUserId}
          AND read_at IS NULL
        """)
    void markConversationAsRead(
        @Param("conversationId") Long conversationId,
        @Param("currentUserId") Long currentUserId,
        @Param("readAt") LocalDateTime readAt
    );

    @Update("""
        UPDATE messages
        SET recalled = TRUE,
            recalled_at = #{recalledAt}
        WHERE id = #{id}
        """)
    void recallMessage(@Param("id") Long id, @Param("recalledAt") LocalDateTime recalledAt);

    @Select("""
        SELECT COUNT(*) FROM messages
        WHERE conversation_id = #{conversationId}
          AND sender_id <> #{currentUserId}
          AND read_at IS NULL
        """)
    int countUnreadMessages(@Param("conversationId") Long conversationId, @Param("currentUserId") Long currentUserId);

    @Select("""
        SELECT COUNT(*) FROM messages
        WHERE conversation_id = #{conversationId}
          AND sender_id = #{senderId}
        """)
    int countMessagesBySender(@Param("conversationId") Long conversationId, @Param("senderId") Long senderId);

    @Select("""
        SELECT COUNT(*) > 0 FROM messages
        WHERE conversation_id = #{conversationId}
          AND sender_id = #{senderId}
        """)
    boolean existsMessageFromSender(@Param("conversationId") Long conversationId, @Param("senderId") Long senderId);

    @Select("""
        SELECT COUNT(*) > 0 FROM blocks
        WHERE blocker_id = #{blockerId} AND blocked_id = #{blockedId}
        """)
    boolean existsBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

    @Insert("""
        INSERT INTO blocks (blocker_id, blocked_id, created_at)
        VALUES (#{blockerId}, #{blockedId}, #{createdAt})
        """)
    void insertBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId, @Param("createdAt") LocalDateTime createdAt);

    @Delete("""
        DELETE FROM blocks
        WHERE blocker_id = #{blockerId} AND blocked_id = #{blockedId}
        """)
    void deleteBlock(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);
}
