package com.youtube.app.repository;

import com.youtube.app.model.Channel;
import com.youtube.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
    Optional<Channel> findByOwner(User owner);
    
    Optional<Channel> findByName(String name);
    
    boolean existsByName(String name);
}