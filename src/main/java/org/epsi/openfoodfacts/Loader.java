package org.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

/**
 * Cette classe est responsable du chargement des données dans une table de base de données.
 * This class is responsible for loading data into a database table.
 */
public class Loader {

    /**
     * Charge les données dans une table de base de données.
     * Loads data into a database table.
     *
     * @param df Le Dataset à insérer dans la base de données. / The Dataset to insert into the database.
     * @param dbHost Le nom d'hôte du serveur de base de données. / The host name of the database server.
     * @param dbUser Le nom d'utilisateur pour se connecter à la base de données. / The username for connecting to the database.
     * @param dbPassword Le mot de passe pour se connecter à la base de données. / The password for connecting to the database.
     * @param dbTable Le nom de la table dans laquelle insérer les données. / The name of the table into which to insert data.
     */
    public static void loadToDatabase(Dataset<Row> df, String dbHost, String dbUser, String dbPassword, String dbTable) {
        df.write()
                .format("jdbc")
                .option("url", dbHost)
                .option("dbtable", dbTable)
                .option("user", dbUser)
                .option("password", dbPassword)
                .mode(SaveMode.Append)
                .save();
    }
}