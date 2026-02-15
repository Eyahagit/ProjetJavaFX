package org.example.Controller;

import org.example.Models.Ressource;
import org.example.Services.ressourceService;

import java.util.List;
import java.util.Optional;

public class RessourceController {

    private final ressourceService ressourceService = new ressourceService();

    public Ressource create(Ressource ressource) {
        return ressourceService.create(ressource);
    }

    public Optional<Ressource> findById(int id) {
        return ressourceService.findById(id);
    }

    public List<Ressource> findAll() {
        return ressourceService.findAll();
    }

    public Ressource update(Ressource ressource) {
        return ressourceService.update(ressource);
    }

    public boolean delete(int id) {
        return ressourceService.delete(id);
    }

    public List<Ressource> findByType(String type) {
        return ressourceService.findByType(type);
    }

    public List<Ressource> findByCategory(String category) {
        return ressourceService.findByCategory(category);
    }

    public List<Ressource> findByStatus(String status) {
        return ressourceService.findByStatus(status);
    }
}
