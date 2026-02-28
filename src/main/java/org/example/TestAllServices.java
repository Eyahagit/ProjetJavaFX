package org.example;

import org.example.Controllers.*;
import org.example.Models.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Run this class to test all controllers (and thus all services).
 * Prerequisites: MySQL running, database eyaprojet exists, connection in
 * MyDatabase is correct.
 *
 * How to run:
 * 1. From IDE: right-click TestAllServices.java → Run 'TestAllServices.main()'
 * 2. From terminal: mvn compile exec:java
 * -Dexec.mainClass="org.example.TestAllServices"
 * (or add exec-maven-plugin to pom.xml and use the above)
 * 3. Or run Main.main() once, then run TestAllServices.main()
 */
public class TestAllServices {

    public static void main(String[] args) {
        System.out.println("========== Initializing DB and static user ==========");
        Main.initDatabaseAndStaticUser();
        if (!org.example.utils.StaticUser.isSet()) {
            System.err.println("Static user not set. Check DB connection and run again.");
            return;
        }

        RessourceController ressourceCtrl = new RessourceController();
        EvaluationController evaluationCtrl = new EvaluationController();
        FavoriController favoriCtrl = new FavoriController();

        int staticUserId = org.example.utils.StaticUser.getId();
        System.out.println("Static user id = " + staticUserId + "\n");

        // ---- 1) Ressource controller ----
        System.out.println("\n---------- 1) RessourceController ----------");
        Ressource res = new Ressource("Test Article", "Description", "ARTICLE", "Santé", "Content here", "Author",
                LocalDate.now(), "PUBLISHED");
        res = ressourceCtrl.create(res);
        System.out.println("Create: id=" + res.getId() + ", title=" + res.getTitle());

        ressourceCtrl.findById(res.getId()).ifPresent(r -> System.out.println("FindById: " + r.getTitle()));

        List<Ressource> byType = ressourceCtrl.findByType("ARTICLE");
        System.out.println("FindByType(ARTICLE) count: " + byType.size());

        List<Ressource> byCategory = ressourceCtrl.findByCategory("Santé");
        System.out.println("FindByCategory(Santé) count: " + byCategory.size());

        List<Ressource> byStatus = ressourceCtrl.findByStatus("PUBLISHED");
        System.out.println("FindByStatus(PUBLISHED) count: " + byStatus.size());

        List<Ressource> allRessources = ressourceCtrl.findAll();
        System.out.println("FindAll count: " + allRessources.size());

        res.setTitle("Updated Title");
        ressourceCtrl.update(res);
        System.out.println("Update: title=" + ressourceCtrl.findById(res.getId()).map(Ressource::getTitle).orElse("?"));

        // ---- 2) Evaluation controller (uses static user + ressource) ----------
        System.out.println("\n---------- 2) EvaluationController ----------");
        Evaluation eval = new Evaluation(staticUserId, res, 5, "Great!", LocalDate.now());
        eval = evaluationCtrl.create(eval);
        System.out.println("Create: id=" + eval.getId() + ", note=" + eval.getNote());

        evaluationCtrl.findById(eval.getId()).ifPresent(e -> System.out.println("FindById: note=" + e.getNote()));

        List<Evaluation> byUser = evaluationCtrl.findByUserId(staticUserId);
        System.out.println("FindByUserId(" + staticUserId + ") count: " + byUser.size());

        List<Evaluation> byRessource = evaluationCtrl.findByRessourceId(res.getId());
        System.out.println("FindByRessourceId(" + res.getId() + ") count: " + byRessource.size());

        eval.setNote(4);
        evaluationCtrl.update(eval);
        System.out.println("Update: note=" + evaluationCtrl.findById(eval.getId()).map(Evaluation::getNote).orElse(0));

        // ---- 3) Favori controller ----------
        System.out.println("\n---------- 3) FavoriController ----------");
        Favori fav = new Favori(staticUserId, res, LocalDate.now());
        fav = favoriCtrl.create(fav);
        System.out.println("Create: id=" + fav.getId());

        favoriCtrl.findById(fav.getId()).ifPresent(f -> System.out.println("FindById: id=" + f.getId()));

        Optional<Favori> byUserAndRessource = favoriCtrl.findByUserIdAndRessourceId(staticUserId, res.getId());
        System.out.println("FindByUserIdAndRessourceId: " + (byUserAndRessource.isPresent() ? "found" : "empty"));

        List<Favori> favorisByUser = favoriCtrl.findByUserId(staticUserId);
        System.out.println("FindByUserId count: " + favorisByUser.size());

        List<Favori> favorisByRessource = favoriCtrl.findByRessourceId(res.getId());
        System.out.println("FindByRessourceId count: " + favorisByRessource.size());

        boolean removed = favoriCtrl.deleteByUserIdAndRessourceId(staticUserId, res.getId());
        System.out.println("DeleteByUserIdAndRessourceId: " + removed);

        // Cleanup: delete evaluation (favori already removed above), ressource, then
        // test user
        evaluationCtrl.delete(eval.getId());
        ressourceCtrl.delete(res.getId());
        System.out.println("\n---------- Cleanup done (evaluation, ressource deleted) ----------");
        System.out.println("========== All services tested successfully ==========");
    }
}
