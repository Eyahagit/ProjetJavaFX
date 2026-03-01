package Controllers;

import Models.Favori;
import Services.favoriService;

import java.util.List;
import java.util.Optional;

public class FavoriController {

    private final favoriService favoriService = new favoriService();

    public Favori create(Favori favori) {
        return favoriService.create(favori);
    }

    public Optional<Favori> findById(int id) {
        return favoriService.findById(id);
    }

    public List<Favori> findAll() {
        return favoriService.findAll();
    }

    public Favori update(Favori favori) {
        return favoriService.update(favori);
    }

    public boolean delete(int id) {
        return favoriService.delete(id);
    }

    public List<Favori> findByUserId(int userId) {
        return favoriService.findByUserId(userId);
    }

    public List<Favori> findByRessourceId(int ressourceId) {
        return favoriService.findByRessourceId(ressourceId);
    }

    public Optional<Favori> findByUserIdAndRessourceId(int userId, int ressourceId) {
        return favoriService.findByUserIdAndRessourceId(userId, ressourceId);
    }

    public boolean deleteByUserIdAndRessourceId(int userId, int ressourceId) {
        return favoriService.deleteByUserIdAndRessourceId(userId, ressourceId);
    }
}
