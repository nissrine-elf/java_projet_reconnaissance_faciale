package com.example.projetv4;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class gestAdminUser {

    @FXML private TextField nomUser, prenUser;
    @FXML private ImageView imageView;
    @FXML private TableView<User> tableView;
    @FXML private TableColumn<User, String> nameColumn, prenomColumn;
    @FXML private Button addadmin;
    private FaceDetection faceDetection;
    private VideoCapture capture;
    private Timeline timeline;
    private FaceNet faceNet;
    private User u;
    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        List<User> luser=DatabaseManager.getAllUser();
        tableView.getItems().setAll(luser);

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           u=newValue;
     if (newValue != null) {
     nomUser.setText(newValue.getName());
     prenUser.setText(newValue.getPrenom());

  }
});



                    // Initialisation de la capture vidéo
        capture = new VideoCapture(0); // Utilisation de la caméra par défaut
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra.");
            return;
        }

        // Initialiser les objets de détection faciale et de reconnaissance (FaceNet)
        faceDetection = new FaceDetection("src/main/resources/haarcascade_frontalface_default.xml");
        faceNet = new FaceNet("src/main/resources/20180408-102900.pb");

        // Timeline pour capturer des images toutes les 0.088 secondes
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.088), e -> captureAndDisplay())
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Répéter indéfiniment
        timeline.play(); // Démarrer la capture vidéo
    }

    // Méthode de capture et d'affichage d'une image à chaque cycle de la Timeline
    private void captureAndDisplay() {
        Mat frame = new Mat();
        capture.read(frame); // Lire une image depuis la webcam
        if (!frame.empty()) {
            // Détecter les visages dans l'image
            frame = faceDetection.detectFaces(frame);

            // Convertir l'image de BGR (OpenCV) à RGB (JavaFX)
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

            // Convertir l'image OpenCV (Mat) en image JavaFX et l'afficher dans l'ImageView
            imageView.setImage(matToImage(frame));
        } else {
            System.out.println("Aucun cadre capturé.");
        }
    }

    // Méthode pour ajouter un utilisateur à la base de données
    @FXML
    private void addUserToDatabase() {
        String name = nomUser.getText();
        String prenom = prenUser.getText();
        if (name.isEmpty() || prenom.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // Récupérer l'image capturée depuis la webcam
        Mat capturedImage = getCapturedImage();

        // Détecter le visage dans l'image capturée
        Mat detectedFace = faceDetection.detectFaces(capturedImage);
        if (detectedFace.empty()) {
            showAlert("Erreur", "Aucun visage détecté. Essayez à nouveau.");
            return;
        }

        // Extraire l'empreinte faciale avec FaceNet
        float[] faceEmbedding = faceNet.getFaceEmbedding(detectedFace);
        if (faceEmbedding == null || faceEmbedding.length == 0) {
            showAlert("Erreur", "Impossible d'extraire l'empreinte faciale.");
            return;
        }

        // Convertir l'empreinte faciale en tableau de bytes pour l'enregistrement
        byte[] embeddingBytes = new byte[faceEmbedding.length * Float.BYTES];
        for (int i = 0; i < faceEmbedding.length; i++) {
            byte[] bytes = ByteBuffer.allocate(Float.BYTES).putFloat(faceEmbedding[i]).array();
            System.arraycopy(bytes, 0, embeddingBytes, i * Float.BYTES, Float.BYTES);
        }

        // Ajouter l'utilisateur avec l'empreinte faciale à la base de données
        DatabaseManager.addUser(name, prenom, embeddingBytes);

        List<User> ss=DatabaseManager.getAllUser();
        tableView.getItems().setAll(ss);
        // Afficher une alerte de succès
        showAlert("Succès", "Utilisateur ajouté avec succès !");
        DatabaseManager.getUsers();
    }

    // Méthode pour afficher une alerte
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour récupérer l'image capturée depuis la webcam
    private Mat getCapturedImage() {
        Mat frame = new Mat();
        capture.read(frame); // Lire une image de la caméra
        return frame;
    }

    // Méthode pour convertir une image OpenCV (Mat) en une image JavaFX
    private Image matToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, buffer); // Encoder l'image en format BMP
        return new Image(new ByteArrayInputStream(buffer.toArray())); // Convertir en Image JavaFX
    }

    // Méthode pour arrêter la capture et libérer les ressources
    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (faceNet != null) {
            faceNet.close(); // Libérer les ressources de FaceNet
        }
    }

    @FXML
    public void DeletUser(ActionEvent event){
        if (u!=null){
            DatabaseManager.deleteUser(u.getId());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("L'utilisateur a été supprimé avec succès.");
            alert.setTitle("supprimer user");
            alert.showAndWait();
            tableView.getItems().remove(u);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer.");
            alert.setTitle("Erreur");
            alert.showAndWait();
        }
        }
    public void addAdmin(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addadmin-view.fxml"));
            Scene scene = new Scene(loader.load(),800,600);
            Stage stage = (Stage) addadmin.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    }
