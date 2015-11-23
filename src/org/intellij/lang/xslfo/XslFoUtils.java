package org.intellij.lang.xslfo;

import com.intellij.execution.Platform;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.File;

/**
 * @author Dmitry_Cherkas
 */
public class XslFoUtils {

    public static VirtualFile findFopExecutable(String pathToFopInstallationDir) {
        if(pathToFopInstallationDir == null) {
            return null;
        }
        String url = VfsUtilCore.pathToUrl(pathToFopInstallationDir).replace(File.separatorChar, '/');
        VirtualFile fopInstallationDir = VirtualFileManager.getInstance().findFileByUrl(url);
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

    public static VirtualFile findFopUserConfig(String userConfigLocation) {
        String url = VfsUtilCore.pathToUrl(userConfigLocation).replace(File.separatorChar, '/');
        return VirtualFileManager.getInstance().findFileByUrl(url);
    }
}
