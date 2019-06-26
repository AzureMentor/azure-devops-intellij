// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.common.ui.authentication;

import com.microsoft.alm.auth.oauth.DeviceFlowResponse;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.ui.common.BaseDialogImpl;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.util.Collections;

public class DeviceFlowResponsePromptDialog extends BaseDialogImpl {

    public static final String DEVICE_FLOW_PROMPT_DIALOG_CONTEXT = "DeviceFlowLogin";

    private DeviceFlowResponsePromptForm deviceFlowResponsePromptForm;

    public DeviceFlowResponsePromptDialog(){
        super(null,
                TfPluginBundle.message(TfPluginBundle.KEY_DEVICE_FLOW_PROMPT_TITLE),
                TfPluginBundle.message(TfPluginBundle.KEY_DEVICE_FLOW_PROMPT_CONTINUE_BUTTON),
                DEVICE_FLOW_PROMPT_DIALOG_CONTEXT, //feedback context
                false, Collections.<String, Object>emptyMap());
    }

    public void setResponse(final DeviceFlowResponse response) {
        this.deviceFlowResponsePromptForm.setResponse(response);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        deviceFlowResponsePromptForm = new DeviceFlowResponsePromptForm();
        final JPanel deviceFlowResponsePanel = deviceFlowResponsePromptForm.getContentPanel();
        deviceFlowResponsePanel.setPreferredSize(new Dimension(360, 140));
        return deviceFlowResponsePanel;
    }

    public void dismiss() {
        this.dispose();
    }
}
