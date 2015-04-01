package org.intellij.lang.xslfo.run.editor;

import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;

import org.intellij.lang.xslfo.run.XslFoRunConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoSettingsEditor extends SettingsEditor<XslFoRunConfiguration> {

    private final Project myProject;

    private XsltFileField myXsltFile;
    private XmlInputFileField myXmlInputFile;
    private JPanel myComponent;
    private TextFieldWithBrowseButton myOutputFile;
    private JCheckBox myOpenOutputFile;
    private JCheckBox myOpenInBrowser;
    private TextFieldWithBrowseButton myFopInstallationDir;
    private TextFieldWithBrowseButton myUserConfigLocation;

    public XslFoSettingsEditor(Project project) {
        this.myProject = project;

        myOutputFile.addBrowseFolderListener("Choose Output File", "The selected file will be overwritten during execution.",
                                             myProject, FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor());

        myFopInstallationDir.addBrowseFolderListener("Choose FOP Installation Directory",
                                                     "FOP installation directory should contain fop shell scripts (fop.bat,fop.cmd, etc).",
                                                     myProject, FileChooserDescriptorFactory.createSingleFolderDescriptor());

        myUserConfigLocation.addBrowseFolderListener("Choose User Configuration File", "Optional userconfig.xml file may be selected.",
                                                     myProject, FileChooserDescriptorFactory.createSingleFileDescriptor(StdFileTypes.XML));
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
        myOpenInBrowser.setSelected(s.isOpenInBrowser());

        VirtualFile fopInstallationDir = s.getFopInstallationDir();
        if (fopInstallationDir != null) {
            myFopInstallationDir.setText(fopInstallationDir.getPath());
        }

        VirtualFile fopUserConfig = s.getFopUserConfig();
        if (fopUserConfig != null) {
            myUserConfigLocation.setText(fopUserConfig.getPath());
        }

        FileChooserDescriptor xmlDescriptor = myXmlInputFile.getDescriptor();

        final VirtualFile xmlInputFile = s.findXmlInputFile();
        if (xmlInputFile != null) {
            final Module contextModule = ProjectRootManager.getInstance(s.getProject()).getFileIndex().getModuleForFile(xmlInputFile);
            if (contextModule != null) {
                xmlDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, contextModule);
            } else {
                xmlDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, s.getModule());
            }
        } else {
            xmlDescriptor.putUserData(LangDataKeys.MODULE_CONTEXT, s.getModule());
        }
    }

    @Override
    protected void applyEditorTo(XslFoRunConfiguration s) throws ConfigurationException {
        s.setXsltFile(myXsltFile.getText());
        s.setXmlInputFile(myXmlInputFile.getXmlInputFile());
        s.setOutputFile(myOutputFile.getText());
        s.setOpenOutputFile(myOpenOutputFile.isSelected());
        s.setOpenInBrowser(myOpenInBrowser.isSelected());
        s.setFopInstallationDir(myFopInstallationDir.getText());
        s.setFopUserConfig(myUserConfigLocation.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myComponent;
    }
}
