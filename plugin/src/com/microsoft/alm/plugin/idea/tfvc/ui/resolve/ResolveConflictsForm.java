// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.microsoft.alm.plugin.idea.tfvc.ui.resolve;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.ui.common.forms.BasicForm;
import org.jetbrains.annotations.NonNls;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionListener;

public class ResolveConflictsForm implements BasicForm {

    @VisibleForTesting
    protected JTable myItemsTable;
    private JPanel myContentPanel;
    @VisibleForTesting
    protected JButton myAcceptYoursButton;
    @VisibleForTesting
    protected JButton myAcceptTheirsButton;
    @VisibleForTesting
    protected JButton myMergeButton;
    private boolean initialized = false;
    private boolean isLoading = false;

    @NonNls
    public static final String CMD_MERGE = "merge";
    @NonNls
    public static final String CMD_ACCEPT_YOURS = "acceptYours";
    @NonNls
    public static final String CMD_ACCEPT_THEIRS = "acceptTheirs";

    public ResolveConflictsForm() {
        myItemsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public JPanel getContentPanel() {
        ensureInitialized();
        return myContentPanel;
    }

    private void ensureInitialized() {
        if (!initialized) {
            myAcceptYoursButton.setText(TfPluginBundle.message(TfPluginBundle.KEY_TFVC_CONFLICT_DIALOG_ACCEPT_YOURS));
            myAcceptTheirsButton.setText(TfPluginBundle.message(TfPluginBundle.KEY_TFVC_CONFLICT_DIALOG_ACCEPT_THEIRS));
            myMergeButton.setText(TfPluginBundle.message(TfPluginBundle.KEY_TFVC_CONFLICT_DIALOG_MERGE));

            myAcceptTheirsButton.setActionCommand(CMD_ACCEPT_THEIRS);
            myAcceptYoursButton.setActionCommand(CMD_ACCEPT_YOURS);
            myMergeButton.setActionCommand(CMD_MERGE);

            myItemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(final ListSelectionEvent se) {
                    int[] selectedIndices = myItemsTable.getSelectedRows();
                    enableButtons(selectedIndices);
                }
            });

            initialized = true;
        }
    }

    public void setModelForView(final ConflictsTableModel conflictsTableModel) {
        myItemsTable.setModel(conflictsTableModel);

        if (myItemsTable.getColumnModel() != null && myItemsTable.getColumnModel().getColumnCount() > 1) {
            // Set the alignment up for Type column in the table
            final DefaultTableCellRenderer typeRenderer = new DefaultTableCellRenderer();
            typeRenderer.setHorizontalAlignment(JLabel.CENTER);
            myItemsTable.getColumnModel().getColumn(1).setCellRenderer(typeRenderer);
        }
    }

    public void setLoading(final boolean isLoading) {
        this.isLoading = isLoading;

        myItemsTable.setCellSelectionEnabled(!isLoading);

        // disable buttons when loading, they will be enabled when the table populates if they are applicable
        if (isLoading) {
            myItemsTable.setForeground(Color.GRAY);
            myAcceptYoursButton.setEnabled(false);
            myAcceptTheirsButton.setEnabled(false);
            myMergeButton.setEnabled(false);
        } else {
            myItemsTable.setForeground(Color.BLACK);
        }
    }

    public void addActionListener(final ActionListener listener) {
        myAcceptYoursButton.addActionListener(listener);
        myAcceptTheirsButton.addActionListener(listener);
        myMergeButton.addActionListener(listener);
    }

    @VisibleForTesting
    protected void enableButtons(final int[] selectedIndices) {
        myAcceptYoursButton.setEnabled(!isLoading && selectedIndices.length > 0);
        myAcceptTheirsButton.setEnabled(!isLoading && selectedIndices.length > 0);
        // treating merge like other buttons and not checking if merge should not be allowed
        // if a merge can't be done then we will present the user with an error later on
        myMergeButton.setEnabled(!isLoading && selectedIndices.length > 0);
    }

    public int[] getSelectedRows() {
        return myItemsTable.getSelectedRows();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myItemsTable;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        myContentPanel = new JPanel();
        myContentPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        myContentPanel.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myAcceptYoursButton = new JButton();
        myAcceptYoursButton.setEnabled(false);
        myAcceptYoursButton.setText("Accept Yours");
        myAcceptYoursButton.setMnemonic('Y');
        myAcceptYoursButton.setDisplayedMnemonicIndex(7);
        panel1.add(myAcceptYoursButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        myAcceptTheirsButton = new JButton();
        myAcceptTheirsButton.setEnabled(false);
        myAcceptTheirsButton.setText("Accept Theirs");
        myAcceptTheirsButton.setMnemonic('T');
        myAcceptTheirsButton.setDisplayedMnemonicIndex(7);
        panel1.add(myAcceptTheirsButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        myMergeButton = new JButton();
        myMergeButton.setEnabled(false);
        myMergeButton.setText("Merge");
        myMergeButton.setMnemonic('M');
        myMergeButton.setDisplayedMnemonicIndex(0);
        panel1.add(myMergeButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JBScrollPane jBScrollPane1 = new JBScrollPane();
        myContentPanel.add(jBScrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        myItemsTable = new JBTable();
        myItemsTable.putClientProperty("Table.isFileList", Boolean.FALSE);
        jBScrollPane1.setViewportView(myItemsTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return myContentPanel;
    }
}


