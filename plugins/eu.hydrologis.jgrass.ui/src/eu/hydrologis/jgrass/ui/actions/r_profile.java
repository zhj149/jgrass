package eu.hydrologis.jgrass.ui.actions;

import net.refractions.udig.project.ui.ApplicationGIS;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class r_profile implements IWorkbenchWindowActionDelegate {

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {

		IAction tool = ApplicationGIS.getToolManager().getToolAction(
				"eu.hydrologis.jgrass.tools.profiletool", //$NON-NLS-1$
				"eu.hydrologis.jgrass.category.info"); //$NON-NLS-1$
		tool.run();

	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
