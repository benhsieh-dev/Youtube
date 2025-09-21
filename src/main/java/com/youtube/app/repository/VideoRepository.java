package com.youtube.app.repository;

import com.youtube.app.model.Video;
import com.youtube.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    List<Video> findByUploaderOrderByCreatedAtDesc(User uploader);
    
    Page<Video> findByStatusOrderByCreatedAtDesc(Video.VideoStatus status, Pageable pageable);
    
    @Query("SELECT v FROM Video v WHERE v.status = :status AND " +
           "(LOWER(v.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Video> findByStatusAndTitleOrDescriptionContainingIgnoreCase(
        @Param("status") Video.VideoStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    @Query("SELECT v FROM Video v WHERE v.status = :status ORDER BY v.viewCount DESC")
    Page<Video> findByStatusOrderByViewCountDesc(
        @Param("status") Video.VideoStatus status,
        Pageable pageable
    );
    
    Long countByUploader(User uploader);
}