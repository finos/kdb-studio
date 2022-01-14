package studio.utils;

public enum LineEnding {
    Unix("Unix", "\n"),
    Windows("Windows", "\r\n"),
    MacOS9("Mac OS 9", "\r");

    LineEnding(String description, String chars) {
        this.description = description;
        this.chars = chars;
    }

    private final String description;
    private final String chars;

    public String getDescription() {
        return description;
    }

    public String getChars() {
        return chars;
    }

    @Override
    public String toString() {
        return description;
    }
}
