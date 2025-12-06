# MiniCloud Project

MiniCloud est un projet de cloud personnel développé en Java avec CORBA pour la communication serveur, une interface REST via SparkJava pour exposer les fonctionnalités du serveur et une interface front-end React pour interagir avec le cloud depuis un navigateur.

## Structure du projet

```
MiniCloud_CORBA/
├── src/
│   ├── cloud/
│   │   ├── server/
│   │   │   ├── CloudServer.java       # Serveur CORBA
│   │   │   ├── CloudRestServer.java   # Serveur REST pour exposer CloudServer
│   │   │   └── FileManager.java       # Gestion des fichiers (save/read/delete/list)
│   │   └── CloudModule/               # Fichiers générés par idlj
├── cloud-client/
│   └── src/
│       └── CloudClient.js             # Interface React
├── pom.xml                             # Gestion des dépendances Maven
└── README.md
```

## Prérequis

- Java JDK 8 ou supérieur
- Maven pour gérer les dépendances
- Node.js et npm/yarn pour React
- IntelliJ IDEA
- tnameserv pour CORBA NameService

## Lancer le CORBA NameService

Le serveur CORBA utilise un NameService pour enregistrer et récupérer les objets.

1. Ouvrir un terminal.
2. Lancer le NameService sur le port 1050 :

```bash
tnameserv -ORBInitialPort 1050
```

⚠️ Sur certaines versions, `-ORBInitialHost` n'est pas nécessaire. Le port suffit.

## Lancer le serveur CORBA (CloudServer)

1. Compiler et exécuter CloudServer.java :

```bash
cd MiniCloud_CORBA/src
javac cloud/server/*.java
java cloud.server.CloudServer
```

Le serveur est maintenant prêt et écoute les appels CORBA.

## Lancer le serveur REST (CloudRestServer)

1. Compiler et exécuter CloudRestServer.java :

```bash
javac cloud/server/CloudRestServer.java
java cloud.server.CloudRestServer
```

Le serveur REST s'expose sur `http://127.0.0.1:4567`.

⚠️ CORS est activé pour permettre l'accès depuis React (localhost:3000).

## Lancer le client React

1. Aller dans le dossier cloud-client/ :

```bash
cd cloud-client
```

2. Installer les dépendances :

```bash
npm install
```

3. Lancer le serveur de développement React :

```bash
npm start
```

4. Ouvrir le navigateur sur `http://localhost:3000`.

Tu verras :
- Drag & Drop pour uploader des fichiers
- Liste des fichiers disponibles sur le serveur
- Boutons pour télécharger ou supprimer les fichiers

## Fonctionnement général

- **CloudServer** gère les fichiers localement et expose les méthodes CORBA : upload, download, deleteFile, listFiles.
- **CloudRestServer** agit comme passerelle, en utilisant SparkJava pour exposer les méthodes CORBA vers HTTP.
- **CloudClient** (React) permet d'interagir avec le serveur REST via le navigateur : upload, download, suppression et liste des fichiers.
- CORS est géré côté REST pour permettre la communication avec React sur un autre port.

## Notes importantes

- Les fichiers sont stockés dans le dossier où s'exécute le serveur CORBA (par défaut ./).
- La liste des fichiers est renvoyée en JSON valide pour que React puisse la traiter.
- Assurez-vous que tnameserv est lancé avant CloudServer.
- Pour les fichiers volumineux, la communication REST utilise InputStream et ByteArrayOutputStream.

## Dépendances principales

### Java / CORBA
- SparkJava pour REST
- React + react-dropzone + react-toastify
- SLF4J (logger, optionnel)
- Gson (optionnel pour JSON côté serveur)

