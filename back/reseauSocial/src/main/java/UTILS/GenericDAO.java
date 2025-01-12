package UTILS;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import Database.ConnectionManager;
import INTERFACE.IDAO;
import Model.ENTITIES.*;

public class GenericDAO<T> implements IDAO<T> {
	
    private Class<T> entityClass; //La classe à scanner

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T findById(int id) {
        String query = "SELECT * FROM " + getTableName() + " WHERE id_" + getTableName() + " = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToObject(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        String query = "SELECT * FROM " + getTableName();
        try (Connection connection = ConnectionManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) 
        {
        	
            while (resultSet.next()) {
                entities.add(mapResultSetToObject(resultSet));
            }
            
        } 
        
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        
        return entities;
    }

    @Override
    public void save(T obj) {
        int id = getId(obj);
        String query;
        boolean isUpdate = id > 0;

        if (isUpdate) {
            StringBuilder updateColumns = new StringBuilder();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (!field.getName().equals("id_" + getTableName())) {
                    updateColumns.append(field.getName()).append(" = ?, ");
                }
            }
            updateColumns.delete(updateColumns.length() - 2, updateColumns.length());
            query = "UPDATE " + getTableName() + " SET " + updateColumns + " WHERE id_" + getTableName() + " = ?";
        } else {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                columns.append(field.getName()).append(", ");
                values.append("?, ");
            }
            columns.delete(columns.length() - 2, columns.length());
            values.delete(values.length() - 2, values.length());
            query = "INSERT INTO " + getTableName() + " (" + columns + ") VALUES (" + values + ")";
        }

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);
            int parameterIndex = 1;
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (isUpdate && field.getName().equals("id_" + getTableName())) {
                    continue;
                }
                statement.setObject(parameterIndex++, field.get(obj));
            }
            if (isUpdate) {
                statement.setInt(parameterIndex, id);
            }
            statement.executeUpdate();
            if (!isUpdate) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setGeneratedId(obj, generatedKeys.getInt(1));
                }
            }
            connection.commit();
        } catch (SQLException | IllegalAccessException e) {
            try {
                if (ConnectionManager.getConnection() != null) {
                    ConnectionManager.getConnection().rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM " + getTableName() + " WHERE id_" + getTableName() + " = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<T> search(String keyword) {
        List<T> entities = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(getTableName()).append(" WHERE ");

        Field[] fields = entityClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                queryBuilder.append(" OR ");
            }
            queryBuilder.append(fields[i].getName()).append(" LIKE ?");
        }

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {

            for (int i = 1; i <= fields.length; i++) {
                statement.setString(i, "%" + keyword + "%");
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                entities.add(mapResultSetToObject(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entities;
    }
    
    

    private T mapResultSetToObject(ResultSet resultSet) throws SQLException {
        try {
            T obj = entityClass.getDeclaredConstructor().newInstance();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                
                if (fieldType == String.class) {
                    field.set(obj, resultSet.getString(fieldName));
                } else if (fieldType == Date.class) {
                    // Si c'est un champ Date, on effectue une conversion de java.sql.Date à java.util.Date
                    java.sql.Date sqlDate = resultSet.getDate(fieldName);
                    if (sqlDate != null) {
                        field.set(obj, new Date(sqlDate.getTime())); // Conversion de java.sql.Date à java.util.Date
                    } else {
                        field.set(obj, null);  // Si la valeur est null dans la base, on met null
                    }
                } else {
                    // Pour les autres types (int, double, etc.), on utilise getObject
                    Object value = resultSet.getObject(fieldName);
                    field.set(obj, value);
                }
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du mapping du ResultSet à l'objet " + entityClass.getSimpleName(), e);
        }
    }


    private String getTableName() {
        if (entityClass == Plateforme.class) return "plateforme";
        if (entityClass == Categorie.class) return "categorie";
        if (entityClass == Utilisateur.class) return "utilisateur";
        if (entityClass == Commande.class) return "commande";
        if (entityClass == Developpeur.class) return "developpeur";
        if (entityClass == Jeux.class) return "jeux";
        if (entityClass == Detail.class) return "detail";
        if (entityClass == Evaluation.class) return "evaluation";
        if (entityClass == Commentaire.class) return "commentaire";
        if (entityClass == Video.class) return "video";
        if (entityClass == Photo.class) return "photo";
        if (entityClass == UtilisateurJeux.class) return "utilisateurjeux";
        if (entityClass == DeveloppeurJeux.class) return "developpeurjeux";
        if (entityClass == DetailJeux.class) return "detailjeux";
        if (entityClass == PlateformeJeux.class) return "plateformenjeux";
        if (entityClass == CommentaireJeux.class) return "commentairejeux";
        throw new RuntimeException("Table name not found for class " + entityClass.getSimpleName());
    }

    private int getId(T obj) {
        try {
            Field idField = entityClass.getDeclaredField("id_" + getTableName());
            idField.setAccessible(true);
            return (int) idField.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void setGeneratedId(T obj, int id) {
        try {
            Field idField = entityClass.getDeclaredField("id_" + getTableName());
            idField.setAccessible(true);
            idField.set(obj, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
	/*
	 * ==================================================================
	 * ================= METHODES UTILITAIRES DANS L'APP ================
	 * ==================================================================
	 */
    
    public List<Map<String, Object>> findGameWithRelatedTable(String relatedTable, String relatedField) {
        List<Map<String, Object>> jeuxList = new ArrayList<>();

        // Requête pour récupérer les jeux et leurs photos ou vidéos associées
        String query = "SELECT DISTINCT j.*, MIN(r." + relatedField + ") AS " + relatedField + 
                       " FROM jeux j LEFT JOIN " + relatedTable + " r ON j.id_jeux = r.id_jeux " +
                       "GROUP BY j.id_jeux";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> jeuWithMedia = new HashMap<>();

                // Crée un objet Jeux à partir du ResultSet
                Jeux jeu = new Jeux();
                jeu.setId_jeux(resultSet.getInt("id_jeux"));
                jeu.setTitre(resultSet.getString("titre"));
                jeu.setDate_sortie(resultSet.getString("date_sortie"));
                jeu.setLangue(resultSet.getString("langue"));
                jeu.setPrix(resultSet.getDouble("prix"));
                jeu.setQuantite(resultSet.getInt("quantite"));
                jeu.setResume(resultSet.getString("resume"));

                // Ajoute l'objet Jeux dans la map
                jeuWithMedia.put("jeu", jeu);

                // Ajoute l'URL de la photo ou vidéo
                String urlMedia = resultSet.getString(relatedField);
                jeuWithMedia.put("url_media", urlMedia != null ? urlMedia : "default.jpg");

                jeuxList.add(jeuWithMedia);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return jeuxList;
    }

	/*
	 * ==================================================================
	 * ================= Jeu Populaire classé par nb com ================
	 * ==================================================================
	 */

 // Méthode pour obtenir les jeux les plus populaires
    public List<Map<String, Object>> findPopularGame() {
        List<Map<String, Object>> jeuxList = new ArrayList<>();
        
        // Requête SQL
        String query = 
        		"""
        		SELECT j.id_jeux,j.titre,j.prix,j.resume, MIN(p.url_photo) AS url_photo,
        			COUNT(c.id_commentaire) AS nombre_commentaires_positifs FROM jeux j 
        			LEFT JOIN commentaireJeux cj ON j.id_jeux = cj.id_jeux 
        			LEFT JOIN commentaire c ON cj.id_commentaire = c.id_commentaire 
        			LEFT JOIN photo p ON j.id_jeux = p.id_jeux 
        			WHERE c.statut_com = 1
        			GROUP BY j.id_jeux, j.titre, j.prix, j.resume 
        			ORDER BY id_jeux ASC
        			LIMIT 6
        		""";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> jeuDetails = new HashMap<>();
                jeuDetails.put("id_jeux", resultSet.getInt("id_jeux"));
                jeuDetails.put("titre", resultSet.getString("titre"));
                jeuDetails.put("prix", resultSet.getDouble("prix"));
                jeuDetails.put("resume", resultSet.getString("resume"));
                
                // Ajoute la photo
                String photoUrl = resultSet.getString("url_photo");
                jeuDetails.put("url_photo", photoUrl != null ? photoUrl : "default.jpg"); // Photo par défaut
                
                // Ajoute le nombre de commentaires positifs
                int nombreCommentairePositifs = resultSet.getInt("nombre_commentaires_positifs");
                jeuDetails.put("nombre_commentaires_positifs", nombreCommentairePositifs);
                
                jeuxList.add(jeuDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return jeuxList;
    }

   // Méthode pour récupérer les photos en fonction de l'ID du jeu
    public List<String> findPhotosByJeuxId(int idJeux) {
        List<String> photos = new ArrayList<>();
        String sql = "SELECT url_photo FROM photo WHERE id_jeux = ?";
        
        try (
        		
        	 Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            statement.setInt(1, idJeux);
            ResultSet resultSet = statement.executeQuery();
            
            // Parcourt des résultats et ajout des URLs des photos à la liste
            while (resultSet.next()) {
                photos.add(resultSet.getString("url_photo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return photos;
    }
    
    
//==================
// Méthode pour récupérer la vidéo en fonction de l'ID du jeu
    public Video findVideoByJeuxId(int idJeux) {
        Video video = null;
        String sql = "SELECT id_video, url_video, id_jeux FROM video WHERE id_jeux = ? ORDER BY id_video ASC LIMIT 1";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, idJeux);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                video = new Video();
                video.setId_video(resultSet.getInt("id_video"));
                video.setUrl_video(resultSet.getString("url_video"));
                video.setId_jeux(resultSet.getInt("id_jeux"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return video;
    }


 // Méthode pour obtenir la note moyenne d'un jeu spécifique par son ID en arrondi
    public int getAverageNoteByJeuxId(int idJeux) {
        String sql = "SELECT ROUND(AVG(note)) AS note_moyenne FROM evaluation WHERE id_jeux = ?";
        int noteMoyenne = 0;

        try (
        	 
        	 Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
             
            
            statement.setInt(1, idJeux);
            
           
            ResultSet resultSet = statement.executeQuery();
            
           
            if (resultSet.next()) {
                noteMoyenne = resultSet.getInt("note_moyenne");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return noteMoyenne;
    }
    
    
    
    
    
    // Méthode pour obtenir les jeux les plus populaires
    public List<Map<String, Object>> findRecommanded() {
        List<Map<String, Object>> jeuxList = new ArrayList<>();
        
        // Requête SQL
        String query = 
        		"""
        		SELECT j.id_jeux,j.titre,j.prix,j.resume, MIN(p.url_photo) AS url_photo,
        			COUNT(c.id_commentaire) AS nombre_commentaires_positifs FROM jeux j 
        			LEFT JOIN commentaireJeux cj ON j.id_jeux = cj.id_jeux 
        			LEFT JOIN commentaire c ON cj.id_commentaire = c.id_commentaire 
        			LEFT JOIN photo p ON j.id_jeux = p.id_jeux 
        			WHERE c.statut_com = 1
        			GROUP BY j.id_jeux, j.titre, j.prix, j.resume 
        			ORDER BY id_jeux ASC
        			LIMIT 3
        		""";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> jeuDetails = new HashMap<>();
                jeuDetails.put("id_jeux", resultSet.getInt("id_jeux"));
                jeuDetails.put("titre", resultSet.getString("titre"));
                jeuDetails.put("prix", resultSet.getDouble("prix"));
                jeuDetails.put("resume", resultSet.getString("resume"));
                
                // Ajoute la photo
                String photoUrl = resultSet.getString("url_photo");
                jeuDetails.put("url_photo", photoUrl != null ? photoUrl : "default.jpg"); // Photo par défaut
                
                // Ajoute le nombre de commentaires positifs
                int nombreCommentairePositifs = resultSet.getInt("nombre_commentaires_positifs");
                jeuDetails.put("nombre_commentaires_positifs", nombreCommentairePositifs);
                
                jeuxList.add(jeuDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return jeuxList;
    }

    
    
    
    
 // Méthode pour récupérer les commentaires avec les infos des utilisateurs et des jeux
    public List<CommentaireJeuUtilisateur> findCommentairesJeuxUtilisateurs() {
        List<CommentaireJeuUtilisateur> resultList = new ArrayList<>();

        
        String query = "SELECT c.id_commentaire, c.contenu, c.statut_com,c.accepte, u.nom AS utilisateur_nom, u.image AS utilisateur_image, "
                     + "j.id_jeux, j.titre AS jeu_titre, p.url_photo AS jeu_photo "
                     + "FROM commentaire c "
                     + "JOIN utilisateur u ON c.id_utilisateur = u.id_utilisateur "
                     + "JOIN commentairejeux cj ON c.id_commentaire = cj.id_commentaire "
                     + "JOIN jeux j ON cj.id_jeux = j.id_jeux "
                     + "LEFT JOIN (SELECT id_jeux, MIN(url_photo) AS url_photo FROM photo GROUP BY id_jeux) p "
                     + "ON j.id_jeux = p.id_jeux";

        try (
            Connection connection = ConnectionManager.getConnection();    
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idCommentaire = rs.getInt("id_commentaire");
                String contenu = rs.getString("contenu");
                String utilisateurNom = rs.getString("utilisateur_nom");
                String utilisateurImage = rs.getString("utilisateur_image");
                int idJeux = rs.getInt("id_jeux");  // Récupérer l'id_jeux
                String jeuTitre = rs.getString("jeu_titre");
                String jeuPhoto = rs.getString("jeu_photo");
                boolean statutCom = rs.getBoolean("statut_com");
                boolean accepation = rs.getBoolean("accepte");

                // Créer un objet CommentaireJeuUtilisateur avec tous les attributs, y compris id_jeux
                CommentaireJeuUtilisateur commentaire = new CommentaireJeuUtilisateur(
                    idCommentaire, contenu, utilisateurNom, utilisateurImage, idJeux, jeuTitre, jeuPhoto, statutCom,accepation
                );
                resultList.add(commentaire);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Gérer les erreurs
        }
        return resultList;
    }


    
    
    
    
 // Méthode pour insérer un commentaire et retourner le dernier ID inséré
    public int insertCommentaireAndGetId(Commentaire commentaire) {
        int generatedId = -1;
        String query = "INSERT INTO commentaire (contenu, statut_com, id_utilisateur) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            // Paramétrer la requête avec les données du commentaire
            ps.setString(1, commentaire.getContenu());
            ps.setBoolean(2, commentaire.isStatut_com());
            ps.setInt(3, commentaire.getId_utilisateur());

            // Exécuter l'insertion
            int affectedRows = ps.executeUpdate();

            // Vérifier si l'insertion a réussi et récupérer l'ID généré
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);  // Récupérer l'ID généré
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();  
        }

        return generatedId;  // Retourner l'ID généré ou -1 en cas d'échec
    }
    
    
    
    
    
    public Utilisateur login(String email, String password) {
        Utilisateur utilisateur = null;
        String query = "SELECT * FROM utilisateur WHERE email = ? AND password = ?"; // Assurez-vous que le nom du champ pour le mot de passe est correct

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, password); 

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                utilisateur = new Utilisateur();
                utilisateur.setId_utilisateur(resultSet.getInt("id_utilisateur"));
                utilisateur.setNom(resultSet.getString("nom"));
                utilisateur.setEmail(resultSet.getString("email"));
                utilisateur.setPassword(resultSet.getString("password"));
                utilisateur.setAdmin(resultSet.getBoolean("isAdmin"));
                utilisateur.setImage(resultSet.getString("image"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return utilisateur;
    }

    
    
    
    
	/*
	  ==================================================================
	  ================= DATA FOR DASHBOARD =============================
	  ==================================================================
	*/
    
    //CHIFFRE D’AFFAIRE
    public double getChiffreAffairesTotal() {
        String query = "SELECT SUM(total) AS chiffre_affaires FROM commande";
        double chiffreAffaires = 0.0;

        try (
        	 Connection connection =  ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                chiffreAffaires = resultSet.getDouble("chiffre_affaires");
            }

        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return chiffreAffaires;
    }
    
    //MOYENNE JEUX PAR COMMANDE
    public double getMoyenneJeuxParCommande() {
        String query = "SELECT AVG(d.quantite) AS moyenne_jeux_par_commande FROM detail d";
        double moyenne = 0.0;

        try (
        	 Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                moyenne = resultSet.getDouble("moyenne_jeux_par_commande");
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Gérer l'exception de manière appropriée (logs, etc.)
        }

        return moyenne;
    }
    
    //JEU AVEC LE PLUS DE COMMENTAIRE
    public Optional<Map<String, Object>> getGameWithMostComments() {
        String query = "SELECT j.titre, COUNT(cj.id_commentaire) AS total_commentaires " +
                       "FROM jeux j " +
                       "JOIN commentaireJeux cj ON j.id_jeux = cj.id_jeux " +
                       "GROUP BY j.titre " +
                       "ORDER BY total_commentaires DESC " +
                       "LIMIT 1";

        try (
        	 
        	 Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("titre", rs.getString("titre"));
                    result.put("total_commentaires", rs.getInt("total_commentaires"));
                    return Optional.of(result);  // Retourner le résultat sous forme de Map
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();  // Si aucun résultat n'est trouvé
    }

    
    
    //Nombre total de jeux par catégorie
    public List<Map<String, Object>> getTotalGamesByCategory() {
        String query = "SELECT c.genre, COUNT(j.id_jeux) AS total_jeux " +
                       "FROM jeux j " +
                       "JOIN categorie c ON j.id_categorie = c.id_categorie " +
                       "GROUP BY c.genre";

        List<Map<String, Object>> results = new ArrayList<>();

        try (
        		
        	 Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("genre", rs.getString("genre"));
                row.put("total_jeux", rs.getInt("total_jeux"));
                results.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    
    //ListingForDashbord
 // Méthode pour récupérer les commentaires avec les infos des utilisateurs et des jeux
    public List<CommentaireJeuUtilisateur> findCommentairesJeuxUtilisateursdb() {
        List<CommentaireJeuUtilisateur> resultList = new ArrayList<>();

        
        String query = "SELECT c.id_commentaire, c.contenu, c.statut_com, c.accepte, u.nom AS utilisateur_nom, u.image AS utilisateur_image, "
                + "j.id_jeux, j.titre AS jeu_titre, p.url_photo AS jeu_photo "
                + "FROM commentaire c "
                + "JOIN utilisateur u ON c.id_utilisateur = u.id_utilisateur "
                + "JOIN commentairejeux cj ON c.id_commentaire = cj.id_commentaire "
                + "JOIN jeux j ON cj.id_jeux = j.id_jeux "
                + "LEFT JOIN (SELECT id_jeux, MIN(url_photo) AS url_photo FROM photo GROUP BY id_jeux) p "
                + "ON j.id_jeux = p.id_jeux "
                + "LIMIT 5";

        try (
            Connection connection = ConnectionManager.getConnection();    
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idCommentaire = rs.getInt("id_commentaire");
                String contenu = rs.getString("contenu");
                String utilisateurNom = rs.getString("utilisateur_nom");
                String utilisateurImage = rs.getString("utilisateur_image");
                int idJeux = rs.getInt("id_jeux");  // Récupérer l'id_jeux
                String jeuTitre = rs.getString("jeu_titre");
                String jeuPhoto = rs.getString("jeu_photo");
                boolean statutCom = rs.getBoolean("statut_com");  
                boolean acceptation = rs.getBoolean("accepte");

                // Créer un objet CommentaireJeuUtilisateur avec tous les attributs, y compris id_jeux
                CommentaireJeuUtilisateur commentaire = new CommentaireJeuUtilisateur(
                    idCommentaire, contenu, utilisateurNom, utilisateurImage, idJeux, jeuTitre, jeuPhoto, statutCom,acceptation
                );
                resultList.add(commentaire);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Gérer les erreurs
        }
        return resultList;
    }
    
    
    
    
	/*
	  ==================================================================
	  ================= CHART FOR DASHBOARD ============================
	  ==================================================================
	*/
    
    //Data for Doughtnut chart 
    public List<Map<String, Object>> getRevenuesByDate() {
    	
        List<Map<String, Object>> result = new ArrayList<>();
        String query = "SELECT date_commande, SUM(total) AS total_revenu FROM commande GROUP BY date_commande ORDER BY date_commande";
        
        try (
        		
        	 Connection conn = ConnectionManager.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("date_commande", rs.getDate("date_commande"));
                row.put("total_revenu", rs.getDouble("total_revenu"));
                result.add(row);
            }
            
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        
        return result;
    }

    
    //Data Bar chart
    public List<Map<String, Object>> getJeuxWithCommentsAndEvaluations() {
        List<Map<String, Object>> results = new ArrayList<>();
        String query = "SELECT j.titre, " 
        			   + "(SELECT COUNT(*) FROM commentaireJeux WHERE id_jeux = j.id_jeux) AS total_commentaires, " 
        			   +"(SELECT COUNT(*) FROM evaluation WHERE id_jeux = j.id_jeux) AS total_evaluations " 
        			   +"FROM jeux j";

        try (
        		
        	 Connection conn =ConnectionManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("titre", rs.getString("titre"));
                row.put("total_commentaires", rs.getInt("total_commentaires"));
                row.put("total_evaluations", rs.getInt("total_evaluations"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log the error
        }

        return results;
    }

    
    
    //INSERER ET RETOURNER L ID JEUX DERNIEREMNT INSERE
    public int insertAndReturnId(T entity) throws SQLException {
        String tableName = entity.getClass().getSimpleName().toLowerCase(); // Nom de la table basé sur le nom de la classe
        String insertSQL = "INSERT INTO " + tableName + " (titre, date_sortie, langue, prix, quantite, id_categorie, resume) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (
        	 
        	 Connection connection = ConnectionManager.getConnection(); 
             PreparedStatement statement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            // Supposons que l'entity est de type `Jeux` (vous pouvez gérer cela plus génériquement si nécessaire)
            if (entity instanceof Jeux) {
                Jeux jeu = (Jeux) entity;

                // Remplir les paramètres de la requête préparée
                statement.setString(1, jeu.getTitre());
                statement.setString(2, jeu.getDate_sortie()); 
                statement.setString(3, jeu.getLangue());
                statement.setDouble(4, jeu.getPrix());
                statement.setInt(5, jeu.getQuantite());
                statement.setInt(6, jeu.getId_categorie());
                statement.setString(7, jeu.getResume());

                // Exécuter l'insertion
                int affectedRows = statement.executeUpdate();

                // Vérifier si l'insertion a réussi
                if (affectedRows > 0) {
                    // Récupérer l'ID généré automatiquement
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedId = generatedKeys.getInt(1); // Récupérer l'ID généré
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Erreur lors de l'ajout du jeu et la récupération de l'ID.");
        }

        return generatedId;
    }

    
    
    
    


    
    
    
    
    
    
}
