package pl.andrzejo.aspm.settings;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Serializer {

    public static String serialize(Serializable obj) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(outputStream);
        stream.writeObject(obj);
        stream.close();
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String serialized) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized.getBytes(StandardCharsets.UTF_8));
        ObjectInputStream stream = new ObjectInputStream(inputStream);
        Object obj = stream.readObject();
        stream.close();
        return (T) obj;
    }
}
