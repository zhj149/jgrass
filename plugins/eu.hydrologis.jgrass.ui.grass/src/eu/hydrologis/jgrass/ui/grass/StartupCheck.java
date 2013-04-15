package eu.hydrologis.jgrass.ui.grass;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class StartupCheck implements IStartup {

    @Override
    public void earlyStartup() {
        Display.getDefault().syncExec(new Runnable(){
            public void run() {
                hideMenuCheck();
            }
        });
    }

    private void hideMenuCheck() {
        try {
            URL pluginUrl = Platform.getBundle(GrassUiPlugin.PLUGIN_ID).getResource("/");
            String pluginPath = FileLocator.toFileURL(pluginUrl).getPath();
            File pluginFile = new File(pluginPath);
            File installFolder = pluginFile.getParentFile().getParentFile().getParentFile();

            File grassFolderFile = new File(installFolder, "grass");
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                if (!grassFolderFile.exists() || !grassFolderFile.isDirectory()) {
                    IWorkbenchWindow[] wwindows = PlatformUI.getWorkbench().getWorkbenchWindows();
                    String actionSetID = "eu.hydrologis.jgrass.ui.grassactionset";

                    for( IWorkbenchWindow iWorkbenchWindow : wwindows ) {
                        IWorkbenchPage activePage = iWorkbenchWindow.getActivePage();
                        if (activePage != null) {
                            activePage.hideActionSet(actionSetID);
                            MenuManager mbManager = ((ApplicationWindow) iWorkbenchWindow).getMenuBarManager();
                            for( int i = 0; i < mbManager.getItems().length; i++ ) {
                                IContributionItem item = mbManager.getItems()[i];
                                if (item.getId().equals(actionSetID)) {
                                    item.setVisible(false);
                                }
                            }
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
