package org.intellij.lang.xslfo.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoConfigurationFactory extends ConfigurationFactory {

    public XslFoConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new XslFoRunConfiguration(project, this);
    }

    @Override
    public @NotNull
    @NonNls String getId() {
        return this.getName();
    }

    @Override
    public @NotNull String getName() {
        return "XSL-FO";
    }
}
