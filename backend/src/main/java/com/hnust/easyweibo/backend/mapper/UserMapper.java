package com.hnust.easyweibo.backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;

import com.hnust.easyweibo.backend.domain.entity.UserEntity;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    UserEntity findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    UserEntity findById(Long id);

    @Select("""
        SELECT * FROM users
        WHERE (
            LOWER(username) LIKE CONCAT('%', LOWER(#{query}), '%')
            OR LOWER(nickname) LIKE CONCAT('%', LOWER(#{query}), '%')
        )
          AND status = 'ACTIVE'
        ORDER BY created_at DESC
        """)
    java.util.List<UserEntity> search(@Param("query") String query);

    @Select("""
        SELECT u.* FROM users u
        WHERE (#{viewerId} IS NULL OR u.id <> #{viewerId})
          AND u.status = 'ACTIVE'
        ORDER BY (
            SELECT COUNT(*)
            FROM follows f
            WHERE f.following_id = u.id
        ) DESC, u.created_at DESC
        LIMIT #{limit}
        """)
    java.util.List<UserEntity> findRecommended(@Param("viewerId") Long viewerId, @Param("limit") int limit);

    @Insert("""
        INSERT INTO users (username, password, nickname, avatar_url, cover_url, bio, role, status, mute_status, created_at)
        VALUES (#{username}, #{password}, #{nickname}, #{avatarUrl}, #{coverUrl}, #{bio}, #{role}, #{status}, #{muteStatus}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserEntity user);

    @Update("""
        UPDATE users
        SET nickname = #{nickname},
            bio = #{bio},
            avatar_url = #{avatarUrl},
            cover_url = #{coverUrl}
        WHERE id = #{id}
        """)
    void updateProfile(UserEntity user);

    @Update("UPDATE users SET password = #{encodedPassword} WHERE id = #{id}")
    void updatePassword(@Param("id") Long id, @Param("encodedPassword") String encodedPassword);

    @Update("UPDATE users SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE users SET mute_status = #{muteStatus} WHERE id = #{id}")
    void updateMuteStatus(@Param("id") Long id, @Param("muteStatus") String muteStatus);

    @Select("SELECT COUNT(*) FROM follows WHERE following_id = #{userId}")
    int countFollowers(Long userId);

    @Select("SELECT COUNT(*) FROM follows WHERE follower_id = #{userId}")
    int countFollowing(Long userId);

    @Select("SELECT COUNT(*) > 0 FROM follows WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    boolean existsFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Insert("INSERT INTO follows (follower_id, following_id) VALUES (#{followerId}, #{followingId})")
    void insertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Delete("DELETE FROM follows WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    void deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    @Select("""
        SELECT u.* FROM follows f
        JOIN users u ON u.id = f.following_id
        WHERE f.follower_id = #{userId}
        ORDER BY f.created_at DESC, u.created_at DESC
        """)
    java.util.List<UserEntity> findFollowingUsers(@Param("userId") Long userId);

    @Select("""
        SELECT u.* FROM follows f
        JOIN users u ON u.id = f.follower_id
        WHERE f.following_id = #{userId}
        ORDER BY f.created_at DESC, u.created_at DESC
        """)
    java.util.List<UserEntity> findFollowerUsers(@Param("userId") Long userId);

    @Select("""
        SELECT u.* FROM follows f
        JOIN follows reverse_f ON reverse_f.follower_id = f.following_id AND reverse_f.following_id = f.follower_id
        JOIN users u ON u.id = f.following_id
        WHERE f.follower_id = #{userId}
        ORDER BY reverse_f.created_at DESC, u.created_at DESC
        """)
    java.util.List<UserEntity> findMutualUsers(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM users")
    int countAll();

    @Select("""
        <script>
        SELECT * FROM users
        <where>
          <if test="query != null and query != ''">
            AND (
              LOWER(username) LIKE CONCAT('%', LOWER(#{query}), '%')
              OR LOWER(nickname) LIKE CONCAT('%', LOWER(#{query}), '%')
            )
          </if>
          <if test="status != null and status != ''">
            AND status = #{status}
          </if>
          <if test="muteStatus != null and muteStatus != ''">
            AND mute_status = #{muteStatus}
          </if>
        </where>
        ORDER BY created_at DESC
        </script>
        """)
    java.util.List<UserEntity> findForAdmin(
        @Param("query") String query,
        @Param("status") String status,
        @Param("muteStatus") String muteStatus
    );
}
