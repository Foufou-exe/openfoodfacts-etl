package org.epsi.openfoodfacts;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe est responsable de la génération de menus hebdomadaires pour chaque utilisateur en fonction de leur régime et de leurs préférences.
 * This class is responsible for generating weekly menus for each user based on their regime and preferences.
 */
public class Generator {

    /**
     * Définition du schéma pour les menus hebdomadaires.
     * Ce schéma inclut des champs pour l'ID utilisateur, le jour, le nom du produit pour le petit-déjeuner, le déjeuner et le dîner.
     * Schema definition for weekly menus.
     * This schema includes fields for user ID, day, breakfast product name, lunch product name, and dinner product name.
     */
    private static final StructType schemaWeeklyMenus = DataTypes.createStructType(new StructField[]{
            DataTypes.createStructField("user_id", DataTypes.IntegerType, false),
            DataTypes.createStructField("day", DataTypes.IntegerType, false),
            DataTypes.createStructField("breakfast_product_name", DataTypes.StringType, false),
            DataTypes.createStructField("lunch_product_name", DataTypes.StringType, false),
            DataTypes.createStructField("dinner_product_name", DataTypes.StringType, false)
    });

    /**
     * Génère des menus hebdomadaires pour chaque utilisateur en fonction de leur régime et de leurs préférences.
     * Generates weekly menus for each user based on their regime and preferences.
     *
     * @param sparkSession La session Spark à utiliser pour générer les menus. / The Spark session to use for generating the menus.
     * @param dfUsers Le dataset contenant les informations des utilisateurs. / The dataset containing user information.
     * @param dfRegimes Le dataset contenant les informations des régimes. / The dataset containing regime information.
     * @param dfProductsCleaned Le dataset contenant les informations des produits nettoyés. / The dataset containing cleaned product information.
     * @return Un dataset contenant les menus hebdomadaires générés. / A dataset containing the generated weekly menus.
     */
    public static Dataset<Row> generateWeeklyMenu(SparkSession sparkSession, Dataset<Row> dfUsers, Dataset<Row> dfRegimes, Dataset<Row> dfProductsCleaned) {
        List<Row> menuRows = new ArrayList<>();
        List<Row> userRows = dfUsers.collectAsList();

        for (Row userRow : userRows) {
            // Obtenir l'ID utilisateur et l'ID du régime / Get the user ID and regime ID
            int userId = userRow.getInt(userRow.fieldIndex("user_id"));
            int userRegimeId = userRow.getInt(userRow.fieldIndex("regime_id"));
            String userCountry = userRow.getString(userRow.fieldIndex("country"));

            // Obtenir le plan de régime pour l'utilisateur / Get the regime plan for the user
            Row rowUserRegimeInfo = dfRegimes.filter(dfRegimes.col("regime_id").equalTo(userRegimeId)).first();

            // Générer un menu quotidien pour chaque jour de la semaine / Generate a daily menu for each day of the week
            for (int day = 1; day <= 7; day++) {
                Row dailyMenu = generateDailyMenu(rowUserRegimeInfo, dfProductsCleaned, userCountry);
                Row menuRow = RowFactory.create(userId, day, dailyMenu.getAs(0), dailyMenu.getAs(1), dailyMenu.getAs(2));
                menuRows.add(menuRow);
            }
        }
        return sparkSession.createDataFrame(menuRows, schemaWeeklyMenus);
    }

    /**
     * Génère un menu quotidien en fonction du plan de régime et des produits disponibles.
     * Generates a daily menu based on the regime plan and the available products.
     *
     * @param regimePlan Le plan de régime pour l'utilisateur. / The regime plan for the user.
     * @param dfAvailableProducts Le dataset contenant les produits disponibles. / The dataset containing available products.
     * @param userCountry Le pays de l'utilisateur. / The country of the user.
     * @return Une ligne contenant les noms des produits pour le petit-déjeuner, le déjeuner et le dîner. / A row containing the names of the products for breakfast, lunch, and dinner.
     */
    public static Row generateDailyMenu(Row regimePlan, Dataset<Row> dfAvailableProducts, String userCountry) {
        double maxCarbohydratesPerDay = regimePlan.getAs("max_carbohydrates_g_day");
        double maxProteinsPerDay = regimePlan.getAs("max_proteins_g_day");
        double maxFatPerDay = regimePlan.getAs("max_fat_g_day");

        Dataset<Row> dfFilteredProducts = dfAvailableProducts.filter(
                dfAvailableProducts.col("carbohydrates_100g").leq(maxCarbohydratesPerDay)
                        .and(dfAvailableProducts.col("proteins_100g").leq(maxProteinsPerDay))
                        .and(dfAvailableProducts.col("fat_100g").leq(maxFatPerDay)
                        .and(dfAvailableProducts.col("sold_countries").contains(userCountry)))
        );
        // TODO: Attention cette étape effectue une transformation pour échantillonner les données,
        //  ce qui nécessite de scanner l'ensemble du dataset plusieurs fois (problèmes de performance)
        // TODO: Warning this stage performs a transformation to sample the data,
        //  which requires the entire dataset to be scanned several times (performance issues)
        Row breakfast = dfFilteredProducts.sample(false, 0.1).first();
        Row lunch = dfFilteredProducts.sample(false, 0.1).first();
        Row dinner = dfFilteredProducts.sample(false, 0.1).first();

        String breakfastProductName = breakfast.getAs("product_name");
        String lunchProductName = lunch.getAs("product_name");
        String dinnerProductName = dinner.getAs("product_name");

        return RowFactory.create(breakfastProductName, lunchProductName, dinnerProductName);
    }
}