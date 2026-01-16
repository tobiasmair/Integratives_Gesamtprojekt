package com.gesamtprojekt.application.repositories;

import com.gesamtprojekt.application.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
