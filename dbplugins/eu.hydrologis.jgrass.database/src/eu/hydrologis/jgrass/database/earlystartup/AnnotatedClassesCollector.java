/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
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
package eu.hydrologis.jgrass.database.earlystartup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;

/**
 * This collects all annotated classes..
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class AnnotatedClassesCollector implements IStartup {

    private static List<String> annotatedClassesList = new ArrayList<String>();

    public void earlyStartup() {
        if (annotatedClassesList.size() > 0) {
            // it was called already manually
            return;
        }

        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IConfigurationElement[] extensions = reg
                .getConfigurationElementsFor("eu.hydrologis.jgrass.database.annotatedclasses"); //$NON-NLS-1$

        for( int i = 0; i < extensions.length; i++ ) {
            IConfigurationElement element = extensions[i];
            if (!element.getName().equals("annotatedclass")) { //$NON-NLS-1$
                continue;
            }
            String classStr = element.getAttribute("annotatedclass"); //$NON-NLS-1$
            if (!annotatedClassesList.contains(classStr)) {
                annotatedClassesList.add(classStr);
            }
        }
    }

    public static List<String> getAnnotatedClassesList() {
        if (annotatedClassesList.size() == 0) {
            AnnotatedClassesCollector tmp = new AnnotatedClassesCollector();
            tmp.earlyStartup();
        }
        return annotatedClassesList;
    }

}
