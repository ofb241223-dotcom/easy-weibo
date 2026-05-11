package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.hnust.easyweibo.backend.domain.entity.NotificationEntity;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notifications WHERE recipient_id = #{recipientId} ORDER BY created_at DESC")
    List<NotificationEntity> findByRecipientId(Long recipientId);

    @Insert("""
        INSERT INTO notifications (type, recipient_id, actor_id, post_id, message, action_label, action_url, is_read, created_at)
        VALUES (#{type}, #{recipientId}, #{actorId}, #{postId}, #{message}, #{actionLabel}, #{actionUrl}, #{isRead}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(NotificationEntity notification);

    @Update("UPDATE notifications SET is_read = TRUE WHERE id = #{id} AND recipient_id = #{recipientId}")
    void markAsRead(@Param("id") Long id, @Param("recipientId") Long recipientId);

    @Update("UPDATE notifications SET is_read = TRUE WHERE recipient_id = #{recipientId} AND is_read = FALSE")
    void markAllAsRead(Long recipientId);

    @Delete("""
        DELETE FROM notifications
        WHERE type = #{type}
          AND recipient_id = #{recipientId}
          AND actor_id = #{actorId}
          AND post_id = #{postId}
        """)
    void deleteByTypeAndRecipientAndActorAndPost(
        @Param("type") String type,
        @Param("recipientId") Long recipientId,
        @Param("actorId") Long actorId,
        @Param("postId") Long postId
    );

    @Delete("""
        DELETE FROM notifications
        WHERE type = #{type}
          AND recipient_id = #{recipientId}
          AND actor_id = #{actorId}
          AND post_id IS NULL
        """)
    void deleteByTypeAndRecipientAndActor(
        @Param("type") String type,
        @Param("recipientId") Long recipientId,
        @Param("actorId") Long actorId
    );
}
