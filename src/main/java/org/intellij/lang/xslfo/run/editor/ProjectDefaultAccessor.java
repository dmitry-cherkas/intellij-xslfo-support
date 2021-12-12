package org.intellij.lang.xslfo.run.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
class ProjectDefaultAccessor implements TextComponentAccessor<JTextField> {

    private final Project project;

    public ProjectDefaultAccessor(Project project) {
        this.project = project;
    }

    public String getText(JTextField component) {
        final String text = component.getText();
        final VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
        return text.length() > 0 ? text : (baseDir != null ? baseDir.getPresentableUrl() : "");
    }

    public void setText(JTextField component, @NotNull String text) {
        component.setText(text);
    }
}
