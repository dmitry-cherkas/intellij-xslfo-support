package org.intellij.lang.xslfo.run.editor;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ArrayUtil;

import org.intellij.lang.xpath.xslt.associations.FileAssociationsManager;
import org.intellij.lang.xpath.xslt.associations.impl.AnyXMLDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

/**
 * @author Dmitry_Cherkas
 */
public class XmlInputFileField extends ComboboxWithBrowseButton {

    private final AnyXMLDescriptor myXmlDescriptor;
    private final ProjectDefaultAccessor myProjectDefaultAccessor;

    private XsltFileField myXsltFileField;

    /**
     * Decision to inject XsltFileField as a dependency is questionable, but currently Xml field depends on Xsl field by design.
     */
    public XmlInputFileField(final Project project, XsltFileField xsltFileField) {
        this.getComboBox().setEditable(true);

        myXsltFileField = xsltFileField;
        myProjectDefaultAccessor = new ProjectDefaultAccessor(project);
        myXmlDescriptor = new AnyXMLDescriptor(false);

        this.addBrowseFolderListener("Choose XML File", null, project, myXmlDescriptor, new TextComponentAccessor<JComboBox>() {
            public String getText(JComboBox comboBox) {
                Object item = comboBox.getEditor().getItem();
                if (item.toString().length() == 0) {
                    final String text = myProjectDefaultAccessor.getText(myXsltFileField.getChildComponent());
                    final VirtualFile file =
                            VirtualFileManager.getInstance().findFileByUrl(VfsUtil.pathToUrl(text.replace(File.separatorChar, '/')));
                    if (file != null && !file.isDirectory()) {
                        final VirtualFile parent = file.getParent();
                        assert parent != null;
                        return parent.getPresentableUrl();
                    }
                }
                return item.toString();
            }

            public void setText(JComboBox comboBox, @NotNull String text) {
                comboBox.getEditor().setItem(text);
            }
        });

        myXsltFileField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            final PsiManager psiManager = PsiManager.getInstance(project);
            final VirtualFileManager fileMgr = VirtualFileManager.getInstance();
            final FileAssociationsManager associationsManager = FileAssociationsManager.getInstance(project);

            protected void textChanged(DocumentEvent e) {
                final String text = myXsltFileField.getText();
                final JComboBox comboBox = XmlInputFileField.this.getComboBox();
                final Object oldXml = getXmlInputFile();
                if (text.length() != 0) {
                    final ComboBoxModel model = comboBox.getModel();

                    boolean found = false;
                    for (int i = 0; i < model.getSize(); i++) {
                        if (oldXml.equals(model.getElementAt(i))) {
                            found = true;
                        }
                    }
                    final VirtualFile virtualFile = fileMgr.findFileByUrl(VfsUtil.pathToUrl(text.replace(File.separatorChar, '/')));
                    final PsiFile psiFile;
                    if (virtualFile != null && (psiFile = psiManager.findFile(virtualFile)) != null) {
                        final PsiFile[] files = associationsManager.getAssociationsFor(psiFile);

                        final Object[] associations = new String[files.length];
                        for (int i = 0; i < files.length; i++) {
                            final VirtualFile f = files[i].getVirtualFile();
                            assert f != null;
                            associations[i] = f.getPath().replace('/', File.separatorChar);
                        }
                        comboBox.setModel(new DefaultComboBoxModel(associations));
                    }
                    if (!found) {
                        comboBox.getEditor().setItem(oldXml);
                    }
                    comboBox.setSelectedItem(oldXml);
                } else {
                    comboBox.setModel(new DefaultComboBoxModel(ArrayUtil.EMPTY_OBJECT_ARRAY));
                    comboBox.getEditor().setItem(oldXml);
                }
            }
        });
    }

    public String getXmlInputFile() {
        final JComboBox comboBox = this.getComboBox();
        final Object currentItem = comboBox.getEditor().getItem();
        String s = (String) (currentItem != null ? currentItem : comboBox.getSelectedItem());
        return s != null ? s : "";
    }

    public FileChooserDescriptor getDescriptor() {
        return myXmlDescriptor;
    }
}
