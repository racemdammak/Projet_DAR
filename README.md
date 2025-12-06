# MiniCloud - Système de Stockage en Nuage

MiniCloud est un système de stockage en nuage simple et distribué, composé d'un client web React et d'un serveur backend Java utilisant CORBA pour la gestion des fichiers. Le système permet d'uploader, télécharger, lister et supprimer des fichiers via une interface utilisateur intuitive.

## Architecture du Système

Le projet est divisé en deux composants principaux :

1. **cloud-client** : Application React pour l'interface utilisateur
2. **MiniCloud_CORBA** : Serveur Java utilisant CORBA pour la logique métier et une API REST (SparkJava) pour la communication avec le client

### Flux de Données
- Le client React communique avec le serveur sur le port 4567
- Le serveur REST utilise CORBA pour accéder aux services de stockage (port 1050)

## Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Node.js** (version 16 ou supérieure) pour le client React
- **Java JDK** (version 8 ou supérieure) pour le serveur CORBA
- **Maven** pour la gestion des dépendances Java

### Installation des Prérequis

#### Sur Windows
1. Téléchargez et installez Node.js depuis [nodejs.org](https://nodejs.org/)
2. Téléchargez et installez Java JDK depuis [oracle.com](https://www.oracle.com/java/technologies/javase-downloads.html)
3. Téléchargez et installez Maven depuis [maven.apache.org](https://maven.apache.org/download.cgi)
4. Ajoutez Java et Maven à votre PATH système

## Installation et Configuration

### Étape 1 : Cloner le Repository

```bash
git clone <url-du-repository>
cd mini-cloud
```

### Étape 2 : Configuration du Serveur CORBA

1. Naviguez vers le répertoire du serveur :
```bash
cd MiniCloud_CORBA
```

2. Compilez le projet avec Maven :
```bash
mvn clean compile
```

### Étape 3 : Configuration du Client React

1. Ouvrez un nouveau terminal et naviguez vers le répertoire du client :
```bash
cd cloud-client
```

2. Installez les dépendances Node.js :
```bash
npm install
```

## Démarrage du Système

Le système doit être démarré dans un ordre spécifique : serveur CORBA en premier, puis serveur REST, enfin le client React.

### Étape 1 : Démarrer le Serveur CORBA

1. Ouvrez un terminal dans `MiniCloud_CORBA`
2. Démarrez le serveur CORBA :
```bash
mvn exec:java -Dexec.mainClass="cloud.server.CloudServer"
```

Le serveur CORBA démarrera sur le port 1050 et créera automatiquement le répertoire `cloud_storage` pour stocker les fichiers.

### Étape 2 : Démarrer le Serveur des api

1. Ouvrez un deuxième terminal dans `MiniCloud_CORBA`
2. Démarrez le serveur:
```bash
mvn exec:java -Dexec.mainClass="cloud.server.CloudRestServer"
```

Le serveur des api démarrera sur le port 4567 et se connectera au service CORBA.

### Étape 3 : Démarrer le Client React

1. Ouvrez un troisième terminal dans `cloud-client`
2. Démarrez l'application React :
```bash
npm start
```

L'application sera accessible à l'adresse `http://localhost:3000`

## Utilisation

### Interface Utilisateur

Une fois l'application démarrée :

1. **Upload de fichiers** :
   - Glissez-déposez des fichiers dans la zone dédiée
   - Ou cliquez pour sélectionner des fichiers
   - Les fichiers sont automatiquement uploadés vers le serveur

2. **Gestion des fichiers** :
   - La liste des fichiers disponibles s'affiche automatiquement
   - Cliquez sur "Télécharger" pour récupérer un fichier
   - Cliquez sur "Supprimer" pour effacer un fichier

3. **Notifications** :
   - Des toasts apparaissent pour confirmer les actions réussies ou signaler les erreurs

### API REST

Le serveur expose les endpoints suivants :

- `POST /upload` : Upload d'un fichier
- `GET /download/{filename}` : Téléchargement d'un fichier
- `GET /list` : Liste des fichiers disponibles
- `DELETE /delete/{filename}` : Suppression d'un fichier

## Structure des Fichiers

```
mini-cloud/
├── cloud-client/              # Application React
│   ├── public/               # Assets statiques
│   ├── src/                  # Code source React
│   │   ├── CloudClient.js    # Composant principal
│   │   ├── App.js           # Application principale
│   │   └── index.js         # Point d'entrée
│   └── package.json         # Dépendances Node.js
├── MiniCloud_CORBA/          # Serveur Java CORBA
│   ├── src/main/java/cloud/
│   │   ├── Cloud.idl        # Définition CORBA
│   │   ├── CloudModule/     # Classes générées CORBA
│   │   └── server/          # Serveur REST et CORBA
│   │       ├── CloudServer.java     # Serveur CORBA
│   │       ├── CloudRestServer.java # Serveur REST
│   │       └── FileManager.java     # Gestionnaire de fichiers
│   ├── pom.xml              # Configuration Maven
│   └── cloud_storage/       # Répertoire de stockage
└── README.md               # Ce fichier
```

## Dépannage

### Problèmes Courants

1. **Erreur de connexion CORBA** :
   - Vérifiez que le serveur CORBA est démarré sur le port 1050
   - Assurez-vous que les ports ne sont pas utilisés par d'autres applications

2. **Erreur lors de l'upload** :
   - Vérifiez les permissions d'écriture dans le répertoire `cloud_storage`
   - Assurez-vous que le serveur REST est accessible sur le port 4567

3. **Erreur de compilation Java** :
   - Vérifiez que Java JDK et Maven sont correctement installés
   - Exécutez `mvn clean compile` pour recompiler

4. **Erreur React** :
   - Vérifiez que Node.js est installé
   - Supprimez `node_modules` et réexécutez `npm install`
