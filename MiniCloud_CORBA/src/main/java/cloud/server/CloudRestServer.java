package cloud.server;

import cloud.CloudModule.Cloud;
import cloud.CloudModule.CloudHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import spark.Spark;

import java.io.*;

import static spark.Spark.before;

public class CloudRestServer {

    private static Cloud cloud;

    public static void main(String[] args) {
        try {
            // Initialisation ORB
            String[] orbArgs = { "-ORBInitialPort", "1050", "-ORBInitialHost", "127.0.0.1" };
            ORB orb = ORB.init(orbArgs, null);

            // Connexion au NameService CORBA
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Récupération de l'objet Cloud
            cloud = CloudHelper.narrow(ncRef.resolve_str("Cloud"));
            System.out.println("Objet CORBA récupéré : " + cloud);

            // Lancer Spark sur le port 4567 AVANT les routes
            Spark.port(4567);

            // ===== CORS =====
            before((request, response) -> {
                response.header("Access-Control-Allow-Origin", "*");
                response.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
                response.header("Access-Control-Allow-Headers", "*");
            });

            // Gestion des preflight OPTIONS
            Spark.options("/*", (request, response) -> {
                String reqHeaders = request.headers("Access-Control-Request-Headers");
                if (reqHeaders != null) {
                    response.header("Access-Control-Allow-Headers", reqHeaders);
                }
                String reqMethod = request.headers("Access-Control-Request-Method");
                if (reqMethod != null) {
                    response.header("Access-Control-Allow-Methods", reqMethod);
                }
                return "OK";
            });

            // ===== Routes =====

            // Upload
            Spark.post("/upload", "multipart/form-data", (req, res) -> {
                try {
                    // Parser le multipart
                    String filename = req.queryParams("filename");
                    if (filename == null || filename.isEmpty()) {
                        // Essayer de récupérer depuis le body si pas en query param
                        String contentType = req.contentType();
                        if (contentType != null && contentType.contains("multipart/form-data")) {
                            // Lire directement depuis l'input stream
                            // Le filename est envoyé comme query param ou dans le form
                            filename = req.queryParams("filename");
                        }
                        if (filename == null || filename.isEmpty()) {
                            res.status(400);
                            return "Nom de fichier manquant";
                        }
                    }

                    // Lire le body complet (le fichier est dans le body)
                    InputStream is = req.raw().getInputStream();
                    byte[] allData = readInputStream(is);
                    
                    if (allData.length == 0) {
                        res.status(400);
                        return "Fichier vide";
                    }
                    
                    // Extraire les données du fichier depuis le multipart
                    byte[] fileData = extractFileFromMultipart(allData, filename);
                    
                    if (fileData.length == 0) {
                        // Si l'extraction échoue, utiliser tout le body (pour compatibilité)
                        fileData = allData;
                    }
                    
                    cloud.upload(filename, fileData);
                    System.out.println("Fichier uploadé avec succès : " + filename + " (" + fileData.length + " bytes)");
                    res.status(200);
                    return "Upload OK : " + filename;
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(500);
                    return "Erreur upload : " + e.getMessage();
                }
            });

            // Download
            Spark.get("/download/:filename", (req, res) -> {
                String filename = req.params("filename");
                byte[] data = cloud.download(filename);
                if (data == null || data.length == 0) {
                    res.status(404);
                    return "Fichier non trouvé";
                }
                res.type("application/octet-stream");
                res.raw().getOutputStream().write(data);
                res.raw().getOutputStream().flush();
                return res.raw();
            });

            // List
            Spark.get("/list", (req, res) -> {
                String[] files = cloud.listFiles();
                res.type("application/json");
                // JSON valide pour React
                return new com.google.gson.Gson().toJson(files);
            });

            // Delete
            Spark.delete("/delete/:filename", (req, res) -> {
                String filename = req.params("filename");
                cloud.deleteFile(filename);
                return "Fichier supprimé : " + filename;
            });

            System.out.println("REST server running on http://127.0.0.1:4567");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Utilitaire pour lire InputStream =====
    private static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[8192]; // Buffer plus grand pour les gros fichiers
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
    
    // ===== Extraire le fichier depuis le multipart =====
    private static byte[] extractFileFromMultipart(byte[] multipartData, String filename) {
        try {
            String dataStr = new String(multipartData, "ISO-8859-1");
            
            // Chercher le début des données du fichier
            // Format: Content-Disposition: form-data; name="file"; filename="..."
            String fileMarker = "name=\"file\"";
            int fileStart = dataStr.indexOf(fileMarker);
            
            if (fileStart == -1) {
                // Si pas trouvé, retourner les données telles quelles
                return multipartData;
            }
            
            // Trouver la fin de l'en-tête (double saut de ligne)
            int headerEnd = dataStr.indexOf("\r\n\r\n", fileStart);
            if (headerEnd == -1) {
                headerEnd = dataStr.indexOf("\n\n", fileStart);
            }
            
            if (headerEnd == -1) {
                return multipartData;
            }
            
            // Début des données du fichier
            int dataStart = headerEnd + (dataStr.charAt(headerEnd + 1) == '\r' ? 4 : 2);
            
            // Trouver la fin (boundary suivant ou fin)
            int dataEnd = dataStr.indexOf("\r\n--", dataStart);
            if (dataEnd == -1) {
                dataEnd = dataStr.indexOf("\n--", dataStart);
            }
            
            if (dataEnd == -1) {
                // Utiliser la fin des données
                dataEnd = multipartData.length;
            }
            
            // Extraire les bytes
            byte[] fileBytes = new byte[dataEnd - dataStart];
            System.arraycopy(multipartData, dataStart, fileBytes, 0, fileBytes.length);
            
            return fileBytes;
        } catch (Exception e) {
            e.printStackTrace();
            // En cas d'erreur, retourner les données complètes
            return multipartData;
        }
    }
}