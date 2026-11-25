package com.agrogeorgia.ai;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class CropPrediction {

    private final String DATASET = "data/Crop_recommendation.csv";

    private Classifier classifier;
    private Instances structure;
    private boolean ready = false;

    private final Map<String, String> duration = new HashMap<>();

    public CropPrediction() {
        loadDurations();
        train();
    }

    private void loadDurations() {
        duration.put("rice", "120-150 days");
        duration.put("maize", "90-120 days");
        duration.put("chickpea", "90-120 days");
        duration.put("kidneybeans", "90-120 days");
        duration.put("pigeonpeas", "150-180 days");
        duration.put("mothbeans", "75-90 days");
        duration.put("mungbean", "60-75 days");
        duration.put("blackgram", "70-90 days");
        duration.put("lentil", "100-120 days");
        duration.put("pomegranate", "150-180 days");
        duration.put("banana", "270-365 days");
        duration.put("mango", "120-180 days");
        duration.put("grapes", "150-180 days");
        duration.put("watermelon", "80-100 days");
        duration.put("muskmelon", "80-100 days");
        duration.put("apple", "150-180 days");
        duration.put("orange", "180-240 days");
        duration.put("papaya", "180-270 days");
        duration.put("coconut", "6-12 months");
        duration.put("cotton", "150-180 days");
        duration.put("jute", "100-120 days");
        duration.put("coffee", "180-270 days");
        duration.put("millet", "90-120 days");
    }

    private void train() {
        try {
            File file = new File(DATASET);

            if (!file.exists()) {
                System.out.println("Dataset not found: " + file.getAbsolutePath());
                return;
            }

            System.out.println("Loading CSV dataset...");
            System.out.println("Dataset path: " + file.getAbsolutePath());

            CSVLoader loader = new CSVLoader();
            loader.setSource(file);

            Instances data = loader.getDataSet();

            if (data == null || data.numInstances() == 0) {
                System.out.println("Dataset is empty.");
                return;
            }

            System.out.println("Dataset loaded successfully.");
            System.out.println("Rows: " + data.numInstances());
            System.out.println("Columns: " + data.numAttributes());

            data.setClassIndex(data.numAttributes() - 1);

            RandomForest rf = new RandomForest();
            rf.setNumIterations(150);
            rf.setSeed(42);

            System.out.println("Training Random Forest model...");

            rf.buildClassifier(data);

            classifier = rf;
            structure = new Instances(data, 0);

            ready = true;

            System.out.println("AI model trained successfully.");
            System.out.println("Dataset rows: " + data.numInstances());
            System.out.println("Crop classes: " + data.numClasses());

        } catch (Exception e) {
            ready = false;
            System.out.println("AI training failed: " + e.getMessage());
        }
    }

    public String predict(double n, double p, double k, double temp,
                          double humidity, double ph, double rainfall) {

        if (ready) {
            try {

                double[] values = new double[structure.numAttributes()];

                for (int i = 0; i < structure.numAttributes() - 1; i++) {

                    Attribute attribute = structure.attribute(i);

                    String name = attribute.name()
                            .trim()
                            .toLowerCase();

                    if (name.equals("n")) {
                        values[i] = n;
                    }
                    else if (name.equals("p")) {
                        values[i] = p;
                    }
                    else if (name.equals("k")) {
                        values[i] = k;
                    }
                    else if (name.equals("temperature")) {
                        values[i] = temp;
                    }
                    else if (name.equals("temp")) {
                        values[i] = temp;
                    }
                    else if (name.equals("humidity")) {
                        values[i] = humidity;
                    }
                    else if (name.equals("ph")) {
                        values[i] = ph;
                    }
                    else if (name.equals("soil_ph")) {
                        values[i] = ph;
                    }
                    else if (name.equals("rainfall")) {
                        values[i] = rainfall;
                    }
                    else if (name.equals("rain")) {
                        values[i] = rainfall;
                    }
                    else {
                        values[i] = 0;
                    }
                }

                values[structure.classIndex()] = 0;

                DenseInstance instance =
                        new DenseInstance(1.0, values);

                instance.setDataset(structure);

                double result =
                        classifier.classifyInstance(instance);

                return structure.classAttribute()
                        .value((int) result);

            } catch (Exception e) {

                System.out.println(
                        "Prediction failed: " + e.getMessage()
                );
            }
        }

        return fallback(
                n,
                p,
                k,
                temp,
                humidity,
                ph,
                rainfall
        );
    }

    private String fallback(double n,
                            double p,
                            double k,
                            double temp,
                            double humidity,
                            double ph,
                            double rainfall) {

        if (ph < 5.5) {
            return "coffee";
        }

        if (ph > 8.0) {
            return "cotton";
        }

        if (rainfall > 180 && humidity > 70) {
            return "rice";
        }

        if (temp > 28 && rainfall < 100) {
            return "millet";
        }

        if (n > 100 && p > 40 && k > 40) {
            return "maize";
        }

        if (ph >= 6.0 && ph <= 7.5) {
            return "chickpea";
        }

        return "maize";
    }

    public String duration(String crop) {

        if (crop == null) {
            return "Refer to crop database";
        }

        String result = duration.get(crop.toLowerCase());

        if (result != null) {
            return result;
        }

        return "Refer to crop database";
    }

    public String status() {

        if (ready) {
            return "Trained Kaggle Random Forest model";
        }

        return "Demo fallback predictor";
    }
}