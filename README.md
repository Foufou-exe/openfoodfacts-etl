<div align="center">
  <img src="https://world.openfoodfacts.org/images/logos/logo-variants/RVB_HORIZONTAL_BLACK_BG_OFF.svg" alt="off-banner" />
</div>


# 🍽️ Projet ETL Openfoodfacts

Ce projet consiste à mettre en place une solution ETL (Extract, Transform, Load) distribuée pour générer aléatoirement des menus alimentaires adaptés aux besoins des utilisateurs, en utilisant les données disponibles sur OpenFoodFacts.

## 📝 Description du Projet

Le client a exprimé le besoin de créer un système capable de générer des menus alimentaires équilibrés sur une semaine, conformément aux régimes alimentaires sélectionnés par les utilisateurs. Les menus doivent respecter les seuils nutritionnels spécifiques à chaque régime, tout en tenant compte des critères tels que les lipides, les glucides, le sodium, etc.

## 🎯 Objectifs

1. Collecter les données depuis OpenFoodFacts. 
2. Créer des sources de données supplémentaires pour les régimes alimentaires et les utilisateurs. 
3. Assurer la qualité des données en appliquant des critères de sélection. 
4. Transformer les données pour générer des menus alimentaires équilibrés en fonction des régimes alimentaires des utilisateurs. 
5. Charger les menus générés dans un Data Warehouse (DWH).

## 🛠️ Spécificités Techniques

- **Langage de programmation :** Java
- **Outils ETL :** Apache Spark
- **Source des données :** OpenFoodFacts (https://fr.openfoodfacts.org/data)
- **Bases de données :** MySQL (pour le DWH)

## 📋 Prérequis
Pour pouvoir exécuter ce projet, vous devez avoir installé les logiciels suivants :
- [Docker](https://www.docker.com/)
- [Java 20](https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html)
