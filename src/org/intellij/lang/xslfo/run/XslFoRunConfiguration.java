package org.intellij.lang.xslfo.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

import org.intellij.lang.xpath.xslt.XsltSupport;
import org.intellij.lang.xpath.xslt.associations.FileAssociationsManager;
import org.intellij.lang.xslfo.XslFoSettings;
import org.intellij.lang.xslfo.XslFoUtils;
import org.intellij.lang.xslfo.run.editor.XslFoRunConfigurationEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry_Cherkas
 */
public final class XslFoRunConfiguration extends XsltRunConfigurationBase {

    private static final String NAME = "XSL-FO Configuration";

    private String mySuggestedName;

    public XslFoRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory, NAME);
    }

    @Override
    public boolean isCompileBeforeLaunchAddedByDefault() {
        return false;
    }

    @Override
    public boolean excludeCompileBeforeLaunchOption() {
        return true;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        super.checkConfiguration();

        if (XslFoUtils.findFopExecutable(XslFoSettings.getInstance().getFopInstallationDir()) == null) {
            throw new RuntimeConfigurationError("FOP executable not found. Please edit settings (Settings -> Languages & Frameworks -> XSL-FO)");
        }
    }

    @NotNull
    @Override
    public SettingsEditor<XslFoRunConfiguration> getConfigurationEditor() {
        return new XslFoRunConfigurationEditor(getProject());
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
    public String suggestedName() {
        return mySuggestedName;
    }

    public void setupFromFile(@NotNull XmlFile file) {
        assert XsltSupport.isXsltFile(file) : "Not an XSLT file: " + file.getName();
        mySuggestedName = file.getName();
        setGeneratedName();

        final VirtualFile virtualFile = file.getVirtualFile();
        assert virtualFile != null : "No VirtualFile for " + file.getName();

        setXsltFile(virtualFile);

        final PsiFile[] associations = FileAssociationsManager.getInstance(file.getProject()).getAssociationsFor(file);
        if (associations.length > 0) {
            final VirtualFile assoc = associations[0].getVirtualFile();
            assert assoc != null;
            setXmlInputFile(assoc);
        }
    }
}
