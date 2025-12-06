package cloud.client;

import cloud.CloudModule.Cloud;
import cloud.CloudModule.CloudHelper;
import org.omg.CORBA.ORB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

public class CloudClient {

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);
            org.omg.CORBA.Object objRef = orb.string_to_object("corbaloc:localhost:1050/Cloud"); // correction :: -> :
            Cloud cloud = CloudHelper.narrow(objRef);

            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Upload  2. Download  3. Delete  4. List  5. Quit");
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Nom du fichier à uploader: ");
                        String upFile = sc.nextLine();
                        File file = new File(upFile);
                        if (!file.exists()) { System.out.println("Fichier introuvable !"); break; }
                        byte[] data = new byte[(int) file.length()];
                        FileInputStream fis = new FileInputStream(file);
                        fis.read(data);
                        fis.close();
                        cloud.upload(file.getName(), data);
                        System.out.println("Upload OK !");
                        break;

                    case 2:
                        System.out.print("Nom du fichier à télécharger: ");
                        String downFile = sc.nextLine();
                        byte[] downloaded = cloud.download(downFile);
                        if (downloaded == null || downloaded.length == 0) {
                            System.out.println("Fichier introuvable !");
                            break;
                        }
                        FileOutputStream fos = new FileOutputStream(new File(downFile)); // correction pour Java 1.8
                        fos.write(downloaded);
                        fos.close();
                        System.out.println("Download OK !");
                        break;

                    case 3:
                        System.out.print("Nom du fichier à supprimer: ");
                        String delFile = sc.nextLine();
                        cloud.deleteFile(delFile);
                        System.out.println("Fichier supprimé !");
                        break;

                    case 4:
                        String[] files = cloud.listFiles();
                        System.out.println("Fichiers disponibles :");
                        for (String f : files) System.out.println(f);
                        break;

                    case 5:
                        System.exit(0);

                    default:
                        System.out.println("Choix invalide !");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
