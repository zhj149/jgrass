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

import java.io.File;
import java.io.Reader;

import eu.hydrologis.jgrass.console.core.internal.lexer.Lexer;
import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.internal.parser.Parser;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.lexer.NativeMLScanner;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_parameter;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_parameter_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_root;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_variable;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_array;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_assign_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_block;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_bool_true;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_catch;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_comma;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_condition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_conditional_and;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_ctor_call;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_dot;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_elist;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_expression;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_finally;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_identifier;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_if;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_literal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_logical_unequal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_method_call;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_null;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_number_integer;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_plus;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_root;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_slist;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_then;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_try;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_type;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_variable_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_while;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_array;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_class;
import eu.hydrologis.jgrass.console.core.runtime.parser.NativeMLParser;

/**
 * <p>
 * This is the pre-processor that processes a <i>GRASS</i> native based command; translate it into
 * equivalent target code.
 * </p>
 * 
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public final class NativeML4jGroovy extends AbstractML4j {

    // Construction
    /**
     * <p>
     * The copy constructor <code>NativeML4j</code> defines this preprocessor object using the
     * specified project-space.
     * </p>
     * 
     * @param projectSpace - the project-space.
     */
    public NativeML4jGroovy( Projectspace projectSpace ) {

        super(projectSpace);
    } // NativeML4j

    // Operations
    /**
     * <p>
     * Translates a <i>GRASS</i> command - "native_model" - into the tree-like intermediate
     * representation of the target code.
     * </p>
     * 
     * @param symtable - the symbol table.
     * @param root - the root of the syntax tree.
     * @param operator - a "java_model" operator.
     */
    @SuppressWarnings("unchecked")
    private void __native_model( Symtable<SYMs> symtable, AST_root root, APT_native_model operator ) {

        final SYM_type_class __typedef_filewriter = new SYM_type_class("FileWriter"); //$NON-NLS-1$
        final SYM_type_class __typedef_writer = new SYM_type_class("Writer"); //$NON-NLS-1$
        final String __variable_writer;
        symtable.register(__variable_writer = symtable.autoCreateIdentifier("__writer_" //$NON-NLS-1$
                ), __typedef_writer);
        final SYM_type_class __typedef_gisrc = new SYM_type_class("File"); //$NON-NLS-1$
        final String __variable_gisrc;
        symtable.register(__variable_gisrc = symtable.autoCreateIdentifier("__tempfile_" //$NON-NLS-1$
                ), __typedef_gisrc);
        final SYM_type_array __typedef_argv = new SYM_type_array("String"); //$NON-NLS-1$
        final String __variable_argv;
        symtable.register(__variable_argv = symtable.autoCreateIdentifier("__argv_" //$NON-NLS-1$
                ), __typedef_argv);
        final SYM_type_class __typedef_processbuilder = new SYM_type_class("ProcessBuilder"); //$NON-NLS-1$
        final String __variable_pb;
        symtable.register(__variable_pb = symtable.autoCreateIdentifier("__pb_" //$NON-NLS-1$
                ), __typedef_processbuilder);
        final SYM_type_class __typedef_map = new SYM_type_class("Map"); //$NON-NLS-1$
        final String __variable_map;
        symtable.register(__variable_map = symtable.autoCreateIdentifier("__map_" //$NON-NLS-1$
                ), __typedef_map);
        final SYM_type_class __typedef_instance = new SYM_type_class("Process"); //$NON-NLS-1$
        final String __variable_instance;
        symtable.register(__variable_instance = symtable.autoCreateIdentifier("__instance_" //$NON-NLS-1$
                ), __typedef_processbuilder);
        final SYM_type_class __typedef_interruptedexception = new SYM_type_class(
                "InterruptedException"); //$NON-NLS-1$
        final SYM_type_class __typedef_ioexception = new SYM_type_class("IOException"); //$NON-NLS-1$
        final String __variable_e = "e"; //$NON-NLS-1$
        final SYM_type_class __typedef_istreamreader = new SYM_type_class("InputStreamReader"); //$NON-NLS-1$
        final SYM_type_class __typedef_bufferedreader = new SYM_type_class("BufferedReader"); //$NON-NLS-1$
        final String __variable_reader;
        symtable.register(__variable_reader = symtable.autoCreateIdentifier("__reader_" //$NON-NLS-1$
                ), __typedef_bufferedreader);
        final SYM_type_class __typedef_string = new SYM_type_class("String"); //$NON-NLS-1$
        final String __variable_string;
        symtable.register(__variable_string = symtable.autoCreateIdentifier("__string_" //$NON-NLS-1$
                ), __typedef_string);
        final SYM_type_array __typedef_argument = new SYM_type_array("String"); //$NON-NLS-1$

        root
                .__native_call_seg()
                .addChild(
                        new AST_slist(
                                new AST_expression(
                                        new AST_variable_definition(new AST_type(__typedef_gisrc
                                                .type()), new AST_identifier(__variable_gisrc),
                                                new AST_assign_statement(new AST_null()))),
                                new AST_try(
                                        new AST_block(
                                                new AST_slist(
                                                        new AST_expression(
                                                                new AST_assign_statement(
                                                                        new AST_identifier(
                                                                                __variable_gisrc),
                                                                        new AST_dot(
                                                                                new AST_identifier(
                                                                                        __typedef_gisrc
                                                                                                .type()),
                                                                                new AST_method_call(
                                                                                        new AST_identifier(
                                                                                                "createTempFile" //$NON-NLS-1$
                                                                                        ),
                                                                                        new AST_elist(
                                                                                                new AST_literal(
                                                                                                        Projectspace.ARG_VERB_GISRC
                                                                                                                .toLowerCase()),
                                                                                                new AST_null(),
                                                                                                new AST_null()))))),
                                                        new AST_expression(
                                                                new AST_variable_definition(
                                                                        new AST_type(
                                                                                __typedef_writer
                                                                                        .type()),
                                                                        new AST_identifier(
                                                                                __variable_writer),
                                                                        new AST_assign_statement(
                                                                                new AST_ctor_call(
                                                                                        new AST_identifier(
                                                                                                __typedef_filewriter
                                                                                                        .type()),
                                                                                        new AST_elist(
                                                                                                new AST_identifier(
                                                                                                        __variable_gisrc)))))),
                                                        new AST_try(
                                                                new AST_block(
                                                                        new AST_slist(
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_GISDBASE
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_GISDBASE)
                                                                                                                                + System
                                                                                                                                        .getProperty("line.separator") //$NON-NLS-1$
                                                                                                                ))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_DEBUG
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_DEBUG)
                                                                                                                                + System
                                                                                                                                        .getProperty("line.separator") //$NON-NLS-1$
                                                                                                                ))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_MONITOR
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_MONITOR)
                                                                                                                                + System
                                                                                                                                        .getProperty("line.separator") //$NON-NLS-1$
                                                                                                                ))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_MAPSET
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_MAPSET)
                                                                                                                                + System
                                                                                                                                        .getProperty("line.separator") //$NON-NLS-1$
                                                                                                                ))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_LOCATION_NAME
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_LOCATION_NAME)
                                                                                                                                + System
                                                                                                                                        .getProperty("line.separator") //$NON-NLS-1$
                                                                                                                ))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "append" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        Projectspace.ARG_VERB_GRASS_GUI
                                                                                                                                .toUpperCase()
                                                                                                                                + ": " //$NON-NLS-1$
                                                                                                                                + projectSpace()
                                                                                                                                        .getArgVerbValue(
                                                                                                                                                Projectspace.ARG_VERB_GRASS_GUI)))))),
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "flush" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist())))))),
                                                        new AST_finally(
                                                                new AST_block(
                                                                        new AST_slist(
                                                                                new AST_expression(
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_writer),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "close" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist()))))))),
                                                new AST_slist(
                                                        // the array of arguments
                                                        // new AST_expression(
                                                        new AST_variable_definition(
                                                                new AST_type(__typedef_argv.type()),
                                                                new AST_identifier(__variable_argv),
                                                                new AST_assign_statement(
                                                                        new AST_array(
                                                                                __parameter_definition(
                                                                                        symtable,
                                                                                        operator),
                                                                                new AST_type(
                                                                                        __typedef_argument
                                                                                                .type()),
                                                                                new AST_identifier(
                                                                                        __variable_argv))))
                                                        // )
                                                        ,
                                                        // new AST_block(
                                                        // __parameter_definition(
                                                        // symtable,
                                                        // operator))
                                                        // ))),
                                                        new AST_expression(
                                                                new AST_variable_definition(
                                                                        new AST_type(
                                                                                __typedef_processbuilder
                                                                                        .type()),
                                                                        new AST_identifier(
                                                                                __variable_pb),
                                                                        new AST_assign_statement(
                                                                                new AST_ctor_call(
                                                                                        new AST_identifier(
                                                                                                __typedef_processbuilder
                                                                                                        .type()),
                                                                                        new AST_elist(
                                                                                                new AST_identifier(
                                                                                                        __variable_argv)))))),
                                                        // new AST_block(
                                                        new AST_slist(
                                                                new AST_expression(
                                                                        new AST_variable_definition(
                                                                                new AST_type(
                                                                                        __typedef_map
                                                                                                .type()),
                                                                                new AST_identifier(
                                                                                        __variable_map),
                                                                                new AST_assign_statement(
                                                                                        new AST_method_call(
                                                                                                new AST_dot(
                                                                                                        new AST_identifier(
                                                                                                                __variable_pb),
                                                                                                        new AST_identifier(
                                                                                                                "environment") //$NON-NLS-1$
                                                                                                ),
                                                                                                new AST_elist())))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_GISRC),
                                                                                        new AST_dot(
                                                                                                new AST_identifier(
                                                                                                        __variable_gisrc),
                                                                                                new AST_method_call(
                                                                                                        new AST_identifier(
                                                                                                                "getAbsolutePath" //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist()))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_GISBASE),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_GISBASE))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_GRASS_LD_LIBRARY_PATH),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_GRASS_LD_LIBRARY_PATH))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_LD_LIBRARY_PATH),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_LD_LIBRARY_PATH))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_DYLD_LIBRARY_PATH),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_DYLD_LIBRARY_PATH))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_GRASS_SH),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_GRASS_SH))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_USER_HOME),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_USER_HOME)))))

                                                                ,
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_USER_NAME),
                                                                                        new AST_literal(
                                                                                                projectSpace()
                                                                                                        .getArgVerbValue(
                                                                                                                Projectspace.ARG_VERB_USER_NAME))))),
                                                                new AST_expression(
                                                                        new AST_method_call(
                                                                                new AST_dot(
                                                                                        new AST_identifier(
                                                                                                __variable_map),
                                                                                        new AST_identifier(
                                                                                                "put") //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_literal(
                                                                                                Projectspace.ARG_VERB_PATH),
                                                                                        new AST_plus(
                                                                                                new AST_literal(
                                                                                                        projectSpace()
                                                                                                                .getArgVerbValue(
                                                                                                                        Projectspace.ARG_VERB_PATH)),
                                                                                                new AST_method_call(
                                                                                                        new AST_dot(
                                                                                                                new AST_identifier(
                                                                                                                        "System") //$NON-NLS-1$
                                                                                                                ,
                                                                                                                new AST_identifier(
                                                                                                                        "getProperty") //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist(
                                                                                                                new AST_literal(
                                                                                                                        "java.library.path" //$NON-NLS-1$
                                                                                                                ))))))))
                                                        // )
                                                        ,
                                                        new AST_expression(
                                                                new AST_dot(
                                                                        new AST_identifier(
                                                                                __variable_pb),
                                                                        new AST_method_call(
                                                                                new AST_identifier(
                                                                                        "redirectErrorStream" //$NON-NLS-1$
                                                                                ),
                                                                                new AST_elist(
                                                                                        new AST_bool_true())))),
                                                        new AST_expression(
                                                                new AST_variable_definition(
                                                                        new AST_type(
                                                                                __typedef_instance
                                                                                        .type()),
                                                                        new AST_identifier(
                                                                                __variable_instance),
                                                                        new AST_assign_statement(
                                                                                new AST_null()))),
                                                        new AST_try(
                                                                new AST_block(
                                                                        new AST_slist(
                                                                                new AST_expression(
                                                                                        new AST_assign_statement(
                                                                                                new AST_identifier(
                                                                                                        __variable_instance),
                                                                                                new AST_method_call(
                                                                                                        new AST_dot(
                                                                                                                new AST_identifier(
                                                                                                                        __variable_pb),
                                                                                                                new AST_identifier(
                                                                                                                        "start") //$NON-NLS-1$
                                                                                                        ),
                                                                                                        new AST_elist()))),
                                                                                new AST_expression(
                                                                                        new AST_variable_definition(
                                                                                                new AST_type(
                                                                                                        __typedef_bufferedreader
                                                                                                                .type()),
                                                                                                new AST_identifier(
                                                                                                        __variable_reader),
                                                                                                new AST_assign_statement(
                                                                                                        new AST_ctor_call(
                                                                                                                new AST_identifier(
                                                                                                                        __typedef_bufferedreader
                                                                                                                                .type()),
                                                                                                                new AST_elist(
                                                                                                                        new AST_ctor_call(
                                                                                                                                new AST_identifier(
                                                                                                                                        __typedef_istreamreader
                                                                                                                                                .type()),
                                                                                                                                new AST_elist(
                                                                                                                                        new AST_method_call(
                                                                                                                                                new AST_dot(
                                                                                                                                                        new AST_identifier(
                                                                                                                                                                __variable_instance),
                                                                                                                                                        new AST_identifier(
                                                                                                                                                                "getInputStream") //$NON-NLS-1$
                                                                                                                                                ),
                                                                                                                                                new AST_elist())))))))),
                                                                                new AST_expression(
                                                                                        new AST_variable_definition(
                                                                                                new AST_type(
                                                                                                        __typedef_string
                                                                                                                .type()),
                                                                                                new AST_identifier(
                                                                                                        __variable_string))),
                                                                                new AST_while(
                                                                                        new AST_condition(
                                                                                                new AST_logical_unequal(
                                                                                                        new AST_null(),
                                                                                                        new AST_elist(
                                                                                                                new AST_assign_statement(
                                                                                                                        new AST_identifier(
                                                                                                                                __variable_string),
                                                                                                                        new AST_method_call(
                                                                                                                                new AST_dot(
                                                                                                                                        new AST_identifier(
                                                                                                                                                __variable_reader),
                                                                                                                                        new AST_identifier(
                                                                                                                                                "readLine") //$NON-NLS-1$
                                                                                                                                ),
                                                                                                                                new AST_elist()))))),
                                                                                        new AST_block(
                                                                                                new AST_slist(
                                                                                                        new AST_expression(
                                                                                                                new AST_method_call(
                                                                                                                        new AST_identifier(
                                                                                                                                "println") //$NON-NLS-1$
                                                                                                                        ,
                                                                                                                        new AST_elist(
                                                                                                                                new AST_identifier(
                                                                                                                                        __variable_string))))))),
                                                                                new AST_expression(
                                                                                        new AST_method_call(
                                                                                                new AST_dot(
                                                                                                        new AST_identifier(
                                                                                                                __variable_instance),
                                                                                                        new AST_identifier(
                                                                                                                "waitFor") //$NON-NLS-1$
                                                                                                ),
                                                                                                new AST_elist()))))),
                                                        new AST_finally(
                                                                new AST_block(
                                                                        new AST_slist(
                                                                                new AST_if(
                                                                                        new AST_condition(
                                                                                                new AST_conditional_and(
                                                                                                        new AST_logical_unequal(
                                                                                                                new AST_null(),
                                                                                                                new AST_identifier(
                                                                                                                        __variable_instance)),
                                                                                                        new AST_logical_unequal(
                                                                                                                new AST_number_integer(
                                                                                                                        "0") //$NON-NLS-1$
                                                                                                                ,
                                                                                                                new AST_method_call(
                                                                                                                        new AST_dot(
                                                                                                                                new AST_identifier(
                                                                                                                                        __variable_instance),
                                                                                                                                new AST_identifier(
                                                                                                                                        "exitValue") //$NON-NLS-1$
                                                                                                                        ),
                                                                                                                        new AST_elist())))),
                                                                                        new AST_then(
                                                                                                new AST_slist(
                                                                                                        new AST_expression(
                                                                                                                new AST_method_call(
                                                                                                                        new AST_identifier(
                                                                                                                                "println") //$NON-NLS-1$
                                                                                                                        ,
                                                                                                                        new AST_elist(
                                                                                                                                new AST_plus(
                                                                                                                                        new AST_literal(
                                                                                                                                                operator
                                                                                                                                                        .expression()),
                                                                                                                                        new AST_literal(
                                                                                                                                                " : exit code: ") //$NON-NLS-1$
                                                                                                                                        ,
                                                                                                                                        new AST_method_call(
                                                                                                                                                new AST_dot(
                                                                                                                                                        new AST_identifier(
                                                                                                                                                                __variable_instance),
                                                                                                                                                        new AST_identifier(
                                                                                                                                                                "exitValue") //$NON-NLS-1$
                                                                                                                                                ),
                                                                                                                                                new AST_elist())))))))))))))),
                                new AST_catch(new AST_elist(new AST_variable_definition(
                                        new AST_type(__typedef_interruptedexception.type()),
                                        new AST_identifier(__variable_e))),
                                        new AST_block(new AST_slist(new AST_expression(
                                                new AST_method_call(new AST_identifier("println") //$NON-NLS-1$
                                                        , new AST_elist(new AST_identifier(
                                                                __variable_e))))))), new AST_catch(
                                        new AST_elist(new AST_variable_definition(new AST_type(
                                                __typedef_ioexception.type()), new AST_identifier(
                                                __variable_e))), new AST_block(new AST_slist(
                                                new AST_expression(new AST_method_call(
                                                        new AST_identifier("println") //$NON-NLS-1$
                                                        , new AST_elist(new AST_identifier(
                                                                __variable_e))))))),
                                new AST_finally(new AST_block(new AST_slist(new AST_if(
                                        new AST_condition(new AST_logical_unequal(new AST_null(),
                                                new AST_identifier(__variable_gisrc))),
                                        new AST_then(new AST_slist(new AST_expression(new AST_dot(
                                                new AST_identifier(__variable_gisrc),
                                                new AST_method_call(new AST_identifier("delete" //$NON-NLS-1$
                                                ), new AST_elist())))))))))));
    } // __native_model
    /**
     * <p>
     * Creates as output the tree-like intermediate representation of the target code for the
     * command line arguments.
     * </p>
     * 
     * @param operator - a "parameter definition" operator.
     * @return Returns the produced tree-like intermediate representation of the target code.
     */
    @SuppressWarnings("unchecked")
    private AST<ASTs> __parameter_definition( Symtable<SYMs> symtable, APT_native_model operator ) {

        final AST<ASTs> retval = new AST_comma();
        try {

            APT_parameter_definition definition = operator.parameter_defs();
            if (null != definition) {

                for( int i = 0; i < definition.size(); ++i ) {

                    APT_parameter parameter;
                    AST<ASTs> tree;
                    parameter = (APT_parameter) definition.getChild(i);
                    tree = (1 >= parameter.size()) ? retval : retval.addChild(new AST_plus());
                    for( int n = 0; n < parameter.size(); ++n ) {

                        APT<APTs> operand = parameter.getChild(n);
                        switch( operand.identifier() ) {
                        case APT_VARIABLE:
                            APT_variable __variable = (APT_variable) operand;
                            tree.addChild(new AST_identifier(__variable.variable_name()));
                            break;

                        case APT_LITERAL:
                        default:
                            tree.addChild(new AST_literal(operand.expression()));
                        }
                    }
                }
            }

            retval.addChild(0, new AST_plus(new AST_literal(projectSpace().getArgVerbValue(
                    Projectspace.ARG_VERB_BINARY_PATH)
                    + File.separator), new AST_literal(operator.token().expression())));
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            e.printStackTrace();
        }

        return retval;
    } // __parameter_definition

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor#translate()
     */
    @SuppressWarnings("unchecked")
    protected void translate( Symtable<SYMs> symtable, APT<APTs> parseTree, AST<ASTs> syntaxTree ) {

        try {

            AST_root __astroot = (AST_root) syntaxTree;
            switch( parseTree.identifier() ) {
            case APT_NATIVE_MODEL:
                __native_model(symtable, __astroot, (APT_native_model) parseTree);
                break;

            case APT_STATEMENT:
                final APT_statement __model_def;
                __model_def = (APT_statement) parseTree;
                for( int i = 0; i < __model_def.size(); ++i )
                    translate(symtable, __model_def.getChild(i), __astroot);
                break;

            case APT_ROOT:
                final APT_root __aptroot = (APT_root) parseTree;
                for( int i = 0; i < __aptroot.size(); ++i )
                    translate(symtable, __aptroot.getChild(i), __astroot);
                break;
            }
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            e.printStackTrace();
        }
    } // translate

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor#intermediate()
     */
    public final APT<APTs> intermediate( Symtable<SYMs> symtable, Reader sourceCode, int line ) {

        APT<APTs> retval = null;
        try {

            Lexer<TOKs> scanner = new NativeMLScanner(projectSpace(), sourceCode, line);
            Parser<APTs, TOKs, SYMs> parser = new NativeMLParser(projectSpace(), scanner);
            retval = parser.parse(symtable, new APT_root());
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                System.err.println(e);

            e.printStackTrace();
        }

        return retval;
    } // intermediate

} // NativeML4j
