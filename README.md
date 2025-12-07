# MiniCloud - Système de Stockage en Nuage

MiniCloud est un système de stockage en nuage simple et distribué, composé d'un client web React et d'un serveur backend Java utilisant CORBA pour la gestion des fichiers. Le système permet d'uploader, télécharger, lister et supprimer des fichiers via une interface utilisateur intuitive.

## Architecture du Système

Le projet est divisé en trois composants principaux :

1. **cloud-client** : Application React avec interface moderne et futuriste pour l'interface utilisateur
2. **MiniCloud_CORBA** : Serveur Java utilisant CORBA pour la logique métier et une API REST (SparkJava) pour la communication avec le client
3. **AI** : Serveur FastAPI pour le résumé automatique de fichiers PDF avec intelligence artificielle

### Flux de Données
- Le client React communique avec le serveur Java REST sur le port 4567
- Le serveur REST utilise CORBA pour accéder aux services de stockage (port 1050)
- Le client React communique avec le serveur FastAPI sur le port 8000 pour les résumés IA

## Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Node.js** (version 16 ou supérieure) pour le client React
- **Java JDK** (version 8 ou supérieure) pour le serveur CORBA
- **Maven** pour la gestion des dépendances Java
- **Python** (version 3.8 ou supérieure) pour le serveur FastAPI
- **Clé API Google Gemini** pour la fonctionnalité de résumé IA

### Installation des Prérequis

#### Sur Windows
1. Téléchargez et installez Node.js depuis [nodejs.org](https://nodejs.org/)
2. Téléchargez et installez Java JDK depuis [oracle.com](https://www.oracle.com/java/technologies/javase-downloads.html)
3. Téléchargez et installez Maven depuis [maven.apache.org](https://maven.apache.org/download.cgi)
4. Téléchargez et installez Python depuis [python.org](https://www.python.org/downloads/)
5. Ajoutez Java, Maven et Python à votre PATH système

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

### Étape 4 : Configuration du Serveur FastAPI (IA)

1. Ouvrez un nouveau terminal et naviguez vers le répertoire AI :
```bash
cd AI
```

2. Installez les dépendances Python :
```bash
pip install -r requirements.txt
```

3. Configurez votre clé API Google Gemini :
   - Créez un fichier `.env` dans le dossier `AI`
   - Ajoutez votre clé API : `GEMINI_API_KEY=votre_cle_api_ici`
   - Vous pouvez obtenir une clé API sur [Google AI Studio]

## Démarrage du Système

Le système doit être démarré dans un ordre spécifique : serveur CORBA en premier, puis serveur REST, serveur FastAPI, et enfin le client React.

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

### Étape 3 : Démarrer le Serveur FastAPI (IA)

1. Ouvrez un troisième terminal dans `AI`
2. Démarrez le serveur FastAPI :
```bash
uvicorn server:app --reload
```

Le serveur FastAPI démarrera sur le port 8000 et permettra de générer des résumés IA des fichiers PDF.

### Étape 4 : Démarrer le Client React

1. Ouvrez un quatrième terminal dans `cloud-client`
2. Démarrez l'application React :
```bash
npm start
```

L'application sera accessible à l'adresse `http://localhost:3000`

## Fonctionnalités

### Interface Utilisateur Moderne

L'interface a été conçue avec un design moderne et futuriste incluant :

- **Thème sombre** avec dégradés animés (violet, bleu, rose)
- **Effets glassmorphism** (verre dépoli) pour les cartes et modals
- **Animations fluides** et transitions élégantes
- **Indicateur de connexion** pour surveiller l'état du serveur
- **Modal de résumé IA** avec arrière-plan flou pour une meilleure expérience utilisateur
- **Design responsive** adapté à tous les écrans

## Utilisation

### Interface Utilisateur

Une fois l'application démarrée :

1. **Upload de fichiers** :
   - Glissez-déposez des fichiers dans la zone dédiée
   - Ou cliquez pour sélectionner des fichiers
   - Les fichiers sont automatiquement uploadés vers le serveur

2. **Gestion des fichiers** :
   - La liste des fichiers disponibles s'affiche automatiquement
   - Cliquez sur "Résumer avec IA" pour générer un résumé automatique d'un fichier PDF
   - Cliquez sur "Télécharger" pour récupérer un fichier
   - Cliquez sur "Supprimer" pour effacer un fichier

3. **Résumé avec IA** :
   - Sélectionnez un fichier PDF dans la liste
   - Cliquez sur le bouton "Résumer avec IA" 🤖
   - Un modal s'ouvre avec le résumé généré par l'IA
   - Le résumé est généré en utilisant Google Gemini AI

4. **Notifications** :
   - Des toasts apparaissent pour confirmer les actions réussies ou signaler les erreurs
   - Un indicateur de connexion affiche l'état de connexion au serveur

### API REST

#### Serveur Java (Port 4567)

Le serveur REST Java expose les endpoints suivants :

- `POST /upload` : Upload d'un fichier
- `GET /download/{filename}` : Téléchargement d'un fichier
- `GET /list` : Liste des fichiers disponibles
- `DELETE /delete/{filename}` : Suppression d'un fichier

#### Serveur FastAPI (Port 8000)

Le serveur FastAPI expose les endpoints suivants :

- `GET /` : Informations sur le serveur et le répertoire de stockage
- `POST /summarize` : Génération d'un résumé IA d'un fichier PDF
  - Body: `{"filename": "nom_du_fichier.pdf"}`
  - Response: `{"summary": "résumé généré par l'IA"}`

## Structure des Fichiers

```
Projet_DAR/
├── cloud-client/              # Application React
│   ├── public/               # Assets statiques
│   ├── src/                  # Code source React
│   │   ├── CloudClient.js    # Composant principal avec UI moderne
│   │   ├── CloudClient.css   # Styles futuristes
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
├── AI/                       # Serveur FastAPI pour résumé IA
│   ├── server.py            # Serveur FastAPI principal
│   ├── main.py              # Fonctions de résumé PDF
│   ├── genai.py             # Configuration Google Gemini AI
│   ├── requirements.txt     # Dépendances Python
│   └── .env                 # Configuration API key (à créer)
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

5. **Erreur serveur FastAPI** :
   - Vérifiez que Python est installé et dans le PATH
   - Installez les dépendances : `pip install -r requirements.txt`
   - Vérifiez que le fichier `.env` contient votre clé API Gemini
   - Assurez-vous que le serveur FastAPI est démarré sur le port 8000
   - Vérifiez que le dossier `MiniCloud_CORBA/cloud_storage` existe

6. **Erreur résumé IA** :
   - Vérifiez que le fichier PDF existe dans `cloud_storage`
   - Assurez-vous que votre clé API Gemini est valide
   - Vérifiez que le serveur FastAPI est accessible sur `http://127.0.0.1:8000`

7. **Erreur de connexion au serveur REST** :
   - L'interface affiche un indicateur si le serveur n'est pas accessible
   - Vérifiez que le serveur REST Java est démarré sur le port 4567
   - Assurez-vous que le serveur CORBA est démarré avant le serveur REST
