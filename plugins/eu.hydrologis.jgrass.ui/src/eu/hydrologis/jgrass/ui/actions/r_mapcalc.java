package eu.hydrologis.jgrass.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import eu.hydrologis.jgrass.ui.utils.MapcalcGui;

public class r_mapcalc implements IWorkbenchWindowActionDelegate {

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {

	}

	public void run(IAction action) {
		new MapcalcGui();
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
