package org.example.Interface;

import org.example.Models.Favori;

import java.util.List;
import java.util.Optional;

public interface favoriInterface {  Favori create(Favori favori);
    Optional<Favori> findById(int id);
    List<Favori> findAll();
    Favori update(Favori favori);
    boolean delete(int id);

    List<Favori> findByUserId(int userId);
    List<Favori> findByRessourceId(int ressourceId);

    Optional<Favori> findByUserIdAndRessourceId(int userId, int ressourceId);
    boolean deleteByUserIdAndRessourceId(int userId, int ressourceId);
}
