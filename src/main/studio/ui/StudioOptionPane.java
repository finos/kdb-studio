package studio.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.*;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

public class StudioOptionPane {

    public static class Option {
        private String text;
        private int mnemonic;

        public Option(String text, int mnemonic) {
            this.text = text;
            this.mnemonic = mnemonic;
        }

        public String getText() {
            return text;
        }

        public int getMnemonic() {
            return mnemonic;
        }

        public char getKey() {
            return (char)mnemonic;
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (! (obj instanceof Option)) return false;
            return text.equals(((Option)obj).getText());
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final Option OK_OPTION = new Option("OK", KeyEvent.VK_O);
    public static final Option YES_OPTION = new Option("Yes", KeyEvent.VK_Y);
    public static final Option NO_OPTION = new Option("No", KeyEvent.VK_N);
    public static final Option CANCEL_OPTION = new Option("Cancel", KeyEvent.VK_C);

    public static final Option RELOAD_OPTION = new Option("Reload", KeyEvent.VK_R);
    public static final Option IGNOREALL_OPTION = new Option("Ignore all", KeyEvent.VK_I);

    public static final Option[] OK_OPTIONS = new Option[] {OK_OPTION};
    public static final Option[] YES_NO_OPTIONS = new Option[] {YES_OPTION, NO_OPTION};
    public static final Option[] YES_NO_CANCEL_OPTIONS = new Option[] {YES_OPTION, NO_OPTION, CANCEL_OPTION};

    public static final Option[] RELOADFILE_OPTIONS = new Option[] {RELOAD_OPTION, CANCEL_OPTION, IGNOREALL_OPTION};

    public static final int RELOAD_RESULT = 0;
    public static final int CANCEL_RESULT = 1;
    public static final int IGNOREALL_RESULT = 2;

    public static void showError(String message, String title) {
        showError(null, message, title);
    }

    public static void showError(Component parentComponent, String message, String title) {
        showOptionDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE, Util.ERROR_ICON, OK_OPTIONS, OK_OPTION);
    }

    public static void showMessage(Component parentComponent, String message, String title) {
        showOptionDialog(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE, Util.INFORMATION_ICON, OK_OPTIONS, OK_OPTION);
    }

    public static void showWarning(Component parentComponent, String message, String title) {
        showOptionDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE, Util.WARNING_ICON, OK_OPTIONS, OK_OPTION);
    }


    public static int showYesNoDialog(Component parentComponent, String message, String title) {
        return showOptionDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, Util.QUESTION_ICON, YES_NO_OPTIONS, NO_OPTION);
    }

    public static int showYesNoCancelDialog(Component parentComponent, String message, String title) {
        return showOptionDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, Util.QUESTION_ICON, YES_NO_CANCEL_OPTIONS, NO_OPTION);
    }

    public static int reloadFileDialog(Component parentComponent, String message, String title) {
        return showOptionDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, Util.QUESTION_ICON, RELOADFILE_OPTIONS, CANCEL_OPTION);
    }

    public static String showInputDialog(Component parentComponent, String message, String title) {
        return JOptionPane.showInputDialog(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE);
    }

    private static void findButtons(List<JButton> buttons, Container container) {
        if (container instanceof JButton) {
            buttons.add((JButton)container);
        }
        for (Component c: container.getComponents()) {
            if (c instanceof Container) {
                findButtons(buttons, (Container) c);
            }
        }
    }

    public static int showOptionDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Option[] options, Option initialValue) {
        JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.DEFAULT_OPTION, icon, options, initialValue);
        ArrayList<JButton> buttons = new ArrayList<>();
        findButtons(buttons, pane);
        for (JButton button : buttons) {
            Option option = null;
            for (int index=0; index<options.length; index++) {
                if (button.getText().equals(options[index].getText())) {
                    option = options[index];
                    break;
                }
            }
            if (option == null) continue;

            button.setMnemonic(option.getKey());
            Action action = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    button.doClick();
                }
            };
            String actionName = "press " + option.getKey();
            pane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(option.getKey()), actionName);
            pane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(Character.toLowerCase(option.getKey())), actionName);
            pane.getActionMap().put(actionName, action);
        }
        JDialog dialog = pane.createDialog(parentComponent, title);
        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();
        Object result = pane.getValue();

        if (result == null) return JOptionPane.CLOSED_OPTION;

        if (result instanceof Integer)
            return (Integer)result;

        for (int index=0; index<options.length; index++) {
            if (options[index].equals(result)) return index;
        }

        return JOptionPane.CLOSED_OPTION;
    }

}
