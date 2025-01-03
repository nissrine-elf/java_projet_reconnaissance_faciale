package com.example.projetv4;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class WebcamCaptureApp extends Application {

    private VideoCapture capture;
    private ImageView imageView;
    private Timeline timeline;
    private FaceDetection faceDetection;
    private TextField nameField;
    private TextField statusField;
    private Button addButton;
    private FaceNet faceNet;

    @Override
    public void start(Stage primaryStage) {
        // Charger la bibliothèque native OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DatabaseManager.createTables();
        // Créer un ImageView pour afficher les images capturées
        imageView = new ImageView();
        imageView.setFitWidth(300); // Largeur de l'image
        imageView.setFitHeight(300); // Hauteur de l'image
        imageView.setPreserveRatio(true); // Maintenir le ratio

        // Créer un formulaire pour saisir le nom et le statut de l'utilisateur
        nameField = new TextField();
        nameField.setPromptText("Nom de l'utilisateur");

        statusField = new TextField();
        statusField.setPromptText("Statut de l'utilisateur");

        // Bouton pour ajouter l'utilisateur
        addButton = new Button("Ajouter l'utilisateur");
        addButton.setOnAction(e -> addUserToDatabase());

        // Créer un layout pour les champs de texte et le bouton
        VBox formLayout = new VBox(10, nameField, statusField, addButton);

        // Créer un layout principal
        StackPane root = new StackPane(imageView);
        root.getChildren().add(formLayout);
        Scene scene = new Scene(root, 640, 480);
        primaryStage.setTitle("Webcam Capture with OpenCV");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialiser la capture de la webcam
        capture = new VideoCapture(0); // 0 pour la caméra par défaut
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra.");
            return;
        }

        // Initialiser l'objet FaceDetection avec le chemin du classificateur
        faceDetection = new FaceDetection("src/main/resources/haarcascade_frontalface_default.xml");

        // Initialiser FaceNet avec le modèle pré-entraîné
        faceNet = new FaceNet("src/main/resources/20180408-102900.pb");

        // Timeline pour capturer des images à intervalles réguliers
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.088), e -> {
                    Mat frame = new Mat();
                    capture.read(frame); // Lire une image
                    if (!frame.empty()) {
                        // Détecter les visages
                        frame = faceDetection.detectFaces(frame);

                        // Convertir l'image BGR (OpenCV) en RGB (JavaFX)
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

                        // Convertir Mat en Image JavaFX et l'afficher
                        imageView.setImage(matToImage(frame));
                    } else {
                        System.out.println("Aucun cadre capturé.");
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE); // Répéter indéfiniment
        timeline.play(); // Démarrer la capture
    }

    @Override
    public void stop() {
        // Libérer les ressources lorsque l'application est fermée
        if (timeline != null) {
            timeline.stop();
        }
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (faceNet != null) {
            faceNet.close();  // Libération des ressources de TensorFlow si possible
        }
    }


    // Méthode pour ajouter un utilisateur à la base de données
    private void addUserToDatabase() {
        // Vérifier si le formulaire est rempli
        String name = nameField.getText();
        String status = statusField.getText();
        if (name.isEmpty() || status.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // Récupérer l'image capturée depuis la webcam
        Mat capturedImage = getCapturedImage();

        // Détection du visage dans l'image capturée
        Mat detectedFace = faceDetection.detectFaces(capturedImage);

        if (detectedFace.empty()) {
            showAlert("Erreur", "Aucun visage détecté. Essayez à nouveau.");
            return;
        }

        // Extraire l'empreinte faciale avec FaceNet
        float[] faceEmbedding = faceNet.getFaceEmbedding(detectedFace);

        // Vérifier si l'empreinte faciale a été correctement extraite
        if (faceEmbedding == null || faceEmbedding.length == 0) {
            showAlert("Erreur", "Impossible d'extraire l'empreinte faciale.");
            return;
        }

        // Convertir l'empreinte faciale en tableau de bytes
        byte[] embeddingBytes = new byte[faceEmbedding.length * Float.BYTES];
        for (int i = 0; i < faceEmbedding.length; i++) {
            byte[] bytes = ByteBuffer.allocate(Float.BYTES).putFloat(faceEmbedding[i]).array();
            System.arraycopy(bytes, 0, embeddingBytes, i * Float.BYTES, Float.BYTES);
        }

        // Ajouter l'utilisateur avec l'empreinte faciale à la base de données
        DatabaseManager.addUser(name, status, embeddingBytes);

        // Afficher un message de succès
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

    private byte[] matToByteArray(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        // Encoder l'image en format BMP
        Imgcodecs.imencode(".bmp", mat, matOfByte);
        return matOfByte.toArray(); // Retourner le tableau de bytes
    }

    // Méthode pour récupérer l'image capturée de la webcam
    private Mat getCapturedImage() {
        Mat frame = new Mat();
        capture.read(frame);
        return frame;
    }

    // Méthode pour convertir un Mat OpenCV en une Image JavaFX
    private Image matToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, buffer); // Encoder l'image au format BMP
        return new Image(new ByteArrayInputStream(buffer.toArray())); // Convertir en Image JavaFX
    }

    public static void main(String[] args) {
        launch(args);
    }
}
