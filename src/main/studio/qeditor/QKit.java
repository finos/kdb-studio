package studio.qeditor;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.TextAction;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ExtKit;
import studio.utils.CopyCutWithSyntaxAction;


public class QKit extends ExtKit {

    public static final String CONTENT_TYPE = "text/q";

    public String getContentType() {
        return CONTENT_TYPE; // NOI18N
    }

    public Syntax createSyntax(Document document) {
        return new QSyntax();
    }
    
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new QSyntaxSupport(doc);
    }

    public Completion createCompletion(ExtEditorUI extEditorUI) {
        return new QCompletion(extEditorUI);
    }

    @Override
    protected Action[] createActions() {
        return TextAction.augmentList(super.createActions(),
                new Action[] {
                        new CopyCutWithSyntaxAction(CopyCutWithSyntaxAction.Mode.COPY),
                        new CopyCutWithSyntaxAction(CopyCutWithSyntaxAction.Mode.CUT)
                });
    }
}
