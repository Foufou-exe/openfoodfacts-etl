package org.epsi.openfoodfacts;

/**
 * Classe Constants pour stocker toutes les constantes utilisées dans l'application.
 * Constants class to store all the constants used in the application.
 */
public class Constants {
    // Chemin du fichier des produits / Path to the products file
    public static final String DATA_FILE_PRODUCTS = "data/en.openfoodfacts.org.products.csv";

    // Chemin du fichier des pays / Path to the countries file
    public static final String DATA_FILE_COUNTRIES = "data/countries-en.csv";

    // Chemin du fichier des utilisateurs / Path to the users file
    public static final String DATA_FILE_USERS = "data/users.csv";

    // Chemin du fichier des régimes / Path to the regimes file
    public static final String DATA_FILE_REGIMES = "data/regimes.csv";

    // Hôte de la base de données / Database host
    public static final String DB_HOST = "jdbc:mysql://localhost:3306/openfoodfacts-etl";

    // Utilisateur de la base de données / Database user
    public static final String DB_USER = "root";

    // Mot de passe de la base de données / Database password
    public static final String DB_PASSWORD = "";
}