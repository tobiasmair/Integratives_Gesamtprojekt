package com.gesamtprojekt.application.service.implementation;

import com.gesamtprojekt.application.model.Equipment;
import com.gesamtprojekt.application.repositories.EquipmentRepository;
import com.gesamtprojekt.application.service.EquipmentServiceInterface;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService implements EquipmentServiceInterface {

    private final EquipmentRepository repo;

    public EquipmentService(EquipmentRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Equipment> findAll() {
        return repo.findAll();
    }

    @Override
    public Equipment create(Equipment equipment) {
        validate(equipment);
        return repo.save(equipment);
    }

    private void validate(Equipment equipment) {
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is null.");
        }
        String d = equipment.getDescription();
        if (d == null || d.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment description cannot be empty.");
        }
        equipment.setDescription(d.trim());
    }
}


