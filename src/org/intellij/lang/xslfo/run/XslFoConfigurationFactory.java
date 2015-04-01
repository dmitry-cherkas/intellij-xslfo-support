package org.intellij.lang.xslfo.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoConfigurationFactory extends ConfigurationFactory {

    public XslFoConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new XslFoRunConfiguration(project, this);
    }

    @Override
    public String getName() {
        return "XSL-FO";
    }
}
