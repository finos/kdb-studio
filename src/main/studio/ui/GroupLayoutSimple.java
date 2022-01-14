package studio.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GroupLayoutSimple extends GroupLayout {

    public GroupLayoutSimple(Container container) {
        super(container);
        container.setLayout(this);
        setAutoCreateGaps(true);
        setAutoCreateContainerGaps(true);
    }

    public void setStacks(Stack... stacks) {

        int lineCount = stacks[0].lines.size();
        for (int i = 1; i<stacks.length; i++) {
            if (lineCount != stacks[i].lines.size()) {
                throw new IllegalArgumentException("Number of lines in every stack should be the same");
            }
        }

        SequentialGroup horizontalGroup = createSequentialGroup();
        for (Stack stack: stacks) {
            ParallelGroup stackGroup = createParallelGroup();
            for (Component[] line: stack.lines) {
                SequentialGroup lineGroup = createSequentialGroup();
                for (Component component: line) {
                    lineGroup.addComponent(component);
                }
                stackGroup.addGroup(lineGroup);
            }
            horizontalGroup.addGroup(stackGroup);
        }
        setHorizontalGroup(horizontalGroup);

        SequentialGroup verticalGroup = createSequentialGroup();
        for (int lineIndex = 0; lineIndex<lineCount; lineIndex++) {
            ParallelGroup lineGroup = createParallelGroup(GroupLayout.Alignment.BASELINE);
            for (Stack stack: stacks) {
                for (Component component: stack.lines.get(lineIndex)) {
                    lineGroup.addComponent(component);
                }
            }
            verticalGroup.addGroup(lineGroup);
        }
        setVerticalGroup(verticalGroup);
    }

    public static class Stack {
        List<Component[]> lines;
        public Stack() {
            lines = new ArrayList<>();
        }

        public Stack addLine(Component... line) {
            lines.add(line);
            return this;
        }

        public Stack addLineAndGlue(Component... line) {
            int count = line.length;
            Component[] newLine = new Component[count + 1];
            System.arraycopy(line, 0, newLine, 0, count);
            newLine[count] = Box.createGlue();
            lines.add(newLine);
            return this;
        }
    }
}
