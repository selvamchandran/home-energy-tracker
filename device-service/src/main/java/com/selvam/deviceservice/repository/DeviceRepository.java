package com.selvam.deviceservice.repository;

import com.selvam.deviceservice.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device,Long> {
    List<Device> findAllByUserId(Long userId);
}
