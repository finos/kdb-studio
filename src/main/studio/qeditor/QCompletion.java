package studio.qeditor;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.editor.ext.Completion;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.CompletionView;
import org.netbeans.editor.ext.ExtEditorUI;
import org.netbeans.editor.ext.ListCompletionView;

public class QCompletion extends Completion {

    public QCompletion(ExtEditorUI extEditorUI) {
        super(extEditorUI);
    }

    protected CompletionView createView() {
        return new ListCompletionView(new DelegatingCellRenderer());
    }

    protected CompletionQuery createQuery() {
        return new QCompletionQuery();
    }

    public synchronized boolean substituteText(boolean flag) {
        if (getLastResult() != null) {
            int index = getView().getSelectedIndex();
            if (index >= 0) {
                getLastResult().substituteText(index, flag);
            }
            return true;
        } else {
            return false;
        }
    }


    public class DelegatingCellRenderer implements ListCellRenderer {
        ListCellRenderer defaultRenderer = new DefaultListCellRenderer();


        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (value instanceof CompletionQuery.ResultItem) {
                return ((CompletionQuery.ResultItem) value)
                    .getPaintComponent(list, isSelected, cellHasFocus);
            } else {
                return defaultRenderer
                    .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

}
