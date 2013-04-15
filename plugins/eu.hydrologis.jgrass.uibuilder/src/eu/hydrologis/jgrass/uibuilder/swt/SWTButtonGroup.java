/*
 * UIBuilder - a framework to build user interfaces out from XML files
 * Copyright (C) 2007-2008 Patrick Ohnewein
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.hydrologis.jgrass.uibuilder.swt;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;

/**
 * A group of buttons.
 * 
 * The Buttons have to be wrapped in a single composite before added to
 * the client area to avoid interferences by other buttons.
 * 
 * @author Patrick Ohnewein
 */
public class SWTButtonGroup {

	/**
	 * Is it possible that more than one button can be selected.
	 */
	private final boolean multiSelection;

	private class ButtonEntry {
		
		public Button button;
		public SelectionListener selectionListener;
		
		public ButtonEntry(Button button, SelectionListener selectionListener) {
			this.button = button;
			this.selectionListener = selectionListener;
		}
	}
	
	/**
	 * List of buttons registered to this group.
	 */
	private ArrayList<ButtonEntry> buttonEntryList;
	
	private GroupSelectionListener groupSelectionListener;
	
	public SWTButtonGroup(boolean multiSelection) {
		this.multiSelection = multiSelection;
	}
	
	public SWTButtonGroup() {
		this(false);
	}

	public void registerButton(Button button, SelectionListener selectionListener) {
		if (button != null) {
			if (buttonEntryList == null)
				buttonEntryList = new ArrayList<ButtonEntry>();
			if (groupSelectionListener == null)
				groupSelectionListener = new GroupSelectionListener(this);
			buttonEntryList.add(new ButtonEntry(button, selectionListener));
			button.addSelectionListener(groupSelectionListener);
		}
	}
	
	public void unregisterButton(Button button) {
		if (button != null) {
			if (buttonEntryList != null) {
				for (int i = 0, count = buttonEntryList.size(); i < count; i++) {
					ButtonEntry be = buttonEntryList.get(i);
					if (be.button == button && buttonEntryList.remove(be)) {
						button.removeSelectionListener(groupSelectionListener);
					}
				}
			}
		}
	}
	
	private void setSelected(Button button) {
		if (buttonEntryList != null) {
			if (!multiSelection) {
				for (int i = 0, count = buttonEntryList.size(); i < count; i++) {
					ButtonEntry be = buttonEntryList.get(i);
					Button listButton = be.button;
					if (listButton != button) {
						if (listButton.getSelection()) {
							listButton.setSelection(false);
							
							// the programmatical deselection of the button doesn't generate
							// a selection event. Therefore we generate the event
							Event e = new Event();
							e.widget = listButton;
							e.type = SWT.Selection;
							be.selectionListener.widgetSelected(new SelectionEvent(e));
						}
					}
				}
			}
			if (button != null && !button.getSelection())
				button.setSelection(true);
		}
	}

	private class GroupSelectionListener implements SelectionListener {
		
		private final SWTButtonGroup buttonGroup;
		
		public GroupSelectionListener(SWTButtonGroup buttonGroup) {
			this.buttonGroup = buttonGroup;
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			buttonGroup.setSelected((Button)e.getSource());
		}		
	}
}

