package Service;

import java.sql.SQLDataException;
import java.util.List;

public interface Iservice<T> {
    void ajouter(T t) throws SQLDataException;        // plus de throws
    void supprimer(T t) throws SQLDataException;
    void modifier(T t) throws SQLDataException;
    List<T> recuperer() throws SQLDataException;
}
