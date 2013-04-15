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
package eu.hydrologis.jgrass.grass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.geotools.referencing.CRS;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import eu.hydrologis.jgrass.console.editor.ConsoleEditorPlugin;
import eu.hydrologis.jgrass.console.editor.preferences.PreferencesInitializer;
import eu.hydrologis.jgrass.ui.console.ConsoleCommandExecutor;
import eu.udig.catalog.jgrass.core.JGrassService;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GrassProjCompatibilityOp implements IOp {

    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {

        JGrassService mapsetResource = (JGrassService) target;
        File projWktFile = mapsetResource.getProjWktFile();
        BufferedReader bR = new BufferedReader(new FileReader(projWktFile));
        StringBuffer Sb = new StringBuffer();
        String line = null;
        while( (line = bR.readLine()) != null ) {
            Sb.append(line);
        }
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.parseWKT(Sb.toString());
        } catch (Exception e) {
            GrassPlugin
                    .log(
                            "GrassPlugin problem: eu.hydrologis.jgrass.grass#GrassProjCompatibilityOp#op", e); //$NON-NLS-1$
            e.printStackTrace();
        }
        /*
         * if a grass installation is there, use proj to create GRASS proj files
         */

        Iterator<ReferenceIdentifier> iterator = crs.getIdentifiers().iterator();
        if (iterator.hasNext()) {
            String epsg = iterator.next().getCode();

            String command = null;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                command = "grass g.proj.exe -c epsg=" + epsg;
            } else {
                command = "grass g.proj -c epsg=" + epsg;
            }

            ScopedPreferenceStore m_preferences = (ScopedPreferenceStore) ConsoleEditorPlugin
                    .getDefault().getPreferenceStore();
            String gisbase = m_preferences.getString(PreferencesInitializer.CONSOLE_ARGV_GISBASE);
            if (gisbase.length() < 1 || !(new File(gisbase).exists())) {
                gisbase = null;
            }
            ConsoleCommandExecutor cExe = new ConsoleCommandExecutor();
            cExe.execute("g.proj", command, mapsetResource.getPermanetMapsetFile()
                    .getAbsolutePath(), gisbase, ConsoleCommandExecutor.OUTPUTTYPE_BTCONSOLE, null,
                    null);
        }
    }

}