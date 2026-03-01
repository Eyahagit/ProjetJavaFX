package Services;

import java.util.List;

public interface CrudEven<T> {

    void ajouter(T t);

    void modifier(T t);

    void supprimer(int id);

    List<T> afficher();

    T rechercher(int id);
}