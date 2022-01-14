package studio.utils;

import studio.kdb.Config;

public class Content {
    private final String content;
    private LineEnding lineEnding = Config.getInstance().getEnum(Config.DEFAULT_LINE_ENDING);
    private boolean mixedLineEnding;

    public static Content getEmpty() {
        return new Content("");
    }

    public static Content newContent(String text, LineEnding lineEnding) {
        Content content = new Content(text);
        content.lineEnding = lineEnding;
        content.mixedLineEnding = false;
        return content;
    }

    public Content(String text) {
        StringBuilder builder = new StringBuilder();
        int unixEndings = 0;
        int winEndings = 0;
        int macEndings = 0;

        boolean wasCR = false;
        int size = text.length();
        int index = 0;
        while(index < size) {
            char ch = text.charAt(index);

            if (wasCR) {
                wasCR = false;
                builder.append('\n');
                if (ch == '\n') {
                    winEndings++;
                } else {
                    macEndings++;
                    continue;
                }
            } else {
                if (ch == '\r') {
                    wasCR = true;
                } else {
                    if (ch == '\n') {
                        unixEndings++;
                    }
                    builder.append(ch);
                }
            }
            index++;
        }
        if (wasCR) {
            builder.append('\n');
            macEndings++;
        }

        content = builder.toString();

        int count = 0;
        if (unixEndings > 0) count++;
        if (winEndings > 0) count++;
        if (macEndings > 0) count++;

        mixedLineEnding = count > 1;

        if (count>0) { // if no ending of line; lineEding will be null
            if (unixEndings >= winEndings && unixEndings >= macEndings)  lineEnding = LineEnding.Unix;
            else if (winEndings >= unixEndings && winEndings >= macEndings) lineEnding = LineEnding.Windows;
            else lineEnding = LineEnding.MacOS9;
        }
    }

    public String getContent() {
        return content;
    }

    public LineEnding getLineEnding() {
        return lineEnding;
    }

    public boolean hasMixedLineEnding() {
        return mixedLineEnding;
    }

    public String getText() {
        return  convert(content, lineEnding);
    }

    public static String convert(String unixText, LineEnding lineEnding) {
        return unixText.replace("\n", lineEnding.getChars());
    }
}
