package services;

import java.util.List;

public interface CrudPerso<T> {

    void ajouter(T t);

    void modifier(T t);

    void supprimer(int id);

    List<T> afficher();

    T rechercher(int id);
}