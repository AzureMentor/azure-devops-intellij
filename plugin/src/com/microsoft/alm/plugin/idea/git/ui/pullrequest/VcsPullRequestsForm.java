// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.git.ui.pullrequest;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.ui.common.tabs.TabFormImpl;
import com.microsoft.alm.plugin.operations.Operation;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class VcsPullRequestsForm extends TabFormImpl<PullRequestsTreeModel> {
    private Tree pullRequestsTree;

    //commands
    public static final String CMD_ABANDON_SELECTED_PR = "abandonSelectedPullRequest";
    public static final String TOOLBAR_LOCATION = "Vcs.PullRequests";

    private PullRequestsTreeModel pullRequestsTreeModel;

    public VcsPullRequestsForm() {
        super(TfPluginBundle.KEY_VCS_PR_TITLE,
                TfPluginBundle.KEY_CREATE_PR_DIALOG_TITLE,
                TfPluginBundle.KEY_VCS_PR_REFRESH_TOOLTIP,
                TOOLBAR_LOCATION);

        ensureInitialized();
    }

    protected void createCustomView() {
        //Tree in a scroll panel
        pullRequestsTree = new Tree();
        pullRequestsTree.setCellRenderer(new PRTreeCellRenderer());
        pullRequestsTree.setShowsRootHandles(true);
        pullRequestsTree.setRootVisible(false);
        pullRequestsTree.setRowHeight(0); //dynamically have row height computed for each row
        scrollPanel = new JBScrollPane(pullRequestsTree);
    }

    protected void addCustomTools(final JPanel toolBar) {
        // nothing custom to do
    }

    public void setModelForView(final PullRequestsTreeModel treeModel) {
        treeModel.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent treeModelEvent) {
                // do nothing
            }

            @Override
            public void treeNodesInserted(TreeModelEvent treeModelEvent) {
                // do nothing
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
                // do nothing
            }

            @Override
            public void treeStructureChanged(TreeModelEvent treeModelEvent) {
                //expand the tree
                if (pullRequestsTreeModel != null) {
                    pullRequestsTree.expandRow(0);
                    pullRequestsTree.expandRow(pullRequestsTreeModel.getRequestedByMeRoot().getChildCount() + 1);
                }
            }
        });

        this.pullRequestsTreeModel = treeModel;
        pullRequestsTree.setModel(treeModel);
        pullRequestsTree.setSelectionModel(treeModel.getSelectionModel());
        searchFilter.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                onFilterChanged();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                onFilterChanged();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                onFilterChanged();
            }

            private void onFilterChanged() {
                if (timer.isRunning()) {
                    timer.restart();
                } else {
                    timer.start();
                }
            }
        });
    }

    public void addActionListener(final ActionListener listener) {
        super.addActionListener(listener);
        addTreeEventListeners(listener);
    }

    private void addTreeEventListeners(final ActionListener listener) {
        //mouse listener
        pullRequestsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                //double click
                if (mouseEvent.getClickCount() == 2) {
                    triggerEvent(CMD_OPEN_SELECTED_ITEM_IN_BROWSER);
                } else if (mouseEvent.isPopupTrigger() || ((mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
                    //right click, show pop up
                    showPopupMenu(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY(), listener);
                }
            }
        });

        //keyboard listener
        pullRequestsTree.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerEvent(CMD_OPEN_SELECTED_ITEM_IN_BROWSER);
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });
    }

    protected List<JBMenuItem> getMenuItems(final ActionListener listener) {
        return Arrays.asList(
                createMenuItem(TfPluginBundle.KEY_VCS_OPEN_IN_BROWSER, null, CMD_OPEN_SELECTED_ITEM_IN_BROWSER, listener),
                createMenuItem(TfPluginBundle.KEY_VCS_PR_ABANDON, null, VcsPullRequestsForm.CMD_ABANDON_SELECTED_PR, listener));
    }

    public Operation.CredInputsImpl getOperationInputs() {
        return new Operation.CredInputsImpl();
    }

    public void refresh(final boolean isTeamServicesRepository) {
        // nothing to refresh in this tab
    }

    @VisibleForTesting
    Tree getPullRequestTree() {
        return pullRequestsTree;
    }
}
