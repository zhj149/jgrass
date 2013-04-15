package eu.hydrologis.jgrass.ui.grass.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import eu.hydrologis.jgrass.uibuilder.jgrassdependent.utils.UIBuilderActionSupporter;

public class r_sun extends UIBuilderActionSupporter implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		isJGrass = false;
		isGrass = true;
		launchGui(window, new Point(1000, 400));
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}