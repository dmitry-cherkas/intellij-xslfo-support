package org.intellij.lang.xslfo.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import org.intellij.lang.xpath.xslt.XsltSupport;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Enables right-click action to create Run configuration from file.
 *
 * @author Dmitry_Cherkas
 */
public class XslFoConfigurationProducer extends LazyRunConfigurationProducer<XslFoRunConfiguration> {

    @Override
    protected boolean setupConfigurationFromContext(@NotNull XslFoRunConfiguration configuration, @NotNull ConfigurationContext context, Ref<PsiElement> source) {
        final XmlFile file = PsiTreeUtil.getParentOfType(source.get(), XmlFile.class, false);
        if (file != null && file.isPhysical() && XsltSupport.isXsltFile(file)) {
            source.set(file); // update reference
            configuration.setupFromFile(file);
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull XslFoRunConfiguration configuration, ConfigurationContext context) {
        Location location = context.getLocation();
        if (location == null) {
            return false;
        }
        XmlFile contextFile = PsiTreeUtil.getParentOfType(context.getLocation().getPsiElement(), XmlFile.class, false);
        return contextFile != null
               && contextFile.isPhysical()
               && XsltSupport.isXsltFile(contextFile)
               && contextFile.getVirtualFile().getPath().replace('/', File.separatorChar).equals(configuration.getXsltFile());
    }

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return XslFoRunConfigType.getInstance().getConfigurationFactories()[0];
    }
}
