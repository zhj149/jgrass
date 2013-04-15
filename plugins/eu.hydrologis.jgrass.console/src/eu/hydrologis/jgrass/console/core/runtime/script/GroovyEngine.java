/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
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
package eu.hydrologis.jgrass.console.core.runtime.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import eu.hydrologis.jgrass.console.core.internal.script.AbstractScriptEngine;
import eu.hydrologis.jgrass.console.core.internal.script.ScriptEngine;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.libs.scripting.ScriptingLibsPlugin;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;

/**
 * <p>
 * The class <code>GroovyEngine</code> uses the GroovyShell engine to execute a script respectively
 * a source program.
 * </p>
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class GroovyEngine extends AbstractScriptEngine implements ScriptEngine {

    // Attributes
    /** The Groovy shell itself. */
    private GroovyShell shell;
    private Binding binding;
    private StringBuffer classesBuffer;
    private final Projectspace projectSpace;
    private static String groovyDsl;

    // Construction
    /**
     * <p>
     * The copy constructor <code>BeanshellEngine</code> defines this script engine object using the
     * specified project-space to initialize the script engine.
     * </p>
     * 
     * @param projectSpace - the project-space.
     */
    public GroovyEngine( Projectspace projectSpace ) {

        super(projectSpace);
        this.projectSpace = projectSpace;

        try {
            shell = new GroovyShell();

            binding = new Binding();
            binding.setVariable("out", projectSpace.out); //$NON-NLS-1$
            binding.setVariable("err", projectSpace.err); //$NON-NLS-1$

            classesBuffer = new StringBuffer();
            Vector<String> importClasses = projectSpace.importedClasses();
            for( int i = 0; i < importClasses.size(); ++i ) {
                String string = importClasses.elementAt(i);
                classesBuffer.append("import ").append(string).append(";\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }

        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);
        }

    } // BeanshellEngine

    public Object eval( Reader reader ) throws Exception {
        Object retval = null;
        try {
            StringBuffer sB = new StringBuffer();
            if (groovyDsl == null) {
                StringBuilder groovyDslBuilder = createGroovyDsl();
                groovyDsl = groovyDslBuilder.toString();
            }
            sB.append(groovyDsl).append("\n");
            BufferedReader bR = new BufferedReader(reader);
            String line = null;
            while( (line = bR.readLine()) != null ) {
                sB.append(line).append("\n"); //$NON-NLS-1$
            }
            bR.close();

            System.out.println(sB.toString());
            retval = eval(sB.toString());
        } catch (Exception e) {
            projectSpace().err.println(e.getLocalizedMessage());
            throw e;
        }
        return retval;
    } // eval

    private StringBuilder createGroovyDsl() {
        URL helperUrl = Platform.getBundle(ScriptingLibsPlugin.PLUGIN_ID).getResource("console/ConsoleHelper.groovy");
        URL importUrl = Platform.getBundle(ScriptingLibsPlugin.PLUGIN_ID).getResource("console/imports.groovy");

        StringBuilder groovyDsl = new StringBuilder();

        try {
            // add imports
            String importPath = FileLocator.toFileURL(importUrl).getPath();
            File importFile = new File(importPath);
            BufferedReader bR = new BufferedReader(new FileReader(importFile));
            String line = null;
            while( (line = bR.readLine()) != null ) {
                groovyDsl.append(line).append("\n");
            }
            bR.close();

            // add runtime vars
            String gisDbase = projectSpace.getArgVerbValue(Projectspace.ARG_VERB_GISDBASE);
            String location = projectSpace.getArgVerbValue(Projectspace.ARG_VERB_LOCATION);
            String mapset = projectSpace.getArgVerbValue(Projectspace.ARG_VERB_MAPSET);
            String remoteDb = projectSpace.getArgVerbValue(Projectspace.ARG_VERB_REMOTEDB);

            String mapsetPath = gisDbase + File.separator + location + File.separator + mapset;
            mapsetPath = mapsetPath.replaceAll("\\\\", "\\\\\\\\");
            groovyDsl.append("mapsetPath =\"").append(mapsetPath).append("\";\n");
            groovyDsl.append("remotedbUrl =\"").append(remoteDb).append("\";\n");

            // add dsl
            String helperPath = FileLocator.toFileURL(helperUrl).getPath();
            File helperFile = new File(helperPath);
            bR = new BufferedReader(new FileReader(helperFile));
            while( (line = bR.readLine()) != null ) {
                groovyDsl.append(line).append("\n");
            }
            bR.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return groovyDsl;
    }

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.script.ScriptEngine#eval()
     */
    public Object eval( String string ) throws Exception {

        string = classesBuffer.append(string).toString();

        Object retval = null;
        try {
            long c1 = System.currentTimeMillis();
            Script scrpt = shell.parse(string);
            long c2 = System.currentTimeMillis();
            System.out.println((c2 - c1) / 1000);
            scrpt.setBinding(binding);
            long c3 = System.currentTimeMillis();
            System.out.println((c3 - c2) / 1000);
            retval = scrpt.run();
            long c4 = System.currentTimeMillis();
            System.out.println((c4 - c3) / 1000);
        } catch (Exception e) {
            projectSpace().err.println(e.getLocalizedMessage());
            throw e;
        }
        return retval;
    } // eval

} // BeanshellEngine
