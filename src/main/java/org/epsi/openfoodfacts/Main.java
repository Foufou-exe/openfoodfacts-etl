package org.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;


/**
 * Classe principale pour exécuter le pipeline ETL.
 * Main class to run the ETL pipeline.
 */
public class Main {

    /**
     * La méthode principale pour exécuter le pipeline ETL.
     * The main method to run the ETL pipeline.
     *
     * @param args Arguments de la ligne de commande / Command line arguments
     */
    public static void main(String[] args) {
        // Initialiser la session Spark / Initialize Spark session
        SparkSession sparkSession = initializeSparkSession();

        // Extraire les produits du fichier CSV (Étape d'extraction) / Extract products from CSV file (Extract step)
        Dataset<Row> dfProducts = Extractor.extractFromCSV(sparkSession, Constants.DATA_FILE_PRODUCTS, "\t");

        // Extraire les utilisateurs du fichier CSV (Étape d'extraction) / Extract users from CSV file (Extract step)
        Dataset<Row> dfUsers = Extractor.extractFromCSV(sparkSession, Constants.DATA_FILE_USERS, ",");

        // Extraire les régimes du fichier CSV (Étape d'extraction) / Extract regimes from CSV file (Extract step)
        Dataset<Row> dfRegimes = Extractor.extractFromCSV(sparkSession, Constants.DATA_FILE_REGIMES, ",");

        // Nettoyer les données des produits (Étape de transformation) / Clean products data (Transform step)
        Dataset<Row> dfProductsCleaned = Transformer.cleanData(dfProducts, sparkSession);

        // Générer des menus hebdomadaires pour chaque utilisateur (Étape de génération) / Generate weekly menus for each user (Generation step)
        Dataset<Row> weeklyMenus = Generator.generateWeeklyMenu(sparkSession, dfUsers, dfRegimes, dfProductsCleaned);

        // AVERTISSEMENT: Le chargement est en mode ajout, donc si vous exécutez le programme plusieurs fois, vous aurez des données dupliquées
        // en raison des différentes FK, vous devez nettoyer la table avant de réexécuter le programme
        // WARNING: The load is in append mode, so if you run the program multiple times, you will have duplicate data
        // due to the different FK, you need to clean the table before running the program again
        // Insérer les régimes dans la base de données (Étape de chargement) / Insert regimes into database (Load step)
        Loader.loadToDatabase(dfRegimes, Constants.DB_HOST, Constants.DB_USER, Constants.DB_PASSWORD, "regime");

        // Insérer les utilisateurs dans la base de données (Étape de chargement) / Insert users into database (Load step)
        Loader.loadToDatabase(dfUsers, Constants.DB_HOST, Constants.DB_USER, Constants.DB_PASSWORD, "user");

        // Insérer les menus hebdomadaires dans la base de données (Étape de chargement) / Insert weekly menus into database (Load step)
        Loader.loadToDatabase(weeklyMenus, Constants.DB_HOST, Constants.DB_USER, Constants.DB_PASSWORD, "daily_menu");

        // Fermer la session Spark / Close the Spark session
        sparkSession.close();
    }

    /**
     * Initialise et retourne une session Spark.
     * Initializes and returns a Spark session.
     *
     * @return SparkSession La session Spark initialisée / The initialized Spark session
     */
    private static SparkSession initializeSparkSession() {
        return SparkSession.builder()
                .appName("openfoodfacts-etl")
                .master("local[4]") // TODO: Veuillez faire attention ici et changer le nombre de cœurs selon votre machine / Please be careful here and change the number of cores according to your machine
                .getOrCreate();
    }
}