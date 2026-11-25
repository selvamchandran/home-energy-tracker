package com.selvam.alertservice.repository;

import com.selvam.alertservice.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
