package org.example.Interface;

import org.example.Models.Ressource;

import java.util.List;
import java.util.Optional;

public interface ressourseInterface {    Ressource create(Ressource ressource);
    Optional<Ressource> findById(int id);
    List<Ressource> findAll();
    Ressource update(Ressource ressource);
    boolean delete(int id);

    List<Ressource> findByType(String type);
    List<Ressource> findByCategory(String category);
    List<Ressource> findByStatus(String status);
}
