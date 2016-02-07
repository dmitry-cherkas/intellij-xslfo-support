package icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, XslFoIcons.class);
    }

    public static final Icon FopLogo = load("/icons/fop-logo-16x16.png");
}
