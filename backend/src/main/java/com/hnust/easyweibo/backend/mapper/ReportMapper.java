package com.hnust.easyweibo.backend.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.hnust.easyweibo.backend.domain.entity.ReportEntity;

@Mapper
public interface ReportMapper {

    @Insert("""
        INSERT INTO reports (post_id, reporter_id, category, details, status, resolved_by, resolved_at, created_at)
        VALUES (#{postId}, #{reporterId}, #{category}, #{details}, #{status}, #{resolvedBy}, #{resolvedAt}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ReportEntity report);

    @Select("""
        SELECT COUNT(*) > 0 FROM reports
        WHERE post_id = #{postId}
          AND reporter_id = #{reporterId}
          AND status = 'OPEN'
        """)
    boolean existsOpenByReporter(@Param("postId") Long postId, @Param("reporterId") Long reporterId);

    @Select("SELECT * FROM reports ORDER BY created_at DESC")
    List<ReportEntity> findAll();

    @Select("SELECT * FROM reports WHERE status = 'OPEN' ORDER BY created_at DESC")
    List<ReportEntity> findOpen();

    @Select("SELECT * FROM reports WHERE id = #{id}")
    ReportEntity findById(Long id);

    @Select("SELECT COUNT(*) FROM reports WHERE status = 'OPEN'")
    int countOpen();

    @Update("""
        UPDATE reports
        SET status = 'RESOLVED',
            resolved_by = #{resolvedBy},
            resolved_at = #{resolvedAt}
        WHERE id = #{id} AND status = 'OPEN'
        """)
    void resolve(@Param("id") Long id, @Param("resolvedBy") Long resolvedBy, @Param("resolvedAt") LocalDateTime resolvedAt);

    @Update("""
        UPDATE reports
        SET status = 'RESOLVED',
            resolved_by = #{resolvedBy},
            resolved_at = #{resolvedAt}
        WHERE post_id = #{postId} AND status = 'OPEN'
        """)
    void resolveByPost(@Param("postId") Long postId, @Param("resolvedBy") Long resolvedBy, @Param("resolvedAt") LocalDateTime resolvedAt);
}
