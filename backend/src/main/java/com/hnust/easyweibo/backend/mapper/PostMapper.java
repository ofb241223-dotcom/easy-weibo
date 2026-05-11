package com.hnust.easyweibo.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.hnust.easyweibo.backend.domain.entity.PostEntity;

@Mapper
public interface PostMapper {

    @Select("SELECT * FROM posts ORDER BY created_at DESC")
    List<PostEntity> findAll();

    @Select("SELECT * FROM posts ORDER BY created_at DESC LIMIT #{limit}")
    List<PostEntity> findRecent(@Param("limit") int limit);

    @Select("""
        <script>
        SELECT * FROM posts
        <where>
          <if test="query != null and query != ''">
            AND LOWER(content) LIKE CONCAT('%', LOWER(#{query}), '%')
          </if>
          <if test="status != null and status != ''">
            AND status = #{status}
          </if>
        </where>
        ORDER BY created_at DESC
        </script>
        """)
    List<PostEntity> findForAdmin(@Param("query") String query, @Param("status") String status);

    @Select("SELECT * FROM posts WHERE id = #{id}")
    PostEntity findById(Long id);

    @Select("SELECT * FROM posts WHERE author_id = #{authorId} ORDER BY created_at DESC")
    List<PostEntity> findByAuthorId(Long authorId);

    @Select("""
        SELECT p.* FROM posts p
        JOIN post_likes pl ON pl.post_id = p.id
        WHERE pl.user_id = #{userId}
        ORDER BY pl.created_at DESC
        """)
    List<PostEntity> findLikedByUserId(Long userId);

    @Select("""
        SELECT p.* FROM posts p
        JOIN bookmarks b ON b.post_id = p.id
        WHERE b.user_id = #{userId}
        ORDER BY b.created_at DESC
        """)
    List<PostEntity> findBookmarkedByUserId(Long userId);

    @Select("""
        SELECT p.* FROM posts p
        JOIN reposts r ON r.post_id = p.id
        WHERE r.user_id = #{userId}
        ORDER BY r.created_at DESC
        """)
    List<PostEntity> findRepostedByUserId(Long userId);

    @Select("""
        SELECT p.* FROM posts p
        JOIN users u ON p.author_id = u.id
        WHERE LOWER(p.content) LIKE CONCAT('%', LOWER(#{query}), '%')
           OR LOWER(u.nickname) LIKE CONCAT('%', LOWER(#{query}), '%')
           OR LOWER(u.username) LIKE CONCAT('%', LOWER(#{query}), '%')
        ORDER BY p.created_at DESC
        """)
    List<PostEntity> search(@Param("query") String query);

    @Select("SELECT * FROM posts WHERE LOWER(content) LIKE CONCAT('%#', LOWER(#{tag}), '%') ORDER BY created_at DESC")
    List<PostEntity> findByTag(String tag);

    @Insert("INSERT INTO posts (author_id, content, status, likes_count, reposts_count, comments_count, views_count, created_at) VALUES (#{authorId}, #{content}, #{status}, #{likesCount}, #{repostsCount}, #{commentsCount}, #{viewsCount}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PostEntity post);

    @Update("UPDATE posts SET content = #{content} WHERE id = #{id}")
    void updateContent(@Param("id") Long id, @Param("content") String content);

    @Update("UPDATE posts SET content = #{content}, status = #{status} WHERE id = #{id}")
    void republish(@Param("id") Long id, @Param("content") String content, @Param("status") String status);

    @Update("UPDATE posts SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM posts WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT COUNT(*) FROM posts")
    int countAll();

    @Select("SELECT COALESCE(SUM(views_count), 0) FROM posts")
    long countTotalViews();

    @Select("SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean existsLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO post_likes (post_id, user_id) VALUES (#{postId}, #{userId})")
    void insertLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = #{postId}")
    void incrementLikeCount(Long postId);

    @Delete("DELETE FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    void deleteLike(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("UPDATE posts SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = #{postId}")
    void decrementLikeCount(Long postId);

    @Select("SELECT COUNT(*) > 0 FROM bookmarks WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean existsBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO bookmarks (post_id, user_id) VALUES (#{postId}, #{userId})")
    void insertBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM bookmarks WHERE post_id = #{postId} AND user_id = #{userId}")
    void deleteBookmark(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) > 0 FROM reposts WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean existsRepost(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO reposts (post_id, user_id) VALUES (#{postId}, #{userId})")
    void insertRepost(@Param("postId") Long postId, @Param("userId") Long userId);

    @Delete("DELETE FROM reposts WHERE post_id = #{postId} AND user_id = #{userId}")
    void deleteRepost(@Param("postId") Long postId, @Param("userId") Long userId);

    @Update("UPDATE posts SET reposts_count = reposts_count + 1 WHERE id = #{postId}")
    void incrementRepostCount(Long postId);

    @Update("UPDATE posts SET reposts_count = GREATEST(reposts_count - 1, 0) WHERE id = #{postId}")
    void decrementRepostCount(Long postId);

    @Update("UPDATE posts SET comments_count = comments_count + 1 WHERE id = #{postId}")
    void incrementCommentCount(Long postId);

    @Update("UPDATE posts SET comments_count = GREATEST(comments_count - 1, 0) WHERE id = #{postId}")
    void decrementCommentCount(Long postId);

    @Update("UPDATE posts SET views_count = views_count + 1 WHERE id = #{postId}")
    void incrementViewCount(Long postId);

    @Select("SELECT COUNT(*) > 0 FROM hidden_posts WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean existsHidden(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("INSERT INTO hidden_posts (post_id, user_id) VALUES (#{postId}, #{userId})")
    void insertHidden(@Param("postId") Long postId, @Param("userId") Long userId);
}
