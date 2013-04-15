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
package eu.hydrologis.jgrass.console.core.runtime.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import eu.hydrologis.jgrass.console.core.internal.compiler.AbstractCompiler;
import eu.hydrologis.jgrass.console.core.internal.compiler.Compiler;
import eu.hydrologis.jgrass.console.core.internal.nodes.Token;
import eu.hydrologis.jgrass.console.core.prefs.ProjectOptions;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.lexer.CLiScanner;
import eu.hydrologis.jgrass.console.core.runtime.lexer.ScriptMLScanner;
import eu.hydrologis.jgrass.console.core.runtime.script.GroovyEngine;

/**
 * <p>
 * The so called "Command Line interpreter / Model Language Compiler" - <i>CLiMLCompiler</i> or
 * <i>compiler</i> for short - controls the command line analysis and the subsequent translation
 * phases; for the subsequent execution phase, a third-party script engine is being in use, unless
 * that phase can be skipped, in the case of the command line should only be compiled - <b>/compile</b>
 * directive.
 * </p>
 * <p>
 * Facing the fact that the input stream is ambiguous - at one hand it can be a source program and
 * at the other a <i>GRASS</i> command or <i>JGRASS</i> model - the compiler assumes a source
 * program respectively the source code of a script file in its input - the compiler expects a
 * <b>jgrass</b> or <b>grass</b> directive; if none was found the compiler treats the input as a
 * statement of a command line assuming a <i>JGRASS</i> model in its input and attempts to
 * translate a <i>JGRASS</i> model statement, if this also fails the compiler then attempts to
 * translate a <i>GRASS</i> command. However, the command line will be executed in any case, except
 * the command line should only be compiled.
 * </p>
 * 
 * @see eu.hydrologis.jgrass.console.core.runtime.lexer.ScriptMLScanner
 * @see eu.hydrologis.jgrass.console.core.runtime.lexer.CLiScanner
 * @see eu.hydrologis.jgrass.console.core.runtime.compiler.JavaML4jBeanshell
 * @see eu.hydrologis.jgrass.console.core.runtime.compiler.NativeML4jBeanshell
 * @see eu.hydrologis.jgrass.console.core.runtime.script.BeanshellEngine
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class CLiMLCompiler extends AbstractCompiler implements Compiler, Runnable {

    private static final String GROOVY = "groovy";
    private static final String BEANSHELL = "beanshell";

    // Attributes
    /**
     * The command line, which is either to compile or to compile and execute by this compiler /
     * interpreter.
     */
    private final String m_commandLine;

    /**
     * The project options, currently used by this command line processor.
     */
    private final ProjectOptions m_projectOptions;

    /**
     * The compilation language to use chosen by the user
     */
    private String languageChoice;

    // Construction
    /**
     * <p>
     * The constructor creates the so called "Command Line interpreter / Model Language Compiler" -
     * <code>CLiMLCompiler</code> - with the specified project options and the specified command
     * line.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.prefs.ProjectOptions
     * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
     * @param projectOptions - the project options.
     * @param commandLine - the command enclosing optional pre-processor directives; typically the
     *        name of a script file or a native based command or either a simple or complex Java
     *        based model.
     * @throws IllegalArgumentException - if <code>projectSpace</code> or <code>commandLine</code>
     *         references the null type.
     */
    public CLiMLCompiler( ProjectOptions projectOptions, String commandLine ) {

        super(__initialize(projectOptions));
        if (null == projectOptions)
            throw new IllegalArgumentException();
        if (null == commandLine)
            throw new IllegalArgumentException();

        m_projectOptions = projectOptions;
        m_commandLine = commandLine.trim();

        languageChoice = (String) projectOptions.getOption(ProjectOptions.CONSOLE_LANGUAGE, GROOVY);

    } // CLiMLCompiler

    /**
     * <p>
     * The method <code>__initialize</code> creates a project space and initialize it with the
     * specified project options for the compiler and interprete run.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.prefs.ProjectOptions
     * @see eu.hydrologis.jgrass.console.core.prefs.Projectspace
     * @param projectOptions - the project options.
     * @return An initialized <code>Projectspace</code> object.
     */
    private static Projectspace __initialize( ProjectOptions projectOptions ) {

        if (null == projectOptions)
            throw new IllegalArgumentException();

        Projectspace retval;
        try {

            retval = new Projectspace(projectOptions.internal, projectOptions.out, projectOptions.err);
            retval.initialize(projectOptions);
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectOptions.err.println(e);

            retval = null;
        }

        return retval;
    } // __initialize

    // Operations
    /**
     * <p>
     * Close the stream.
     * </p>
     * 
     * @param reader - the stream.
     * @throws IOException - if an I/O ERROR occurs.
     */
    private void close( Reader reader ) throws IOException {

        try {

            if (null == reader)
                throw new IllegalArgumentException();

            reader.close();
        } catch (IOException e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            throw e;
        }
    } // close

    /**
     * <p>
     * Opens a stream.
     * </p>
     * 
     * @param sourceFile - the stream.
     * @return The open stream.
     * @throws IOException - if an I/O ERROR occurs.
     */
    private Reader open( File sourceFile ) throws IOException {

        Reader retval;
        try {

            File pathToStreamFile = sourceFile;
            if (false == sourceFile.isAbsolute()) {

                File tempPathToStreamFile = sourceFile.getAbsoluteFile();
                if (false == tempPathToStreamFile.exists()) {

                    String __source_directory = (String) m_projectOptions
                            .getOption(ProjectOptions.CONSOLE_DIRECTORY_SOURCE, null);
                    if (null != __source_directory) {

                        File __directory = new File(__source_directory);
                        tempPathToStreamFile = new File(__directory.getAbsolutePath() + File.separator + sourceFile.getPath());
                        if (true == tempPathToStreamFile.exists())
                            pathToStreamFile = tempPathToStreamFile;
                    }
                }
            }

            if (null == pathToStreamFile) {

                retval = null;
            } else {

                retval = new InputStreamReader(new FileInputStream(pathToStreamFile));
            }
        } catch (IOException e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            throw e;
        }

        return retval;
    } // open

    /**
     * <p>
     * Opens a stream.
     * </p>
     * 
     * @param sourceFile - the name of a file.
     * @return The open stream.
     * @throws IllegalArgumentException - if sourceFile references the <code>null</code> type.
     */
    private Reader open( String sourceFile ) throws IllegalArgumentException {

        Reader retval;
        try {

            if (null == sourceFile)
                throw new IllegalArgumentException();

            retval = new StringReader(sourceFile);
        } catch (IllegalArgumentException e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            throw e;
        }

        return retval;
    } // open

    /**
     * <p>
     * Compiles the source code of a <i>JGRASS</i> model.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.compiler.JavaML4jBeanshell
     * @param sourceCode - the input respectively the source program.
     * @param line - the line number in the source program used by this compiler as start-point.
     * @return The generated target code.
     */
    private Reader __compile_javaML4j( Reader sourceCode, int line ) {

        Reader retval;
        AbstractML4j preprocessor = new JavaML4jGroovy(projectSpace());
        retval = preprocessor.compile(sourceCode, line);
        return retval;
    } // __compile_javaML4j

    /**
     * <p>
     * Compiles the source code of a <i>GRASS</i> command.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.compiler.NativeML4jBeanshell
     * @param sourceCode - the input respectively the source program.
     * @param line - the line number in the source program used by this compiler as start-point.
     * @return The generated target code.
     */
    private Reader __compile_nativeML4j( Reader sourceCode, int line ) {

        Reader retval;
        AbstractML4j preprocessor = new NativeML4jGroovy(projectSpace());
        retval = preprocessor.compile(sourceCode, line);
        return retval;
    } // __compile_nativeML4j

    /**
     * <p>
     * Compiles the specified source program respectively the specified source code.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.Compiler
     * @param sourceCode - the input respectively the source program.
     * @param line - the line number in the source program used by this compiler as start-point.
     * @return The target code respectively the target program translated by this compiler.
     */
    public final Reader compile( Reader sourceCode, int line ) throws Exception {

        final long __startTimeInMillis = System.currentTimeMillis();
        int __modelsCompiled = 0;
        Reader retval = null;
        try {

            projectSpace().internal.println("JGrass 2 Console ML (Model Language) Compiler, " //$NON-NLS-1$
                    + "Version 1.0.0.832, " //$NON-NLS-1$
                    + "for " + System.getProperty("os.arch") //$NON-NLS-2$ //$NON-NLS-1$
                    + "\n" //$NON-NLS-1$
                    + "Copyright (C)" //$NON-NLS-1$
                    + "\n\t" //$NON-NLS-1$
                    + "HydroloGIS - www.hydrologis.com," //$NON-NLS-1$
                    + "\n\t" //$NON-NLS-1$
                    + "C.U.D.A.M. - http://www.unitn.it/dipartimenti/cudam" //$NON-NLS-1$
                    + "\n" //$NON-NLS-1$
                    + "--------------------------------" //$NON-NLS-1$
                    + " Compiling... " //$NON-NLS-1$
                    + "---------------------------------" //$NON-NLS-1$
                    + "\n" //$NON-NLS-1$
                    + m_projectOptions.projectCaption());

            StringBuffer targetCode = new StringBuffer();
            ScriptMLScanner scanner = new ScriptMLScanner(projectSpace(), sourceCode, line);
            Token<TOKs> token;
            while( null != (token = scanner.tokscn()) ) {

                switch( token.identifier() ) {
                case DIRECTIVE_JGRASS:
                case DIRECTIVE_GRASS:
                case DIRECTIVE_R:
                    int nSaveLine = scanner.line();
                    Reader pseudoCode = scanner.blockRead(TOKs.CHARACTER_BRACE_OPEN, TOKs.CHARACTER_BRACE_CLOSE);
                    if (null != pseudoCode) {

                        Reader generatedCode;
                        switch( token.identifier() ) {
                        case DIRECTIVE_JGRASS:
                            ++__modelsCompiled;
                            generatedCode = __compile_javaML4j(pseudoCode, nSaveLine);
                            if (null != generatedCode) {

                                int ch;
                                while( -1 != (ch = generatedCode.read()) )
                                    targetCode.append((char) ch);
                            }
                            break;

                        case DIRECTIVE_GRASS:
                            ++__modelsCompiled;
                            generatedCode = __compile_nativeML4j(pseudoCode, nSaveLine);
                            if (null != generatedCode) {

                                int ch;
                                while( -1 != (ch = generatedCode.read()) )
                                    targetCode.append((char) ch);
                            }
                            break;

                        case DIRECTIVE_R:
                            break;
                        }
                    }
                    break;

                case DIRECTIVE_COMPILE:
                    // By default, do nothing resp. ignore this token...
                    break;

                default:
                    // Assume the token holds already the target code because
                    // it isn't caught by an pre-processor directive...
                    targetCode.append(token.expression());
                }
            }

            // BEGIN 2008/22/02 AHA
            /*
             * if( 0 == __modelsCompiled ) { Reader pseudoCode = __compile_javaML4j( new
             * StringReader( targetCode.toString() ) , line ); if( null == pseudoCode ) pseudoCode =
             * __compile_nativeML4j( new StringReader( targetCode.toString() ) , line ); if( null !=
             * pseudoCode ) { ++__modelsCompiled; targetCode.setLength( 0 ); int ch; while( -1 !=
             * (ch = pseudoCode.read()) ) targetCode.append( ( char )ch ); } }
             */
            // END 2008/22/02 AHA
            if (true == Projectspace.isDebugEnabled()) {

                if (true == Projectspace.isTraceEnabled()) {

                    projectSpace().internal.println("\n[ Project settings ]"); //$NON-NLS-1$
                    // GRASSDB
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GRASSDB + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GRASSDB));
                    // LOCATION
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_LOCATION + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_LOCATION));
                    // MAPSET
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_MAPSET + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_MAPSET));
                    // START_UP
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_TIME_START_UP + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_START_UP));
                    // ENDING_UP
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_TIME_ENDING_UP + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_ENDING_UP));
                    // TIME DELTA
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_TIME_DELTA + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_DELTA));
                    // GIS DATABASE
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GISDBASE + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GISDBASE));
                    // DEBUG
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_DEBUG + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_DEBUG));
                    // MONITOR
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_MONITOR + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_MONITOR));
                    // MAPSET
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_MAPSET + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_MAPSET));
                    // LOCATION NAME
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_LOCATION_NAME + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_LOCATION_NAME));
                    // GRASS GUI
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GRASS_GUI + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GRASS_GUI));
                    // GIS BASE
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GISBASE + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GISBASE));
                    // BINARY PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_BINARY_PATH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_BINARY_PATH));
                    // MSYS SHELL PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GRASS_SH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GRASS_SH));
                    // PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_PATH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_PATH));
                    // GRASS LD LIBRARY PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_GRASS_LD_LIBRARY_PATH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_GRASS_LD_LIBRARY_PATH));
                    // DYLD LIBRARY PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_DYLD_LIBRARY_PATH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_DYLD_LIBRARY_PATH));
                    // LD LIBRARY PATH
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_LD_LIBRARY_PATH + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_LD_LIBRARY_PATH));
                    // USER HOME
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_USER_HOME + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_USER_HOME));
                    // USER
                    projectSpace().internal.println("\t" + Projectspace.ARG_VERB_USER_NAME + ": " //$NON-NLS-2$ //$NON-NLS-1$
                            + projectSpace().getArgVerbValue(Projectspace.ARG_VERB_USER_NAME));
                }

                projectSpace().internal.println("\n[Generated target source code]\n" //$NON-NLS-1$
                        + targetCode);
            }

            retval = new StringReader(targetCode.toString());
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            throw e;
        } finally {

            final long __stopTimeInMillis = System.currentTimeMillis();
            projectSpace().internal.println(m_projectOptions.projectCaption() + " - " //$NON-NLS-1$
                    + Integer.toString(__modelsCompiled) + " model/s compiled, " //$NON-NLS-1$
                    + "total compilation time: " //$NON-NLS-1$
                    + (__stopTimeInMillis - __startTimeInMillis) + " ms" //$NON-NLS-1$
            );
        }

        return retval;
    } // compile

    /**
     * <p>
     * Executes the specified script.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.runtime.script.BeanshellEngine
     * @param reader - the source of the script.
     * @throws RuntimeException - if an ERROR occurs in script.
     * @throws NullPointerException - if the argument is null.
     */
    public final void interpret( Reader reader ) throws Exception {

        try {

            projectSpace().internal.println("\n" //$NON-NLS-1$
                    + "----------------------------------------" //$NON-NLS-1$
                    + "---------------------------------------" //$NON-NLS-1$
                    + "\n" //$NON-NLS-1$
                    + "GroovyShell, Version 1.7.0, http://groovy.codehaus.org " //$NON-NLS-1$
                    + "\n" //$NON-NLS-1$
                    + "------------------------------" //$NON-NLS-1$
                    + " Running model... " //$NON-NLS-1$
                    + "-------------------------------" //$NON-NLS-1$
            );
            
            Projectspace projectSpace = projectSpace();
            GroovyEngine interpreter = new GroovyEngine(projectSpace);
            interpreter.eval(reader);
        } catch (Exception e) {

            if (true == Projectspace.isDebugEnabled())
                projectSpace().internal.println(e);
        }
    } // interpret

    /**
     * <p>
     * Parses the command line.
     * </p>
     * 
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.AbstractCompiler
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.Compiler
     * @param commandLine - the command line.
     * @return A source program respectively the source code that is then fed to subsequent phases.
     */
    private Reader parseCommandLine( String commandLine ) throws IOException {

        Reader retval = null;
        try {

            Reader istream = new StringReader(commandLine);
            CLiScanner scanner = new CLiScanner(projectSpace(), istream, 1);
            Token<TOKs> token;
            while( null == retval && null != (token = scanner.tokscn()) ) {

                switch( token.identifier() ) {
                case DIRECTIVE_COMPILE:
                    Boolean __directive_compile = (Boolean) m_projectOptions.getOption(ProjectOptions.CONSOLE_COMPILE_ONLY,
                            new Boolean(false));
                    if (false == __directive_compile)
                        m_projectOptions.setOption(ProjectOptions.CONSOLE_COMPILE_ONLY, new Boolean(true));
                    break;

                case PATHNAME:
                    retval = open(new File(token.expression()));
                    break;

                case UNKNOWN:
                default:
                    retval = open(commandLine);
                }
            }
        } catch (IOException e) {

            projectSpace().err.println(e);
            throw e;
        }

        return retval;
    } // interprete

    /**
     * <p>
     * The <code>run</code> method initiates the translation of the specified command line with
     * the specified settings at construction time; if the compilation succeeds the generated code
     * is then immediately executed, unless that the command line should only be compiled.
     * </p>
     */
    public final void run() {

        synchronized (this) {

            final long __startTimeInMillis = System.currentTimeMillis();
            Boolean __directive_compile = false;
            try {

                __directive_compile = (Boolean) m_projectOptions.getOption(ProjectOptions.CONSOLE_COMPILE_ONLY,
                        __directive_compile);
                Reader reader = parseCommandLine(m_commandLine);
                if (null != reader) {

                    Reader targetCode = compile(reader, 1);
                    close(reader);
                    if (null != targetCode) {

                        if (false == __directive_compile)
                            interpret(targetCode);

                        close(targetCode);
                    }
                }
            } catch (Exception e) {

                if (true == Projectspace.isErrorEnabled())
                    projectSpace().err.println(e);
                if (true == Projectspace.isTraceEnabled())
                    projectSpace().internal.println(m_commandLine);

                e.printStackTrace();
            } finally {

                final long __stopTimeInMillis = System.currentTimeMillis();
                if (false == __directive_compile)
                    projectSpace().internal.println("\n" //$NON-NLS-1$
                            + "Total run time: " //$NON-NLS-1$
                            + (__stopTimeInMillis - __startTimeInMillis) + " ms" //$NON-NLS-1$
                    );
            }
        }
    } // run

} // CLiMLCompiler
