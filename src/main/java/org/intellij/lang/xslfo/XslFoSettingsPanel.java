package org.intellij.lang.xslfo;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoSettingsPanel {
    private JPanel myPanel;
    private TextFieldWithBrowseButton myFopInstallationDir;
    private TextFieldWithBrowseButton myUserConfigLocation;
    private JPanel myValidationPanel;
    private JLabel myWarningLabel;
    private JSeparator mySeparator;

    public XslFoSettingsPanel() {
        myFopInstallationDir.addBrowseFolderListener("Choose FOP Installation Directory",
                "FOP installation directory should contain fop shell scripts (fop.bat,fop.cmd, etc).",
                null, FileChooserDescriptorFactory.createSingleFolderDescriptor());

        myUserConfigLocation.addBrowseFolderListener("Choose User Configuration File", "Optional userconfig.xml file may be selected.",
                null, FileChooserDescriptorFactory.createSingleFileDescriptor(StdFileTypes.XML));

        // configure Settings Validation
        myWarningLabel.setIcon(AllIcons.General.BalloonError);
        MySettingsPanelChangeListener changeListener = new MySettingsPanelChangeListener();

        myFopInstallationDir.getTextField().getDocument().addDocumentListener(changeListener);
        myUserConfigLocation.getTextField().getDocument().addDocumentListener(changeListener);
        myPanel.addComponentListener(changeListener);

    }

    public JComponent getComponent() {
        return myPanel;
    }

    public String getFopInstallationDir() {
        return myFopInstallationDir.getText();
    }

    public String getUserConfigLocation() {
        return myUserConfigLocation.getText();
    }

    public void setFopInstallationDir(String fopInstallationDir) {
        myFopInstallationDir.setText(fopInstallationDir);
    }

    public void setUserConfigLocation(String userConfigLocation) {
        myUserConfigLocation.setText(userConfigLocation);
    }

    private String validateSettings() {
        if (XslFoUtils.findFopExecutable(myFopInstallationDir.getText()) == null) {
            return "<html><body><b>Error: </b>Selected FOP installation directory is invalid</body></html>";
        } else {
            return "";
        }
    }

    private class MySettingsPanelChangeListener implements ComponentListener, DocumentListener {

        @Override
        public void componentShown(ComponentEvent e) {
            updateWarning();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            updateWarning();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            updateWarning();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            updateWarning();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateWarning();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateWarning();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateWarning();
        }

        private void updateWarning() {
            String errorMsg = validateSettings();

            if (errorMsg.isEmpty()) {
                // no errors, hide validation panel
                mySeparator.setVisible(false);
                myWarningLabel.setVisible(false);
                myValidationPanel.setVisible(false);
            } else {
                mySeparator.setVisible(true);
                myWarningLabel.setVisible(true);
                myWarningLabel.setText(errorMsg);
                myValidationPanel.setVisible(true);
            }
        }
    }
}
