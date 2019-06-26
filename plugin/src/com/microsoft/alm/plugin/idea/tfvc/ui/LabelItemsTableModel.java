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

package com.microsoft.alm.plugin.idea.tfvc.ui;

import com.intellij.util.ui.JBUI;
import com.microsoft.alm.plugin.external.models.ItemInfo;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

public class LabelItemsTableModel extends AbstractTableModel {

    enum Column {
        Item(TfPluginBundle.message(TfPluginBundle.KEY_TFVC_LABEL_DIALOG_ITEM_COLUMN), 300) {
            public String getValue(final ItemInfo itemInfo) {
                return itemInfo.getServerItem();
            }
        },
        Version(TfPluginBundle.message(TfPluginBundle.KEY_TFVC_LABEL_DIALOG_VERSION_COLUMN), 100) {
            public String getValue(final ItemInfo itemInfo) {
                return itemInfo.getServerVersion();
            }
        };

        private final String name;
        private final int width;

        Column(final String name, final int width) {
            this.name = name;
            this.width = JBUI.scale(width);
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public abstract String getValue(final ItemInfo itemInfo);
    }

    private List<ItemInfo> items;

    public LabelItemsTableModel() {
        items = Collections.emptyList();
    }

    public void setItems(final @NotNull List<ItemInfo> items) {
        this.items = items;
        fireTableDataChanged();
    }

    public int getRowCount() {
        return items.size();
    }

    public int getColumnCount() {
        return Column.values().length;
    }

    public String getColumnName(final int column) {
        return Column.values()[column].getName();
    }

    public Object getValueAt(final int rowIndex, final int columnIndex) {
        return Column.values()[columnIndex].getValue(getItem(rowIndex));
    }

    public ItemInfo getItem(final int rowIndex) {
        return items.get(rowIndex);
    }

}
