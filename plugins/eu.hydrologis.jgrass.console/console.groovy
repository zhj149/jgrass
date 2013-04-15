#!/usr/bin/env groovy

if(args.length < 1){
    System.out.println("USAGE: ./console.groovy scriptpath [memory amount (ex. 1000)]");
    System.exit(1);
}
def scriptPath = args[0];
File scriptFile = new File(scriptPath);
if(!scriptFile.exists()){
    System.out.println("USAGE: ./console.groovy scriptpath [memory amount (ex. 1000)]");
    System.exit(1);
}
def memory = 1000;
if(args.length == 2){
    memory = args[1]
}

def hereFile = new File(".").getAbsolutePath()
def tmpFile = new File(hereFile);
def pluginsFolder = new File(tmpFile.getParentFile().getParent())

def rtPath = ""
pluginsFolder.eachDirRecurse{  
    if(it.name.equals("rt"))
        rtPath = it.getAbsolutePath()
} 
println "Using ${rtPath} as rt path"

def dirs = []
println "Adding plugin folders to the classpath"
pluginsFolder.eachDir{ 
    dirs << it.getAbsolutePath()
}

println "Adding jars to the classpath"
def jars = []
pluginsFolder.eachFileRecurse{  
    if(it.name.endsWith(".jar"))
        jars << it.getAbsolutePath()
} 


def classpath = dirs + jars


def cp = classpath.join(":")
cp = cp + """:."""
println ""
//println cp

def main="""
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
import java.io.File;
import java.io.PrintStream;

import eu.hydrologis.jgrass.console.core.JGrass;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.core.runtime.ConsoleEngine;

/**
 * Standalone console engine
 * 
 * @author Andrea Antonello - www.hydrologis.com
 */
public class JGrassConsole {
    private boolean doDebug = false;

    public JGrassConsole(String scriptPath) {
        /*
         * prepare to use the console environment
         */
        PrintStream outStream = new PrintStream(System.out);
        PrintStream errStream = new PrintStream(System.err);
        JGrass console = (JGrass) new ConsoleEngine();
        if (null == console) {
            // throw something
        }

        ProjectOptions projectOptions = new ProjectOptions(outStream,
                outStream, errStream);

        initialize(projectOptions);

        console.dispatch(projectOptions, scriptPath);
    }

    public static void main(String[] args) {

        new JGrassConsole(args[0]);

    }

    public void initialize(ProjectOptions options) {
        options.setOption(ProjectOptions.CONSOLE_ASYNC_MODE, false);
        options.setOption(ProjectOptions.CONSOLE_THREAD_RESTRICTION, 0);
        options.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY, false);

        // Logging Level: DEBUG
        options.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_DEBUG, doDebug);
        // Logging Level: TRACE
        options.setOption(ProjectOptions.CONSOLE_LOGGING_LEVEL_TRACE, doDebug);
        // Include directory; RTTI - runtime type informations - the
        // reserved
        // words...
        options.setOption(ProjectOptions.CONSOLE_DIRECTORY_INCLUDE,
                new String[] { \"${rtPath}\" });

        // Source directory; default script file location...
        options.setOption(ProjectOptions.CONSOLE_DIRECTORY_SOURCE, null);

        // User information - the home directory of the current user...
        options.setOption(ProjectOptions.NATIVE_MODEL_USER_HOME, System
                .getProperty("user.home")); 

        // User information - the user name of the current user...
        options.setOption(ProjectOptions.NATIVE_MODEL_USER_NAME, System
                .getProperty("user.name")); 

        // Debug mode...
        options.setOption(ProjectOptions.NATIVE_MODEL_DEBUG, doDebug);

        // Installation folder of GRASS...
        //options.setOption(ProjectOptions.NATIVE_MODEL_GISBASE, gisbase);

        // GRASS database, location, mapset path...
        //options.setOption(ProjectOptions.COMMON_GRASS_MAPSET, grassDbPath
        //        + File.separator + locationName + File.separator + mapsetName
        //        + File.separator);

        options.setOption(ProjectOptions.JAVA_MODEL_TIME_DELTA, null);
        options.setOption(ProjectOptions.JAVA_MODEL_TIME_ENDING_UP, null);
        options.setOption(ProjectOptions.JAVA_MODEL_TIME_START_UP, null);
    }

}
"""

// create the main class

def fileName = "JGrassConsole.java"
def newFile = new File(fileName);  
newFile.write(main);

 
 
 
 
// compile the code
println """javac -cp cp ${fileName}"""
def command = """javac -cp ${cp} ${fileName}"""
def prox = command.execute()
prox.waitFor()
println "Compilation finished..."

println "Maximum memory given to the process: " + memory

def cmd = ["java", "-Xmx${memory}m", "-cp", "${cp}", "JGrassConsole", scriptPath]
def proc = cmd.execute()

// Obtain status and output
println "stderr: ${proc.err.text}"
println "stdout: ${proc.in.text}"
proc.waitFor()
println "return code: ${ proc.exitValue()}"
