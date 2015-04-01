package org.intellij.lang.xslfo.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.Platform;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;

import org.apache.commons.lang.StringUtils;
import org.intellij.lang.xslfo.run.editor.XslFoSettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dmitry_Cherkas
 */
public final class XslFoRunConfiguration extends XsltRunConfigurationBase {

    private static final String NAME = "XSL-FO Configuration";

    // FOP configuration
    @Nullable
    private VirtualFilePointer myFopUserConfig;
    @Nullable
    private VirtualFilePointer myFopInstallationDir;

    public XslFoRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory, NAME);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();

        if (getFopExecutablePath() == null) {
            throw new RuntimeConfigurationError("Selected FOP installation directory is invalid");
        }
    }

    @NotNull
    @Override
    public SettingsEditor<XslFoRunConfiguration> getConfigurationEditor() {
        return new XslFoSettingsEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        final VirtualFile baseFile = findXsltFile();
        if (baseFile == null) {
            throw new ExecutionException("No XSLT file selected");
        }

        // TODO add filters (see org.intellij.lang.xpath.xslt.run.XsltRunConfiguration.getState)

        return new XslFoCommandLineState(this, environment);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);

        Element e = element.getChild("FopInstallationDir");
        if (e != null) {
            final String url = e.getAttributeValue("url");
            if (url != null) {
                myFopInstallationDir = VirtualFilePointerManager.getInstance().create(url, getProject(), null);
            }
        }

        e = element.getChild("FopUserConfig");
        if (e != null) {
            final String url = e.getAttributeValue("url");
            if (url != null) {
                myFopUserConfig = VirtualFilePointerManager.getInstance().create(url, getProject(), null);
            }
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);

        if (myFopInstallationDir != null) {
            Element e = new Element("FopInstallationDir");
            e.setAttribute("url", myFopInstallationDir.getUrl());
            element.addContent(e);
        }

        if (myFopUserConfig != null) {
            Element e = new Element("FopUserConfig");
            e.setAttribute("url", myFopUserConfig.getUrl());
            element.addContent(e);
        }
    }

    @Nullable
    public VirtualFile getFopExecutablePath() {
        VirtualFile fopInstallationDir = getFopInstallationDir();
        if (fopInstallationDir == null) {
            return null;
        }
        String executableName;
        if (Platform.current() == Platform.WINDOWS) {
            executableName = "fop.bat";
        } else {
            executableName = "fop";
        }

        return fopInstallationDir.findChild(executableName);
    }

    @Override
    public boolean isSaveToFile() {
        return true;
    }

    @Override
    public void setSaveToFile(boolean saveToFile) {
        throw new UnsupportedOperationException("Result of XslFoRunConfiguration should always be saved to file!");
    }

    @Nullable
    public VirtualFile getFopInstallationDir() {
        return myFopInstallationDir != null ? myFopInstallationDir.getFile() : null;
    }

    public void setFopInstallationDir(String fopInstallationDir) {
        if (StringUtils.isEmpty(fopInstallationDir)) {
            myFopInstallationDir = null;
        } else {
            myFopInstallationDir =
                    VirtualFilePointerManager.getInstance()
                            .create(VfsUtilCore.pathToUrl(fopInstallationDir).replace(File.separatorChar, '/'), getProject(), null);
        }
    }

    @Nullable
    public VirtualFile getFopUserConfig() {
        return myFopUserConfig != null ? myFopUserConfig.getFile() : null;
    }

    public void setFopUserConfig(String fopUserConfigFile) {
        if (StringUtils.isEmpty(fopUserConfigFile)) {
            myFopUserConfig = null;
        } else {
            myFopUserConfig =
                    VirtualFilePointerManager.getInstance()
                            .create(VfsUtilCore.pathToUrl(fopUserConfigFile).replace(File.separatorChar, '/'), getProject(), null);
        }
    }
}
