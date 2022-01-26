package studio.ui.rstextarea;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

public class StudioRSyntaxTextArea extends RSyntaxTextArea {

    public StudioRSyntaxTextArea(String text) {
        super(text);
        setName("qEditor");
    }

    @Override
    protected void handleReplaceSelection(String content) {
        //Probably this is a dirty hack, but here is we convert character 160 to space
        //which is pasted by Skype for Business and result in 'char error from kdb
        content = content.replace((char)160,' ');
        super.handleReplaceSelection(content);
    }
}
