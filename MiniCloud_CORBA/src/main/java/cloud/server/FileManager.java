package cloud.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
    private final String storagePath = "cloud_storage";

    public FileManager() {
        File dir = new File(storagePath);
        if (!dir.exists()) dir.mkdir();
    }

    public void saveFile(String filename, byte[] data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(storagePath + "/" + filename)) {
            fos.write(data);
        }
    }

    public byte[] readFile(String filename) throws IOException {
        File file = new File(storagePath + "/" + filename);
        if (!file.exists()) return new byte[0];
        byte[] data = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
        }
        return data;
    }

    public void deleteFile(String filename) {
        File file = new File(storagePath + "/" + filename);
        if (file.exists()) file.delete();
    }

    public String[] listFiles() {
        File dir = new File(storagePath);
        String[] files = dir.list();
        return files != null ? files : new String[0];
    }
}
