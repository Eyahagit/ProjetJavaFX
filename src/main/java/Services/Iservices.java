package Services;

import java.sql.SQLException;  // CHANGE ICI
import java.util.List;

public interface Iservices<T> {
    void ajouter(T t) throws SQLException;      // SQLException au lieu de SQLDataException
    void supprimer(T t) throws SQLException;    // SQLException au lieu de SQLDataException
    void modifier(T t) throws SQLException;     // SQLException au lieu de SQLDataException
    List<T> recuperer() throws SQLException;     // SQLException au lieu de SQLDataException
}