package com.mzo.wasl.repositories;

import com.mzo.wasl.models.Sender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SenderRepository extends JpaRepository<Sender,Long> {
}