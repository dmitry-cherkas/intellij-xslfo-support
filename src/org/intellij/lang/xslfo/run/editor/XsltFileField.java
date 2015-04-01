package org.intellij.lang.xslfo.run.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import org.intellij.lang.xpath.xslt.XsltSupport;

/**
 * @author Dmitry_Cherkas
 */
public class XsltFileField extends TextFieldWithBrowseButton {

    private final FileChooserDescriptor myXsltDescriptor;

    public XsltFileField(final Project project) {

        final PsiManager psiManager = PsiManager.getInstance(project);

        myXsltDescriptor = new FileChooserDescriptor(true, false, false, false, false, false) {
            public boolean isFileVisible(final VirtualFile file, boolean showHiddenFiles) {
                if (file.isDirectory()) {
                    return true;
                }
                if (!super.isFileVisible(file, showHiddenFiles)) {
                    return false;
                }

                return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
                    public Boolean compute() {
                        final PsiFile psiFile = psiManager.findFile(file);
                        return psiFile != null && XsltSupport.isXsltFile(psiFile);
                    }
                });
            }
        };

        this.addBrowseFolderListener("Choose XSLT File", null, project, myXsltDescriptor, new ProjectDefaultAccessor(project));
    }

    public FileChooserDescriptor getDescriptor() {
        return myXsltDescriptor;
    }
}
