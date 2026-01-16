package com.gesamtprojekt.application.service;

import com.gesamtprojekt.application.model.Equipment;

import java.util.List;

public interface EquipmentServiceInterface {

    List<Equipment> findAll();

    Equipment create(Equipment equipment);
}

