// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.common.ui.workitem;

import com.intellij.openapi.project.Project;
import com.microsoft.alm.plugin.events.ServerEvent;
import com.microsoft.alm.plugin.idea.common.ui.common.tabs.TabControllerImpl;
import com.microsoft.alm.plugin.idea.common.ui.common.tabs.TabImpl;
import com.microsoft.alm.plugin.idea.common.ui.controls.WorkItemQueryDropDown;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.util.Observable;

/**
 * Controller for the WorkItems VC tab
 */
public class VcsWorkItemsController extends TabControllerImpl<VcsWorkItemsModel> {
    private static final String EVENT_NAME = "Work Items";
    private static final String WIT_TAB_CREATE_BRANCH_SELECTED_ACTION = "wit-tab-create-branch-selected";

    public VcsWorkItemsController(final @NotNull Project project) {
        super(new TabImpl(new VcsWorkItemsForm(project), EVENT_NAME), new VcsWorkItemsModel(project),
                new ServerEvent[]{ServerEvent.WORK_ITEMS_CHANGED});
    }

    @Override
    protected void performAction(final ActionEvent e) {
        if (VcsWorkItemsForm.CMD_OPEN_SELECTED_ITEM_IN_BROWSER.equals(e.getActionCommand())) {
            //pop up menu - open WIT link in web
            model.openSelectedItemsLink();
        } else if (VcsWorkItemsForm.CMD_CREATE_BRANCH.equals(e.getActionCommand())) {
            //create a new remote branch and add a link for it in the work item
            model.createBranch();
        } else if (WorkItemQueryDropDown.CMD_QUERY_COMBO_BOX_CHANGED.equals(e.getActionCommand())) {
            // reload data if the query selected has changed
            model.loadData();
        } else {
            super.performAction(e);
        }
    }

    @Override
    public void update(final Observable observable, final Object arg) {
        if (VcsWorkItemsModel.CONTEXT_FOUND.equals(arg)) {
            tab.refresh(model.isTeamServicesRepository());
        }

        super.update(observable, arg);
    }
}
