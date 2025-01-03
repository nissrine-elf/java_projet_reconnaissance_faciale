package com.example.projetv4;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class InspectTensorFlowModel {

    public static void main(String[] args) {
        String modelPath = "src/main/resources/20180408-102900.pb";  // Remplacez par le chemin de votre modèle .pb

        // Charger le modèle TensorFlow
        try (Graph graph = new Graph()) {
            byte[] graphBytes = Files.readAllBytes(Paths.get(modelPath));
            graph.importGraphDef(graphBytes);

            // Lister toutes les opérations dans le graphe
            System.out.println("Liste des opérations dans le modèle :");
            graph.operations().forEachRemaining(op -> {
                System.out.println("Opération : " + op.name());
            });
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du modèle : " + e.getMessage());
        }
    }
}
