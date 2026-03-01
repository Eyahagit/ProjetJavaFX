package Controllers;

import Models.Evaluation;
import Services.evaluationService;

import java.util.List;
import java.util.Optional;

public class EvaluationController {

    private final evaluationService evaluationService = new evaluationService();

    public Evaluation create(Evaluation evaluation) {
        return evaluationService.create(evaluation);
    }

    public Optional<Evaluation> findById(int id) {
        return evaluationService.findById(id);
    }

    public List<Evaluation> findAll() {
        return evaluationService.findAll();
    }

    public Evaluation update(Evaluation evaluation) {
        return evaluationService.update(evaluation);
    }

    public boolean delete(int id) {
        return evaluationService.delete(id);
    }

    public List<Evaluation> findByUserId(int userId) {
        return evaluationService.findByUserId(userId);
    }

    public List<Evaluation> findByRessourceId(int ressourceId) {
        return evaluationService.findByRessourceId(ressourceId);
    }
}
