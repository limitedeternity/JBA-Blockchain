import java.io.*;

public class SerializationUtils {
    public static void serialize(Object obj, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(obj);
        }
    }

    public static Object deserialize(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return ois.readObject();
        }
    }
}
