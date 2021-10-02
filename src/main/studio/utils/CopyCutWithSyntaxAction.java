package studio.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.netbeans.editor.*;
import studio.ui.Util;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CopyCutWithSyntaxAction extends BaseAction {

    private static final Logger log = LogManager.getLogger();

    public enum Mode {COPY, CUT};
    private Mode mode;

    public CopyCutWithSyntaxAction(Mode mode) {
        super(mode == Mode.COPY ? DefaultEditorKit.copyAction : DefaultEditorKit.cutAction, ABBREV_RESET | UNDO_MERGE_RESET | WORD_MATCH_RESET);
        this.mode = mode;
    }

    private String toHtml(String text) {
        return text.replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;");
    }

    private void appendColor(StringBuilder builder, Color color) {
        builder.append("#");
        String hex = Integer.toHexString( color.getRGB() & 0x00ffffff );
        int countToPad = 6 - hex.length();
        for (int i=0;i<countToPad; i++) {
            builder.append("0");
        }
        builder.append(hex);
    }

    private void appendHtml(StringBuilder builder, String text, TokenID tokenID, Coloring coloring) {
        StringBuilder style = new StringBuilder();

        Font font = coloring.getFont();
        if (font != null) {
            style.append("font-family: ").append(font.getFamily()).append(", Courier;");
            if (font.isBold()) {
                style.append("font-weight: bold;");
            }
            if (font.isItalic()) {
                style.append("font-style: italic;");
            }
        }
        if (coloring.getForeColor() != null) {
            style.append("color: ");
            appendColor(style, coloring.getForeColor());
            style.append(";");
        }
        if (coloring.getBackColor() != null) {
            style.append("background: ");
            appendColor(style, coloring.getBackColor());
            style.append(";");
        }

        builder.append("<span");
        if (style.length()>0) {
            builder.append(" style=\"").append(style).append("\"");
        }
        builder.append(">").append(toHtml(text)).append("</span>");
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent editor) {
        if (editor == null) return;

        TextUI textUI = editor.getUI();
        if (! (textUI instanceof BaseTextUI)) {
            editor.copy();
            return;
        }

        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        if (start == end) return;

        try {
            EditorUI editorUI = ((BaseTextUI) textUI).getEditorUI();
            BaseKit baseKit = (BaseKit) textUI.getEditorKit(editor);
            Syntax syntax = baseKit.createSyntax(editor.getDocument());
            Document document = editor.getDocument();
            String text = document.getText(0, document.getLength());
            syntax.load(null, text.toCharArray(), 0, text.length(), true, text.length());

            StringBuilder htmlBuilder = new StringBuilder("<pre>");
            StringBuilder textBuilder = new StringBuilder();
            int offset = 0;
            while (offset < end) {
                TokenID token = syntax.nextToken();
                if (token == null) break;
                int newOffset = syntax.getOffset();

                int left = Math.max(start, offset);
                int right = Math.min(end, newOffset);
                if (left < right) {
                    String tokenName = syntax.getTokenContextPath().getFullTokenName(token);
                    Coloring coloring = editorUI.getColoring(tokenName);
                    String tokenText = text.substring(left, right);
                    appendHtml(htmlBuilder, tokenText, token, coloring);
                    textBuilder.append(tokenText);
                }

                offset = newOffset;
            }
            htmlBuilder.append("</pre>");

            Util.copyToClipboard(htmlBuilder.toString(), textBuilder.toString());

            if (mode == Mode.CUT) {
                document.remove(start, end-start);
            }
        } catch (BadLocationException e) {
            log.error("Exception is not expected", e);
        }
    }
}
