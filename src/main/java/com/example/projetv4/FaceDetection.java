package com.example.projetv4;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.core.Size;

public class FaceDetection {

    private CascadeClassifier faceCascade;

    public FaceDetection(String cascadeFile) {
        // Charger le classificateur Haar Cascade pour la détection des visages
        faceCascade = new CascadeClassifier(cascadeFile);
        if (faceCascade.empty()) {
            System.out.println("Erreur : Le classificateur Haar Cascade n'a pas pu être chargé.");
        } else {
            System.out.println("Classificateur Haar Cascade chargé avec succès.");
        }
    }

    public Mat detectFaces(Mat frame) {
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY); // Conversion en niveau de gris

        // Créer un objet MatOfRect pour stocker les visages détectés
        MatOfRect facesArray = new MatOfRect();

        // Détecter les visages
        faceCascade.detectMultiScale(grayFrame, facesArray, 1.1, 2, 0, new Size(30, 30), new Size());

        // Dessiner des rectangles autour des visages détectés
        for (Rect face : facesArray.toArray()) {
            Imgproc.rectangle(frame, face.tl(), face.br(), new Scalar(0, 255, 0), 3);
        }

        return frame; // Retourner l'image avec les visages détectés
    }
}
