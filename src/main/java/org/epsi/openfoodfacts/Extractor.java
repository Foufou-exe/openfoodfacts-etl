package org.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 * Cette classe fournit des méthodes pour extraire des données de diverses sources.
 * This class provides methods for extracting data from various sources.
 */
public class Extractor {

    /**
     * Extrait des données d'un fichier CSV.
     * Extracts data from a CSV file.
     *
     * @param sparkSession La session Spark à utiliser pour lire les données. / The Spark session to use for reading data.
     * @param filePath Le chemin vers le fichier CSV. / The path to the CSV file.
     * @param delimiter Le délimiteur utilisé dans le fichier CSV. / The delimiter used in the CSV file.
     * @return Un Dataset contenant les données extraites. / A Dataset containing the extracted data.
     */
    public static Dataset<Row> extractFromCSV(SparkSession sparkSession, String filePath, String delimiter)
    {
        return sparkSession.read()
                .format("csv")
                .option("header", true)
                .option("delimiter", delimiter)
                .option("inferSchema", true)
                .load(filePath);
    }

    /**
     * Extrait des données d'une table de base de données.
     * Extracts data from a database table.
     *
     * @param sparkSession La session Spark à utiliser pour lire les données. / The Spark session to use for reading data.
     * @param dbHost Le nom d'hôte du serveur de base de données. / The host name of the database server.
     * @param dbUser Le nom d'utilisateur pour se connecter à la base de données. / The username for connecting to the database.
     * @param dbPassword Le mot de passe pour se connecter à la base de données. / The password for connecting to the database.
     * @param dbTable Le nom de la table à partir de laquelle extraire les données. / The name of the table from which to extract data.
     * @return Un Dataset contenant les données extraites. / A Dataset containing the extracted data.
     */
    public static Dataset<Row> extractFromDatabase(SparkSession sparkSession, String dbHost, String dbUser, String dbPassword, String dbTable)
    {
        return sparkSession.read()
                .format("jdbc")
                .option("url", dbHost)
                .option("dbtable", dbTable)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load();
    }
}