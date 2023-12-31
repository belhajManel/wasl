package com.mzo.wasl.repository;

import com.mzo.wasl.model.Sender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SenderRepository extends JpaRepository<Sender,Long> {
    Sender findByUserId(Long id);
}
