package com.example.projetv4;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:access_management.db";

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                System.out.println("Mode WAL activé.");
            } catch (SQLException e) {
                System.out.println("Erreur lors de l'activation du mode WAL : " + e.getMessage());
            }
            return conn;
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données : " + e.getMessage());
            return null;
        }
    }


    public static void createTables() {
        // Créer les tables user et access_log
        String createUsersTable = "CREATE TABLE IF NOT EXISTS user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "prenom TEXT NOT NULL," +
                "face_embedding BLOB NOT NULL" +
                ");";

        String createAccessLogTable = "CREATE TABLE IF NOT EXISTS access_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "attempt_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "status TEXT," +
                "FOREIGN KEY(user_id) REFERENCES user(id)" +
                ");";
        String createAdminTable = "CREATE TABLE IF NOT EXISTS admin (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "password TEXT NOT NULL" +
                ");";


        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createAccessLogTable);
            stmt.execute(createAdminTable);
            System.out.println("Tables créées avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }


    // Ajouter un utilisateur
    public static void addUser(String name, String prenom, byte[] faceEmbedding) {
        String insertUserSQL = "INSERT INTO user (name, prenom, face_embedding,status) VALUES (?, ?, ?,?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, prenom);
            pstmt.setBytes(3, faceEmbedding);
            pstmt.setString(4, "accee");// L'empreinte faciale sous forme de tableau de bytes
            pstmt.executeUpdate();
            System.out.println("Utilisateur ajouté avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }

    public static void getUsers() {
        String selectSQL = "SELECT id, name, prenom, face_embedding FROM user";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String prenom = rs.getString("prenom");
                byte[] embeddingBytes = rs.getBytes("face_embedding");

                // Affichage des informations
                System.out.println("ID: " + id);
                System.out.println("Nom: " + name);
                System.out.println("prenom: " + prenom);

                // Vérifier si l'empreinte faciale est correctement enregistrée
                if (embeddingBytes != null) {
                    System.out.println("Face Embedding: " + embeddingBytes.length + " bytes.");
                } else {
                    System.out.println("Aucune empreinte faciale trouvée.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
    }


    // Enregistrer une tentative d'accès
    public static void logAccess(int userId, String status) {
        String insertLogSQL = "INSERT INTO access_log (user_id, status) VALUES (?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(insertLogSQL)) {
            pstmt.setInt(1, userId); // ID de l'utilisateur
            pstmt.setString(2, status); // Succès ou échec
            pstmt.executeUpdate();
            System.out.println("Tentative d'accès enregistrée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'enregistrement de la tentative d'accès : " + e.getMessage());
        }
    }

public static boolean adimnEx(String nom,String pas){
        boolean ver=false;
        try {Connection con=connect();
            PreparedStatement pst= connect().prepareStatement("select * from admin where name=? and password=?");
            pst.setString(1, nom); // ID de l'utilisateur
            pst.setString(2, pas); // Succès ou échec
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
               ver=true;

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return ver;
}
    public static void getAccessStats() {
        String statsSQL = "SELECT COUNT(*) AS total_attempts, " +
                "SUM(CASE WHEN status = 'success' THEN 1 ELSE 0 END) AS successes, " +
                "SUM(CASE WHEN status = 'failure' THEN 1 ELSE 0 END) AS failures " +
                "FROM access_log";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(statsSQL)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int totalAttempts = rs.getInt("total_attempts");
                int successes = rs.getInt("successes");
                int failures = rs.getInt("failures");
                System.out.println("Total des tentatives : " + totalAttempts);
                System.out.println("Succès : " + successes);
                System.out.println("Échecs : " + failures);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des statistiques : " + e.getMessage());
        }
    }
     public static List<User> getAllUser(){

         List<User> us=new ArrayList<>();
         Connection conn = connect();
         try{
             PreparedStatement pts=conn.prepareStatement("SELECT id, name, prenom FROM user");
             ResultSet rs=pts.executeQuery();
             while (rs.next()){

                 User uss=new User(rs.getInt("id"),rs.getString("name"),"user", rs.getString("prenom"));
                us.add(uss) ;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return us;
     }
public static   void deleteUser(int i){

    Connection conn = connect();
    try{
        PreparedStatement pts=conn.prepareStatement("delete from user  where id=?");
        pts.setInt(1,i);
        pts.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public static void addAdmin(String name, String password) {
        String insertAdminSQL = "INSERT INTO admin (name, password) VALUES (?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(insertAdminSQL)) {
            pstmt.setString(1, name);    // Nom de l'administrateur
            pstmt.setString(2, password); // Mot de passe de l'administrateur
            // Longitude
            pstmt.executeUpdate();
            System.out.println("Admin added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding admin: " + e.getMessage());
        }
    }
    public static List<byte[]> getAllFaceEmbeddings() {
        List<byte[]> embeddings = new ArrayList<>();
        String selectEmbeddingsSQL = "SELECT face_embedding FROM user";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(selectEmbeddingsSQL)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                byte[] embedding = rs.getBytes("face_embedding");
                embeddings.add(embedding);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des empreintes faciales : " + e.getMessage());
        }
        return embeddings;
    }
    public static float[] byteArrayToFloatArray(byte[] byteArray) {
        float[] floatArray = new float[byteArray.length / Float.BYTES];
        ByteBuffer.wrap(byteArray).asFloatBuffer().get(floatArray);
        return floatArray;
    }
}
