package cloud.server;

import cloud.CloudModule.CloudPOA;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNaming.*;

public class CloudServer extends CloudPOA {

    private final FileManager fileManager = new FileManager();

    @Override
    public void upload(String filename, byte[] data) {
        try {
            fileManager.saveFile(filename, data);
            System.out.println("Fichier uploadé : " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] download(String filename) {
        try {
            return fileManager.readFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void deleteFile(String filename) {
        fileManager.deleteFile(filename);
        System.out.println("Fichier supprimé : " + filename);
    }

    @Override
    public String[] listFiles() {
        return fileManager.listFiles();
    }

    public static void main(String[] args) {
        try {
            // Initialisation ORB
            String[] orbArgs = { "-ORBInitialPort", "1050", "-ORBInitialHost", "127.0.0.1" };
            ORB orb = ORB.init(orbArgs, null);

            // Activation du POA
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // Création de l'objet serveur
            CloudServer serverImpl = new CloudServer();
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverImpl);

            // Liaison avec le NameService CORBA
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] name = ncRef.to_name("Cloud");
            ncRef.rebind(name, ref);

            System.out.println("Objet CORBA enregistré dans le NameService : Cloud");
            System.out.println("Serveur CORBA prêt sur 127.0.0.1:1050...");

            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
