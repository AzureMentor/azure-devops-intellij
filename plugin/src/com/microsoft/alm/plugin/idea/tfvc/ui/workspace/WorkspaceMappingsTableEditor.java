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

package com.microsoft.alm.plugin.idea.tfvc.ui.workspace;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.util.ui.AbstractTableCellEditor;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.LocalPathCellEditor;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.ValidatingTableEditor;
import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.external.models.Workspace;
import com.microsoft.alm.plugin.idea.common.resources.TfPluginBundle;
import com.microsoft.alm.plugin.idea.common.utils.VcsHelper;
import com.microsoft.alm.plugin.idea.tfvc.core.TFSVcs;
import com.microsoft.alm.plugin.idea.tfvc.ui.ServerPathCellEditor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ComboBoxModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.alm.plugin.idea.tfvc.core.tfs.VersionControlPath.ROOT_FOLDER;

//TODO: This class is currently not very testable. We could reimplement it and avoid deriving from the IntelliJ class
public class WorkspaceMappingsTableEditor extends ValidatingTableEditor<WorkspaceMappingsTableEditor.Row> {
    public static Logger logger = LoggerFactory.getLogger(WorkspaceMappingsTableEditor.class);

    private final Project project;
    private final ServerContext serverContext;
    private final String defaultLocalPath;
    private final ValidationDispatcher validationDispatcher;

    public interface ValidationDispatcher {
        void showValidationError(final String errorMessage);
    }

    public enum MappingType {
        MAPPED,
        CLOAKED
    }

    public class Row {
        public Row(final Row otherRow) {
            this.serverPath = otherRow.serverPath;
            this.localPath = otherRow.localPath;
            this.mappingType = otherRow.mappingType;
        }

        public Row(final String serverPath, final String localPath, final MappingType mappingType) {
            this.serverPath = serverPath;
            this.localPath = localPath;
            this.mappingType = mappingType;
        }

        public MappingType mappingType;
        public String serverPath;
        public String localPath;
    }

    public WorkspaceMappingsTableEditor(final Project project, final ServerContext serverContext,
                                        final String defaultLocalPath, final ValidationDispatcher validationDispatcher) {
        this.defaultLocalPath = defaultLocalPath;
        this.project = project;
        this.serverContext = serverContext;
        this.validationDispatcher = validationDispatcher;
    }

    public void setMappings(final List<Workspace.Mapping> mappings) {
        if (mappings != null) {
            final List<Row> rows = new ArrayList<Row>(mappings.size());
            for (final Workspace.Mapping mapping : mappings) {
                rows.add(new Row(mapping.getServerPath(), mapping.getLocalPath(),
                        mapping.isCloaked() ? MappingType.CLOAKED : MappingType.MAPPED));
            }
            setModel(new ColumnInfo[]{new MappingTypeColumn(), new ServerPathColumn(project, serverContext), new LocalPathColumn(project)}, rows);
        }
    }

    public List<Workspace.Mapping> getMappings() {
        final List<Workspace.Mapping> result = new ArrayList<Workspace.Mapping>(getItems().size());
        for (final Row r : getItems()) {
            final Workspace.Mapping mapping = new Workspace.Mapping(r.serverPath, r.localPath, r.mappingType == MappingType.CLOAKED);
            result.add(mapping);
        }
        return result;
    }

    @Override
    protected Row cloneOf(final Row item) {
        return new Row(item);
    }

    @Override
    protected Row createItem() {
        final List<Row> items = getItems();
        // make the new row a copy of the last row
        if (items != null && items.size() > 0) {
            return new Row(items.get(items.size() - 1));
        } else {
            // if no rows exist then get the path from the team project
            final ServerContext serverContext = TFSVcs.getInstance(project).getServerContext(false);
            if (serverContext != null && serverContext.getTeamProjectReference() != null && StringUtils.isNotEmpty(serverContext.getTeamProjectReference().getName())) {
                return new Row(ROOT_FOLDER.concat(serverContext.getTeamProjectReference().getName()), defaultLocalPath, MappingType.MAPPED);
            }
            // not great if we get here but browse will still work except the root name won't populate
            logger.info("No rows or team project could be found so passing in empty root to new row");
            return new Row(StringUtils.EMPTY, defaultLocalPath, MappingType.MAPPED);
        }
    }

    public String getFirstValidationError() {
        for (final Row r : getItems()) {
            final String error = validate(r);
            if (StringUtils.isNotEmpty(error)) {
                return error;
            }
        }

        return null;
    }

    @Nullable
    protected String validate(final Row item) {
        if (StringUtils.isEmpty(item.serverPath)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_ERRORS_SERVER_PATH_EMPTY);
        }
        if (!item.serverPath.startsWith(VcsHelper.TFVC_ROOT)) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_ERRORS_SERVER_PATH_INVALID);
        }
        if (StringUtils.isEmpty(item.localPath) && item.mappingType == MappingType.MAPPED) {
            return TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_ERRORS_LOCAL_PATH_EMPTY);
        }
        return null;
    }

    @Override
    protected void displayMessageAndFix(@Nullable final Pair<String, Fix> messageAndFix) {
        if (validationDispatcher != null) {
            final String validationMessage = messageAndFix != null ? messageAndFix.first : null;
            if (StringUtils.isNotEmpty(validationMessage)) {
                validationDispatcher.showValidationError(validationMessage);
            }
        }
    }

    @NotNull
    @Override
    public StatusText getEmptyText() {
        return new StatusText() {
            @Override
            protected boolean isStatusVisible() {
                return true;
            }
        };
    }

    private static class MappingTypeColumn extends ColumnInfo<Row, MappingType> {
        public MappingTypeColumn() {
            super(TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_COLUMN_HEADERS_STATUS));
        }

        @Override
        public MappingType valueOf(final Row item) {
            return item.mappingType;
        }

        @Override
        public void setValue(final Row item, final MappingType value) {
            item.mappingType = value;
        }

        @Override
        public boolean isCellEditable(final Row item) {
            return true;
        }

        @Override
        public int getWidth(final JTable table) {
            return JBUI.scale(80);
        }

        @Override
        public TableCellEditor getEditor(final Row item) {
            return new AbstractTableCellEditor() {
                private ComboBox myCombo;

                public Object getCellEditorValue() {
                    return myCombo.getSelectedItem();
                }

                public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
                    final ComboBoxModel model = new EnumComboBoxModel<MappingType>(MappingType.class);
                    model.setSelectedItem(value);
                    myCombo = new ComboBox(model, getWidth(table));
                    return myCombo;
                }
            };
        }
    }

    private class LocalPathColumn extends ColumnInfo<Row, String> implements ValidatingTableEditor.RowHeightProvider {
        private final Project project;

        public LocalPathColumn(final Project project) {
            super(TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_COLUMN_HEADERS_LOCAL_PATH));
            this.project = project;
        }

        @Override
        public String valueOf(final Row item) {
            return item.localPath;
        }

        @Override
        public boolean isCellEditable(final Row Row) {
            return true;
        }

        @Override
        public void setValue(final Row item, final String value) {
            item.localPath = value;
        }

        @Override
        public TableCellEditor getEditor(final Row item) {
            return new LocalPathCellEditor(this.getName(), project);
        }

        public int getRowHeight() {
            return new JTextField().getPreferredSize().height + 1;
        }
    }

    private static class ServerPathColumn extends ColumnInfo<Row, String> {
        private final Project project;
        private final ServerContext serverContext;

        public ServerPathColumn(final Project project, final ServerContext serverContext) {
            super(TfPluginBundle.message(TfPluginBundle.KEY_WORKSPACE_DIALOG_COLUMN_HEADERS_SERVER_PATH));
            this.project = project;
            this.serverContext = serverContext;
        }

        @Override
        public String valueOf(final Row item) {
            return item.serverPath;
        }

        @Override
        public void setValue(final Row item, final String value) {
            item.serverPath = value;
        }

        @Override
        public boolean isCellEditable(final Row item) {
            return true;
        }

        @Override
        public TableCellEditor getEditor(final Row item) {
            return new ServerPathCellEditor(this.getName(), project, serverContext);
        }
    }
}
