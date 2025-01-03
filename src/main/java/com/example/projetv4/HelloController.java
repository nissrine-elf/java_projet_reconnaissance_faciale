package com.example.projetv4;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

public class HelloController {

    @FXML
    private Button signIn;
    @FXML
    private Button starCam,gestUser;
    @FXML private TextField adminUsername ;
    @FXML private TextField adminPassword ;
    private VideoCapture capture;
    @FXML
    private ImageView imageView;
    private Timeline timeline;
    private FaceDetection faceDetection;
    private FaceNet faceNet;
    private Rectangle border;
    private Label nameLabel;

    @FXML
    void handleGererAdmin() {
        String nom=adminUsername.getText();
        String  pas=adminPassword.getText();
        boolean ver=DatabaseManager.adimnEx(nom,pas);

        if (ver){ try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
            Scene scene = new Scene(loader.load(),800,600);
            Stage stage = (Stage) signIn.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();}}
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("verification");
            alert.setContentText("Veuillez remplir correctemet tous les champs");
            alert.showAndWait();

        }

       }
       @FXML
   void  gestUser(){
           try {
               FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
               Scene scene = new Scene(loader.load(),800,600);
               Stage stage = (Stage) gestUser.getScene().getWindow();
               stage.setScene(scene);
               stage.show();
           } catch (Exception e) {
               e.printStackTrace();
           }




       }
    @FXML
    public void handleGererUser() {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Initialize the webcam capture
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Erreur : Impossible d'ouvrir la caméra.");
            return;
        }

        // Create an ImageView to display the captured images
        imageView = new ImageView();
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        // Create a border rectangle around the ImageView
        border = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        border.setStrokeWidth(5);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.GRAY); // Initial border color

        // Create a label to display the name of the authorized user
        nameLabel = new Label();
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        // Create a layout for displaying the webcam feed
        StackPane root = new StackPane();
        root.getChildren().addAll(imageView, border, nameLabel);
        Scene scene = new Scene(root, 400, 400);

        // Initialize face detection and face recognition
        faceDetection = new FaceDetection("src/main/resources/haarcascade_frontalface_default.xml");
        faceNet = new FaceNet("src/main/resources/20180408-102900.pb");

        // Initialize counters and flags for detecting a stable face
        final int[] stableFaceFrames = {0};
        final int requiredStableFrames = 30; // Number of frames to wait for stable detection (e.g., 1 second at 30 FPS)

        // Create a timeline to capture and update the frame at regular intervals
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.033), e -> {  // Update rate (1/30 FPS = 0.033 seconds)
                    Mat frame = new Mat();
                    capture.read(frame); // Read the next frame from the webcam
                    if (!frame.empty()) {
                        // Detect faces in the frame
                        frame = faceDetection.detectFaces(frame);

                        // Convert BGR image (OpenCV) to RGB (JavaFX)
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

                        // Convert Mat to JavaFX Image and display it
                        imageView.setImage(matToImage(frame));

                        // If a face is detected, proceed to identify the user
                        float[] faceEmbedding = faceNet.getFaceEmbedding(frame);
                        if (faceEmbedding != null) {
                            stableFaceFrames[0]++; // Increment the stable face frame count
                            // Check if enough frames have passed for a stable detection
                            if (stableFaceFrames[0] >= requiredStableFrames) {
                                // Check if the face embedding matches a user in the database
                                boolean isAuthorized = authorizeUser(faceEmbedding);

                                // Change border color and display user name based on authorization result
                                if (isAuthorized) {
                                    border.setStroke(Color.GREEN);  // Authorized -> Green border
                                    nameLabel.setText("Utilisateur autorisé !");  // Display authorized message
                                    System.out.println("Utilisateur autorisé !");
                                    showAlert("Accès autorisé", "Vous avez été authentifié avec succès.");
                                    timeline.stop(); // Stop the frame capture once user is authorized
                                } else {
                                    border.setStroke(Color.RED);    // Not authorized -> Red border
                                    nameLabel.setText("Utilisateur non autorisé.");  // Display unauthorized message
                                    System.out.println("Utilisateur non autorisé.");
                                    showAlert("Accès refusé", "Utilisateur non autorisé.");
                                    timeline.stop(); // Stop the frame capture if user is not authorized
                                }
                            }
                        }
                    } else {
                        System.out.println("Aucun cadre capturé.");
                    }
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat indefinitely
        timeline.play(); // Start capturing frames

        // Set the scene and show the window
        Stage stage = new Stage();
        stage.setTitle("Webcam Capture");
        stage.setScene(scene);
        stage.show();
    }

    // Show an alert for error/success
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }




    private boolean authorizeUser(float[] extractedEmbedding) {
        List<byte[]> storedEmbeddings = DatabaseManager.getAllFaceEmbeddings(); // Retrieve all face embeddings from the database

        // Compare the extracted embedding with those stored in the database
        for (byte[] storedEmbeddingBytes : storedEmbeddings) {
            float[] storedEmbedding = new float[extractedEmbedding.length];
            for (int i = 0; i < storedEmbedding.length; i++) {
                storedEmbedding[i] = ByteBuffer.wrap(storedEmbeddingBytes, i * Float.BYTES, Float.BYTES).getFloat();
            }

            // Calculate the Euclidean distance between the two embeddings
            double distance = calculateEuclideanDistance(extractedEmbedding, storedEmbedding);
            if (distance < 0.22) { // Similarity threshold
                return true; // The embedding is similar to an authorized user's
            }
        }
        return false; // No matching user found
    }

    // Method to calculate the Euclidean distance between two embeddings
    private double calculateEuclideanDistance(float[] embedding1, float[] embedding2) {
        double sum = 0.0;
        for (int i = 0; i < embedding1.length; i++) {
            sum += Math.pow(embedding1[i] - embedding2[i], 2);
        }
        return Math.sqrt(sum);
    }

    // Method to convert OpenCV Mat to JavaFX Image
    private Image matToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, buffer); // Encode the image in BMP format
        return new Image(new ByteArrayInputStream(buffer.toArray())); // Convert to JavaFX Image
    }

    // Show an alert for error/success


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


}