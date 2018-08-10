// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.common.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.microsoft.alm.plugin.idea.common.resources.Icons;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.ui.workitem.SelectWorkItemsDialog;
import com.microsoft.alm.plugin.idea.common.utils.IdeaHelper;
import com.microsoft.alm.plugin.idea.common.utils.VcsHelper;
import com.microsoft.alm.plugin.idea.git.utils.TfGitHelper;
import com.microsoft.alm.plugin.idea.tfvc.core.TFSVcs;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SelectWorkItemsAction extends DumbAwareAction {
    public static final Logger logger = LoggerFactory.getLogger(SelectWorkItemsAction.class);

    public SelectWorkItemsAction() {
        super(TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_SELECT_WORK_ITEMS_TITLE),
                TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_SELECT_WORK_ITEMS_MSG),
                Icons.WIT_ADD);
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        final Project project = CommonDataKeys.PROJECT.getData(anActionEvent.getDataContext());

        // if this is a non-VSTS repo and is in Rider then hide the button
        if (IdeaHelper.isRider() && !VcsHelper.isVstsRepo(project)) {
            anActionEvent.getPresentation().setVisible(false);
            return;
        }

        // for all other IDEs make the button visible but disable it if a repo can't be detected or critical information is missing
        anActionEvent.getPresentation().setVisible(true);
        boolean disableButton;
        try {
            // project might be null or in an unexpected state if it not a TFS repo which causes exceptions to be thrown
            // this is a weird situation since the Commit dialog can be brought up in many situations
            disableButton = (project == null || (TfGitHelper.getTfGitRepository(project) == null && TFSVcs.getInstance(project) == null) ||
                    VcsHelper.getRepositoryContext(project) == null);
        } catch (Exception e) {
            logger.warn("Exception finding if project is TFS for work item association", e);
            disableButton = true;
        }

        if (disableButton) {
            anActionEvent.getPresentation().setEnabled(false);
            // change hover text to explain why button is disabled
            anActionEvent.getPresentation().setText(TfPluginBundle.message(TfPluginBundle.KEY_ERRORS_NOT_TFS_REPO,
                    TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_SELECT_WORK_ITEMS_ACTION)));
        } else {
            anActionEvent.getPresentation().setEnabled(true);
            // update hover text in case it was disabled before
            anActionEvent.getPresentation().setText(TfPluginBundle.message(TfPluginBundle.KEY_ACTIONS_SELECT_WORK_ITEMS_TITLE));
        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final DataContext dc = anActionEvent.getDataContext();
        final Project project = CommonDataKeys.PROJECT.getData(dc);
        final Refreshable panel = CheckinProjectPanel.PANEL_KEY.getData(dc);
        final CommitMessageI commitMessageI = (panel instanceof CommitMessageI) ? (CommitMessageI) panel : VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(dc);

        if (commitMessageI != null && project != null) {
            String commitMessage = "";
            // Attempt to append the message instead of overwriting it
            if (commitMessageI instanceof CommitChangeListDialog) {
                commitMessage = ((CommitChangeListDialog) commitMessageI).getCommitMessage();
            }

            SelectWorkItemsDialog dialog = new SelectWorkItemsDialog(project);
            if (dialog.showAndGet()) {
                if (StringUtils.isNotEmpty(commitMessage)) {
                    commitMessage += "\n" + dialog.getComment();
                } else {
                    commitMessage = dialog.getComment();
                }

                commitMessageI.setCommitMessage(commitMessage);
            }
        }
    }
}
