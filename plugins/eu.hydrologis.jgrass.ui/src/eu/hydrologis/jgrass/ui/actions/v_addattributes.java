package eu.hydrologis.jgrass.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils.UIBuilderActionSupporter;

public class v_addattributes extends UIBuilderActionSupporter implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		isJGrass = true;
		isGrass = false;
		isTimeDependent = true;
		launchGui(window, null);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
