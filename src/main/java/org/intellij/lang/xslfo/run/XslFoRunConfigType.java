package org.intellij.lang.xslfo.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import icons.XslFoIcons;

import javax.swing.*;

public class XslFoRunConfigType implements ConfigurationType {

    private final ConfigurationFactory myXslFoFactory;

    public XslFoRunConfigType() {
        myXslFoFactory = new XslFoConfigurationFactory(this);
    }

    public static XslFoRunConfigType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(XslFoRunConfigType.class);
    }

    public String getDisplayName() {
        return "XSL-FO";
    }

    @NonNls
    @NotNull
    public String getId() {
        return "XSL-FO";
    }

    public String getConfigurationTypeDescription() {
        return "Run XSL-FO Transformation";
    }

    public Icon getIcon() {
        return XslFoIcons.FopLogo;
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{myXslFoFactory};
    }
}
