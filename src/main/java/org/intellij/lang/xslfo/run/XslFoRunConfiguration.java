package org.intellij.lang.xslfo.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

import org.apache.commons.lang.StringUtils;
import org.intellij.lang.xpath.xslt.XsltSupport;
import org.intellij.lang.xpath.xslt.associations.FileAssociationsManager;
import org.intellij.lang.xslfo.XslFoSettings;
import org.intellij.lang.xslfo.XslFoUtils;
import org.intellij.lang.xslfo.run.editor.XslFoRunConfigurationEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dmitry_Cherkas
 */
public final class XslFoRunConfiguration extends LocatableConfigurationBase<XslFoRunConfiguration>
    implements RunConfigurationWithSuppressedDefaultDebugAction, RunProfileWithCompileBeforeLaunchOption {

    private static final String NAME = "XSL-FO Configuration";

    private String mySuggestedName;

    private String myOutputFile;
    private boolean myOpenOutputFile;
    private boolean useTemporaryFiles;

    @Nullable
    private VirtualFilePointer myXsltFile = null;
    @Nullable
    private VirtualFilePointer myXmlInputFile = null;

    public XslFoRunConfiguration(Project project, ConfigurationFactory factory) {
        super(project, factory, NAME);
    }

    @Override
    public boolean isExcludeCompileBeforeLaunchOption() {
        return true;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
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

    @Override
    @SuppressWarnings({"unchecked"})
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);

        Element e = element.getChild("XsltFile");
        if (e != null) {
            final String url = e.getAttributeValue("url");
            if (url != null) {
                myXsltFile = VirtualFilePointerManager.getInstance().create(url, getProject(), null);
            }
        }
        e = element.getChild("XmlFile");
        if (e != null) {
            final String url = e.getAttributeValue("url");
            if (url != null) {
                myXmlInputFile = VirtualFilePointerManager.getInstance().create(url, getProject(), null);
            }
        }

        e = element.getChild("OutputFile");
        if (e != null) {
            myOutputFile = e.getAttributeValue("path");
            myOpenOutputFile = Boolean.valueOf(e.getAttributeValue("openOutputFile"));
        }
        useTemporaryFiles = Boolean.parseBoolean(element.getAttributeValue("useTemporaryFiles"));
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);

        Element e = new Element("XsltFile");
        if (myXsltFile != null) {
            e.setAttribute("url", myXsltFile.getUrl());
            element.addContent(e);
        }
        e = new Element("XmlFile");
        if (myXmlInputFile != null) {
            e.setAttribute("url", myXmlInputFile.getUrl());
            element.addContent(e);
        }
        e = new Element("OutputFile");
        if (myOutputFile != null) {
            e.setAttribute("path", myOutputFile);
            e.setAttribute("openOutputFile", Boolean.toString(myOpenOutputFile));
            element.addContent(e);
        }
        element.setAttribute("useTemporaryFiles", Boolean.toString(useTemporaryFiles));
    }

    @Override
    public RunConfiguration clone() {
        final XslFoRunConfiguration configuration = (XslFoRunConfiguration) super.clone();
        if (myXsltFile != null) {
            configuration.myXsltFile = VirtualFilePointerManager.getInstance().duplicate(myXsltFile, getProject(), null);
        }
        if (myXmlInputFile != null) {
            configuration.myXmlInputFile = VirtualFilePointerManager.getInstance().duplicate(myXmlInputFile, getProject(), null);
        }
        return configuration;
    }

    public void setXsltFile(@NotNull String xsltFile) {
        if (StringUtils.isEmpty(xsltFile)) {
            myXsltFile = null;
        } else {
            myXsltFile =
                    VirtualFilePointerManager.getInstance()
                            .create(VfsUtilCore.pathToUrl(xsltFile).replace(File.separatorChar, '/'), getProject(), null);
        }
    }

    public void setXsltFile(VirtualFile virtualFile) {
        myXsltFile = VirtualFilePointerManager.getInstance().create(virtualFile, getProject(), null);
    }

    @Nullable
    public String getXsltFile() {
        return myXsltFile != null ? myXsltFile.getPresentableUrl() : null;
    }

    @Nullable
    public VirtualFile findXsltFile() {
        return myXsltFile != null ? myXsltFile.getFile() : null;
    }

    public void setXmlInputFile(@NotNull String xmlInputFile) {
        if (StringUtils.isEmpty(xmlInputFile)) {
            myXmlInputFile = null;
        } else {
            myXmlInputFile =
                    VirtualFilePointerManager.getInstance()
                            .create(VfsUtilCore.pathToUrl(xmlInputFile).replace(File.separatorChar, '/'), getProject(), null);
        }
    }

    public void setXmlInputFile(VirtualFile xmlInputFile) {
        myXmlInputFile = VirtualFilePointerManager.getInstance().create(xmlInputFile, getProject(), null);
    }

    @Nullable
    public String getXmlInputFile() {
        return myXmlInputFile != null ? myXmlInputFile.getPresentableUrl() : null;
    }

    @Nullable
    public VirtualFile findXmlInputFile() {
        return myXmlInputFile != null ? myXmlInputFile.getFile() : null;
    }

    public boolean isOpenOutputFile() {
        return myOpenOutputFile;
    }

    public void setOpenOutputFile(boolean openOutputFile) {
        this.myOpenOutputFile = openOutputFile;
    }

    public String getOutputFile() {
        return myOutputFile;
    }

    public void setOutputFile(String outputFile) {
        this.myOutputFile = outputFile;
    }

    public void setUseTemporaryFiles(boolean useTemporaryFiles) {
        this.useTemporaryFiles = useTemporaryFiles;
    }

    public boolean isUseTemporaryFiles() {
        return useTemporaryFiles;
    }
}
