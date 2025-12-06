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
            Spark.post("/upload", (req, res) -> {
                String filename = req.queryParams("filename");
                if (filename == null || filename.isEmpty()) {
                    res.status(400);
                    return "Nom de fichier manquant";
                }

                InputStream is = req.raw().getInputStream();
                byte[] data = readInputStream(is);
                cloud.upload(filename, data);
                return "Upload OK : " + filename;
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
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
