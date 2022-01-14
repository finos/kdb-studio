package studio.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReaderWriter {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static void write(String fileName, String content, LineEnding lineEnding) throws IOException {
        write(fileName, content, lineEnding, DEFAULT_CHARSET);
    }

    public static void write(String fileName, String content, LineEnding lineEnding, Charset charset) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName), charset))  {
            writer.write(Content.convert(content, lineEnding));
        } catch (IOException e) {
            throw e;
        }
    }

    public static Content read(String fileName) throws IOException {
        return read(fileName, DEFAULT_CHARSET);
    }

    public static Content read(String fileName, Charset charset) throws IOException {
        Path path = Paths.get(fileName);
        if (! Files.exists(path)) throw new IOException("File " + fileName + " + doesn't exist");
        if (! Files.isRegularFile(path)) throw new IOException(fileName + " is not a regular file");

        byte[] bytes = Files.readAllBytes(path);
        String fileContent = new String(bytes, charset);
        return new Content(fileContent);
    }


}
