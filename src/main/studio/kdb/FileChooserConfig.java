package studio.kdb;

import java.awt.*;
import java.util.Objects;

public class FileChooserConfig {
    private String filename;
    private Dimension preferredSize;

    public FileChooserConfig() {
        filename = "";
        preferredSize = new Dimension(0,0);
    }

    public FileChooserConfig(String filename, Dimension preferredSize) {
        this.filename = filename;
        this.preferredSize = preferredSize;
    }

    public String getFilename() {
        return filename;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileChooserConfig)) return false;
        FileChooserConfig that = (FileChooserConfig) o;
        return Objects.equals(filename, that.filename) && Objects.equals(preferredSize, that.preferredSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, preferredSize);
    }
}
