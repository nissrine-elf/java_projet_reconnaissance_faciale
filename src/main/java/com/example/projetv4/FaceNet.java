package com.example.projetv4;

import org.opencv.core.*;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.opencv.imgproc.Imgproc;

import java.nio.FloatBuffer;

public class FaceNet {

    private Graph graph;
    private Session session;

    public FaceNet(String modelPath) {
        // Charger le modèle pré-entraîné FaceNet
        graph = new Graph();
        try {
            byte[] graphBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(modelPath));
            graph.importGraphDef(graphBytes);
            session = new Session(graph);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float[] getFaceEmbedding(Mat face) {
        // Redimensionner l'image à 160x160 pixels
        Imgproc.resize(face, face, new Size(160, 160));

        // Si l'image n'a pas 3 canaux (RGB), la convertir
        if (face.channels() != 3) {
            Mat convertedFace = new Mat();
            Imgproc.cvtColor(face, convertedFace, Imgproc.COLOR_GRAY2BGR); // Convertir en 3 canaux si l'image est en niveaux de gris
            face = convertedFace;
        }

        // Convertir l'image en flottant (float) pour TensorFlow
        face.convertTo(face, CvType.CV_32F);

        // Normaliser les valeurs des pixels (diviser par 255)
        Mat normalizedFace = new Mat();
        Core.multiply(face, new Scalar(1.0 / 255.0), normalizedFace); // Normalisation des valeurs des pixels (diviser par 255)

        // Créer un tableau de float[] pour les données d'entrée
        FloatBuffer buffer = FloatBuffer.allocate((int)(normalizedFace.total() * normalizedFace.channels()));
        normalizedFace.get(0, 0, buffer.array()); // Assurez-vous que nous utilisons les bons canaux ici
        Tensor<Float> inputTensor = Tensor.create(new long[]{1, 160, 160, 3}, buffer); // Shape [1, 160, 160, 3] pour l'entrée FaceNet

        // Créer un tensor pour le placeholder 'phase_train' avec la valeur 'false'
        Tensor<Boolean> phaseTrainTensor = Tensor.create(Boolean.FALSE, Boolean.class); // Créer un tensor de type Boolean avec la valeur false

        // Exécuter le modèle pour obtenir l'empreinte faciale
        Tensor<Float> resultTensor = session.runner()
                .feed("input", inputTensor)  // Spécifiez le nom du tensor d'entrée
                .feed("phase_train", phaseTrainTensor)  // Ajouter le feed pour phase_train
                .fetch("embeddings")         // Spécifiez le nom de la sortie des embeddings
                .run()
                .get(0)
                .expect(Float.class);

        // Convertir le résultat en tableau float[]
        float[] embeddings = new float[512]; // FaceNet génère un embedding de taille 128
        resultTensor.copyTo(new float[1][512]);  // Copie du résultat dans un tableau 2D [1][128]

        // Extraire les valeurs du tableau 2D et les mettre dans un tableau 1D
        float[] flattenedEmbeddings = new float[512];
        System.arraycopy(resultTensor.copyTo(new float[1][512])[0], 0, flattenedEmbeddings, 0, 128);

        return flattenedEmbeddings;
    }






    public void close() {
        session.close();
        graph.close();
    }
}
