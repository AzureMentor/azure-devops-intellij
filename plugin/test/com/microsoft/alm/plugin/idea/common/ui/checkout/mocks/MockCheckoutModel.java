// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.common.ui.checkout.mocks;

import com.microsoft.alm.plugin.idea.common.ui.checkout.CheckoutModel;
import com.microsoft.alm.plugin.idea.common.ui.common.ServerContextTableModel;
import com.microsoft.alm.plugin.idea.git.ui.checkout.GitCheckoutModel;

public class MockCheckoutModel extends CheckoutModel {
    public MockCheckoutModel() {
        super(null, null, new GitCheckoutModel(), new MockCheckoutPageModel(null, ServerContextTableModel.VSO_GIT_REPO_COLUMNS),
                new MockCheckoutPageModel(null, ServerContextTableModel.TFS_GIT_REPO_COLUMNS), true);
        ((MockCheckoutPageModel) getVsoModel()).initialize(this);
        ((MockCheckoutPageModel) getTfsModel()).initialize(this);
    }

    public MockCheckoutPageModel getMockVsoModel() {
        return (MockCheckoutPageModel) getVsoModel();
    }

    public MockCheckoutPageModel getMockTfsModel() {
        return (MockCheckoutPageModel) getTfsModel();
    }
}
