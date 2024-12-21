package com.ecommerce.ecommerce_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.ecommerce_service.model.BonusLog;

@Repository
public interface BonusLogRepository extends JpaRepository<BonusLog, Long> {
  List<BonusLog> findByResolvedFalse();
}