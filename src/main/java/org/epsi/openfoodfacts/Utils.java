package org.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

/**
 * Classe utilitaire contenant des méthodes d'assistance.
 * Utility class containing helper methods.
 */
public class Utils {

    /**
     * Divise une colonne contenant des chaînes délimitées en mots individuels et renvoie des mots uniques sous forme de Dataset de chaînes.
     * Split a column containing delimited strings into individual words and return unique words as a Dataset of Strings.
     *
     * @param df Dataset\<Row\> contenant les données à traiter. / Dataset\<Row\> containing the data to process.
     * @param columnNameInput Nom de la colonne à diviser. / Name of the column to split.
     * @param columnNameOutput Nom de la colonne contenant les mots uniques. / Name of the column containing the unique words.
     * @param delimiter Délimiteur utilisé pour diviser les chaînes. / Delimiter used to split the strings.
     * @return Dataset\<String\> contenant les mots uniques extraits de la colonne spécifiée. / Dataset\<String\> containing unique words extracted from the specified column.
     */
    public static Dataset<String> splitColumnIntoWords(Dataset<Row> df, String columnNameInput, String columnNameOutput ,String delimiter) {
        // Divise la colonne en mots individuels en utilisant le délimiteur spécifié / Split the column into individual words using the specified delimiter
        Dataset<Row> splitData = df.withColumn(columnNameInput, split(col(columnNameInput), delimiter));
        // Sélectionne les mots uniques / Select unique words
        Dataset<Row> uniqueWords = splitData.select(explode(col(columnNameInput)).as(columnNameOutput)).distinct();
        // Convertit les mots uniques en Dataset de chaînes / Convert unique words into a Dataset of Strings
        Dataset<String> listWords = uniqueWords.select(columnNameOutput).as(Encoders.STRING());
        return listWords;
    }
}