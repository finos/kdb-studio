package studio.kdb;

import org.junit.jupiter.api.Test;

import javax.swing.tree.TreeNode;

import static org.junit.jupiter.api.Assertions.*;


public class ServerTreeNodeTest {

    @Test
    public void findPathTest() {
        ServerTreeNode serverTree = new ServerTreeNode();
        ServerTreeNode folderA = serverTree.add("a");
        ServerTreeNode folderB = folderA.add("b");
        ServerTreeNode serverNode = folderB.add(new Server());

        ServerTreeNode folderB1 = serverTree.add("b");
        ServerTreeNode serverNode1 = folderB1.add(new Server());

        assertTrue( folderB1.theSame(serverTree.findPath(new TreeNode[] {new ServerTreeNode(), new ServerTreeNode("b")})));

        ServerTreeNode folderC = serverTree.findPath(new TreeNode[] {new ServerTreeNode(), new ServerTreeNode("c")});
        assertNull(folderC);

        folderC = serverTree.findPath(new TreeNode[] {new ServerTreeNode(), new ServerTreeNode("c")}, true);
        assertNotNull(folderC);
        assertEquals(3, serverTree.getChildCount());
        assertTrue(folderC.theSame(serverTree.getChild(2)));

        // The first TreeNode should be ignored.
        ServerTreeNode folderD = serverTree.findPath(new TreeNode[] {new ServerTreeNode("c"), new ServerTreeNode("d")}, true);
        assertNotNull(folderD);
        assertEquals(0, folderC.getChildCount());
        assertEquals(4, serverTree.getChildCount());
        assertTrue(folderD.theSame(serverTree.getChild(3)));
        assertEquals("d", serverTree.getChild(3).getFolder());

    }
}
