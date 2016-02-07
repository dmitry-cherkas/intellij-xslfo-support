package org.intellij.lang.xslfo.run.editor;

import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;

import org.intellij.lang.xslfo.run.XslFoRunConfiguration;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoRunConfigurationEditor extends SettingsEditor<XslFoRunConfiguration> {

    private final Project myProject;

    private XsltFileField myXsltFile;
    private XmlInputFileField myXmlInputFile;
    private JPanel myComponent;
    private TextFieldWithBrowseButton myOutputFile;
    private JCheckBox myOpenOutputFile;
    private JCheckBox myUseTemporaryFiles;

    public XslFoRunConfigurationEditor(Project project) {
        this.myProject = project;

        myOutputFile.addBrowseFolderListener("Choose Output File", "The selected file will be overwritten during execution.",
                                             myProject, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor());
        myUseTemporaryFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateComponentsState();
            }
        });
        updateComponentsState();
    }

    private void createUIComponents() {
        myXsltFile = new XsltFileField(myProject);
        myXmlInputFile = new XmlInputFileField(myProject, myXsltFile);
    }


    /* SettingsEditor<XslFoRunConfiguration> implementation */
    @Override
    protected void resetEditorFrom(XslFoRunConfiguration s) {
        myXsltFile.setText(s.getXsltFile());
        myXmlInputFile.getComboBox().setSelectedItem(s.getXmlInputFile());
        myOutputFile.setText(s.getOutputFile());
        myOpenOutputFile.setSelected(s.isOpenOutputFile());
        myUseTemporaryFiles.setSelected(s.isUseTemporaryFiles());

        FileChooserDescriptor xmlDescriptor = myXmlInputFile.getDescriptor();

        final VirtualFile xmlInputFile = s.findXmlInputFile();
        if (xmlInputFile != null) {
            final Module contextModule = ProjectRootManager.getInstance(s.getProject()).getFileIndex().getModuleForFile(xmlInputFile);
            if (contextModule != null) {
                xmlDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, contextModule);
            }
        }
        updateComponentsState();
    }

    @Override
    protected void applyEditorTo(XslFoRunConfiguration s) throws ConfigurationException {
        s.setXsltFile(myXsltFile.getText());
        s.setXmlInputFile(myXmlInputFile.getXmlInputFile());
        s.setOutputFile(myOutputFile.getText());
        s.setOpenOutputFile(myOpenOutputFile.isSelected());
        s.setUseTemporaryFiles(myUseTemporaryFiles.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myComponent;
    }

    private void updateComponentsState() {
        myOutputFile.setEnabled(!myUseTemporaryFiles.isSelected());
        myOpenOutputFile.setEnabled(!myUseTemporaryFiles.isSelected());

        if (myUseTemporaryFiles.isSelected()) {
            myOpenOutputFile.setSelected(true);
        }
    }
}
