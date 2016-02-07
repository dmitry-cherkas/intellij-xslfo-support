package org.intellij.lang.xslfo.run;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import org.apache.commons.lang.StringUtils;
import org.intellij.lang.xslfo.XslFoSettings;
import org.intellij.lang.xslfo.XslFoUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoCommandLineState extends CommandLineState {

    private final XslFoSettings mySettings = XslFoSettings.getInstance();
    private final XslFoRunConfiguration myXslFoRunConfiguration;

    public XslFoCommandLineState(XslFoRunConfiguration xslFoRunConfiguration,
                                 ExecutionEnvironment environment) {
        super(environment);

        this.myXslFoRunConfiguration = xslFoRunConfiguration;
    }

    @NotNull
    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        final GeneralCommandLine commandLine = buildCommandLine();

        final OSProcessHandler processHandler = new OSProcessHandler(commandLine.createProcess(), commandLine.getCommandLineString()) {
            @Override
            public Charset getCharset() {
                return commandLine.getCharset();
            }
        };
        ProcessTerminatedListener.attach(processHandler);
        final org.intellij.lang.xslfo.run.XsltRunConfigurationBase runConfiguration = myXslFoRunConfiguration;
        processHandler.addProcessListener(new ProcessAdapter() {
            private final XsltRunConfigurationBase myXsltRunConfiguration = runConfiguration;

            @Override
            public void processTerminated(final ProcessEvent event) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (event.getExitCode() == 0) {
                                    if (myXsltRunConfiguration.isOpenInBrowser()) {
                                        BrowserUtil.browse(myXsltRunConfiguration.getOutputFile());
                                    }
                                    if (myXsltRunConfiguration.isOpenOutputFile()) {
                                        final String url = VfsUtilCore.pathToUrl(myXsltRunConfiguration.getOutputFile());
                                        final VirtualFile fileByUrl = VirtualFileManager
                                                .getInstance().refreshAndFindFileByUrl(url.replace(File.separatorChar, '/'));
                                        if (fileByUrl != null) {
                                            fileByUrl.refresh(false, false);
                                            new OpenFileDescriptor(myXsltRunConfiguration.getProject(), fileByUrl).navigate(true);
                                            return;
                                        }
                                    }
                                    VirtualFileManager.getInstance().asyncRefresh(null);
                                }
                            }
                        };
                        ApplicationManager.getApplication().runWriteAction(runnable);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        });

        return processHandler;
    }

    protected GeneralCommandLine buildCommandLine() throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        VirtualFile fopExecutablePath = XslFoUtils.findFopExecutable(mySettings.getFopInstallationDir());
        if (fopExecutablePath == null) {
            throw new CantRunException("Invalid FOP installation directory");
        }
        commandLine.setExePath(fopExecutablePath.getPath());

        VirtualFile fopUserConfig = XslFoUtils.findFopUserConfig(mySettings.getUserConfigLocation());
        if (fopUserConfig != null) {
            commandLine.addParameters("-c", fopUserConfig.getPath());
        }

        // XML
        if (StringUtils.isEmpty(myXslFoRunConfiguration.getXmlInputFile())) {
            throw new CantRunException("No XML input file selected");
        }
        commandLine.addParameters("-xml", myXslFoRunConfiguration.getXmlInputFile());

        // XSL
        if (StringUtils.isEmpty(myXslFoRunConfiguration.getXsltFile())) {
            throw new CantRunException("No XSLT file selected");
        }
        commandLine.addParameters("-xsl", myXslFoRunConfiguration.getXsltFile());

        // OUTPUT FORMAT (TODO: add other formats support)
        commandLine.addParameter("-pdf");

        // OUT FILE
        commandLine.addParameter(myXslFoRunConfiguration.getOutputFile());
        return commandLine;
    }
}