package org.intellij.lang.xslfo;

import com.google.common.base.Objects;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoConfigurable implements SearchableConfigurable, Configurable.NoScroll, Disposable {

    private final XslFoSettings mySettings = XslFoSettings.getInstance();
    private XslFoSettingsPanel mySettingsPanel;

    @Nls
    @Override
    public String getDisplayName() {
        return "XSL-FO";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return getId();
    }

    @NotNull
    @Override
    public String getId() {
        return "settings.xslfo";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (mySettingsPanel == null) {
            mySettingsPanel = new XslFoSettingsPanel();
        }
        reset();
        return mySettingsPanel.getComponent();
    }

    @Override
    public boolean isModified() {
        return mySettingsPanel == null
                || !Objects.equal(mySettings.getFopInstallationDir(), mySettingsPanel.getFopInstallationDir())
                || !Objects.equal(mySettings.getUserConfigLocation(), mySettingsPanel.getUserConfigLocation());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (mySettingsPanel != null) {
            mySettings.setFopInstallationDir(mySettingsPanel.getFopInstallationDir());
            mySettings.setUserConfigLocation(mySettingsPanel.getUserConfigLocation());
        }
    }

    @Override
    public void reset() {
        if (mySettingsPanel != null) {
            mySettingsPanel.setFopInstallationDir(mySettings.getFopInstallationDir());
            mySettingsPanel.setUserConfigLocation(mySettings.getUserConfigLocation());
        }
    }

    @Override
    public void disposeUIResources() {
        Disposer.dispose(this);
    }

    @Override
    public void dispose() {
        mySettingsPanel = null;
    }
}
