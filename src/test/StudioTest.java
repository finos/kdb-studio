import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Assert;
import org.junit.Test;
import studio.ui.StudioPanel;

import java.awt.event.KeyEvent;

public class StudioTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        StudioPanel panel = GuiActionRunner.execute(() -> StudioPanel.init(new String[0]));
        Assert.assertNotEquals("panel is not null", null, panel);
        window = new FrameFixture(robot(), panel.frame());
        window.show(); // shows the frame to test
    }

    private void type(JTextComponentFixture tb, String text) {
        for (int i = 0; i < text.length(); ++i) {
            int code = text.charAt(i);
            if (code >= 'a' && code <= 'z') {
                tb.pressAndReleaseKey(KeyPressInfo.keyCode(code - 'a' + 'A'));
            } else {
                tb.pressAndReleaseKey(KeyPressInfo.keyCode(code));
            }
        }
    }

    @Test
    public void test() {
        JTextComponentFixture tb = window.textBox("qEditor");
        tb.requireEmpty();
        tb.pressAndReleaseKey(KeyPressInfo.keyCode(KeyEvent.VK_A));
        tb.requireText("a");
        type(tb, "bc\ndef");
        tb.requireText("abc\ndef");
        window.button("undo").click();
        tb.requireEmpty();
        window.button("redo").click();
        tb.requireText("abc\ndef");
    }

}
