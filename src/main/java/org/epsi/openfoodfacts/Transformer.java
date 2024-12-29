package org.epsi.openfoodfacts;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.when;

/**
 * Cette classe fournit des méthodes pour les opérations de nettoyage des données.
 * This class provides methods for data cleaning operations.
 */
public class Transformer {

    /**
     * Nettoie les données en effectuant les opérations suivantes :
     * - Sélection et conversion des colonnes
     * - Suppression des valeurs manquantes
     * - Suppression des doublons
     * - Suppression des valeurs négatives
     * - Suppression des chaînes vides
     * - Suppression des caractères non-ASCII
     * - Suppression des lignes avec tous les nutriments à zéro
     * - Suppression des valeurs de nutriments hors plage
     * - Nettoyage des noms de pays
     *
     * @param df le DataFrame d'entrée / the input DataFrame
     * @param sparkSession la SparkSession / the SparkSession
     * @return Le DataFrame nettoyé / The cleaned DataFrame
     */
    public static Dataset<Row> cleanData(Dataset<Row> df, SparkSession sparkSession) {
        df = selectAndCastColumns(df);
        df = removeMissingValues(df);
        df = removeDuplicates(df);
        df = removeNegativeValues(df);
        df = removeEmptyStrings(df);
        df = removeNonAsciiCharacters(df);
        df = removeRowsWithAllZeroNutrients(df);
        df = removeOutOfRangeNutrientValues(df);
        df = cleanCountryNames(df, sparkSession);
        return df;
    }

    /**
     * Sélectionne et convertit les colonnes.
     * Select and cast columns.
     *
     * @param df Le Dataset d'entrée / The input Dataset
     * @return Le Dataset avec les colonnes sélectionnées et converties / The Dataset with selected and cast columns
     */
    private static Dataset<Row> selectAndCastColumns(Dataset<Row> df) {
        return df.select(
                        df.col("product_name").cast("string"),
                        df.col("categories_en").cast("string"),
                        df.col("countries_en").cast("string"),
                        df.col("proteins_100g").cast("float"),
                        df.col("carbohydrates_100g").cast("float"),
                        df.col("fat_100g").cast("float")
                ).withColumnRenamed("countries_en", "sold_countries")
                .withColumnRenamed("categories_en", "categories");
    }

    /**
     * Supprime les lignes avec des valeurs manquantes du dataset.
     * Removes rows with any missing values from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes contenant des valeurs manquantes supprimées / A new dataset with rows containing any missing values removed
     */
    private static Dataset<Row> removeMissingValues(Dataset<Row> df) {
        return df.na().drop();
    }

    /**
     * Supprime les lignes en double du dataset.
     * Removes duplicate rows from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes en double supprimées / A new dataset with duplicate rows removed
     */
    private static Dataset<Row> removeDuplicates(Dataset<Row> df) {
        return df.dropDuplicates();
    }

    /**
     * Supprime les lignes avec des valeurs de nutriments négatives du dataset.
     * Removes rows with negative nutrient values from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes contenant des valeurs de nutriments négatives supprimées / A new dataset with rows containing negative nutrient values removed
     */
    private static Dataset<Row> removeNegativeValues(Dataset<Row> df) {
        return df.filter(
                df.col("proteins_100g").geq(0)
                        .and(df.col("carbohydrates_100g").geq(0))
                        .and(df.col("fat_100g").geq(0))
        );
    }

    /**
     * Supprime les lignes avec des chaînes vides dans les colonnes spécifiées du dataset.
     * Removes rows with empty strings in specified columns from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes contenant des chaînes vides supprimées des colonnes spécifiées / A new dataset with rows containing empty strings removed from specified columns
     */
    private static Dataset<Row> removeEmptyStrings(Dataset<Row> df) {
        return df.filter(
                df.col("product_name").notEqual("")
                        .and(df.col("categories").notEqual(""))
                        .and(df.col("sold_countries").notEqual(""))
        );
    }

    /**
     * Supprime les lignes avec des caractères non-ASCII dans les colonnes spécifiées du dataset.
     * Removes rows with non-ASCII characters in specified columns from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes contenant des caractères non-ASCII supprimées des colonnes spécifiées / A new dataset with rows containing non-ASCII characters removed from specified columns
     */
    private static Dataset<Row> removeNonAsciiCharacters(Dataset<Row> df) {
        return df.filter(
                df.col("product_name").rlike("^[\\x00-\\x7F]*$")
                        .and(df.col("categories").rlike("^[\\x00-\\x7F]*$"))
                        .and(df.col("sold_countries").rlike("^[\\x00-\\x7F]*$"))
        );
    }

    /**
     * Supprime les lignes où toutes les valeurs de nutriments sont à zéro du dataset.
     * Removes rows where all nutrient values are zero from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes où toutes les valeurs de nutriments sont à zéro supprimées / A new dataset with rows where all nutrient values are zero removed
     */
    private static Dataset<Row> removeRowsWithAllZeroNutrients(Dataset<Row> df) {
        return df.filter(
                df.col("proteins_100g").gt(0)
                        .or(df.col("carbohydrates_100g").gt(0))
                        .or(df.col("fat_100g").gt(0))
        );
    }

    /**
     * Supprime les lignes où les valeurs de nutriments sont hors plage du dataset.
     * Removes rows where nutrient values are out of range from the dataset.
     *
     * @param df Le dataset à nettoyer / The dataset to clean
     * @return Un nouveau dataset avec les lignes où les valeurs de nutriments sont hors plage supprimées / A new dataset with rows where nutrient values are out of range removed
     */
    private static Dataset<Row> removeOutOfRangeNutrientValues(Dataset<Row> df) {
        return df.filter(
                df.col("proteins_100g").leq(100)
                        .and(df.col("carbohydrates_100g").leq(100))
                        .and(df.col("fat_100g").leq(100))
        );
    }

    /**
     * Nettoie les noms de pays dans le dataset en supprimant les noms de pays invalides.
     * Cleans country names in the dataset by removing invalid country names.
     *
     * @param dfSelectedColumns Le dataset avec les colonnes sélectionnées / The dataset with selected columns
     * @param sparkSession La session Spark / The Spark session
     * @return Un nouveau dataset avec les noms de pays nettoyés / A new dataset with cleaned country names
     */
    private static Dataset<Row> cleanCountryNames(Dataset<Row> dfSelectedColumns, SparkSession sparkSession) {
        // Divise la colonne "sold_countries" en mots individuels / Split the column "sold_countries" into individual words
        Dataset<String> listCountry = Utils.splitColumnIntoWords(dfSelectedColumns, "sold_countries", "country", ",");
        // Charge la liste des pays à partir du fichier CSV / Load the list of countries from the CSV file
        Dataset<Row> dfCountries = sparkSession.read()
                .format("csv")
                .option("header", false)
                .option("delimiter", ",")
                .load(Constants.DATA_FILE_COUNTRIES)
                .toDF("index", "country_id", "country_code_2", "country_code_3", "country_fr", "country_en");
        // Joint la liste des pays avec la liste des noms de pays / Join the list of countries with the list of country names
        Dataset<Row> dfMergedData = listCountry.join(dfCountries, listCountry.col("country").equalTo(dfCountries.col("country_en")), "left_outer");
        // Définit le Dataset des faux pays (c'est-à-dire des pays qui ne sont pas dans la liste des pays) / Define Dataset of false countries (i.e. countries that are not in the list of countries)
        Dataset<Row> dfFalseCountry = dfMergedData.filter(dfMergedData.col("country_en").isNull()).select("country");
        // Convertit le Dataset des faux pays en une liste de chaînes / Convert Dataset of false countries into a list of Strings
        List<String> falseCountryList = dfFalseCountry.as(Encoders.STRING()).collectAsList();
        // Crée une nouvelle colonne avec la chaîne des noms de pays nettoyés ou "null" si le pays n'est pas dans la liste des pays / Create a new column with the cleaned country names string or "null" if the country is not in the list of countries
        Dataset<Row> dfCleaned = dfSelectedColumns.withColumn("cleaned_sold_countries",
                expr("concat_ws(',', filter(split(sold_countries, ','), country -> !array_contains(array('" + String.join("','", falseCountryList) + "'), country)))"));
        // Remplace les chaînes vides de la colonne "cleaned_sold_countries" par la valeur "null" / Replace empty strings of the column "cleaned_sold_countries" with "null" value
        dfCleaned = dfCleaned.withColumn("cleaned_sold_countries",
                when(dfCleaned.col("cleaned_sold_countries").equalTo(""), null)
                        .otherwise(dfCleaned.col("cleaned_sold_countries")));
        // Supprime les lignes où la valeur de la colonne "cleaned_sold_countries" est null / Remove rows where value in column "cleaned_sold_countries" is null
        dfCleaned = dfCleaned.na().drop(new String[]{"cleaned_sold_countries"});
        // Supprime la colonne "sold_countries" car nous avons une version nettoyée de celle-ci / Drop the column "sold_countries" because we have a cleaned version of it
        dfCleaned = dfCleaned.drop("sold_countries");
        // Renomme la colonne "cleaned_sold_countries" en "sold_countries" / Rename the column "cleaned_sold_countries" to "sold_countries"
        dfCleaned = dfCleaned.withColumnRenamed("cleaned_sold_countries", "sold_countries");
        return dfCleaned;
    }
}