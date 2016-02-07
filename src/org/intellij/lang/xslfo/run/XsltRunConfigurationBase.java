package org.intellij.lang.xslfo.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.ModuleRunConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dmitry_Cherkas
 */
public abstract class XsltRunConfigurationBase extends LocatableConfigurationBase
        implements ModuleRunConfiguration, RunConfigurationWithSuppressedDefaultDebugAction {

    private String myOutputFile;
    private boolean myOpenOutputFile;
    private boolean myOpenInBrowser;

    @Nullable
    private VirtualFilePointer myXsltFile = null;
    @Nullable
    private VirtualFilePointer myXmlInputFile = null;

    public String myModule;

    protected XsltRunConfigurationBase(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (myXsltFile == null) {
            throw new RuntimeConfigurationError("No XSLT File selected");
        }
        if (myXsltFile.getFile() == null) {
            throw new RuntimeConfigurationError("Selected XSLT File not found");
        }
        if (myXmlInputFile == null) {
            throw new RuntimeConfigurationError("No XML Input File selected");
        }
        if (myXmlInputFile.getFile() == null) {
            throw new RuntimeConfigurationError("Selected XML Input File not found");
        }

        if (StringUtils.isEmpty(getOutputFile())) {
            throw new RuntimeConfigurationError("No output file selected");
        }
        final File f = new File(getOutputFile());
        if (f.isDirectory()) {
            throw new RuntimeConfigurationError("Selected output file points to a directory");
        } else if (f.exists() && !f.canWrite()) {
            throw new RuntimeConfigurationError("Selected output file is not writable");
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        DefaultJDOMExternalizer.readExternal(this, element);

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
            myOpenInBrowser = Boolean.valueOf(e.getAttributeValue("openInBrowser"));
        }
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        DefaultJDOMExternalizer.writeExternal(this, element);

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
            e.setAttribute("openInBrowser", Boolean.toString(myOpenInBrowser));
            element.addContent(e);
        }
    }

    public void setModule(Module module) {
        myModule = module != null ? module.getName() : null;
    }

    @Nullable
    public Module getModule() {
        return myModule != null ? ModuleManager.getInstance(getProject()).findModuleByName(myModule) : null;
    }

    // return modules to compile before run. Null or empty list to make project
    @Override
    @NotNull
    public Module[] getModules() {
        return getModule() != null ? new Module[]{getModule()} : Module.EMPTY_ARRAY;
    }

    @Override
    public RunConfiguration clone() {
        final XsltRunConfigurationBase configuration = (XsltRunConfigurationBase) super.clone();
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

    public boolean isOpenInBrowser() {
        return myOpenInBrowser;
    }

    public void setOpenInBrowser(boolean openInBrowser) {
        this.myOpenInBrowser = openInBrowser;
    }

    public String getOutputFile() {
        return myOutputFile;
    }

    public void setOutputFile(String outputFile) {
        this.myOutputFile = outputFile;
    }
}
