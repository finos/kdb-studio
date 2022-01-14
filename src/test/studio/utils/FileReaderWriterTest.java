package studio.utils;

import org.junit.jupiter.api.Test;
import studio.kdb.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileReaderWriterTest {

    private Content read(String text) throws IOException {
        File tmpFile = File.createTempFile("fileReaderWriterTest", ".tmp");
        tmpFile.deleteOnExit();
        Files.write(Paths.get(tmpFile.getPath()),text.getBytes(FileReaderWriter.DEFAULT_CHARSET));

        return FileReaderWriter.read(tmpFile.getPath());
    }

    @Test
    public void testReadContent() throws IOException {
        Content content = read("something ha-ha-ha");
        assertEquals("something ha-ha-ha", content.getContent());
        assertEquals(Config.getInstance().getEnum(Config.DEFAULT_LINE_ENDING), content.getLineEnding());

        content = read("something\nha-ha-ha");
        assertEquals("something\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.Unix, content.getLineEnding());
        assertFalse(content.hasMixedLineEnding());

        content = read("something\r\nha-ha-ha");
        assertEquals("something\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.Windows, content.getLineEnding());
        assertFalse(content.hasMixedLineEnding());

        content = read("something\n\rha-ha-ha");
        assertEquals("something\n\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.Unix, content.getLineEnding());
        assertTrue(content.hasMixedLineEnding());

        content = read("\r\nsomething\n\rha-ha-ha");
        assertEquals("\nsomething\n\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.Unix, content.getLineEnding());
        assertTrue(content.hasMixedLineEnding());

        content = read("something\n\rha-ha-ha\r");
        assertEquals("something\n\nha-ha-ha\n", content.getContent());
        assertEquals(LineEnding.MacOS9, content.getLineEnding());
        assertTrue(content.hasMixedLineEnding());

        content = read("\r\nsomething\n\r\nha-ha-ha");
        assertEquals("\nsomething\n\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.Windows, content.getLineEnding());
        assertTrue(content.hasMixedLineEnding());

        content = read("something\r\rha-ha-ha");
        assertEquals("something\n\nha-ha-ha", content.getContent());
        assertEquals(LineEnding.MacOS9, content.getLineEnding());
        assertFalse(content.hasMixedLineEnding());
    }


    private void assertWrite(String expected, String text, LineEnding lineEnding) throws IOException {
        File tmpFile = File.createTempFile("fileReaderWriterTest", ".tmp");
        tmpFile.deleteOnExit();

        FileReaderWriter.write(tmpFile.getPath(), text, lineEnding);
        String content = new String(Files.readAllBytes(Paths.get(tmpFile.getPath())), FileReaderWriter.DEFAULT_CHARSET);

        assertEquals(expected, content);
    }

    @Test
    public void testWrite() throws IOException {
        assertWrite("something\nha-ha-ha", "something\nha-ha-ha", LineEnding.Unix);
        assertWrite("something\r\nha-ha-ha", "something\nha-ha-ha", LineEnding.Windows);
        assertWrite("something\rha-ha-ha", "something\nha-ha-ha", LineEnding.MacOS9);

        assertWrite("something ha-ha-ha", "something ha-ha-ha", LineEnding.MacOS9);

        assertWrite("something ha-ha-ha - Дима\r\n", "something ha-ha-ha - Дима\n", LineEnding.Windows);

        assertWrite("\r\rsomething ha-ha-ha\rДима\r", "\n\nsomething ha-ha-ha\nДима\n", LineEnding.MacOS9);
    }
}
