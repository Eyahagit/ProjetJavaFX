package Services;

import Models.Cabinet;
import utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCabinet implements Iservices<Cabinet> {

    private Connection connection;

    public ServiceCabinet() {
        connection = Database.getInstance().getConnection(); // singleton
    }

    @Override
    public void ajouter(Cabinet c) {

        String sql = "INSERT INTO cabinet (nomcabinet, adresse, ville, telephone, email, description, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, c.getNomcabinet());
            ps.setString(2, c.getAdresse());
            ps.setString(3, c.getVille());
            ps.setInt(4, c.getTelephone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getDescription());
            ps.setString(7, c.getStatus());

            ps.executeUpdate();
            System.out.println("Cabinet ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void supprimer(Cabinet cabinet) throws SQLDataException{
        String sql = "DELETE FROM cabinet WHERE idCabinet = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cabinet.getIdCabinet());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(Cabinet cabinet) throws SQLDataException{
        String sql = "UPDATE cabinet SET nomCabinet=?, adresse=?, ville=?, telephone=?, email=?, description=?, status=? WHERE idCabinet=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, cabinet.getNomcabinet());
            ps.setString(2, cabinet.getAdresse());
            ps.setString(3, cabinet.getVille());
            ps.setInt(4, cabinet.getTelephone());
            ps.setString(5, cabinet.getEmail());
            ps.setString(6, cabinet.getDescription());
            ps.setString(7, cabinet.getStatus());
            ps.setInt(8, cabinet.getIdCabinet());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Cabinet> recuperer() {

        List<Cabinet> list = new ArrayList<>();
        String sql = "SELECT * FROM cabinet";

        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                Cabinet c = new Cabinet();
                c.setIdCabinet(rs.getInt("idCabinet"));
                c.setNomcabinet(rs.getString("nomcabinet"));
                c.setAdresse(rs.getString("adresse"));
                c.setVille(rs.getString("ville"));
                c.setTelephone(rs.getInt("telephone"));
                c.setEmail(rs.getString("email"));
                c.setDescription(rs.getString("description"));
                c.setStatus(rs.getString("status"));

                list.add(c);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }

}
