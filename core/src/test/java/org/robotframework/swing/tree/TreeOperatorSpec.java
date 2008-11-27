package org.robotframework.swing.tree;

import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jdave.ExpectationFailedException;
import jdave.junit4.JDaveRunner;

import org.jmock.Expectations;
import org.junit.runner.RunWith;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.robotframework.swing.contract.FieldIsNotNullContract;
import org.robotframework.swing.keyword.MockSupportSpecification;
import org.robotframework.swing.popup.IPopupCaller;


@RunWith(JDaveRunner.class)
public class TreeOperatorSpec extends MockSupportSpecification<TreeOperator> {
    private JTree tree;
    private TreePath treePath;
    private String nodeIdentifier = "some|node";
    
    public class Any {
        public void hasPopupCaller() {
            TreeOperator enhancedTreeOperator = new TreeOperator(dummy(JTree.class));
            specify(enhancedTreeOperator, satisfies(new FieldIsNotNullContract("popupCaller")));
        }
    }

    public class Operating {
        private TreeOperator enhancedTreeOperator;
        
        public TreeOperator create() {
            treePath = mock(TreePath.class);
            tree = createMockJTree();
            enhancedTreeOperator = new TreeOperator(tree);
            injectMockPathFactory();
            return enhancedTreeOperator;
        }

        public void expandsPath() {
            checking(new Expectations() {{
                one(tree).expandPath(treePath);
            }});
            
            context.expand(nodeIdentifier);
        }
        
        public void collapsesPath() {
            checking(new Expectations() {{
                one(tree).collapsePath(treePath);
            }});
            
            context.collapse(nodeIdentifier);
        }
        
        public void addsSelection() {
            checking(new Expectations() {{
                one(tree).addSelectionPath(treePath);
            }});
            
            context.addSelection(nodeIdentifier);
        }
        
        public void removesSelection() {
            checking(new Expectations() {{
                one(tree).removeSelectionPath(treePath);
            }});
            
            context.removeSelection(nodeIdentifier);
        }
        

        public void checksExpanded() {
            checking(new Expectations() {{
                one(tree).isExpanded(treePath); will(returnValue(true));
            }});
            
            specify(context.isExpanded(nodeIdentifier));
        }
        
        public void checksCollapsed() {
            checking(new Expectations() {{
                one(tree).isCollapsed(treePath); will(returnValue(true));
            }});
            
            specify(context.isCollapsed(nodeIdentifier));
        }
        
        public void checksIsLeaf() {
            final TreeNode treeNode = mock(TreeNode.class);
            checking(new Expectations() {{
                one(treePath).getLastPathComponent(); will(returnValue(treeNode));
                one(treeNode).isLeaf(); will(returnValue(true));
            }});
            
            specify(context.isLeaf(nodeIdentifier));
        }
        
        public void checksIsSelected() {
            checking(new Expectations() {{
                one(tree).isPathSelected(treePath); will(returnValue(true));
            }});
            
            specify(context.isPathSelected(nodeIdentifier));
        }

        public void checksIsVisible() {
            checking(new Expectations() {{
                one(tree).isVisible(treePath); will(returnValue(true));
            }});
            
            specify(context.isVisible(nodeIdentifier));
        }
        
        private void injectMockPathFactory() {
            final TreePathFactory pathFactory = injectMockTo(enhancedTreeOperator, TreePathFactory.class);
            checking(new Expectations() {{
                one(pathFactory).createTreePath(nodeIdentifier);
                will(returnValue(treePath));
            }});
        }
    }

    public class GettingLabel {
        public TreeOperator create() {
            treePath = mock(TreePath.class);
            tree = createMockJTree();
            return new TreeOperator(tree);
        }
        
        public void getTreeNodeLabel() {
            final String label = "someNode";
            checking(new Expectations() {{
                one(tree).getPathForRow(2); will(returnValue(treePath));
                one(treePath).getLastPathComponent(); will(returnValue(label));
            }});
            
            specify(context.getTreeNodeLabel(2), label);
        }
    }
    
    public class CreatingPopupOperator {
        private TreePopupMenuOperatorFactory popupFactory;
        private JPopupMenuOperator popupMenuOperator = dummy(JPopupMenuOperator.class);

        public TreeOperator create() {
            popupFactory = mock(TreePopupMenuOperatorFactory.class);
            return new TreeOperator(dummy(JTree.class)) {
                TreePopupMenuOperatorFactory createPopupFactory() {
                    return popupFactory;
                }
            };
        }
        
        public void createsPopupOperator() {
            checking(new Expectations() {{
                one(popupFactory).createOperator(nodeIdentifier);
                will(returnValue(popupMenuOperator));
            }});
            
            specify(context.createPopupOperator(nodeIdentifier), popupMenuOperator);
        }
        
        public void createsPopupOperatorOnSelectedNodes() {
            checking(new Expectations() {{
                one(popupFactory).createOperatorBySelection();
                will(returnValue(popupMenuOperator));
            }});
            
            specify(context.createPopupOperatorOnSelectedNodes(), popupMenuOperator);
        }
    }
    
    public class CallingPopup {
        private int selectRowCallCount = 0;
        private int scrollToRowCallCount = 0;
        private Point pointToClick = new Point(1, 1);

        public void callsPopupOnRow() {
            int expectedRow = 2;
            TreeOperator treeOperator = createTreeOperatorWithExpectedRow(expectedRow);
            JPopupMenu popupMenu = injectPopupCallerTo(treeOperator);

            specify(treeOperator.callPopupOnRow(expectedRow), must.equal(popupMenu));
            specify(selectRowCallCount, must.equal(1));
            specify(scrollToRowCallCount, must.equal(1));
        }

        private JPopupMenu injectPopupCallerTo(final TreeOperator treeOperator) {
            final IPopupCaller popupCaller = injectMockTo(treeOperator, IPopupCaller.class);
            final JPopupMenu popupMenu = dummy(JPopupMenu.class);
            checking(new Expectations() {{
                one(popupCaller).callPopupOnComponent(treeOperator, pointToClick);
                will(returnValue(popupMenu));
            }});
            return popupMenu;
        }

        private TreeOperator createTreeOperatorWithExpectedRow(final int expectedRow) {
            return new TreeOperator(dummy(JTree.class)) {
                public void selectRow(int row) {
                    if (expectedRow == row) {
                        selectRowCallCount++;
                    } else {
                        throw new ExpectationFailedException("Expected " + expectedRow + ", but got: " + row);
                    }
                }
                
                public void scrollToRow(int row) {
                    if (expectedRow == row) {
                        scrollToRowCallCount++;
                    } else {
                        throw new ExpectationFailedException("Expected " + expectedRow + ", but got: " + row);
                    }
                }
                
                public Point getPointToClick(int row) {
                    if (expectedRow == row) {
                        return pointToClick;
                    }
                    throw new ExpectationFailedException("Expected " + expectedRow + ", but got: " + row);
                }
            };
        }
    }

    private JTree createMockJTree() {
        final JTree tree = mock(JTree.class);
        checking(new Expectations() {{
            allowing(tree).isShowing(); will(returnValue(true));
        }});
        return tree;
    }
}