package org.example.Interface;

import org.example.Models.Evaluation;

import java.util.List;
import java.util.Optional;

public interface evaluationInterface { Evaluation create(Evaluation evaluation);
    Optional<Evaluation> findById(int id);
    List<Evaluation> findAll();
    Evaluation update(Evaluation evaluation);
    boolean delete(int id);

    List<Evaluation> findByUserId(int userId);
    List<Evaluation> findByRessourceId(int ressourceId);
}
