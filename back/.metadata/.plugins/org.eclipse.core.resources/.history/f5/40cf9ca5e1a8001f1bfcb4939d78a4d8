package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String url = "jdbc:mysql://localhost:3306/jeuxVideo";
    private static final String utilisateur = "root";
    private static final String motDePasse = "";
    private static Connection connexion = null;


    public static Connection getConnection() {
        try {
            if (connexion == null || connexion.isClosed()) {
               
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                
                connexion = DriverManager.getConnection(url, utilisateur, motDePasse);
                System.out.println("CONNEXION A LA BD REUISSIE !!!");
            }
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
        	System.out.println("CONNEXION A LA BD ECHOUEE !!!");
        	ex.printStackTrace();
        }
        return connexion;
    }

}
