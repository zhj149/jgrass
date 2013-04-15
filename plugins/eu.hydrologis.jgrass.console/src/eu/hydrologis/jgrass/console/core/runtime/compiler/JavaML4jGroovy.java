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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerError;
import eu.hydrologis.jgrass.console.core.internal.analysis.CompilerWarning;
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
import eu.hydrologis.jgrass.console.core.runtime.lexer.JavaMLScanner;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_argument;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_argument_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_input;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_io_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_literal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_output;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_root;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_variable;
import eu.hydrologis.jgrass.console.core.runtime.nodes.APT_variable_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_array;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_assign_statement;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_block;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_bool_false;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_bool_true;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_comma;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_condition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_ctor_call;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_dot;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_elist;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_else;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_expression;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_identifier;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_if;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_literal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_logical_equal;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_method_call;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_null;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_number_integer;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_plus;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_root;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_slist;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_then;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_throw_call;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_type;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_variable_definition;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_while;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_input;
import eu.hydrologis.jgrass.console.core.runtime.nodes.N_output;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_constant_value;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_array;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_class;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_long;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_primitive;
import eu.hydrologis.jgrass.console.core.runtime.parser.JavaMLParser;

/**
 * <p>
 * This is the pre-processor that processes a <i>JGRASS</i> Java based, OpenMI compliant model;
 * translate it into equivalent target code.
 * </p>
 * 
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 * @author Andrea Antonello (www.hydrologis.com)
 */
public final class JavaML4jGroovy extends AbstractML4j {

    private List<String> outputTrees;
    private List<APT_output> outputParserTrees;
    private List<String> outputAsteriscTrees;
    private List<APT_output> outputAsteriscParserTrees;

    // Construction
    /**
     * <p>
     * The copy constructor <code>JavaML4j</code> defines this preprocessor object using the
     * specified project-space.
     * </p>
     * 
     * @param projectSpace - the project-space.
     */
    public JavaML4jGroovy( Projectspace projectSpace ) {

        super(projectSpace);

        outputTrees = new ArrayList<String>();
        outputParserTrees = new ArrayList<APT_output>();
        outputAsteriscTrees = new ArrayList<String>();
        outputAsteriscParserTrees = new ArrayList<APT_output>();
    } // JavaML4j

    // Operations
    /**
     * <p>
     * Creates as output the tree-like intermediate representation of the target code for the
     * argument definitions of a linkable component - "input", "output", "java_model".
     * </p>
     * 
     * @param operator - a "argument definition" operator.
     * @return Returns the produced tree-like intermediate representation of the target code.
     */
    @SuppressWarnings("unchecked")
    private AST<ASTs> __argument_definition( APT_argument_definition operator ) {

        final AST_comma retval = new AST_comma();
        for( int i = 0; i < operator.size(); ++i ) {

            APT_argument operand = (APT_argument) operator.getChild(i);
            final AST<ASTs> lvalue;
            final AST<ASTs> rvalue;
            switch( operand.flag().identifier() ) {
            case APT_LITERAL:
                lvalue = new AST_literal(((APT_literal) operand.flag()).expression());
                break;

            default:
                lvalue = null;
            }

            switch( operand.value().identifier() ) {
            case APT_VARIABLE:
                rvalue = new AST_identifier(((APT_variable) operand.value()).variable_name());
                break;

            case APT_LITERAL:
                rvalue = new AST_literal(((APT_literal) operand.value()).expression());
                break;

            default:
                rvalue = null;
            }

            retval.addChild(new AST_ctor_call(new AST_identifier("Argument") //$NON-NLS-1$
                    , new AST_elist(lvalue, rvalue, new AST_bool_true())));
        }

        return retval;
    } // __argument_definition

    /**
     * <p>
     * Translation of the "root" operator into the tree-like intermediate representation of the
     * target code; adds default constants initialization for the model statement - path to the
     * <i>GRASS</i> database, the used location and mapset.
     * </p>
     * 
     * @param symtable - the symbol table.
     * @param root - the root of the syntax tree.
     * @param operator - the root of the parse tree.
     */
    @SuppressWarnings("unchecked")
    private void __initialize( Symtable<SYMs> symtable, AST_root root, APT_root operator ) {

        // GRASSDB
        final SYM_constant_value grassdb = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_GRASSDB);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_grassdb") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(grassdb.value())))));

        // LOCATION
        final SYM_constant_value location = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_LOCATION);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_location") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(location.value())))));

        // MAPSET
        final SYM_constant_value mapset = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_MAPSET);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_mapset") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(mapset.value())))));

        // START ADDITION Andrea Antonello - andrea.antonello@gmail.com
        // startdate
        final SYM_constant_value startdate = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_TIME_START_UP);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_startdate") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(startdate.value())))));
        // enddate
        final SYM_constant_value enddate = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_TIME_ENDING_UP);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_enddate") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(enddate.value())))));
        // deltat
        final SYM_constant_value deltat = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_TIME_DELTA);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_deltat") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(deltat.value())))));
        // remotedburl
        final SYM_constant_value remotedburl = (SYM_constant_value) symtable
                .lookup(Projectspace.ARG_VERB_REMOTEDB);
        root.__vdefs_seg().addChild(
                new AST_expression(new AST_variable_definition(new AST_type("String") //$NON-NLS-1$
                        , new AST_identifier("__global_remotedb") //$NON-NLS-1$
                        , new AST_assign_statement(new AST_literal(remotedburl.value())))));
        // END ADDITION
    } // __initialize

    /**
     * <p>
     * The method<code>__is_nested</code> returns <code>true</code>, if the specified candiate
     * belongs to a nested model, otherwise <code>false</code>.
     * </p>
     * 
     * @param candidate - a node or leaf node of the abstract parse tree.
     * @return If the specified candiate belongs to a nested model the method returns
     *         <code>true</code>, otherwise <code>false</code>.
     */
    private boolean __is_nested( APT<APTs> candidate ) {

        APT<APTs> __modeldef = candidate.lookup(APTs.APT_STATEMENT);
        if (null != __modeldef)
            __modeldef = __modeldef.lookup(APTs.APT_STATEMENT);

        return (null != __modeldef) ? true : false;
    } // __is_nested

    /**
     * <p>
     * Translates a linkable model component - "java_model" - into the tree-like intermediate
     * representation of the target code.
     * </p>
     * 
     * @param symtable - the symbol table.
     * @param root - the root of the syntax tree.
     * @param operator - a "java_model" operator.
     */
    @SuppressWarnings("unchecked")
    private void __java_model( Symtable<SYMs> symtable, AST_root root, APT_java_model operator ) {

        final APT_variable_definition __variable_def;
        __variable_def = (APT_variable_definition) operator.linkable_variable_def();
        final SYM_type_java_model aptmodel_typedef;
        aptmodel_typedef = (SYM_type_java_model) symtable.lookup(operator.linkable_variable_def()
                .variable_name());

        final SYM_type_array __typedef_argv = new SYM_type_array("Argument"); //$NON-NLS-1$
        final String __variable_argv;
        symtable.register(__variable_argv = symtable.autoCreateIdentifier("__argv_" //$NON-NLS-1$
                ), __typedef_argv);

        root
                .__ctor_call_seg()
                .addChild(
                        new AST_slist(
                                new AST_expression(new AST_variable_definition(
                                        new AST_identifier("def ")
                                        // new AST_type(__variable_def.type_name())
                                        , new AST_identifier(__variable_def.variable_name()),
                                        new AST_assign_statement(new AST_ctor_call(
                                                new AST_identifier(__variable_def.type_name()),
                                                new AST_elist(new AST_method_call(new AST_dot(
                                                        new AST_identifier("out") //$NON-NLS-1$
                                                        )), new AST_method_call(new AST_dot(
                                                        new AST_identifier("err") //$NON-NLS-1$
                                                        ))))))),
                                // new AST_block(
                                new AST_slist(
                                // new AST_expression(
                                        new AST_variable_definition(
                                                new AST_type(__typedef_argv.type()),
                                                new AST_identifier(__variable_argv),
                                                new AST_assign_statement(
                                                        new AST_array(
                                                                __argument_definition((APT_argument_definition) operator
                                                                        .argument_defs()),
                                                                new AST_type(__typedef_argv.type()),
                                                                new AST_identifier(__variable_argv))))
                                        // )
                                        , new AST_expression(new AST_method_call(new AST_dot(
                                                new AST_identifier(__variable_def.variable_name()),
                                                new AST_identifier("initialize") //$NON-NLS-1$
                                                ), new AST_elist(
                                                        new AST_identifier(__variable_argv)))))
                        // )
                        ));

        root.__ptor_call_seg().addChild(
                0,
                new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                        __variable_def.variable_name()), new AST_identifier("prepare") //$NON-NLS-1$
                        ), new AST_elist())));

        if (true == operator.token().__asterisk() || false == aptmodel_typedef.hasExchangeItems()) {

            AST<ASTs> param2;
            AST<ASTs> __rvalue = new AST_expression(new AST_method_call(new AST_dot(
                    new AST_identifier(__variable_def.variable_name()), new AST_identifier(
                            "getValues") //$NON-NLS-1$
                    ), new AST_elist(new AST_null(), param2 = new AST_null())));

            String szEndingup = projectSpace()
                    .getArgVerbValue(Projectspace.ARG_VERB_TIME_ENDING_UP);
            String szStartup = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_START_UP);
            String szDelta = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_DELTA);

            if (null == szDelta || null == szStartup || null == szEndingup) {

                root.__gtor_call_seg().addChild(__rvalue);
            } else {

                root.__gtor_call_seg().addChild(
                        __time_loop(symtable, __variable_def.variable_name(), param2));
            }
        }

        root.__dtor_call_seg().addChild(
                0,
                new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                        __variable_def.variable_name()), new AST_identifier("finish") //$NON-NLS-1$
                        ), new AST_elist())));

        // Nested model?
        if (null != operator.lookup(APTs.APT_IO_DEFINITION)) {

            final APT<APTs> __io_xchng_item = operator.parent().parent();
            final String quantity;
            switch( __io_xchng_item.identifier() ) {
            case APT_INPUT:
                final N_input itoken = ((APT_input) __io_xchng_item).token_flag();
                quantity = itoken.__quantity();
                break;

            case APT_OUTPUT:
                final N_output otoken = ((APT_output) __io_xchng_item).token_flag();
                quantity = otoken.__quantity();
                break;

            default:
                quantity = null;
            }

            APT<APTs> __model = ((APT_statement) __io_xchng_item.lookup(APTs.APT_STATEMENT))
                    .__model();
            final SYM_type_java_model __model_typedef;
            __model_typedef = (SYM_type_java_model) symtable.lookup(operator
                    .linkable_variable_def().variable_name());
            root.__ctor_call_seg().addChild(
                    __linkage(quantity, operator, aptmodel_typedef.exchangeItem(quantity), __model,
                            __model_typedef.exchangeItem(quantity)));
        }
    } // __java_model

    /**
     * <p>
     * Creates as output the tree-like intermediate representation of the target code for the
     * connection of linkable components: "input", "output", "java_model".
     * </p>
     * 
     * @param quantity - the quantity respectively the quality value of the connection being
     *        created.
     * @param op1 - a "input", "output" or a "java_model" operator.
     * @param item1 - the associated exchange item, either a input or a output exchange item.
     * @param op2 - a "input", "output" or a "java_model" operator.
     * @param item2 - the associated exchange item, either a input or a output exchange item.
     * @return Returns the produced tree-like intermediate representation of the target code.
     */
    @SuppressWarnings("unchecked")
    private AST<ASTs> __linkage( String quantity, APT<APTs> op1, String item1, APT<APTs> op2,
            String item2 ) {

        // Required variable informations
        final APT_variable_definition __link_variable_def;
        final APT_variable_definition __out_variable_def;
        final APT_variable_definition __in_variable_def;

        // Gathering required informations...
        if (APTs.APT_OUTPUT == op2.identifier()) {

            __link_variable_def = ((APT_output) op2).link_variable_def();
        } else {

            switch( op1.identifier() ) {
            case APT_INPUT:
                __link_variable_def = ((APT_input) op1).link_variable_def();
                break;

            case APT_JAVA_MODEL:
                __link_variable_def = ((APT_java_model) op1).link_variable_def();
                break;

            case APT_OUTPUT:
                __link_variable_def = ((APT_output) op1).link_variable_def();
                break;

            default:
                __link_variable_def = null;
            }
        }

        switch( op1.identifier() ) {
        case APT_INPUT:
            __out_variable_def = ((APT_input) op1).linkable_variable_def();
            break;

        case APT_JAVA_MODEL:
            __out_variable_def = ((APT_java_model) op1).linkable_variable_def();
            break;

        case APT_OUTPUT:
            __out_variable_def = ((APT_output) op1).linkable_variable_def();
            break;

        default:
            __out_variable_def = null;
        }

        switch( op2.identifier() ) {
        case APT_INPUT:
            __in_variable_def = ((APT_input) op2).linkable_variable_def();
            break;

        case APT_JAVA_MODEL:
            __in_variable_def = ((APT_java_model) op2).linkable_variable_def();
            break;

        case APT_OUTPUT:
            __in_variable_def = ((APT_output) op2).linkable_variable_def();
            break;

        default:
            __in_variable_def = null;
        }

        // Creating the Abstract Syntax Tree for the linkage...
        return new AST_slist(new AST_expression(new AST_variable_definition(new AST_type(
                __link_variable_def.type_name()), new AST_identifier(__link_variable_def
                .variable_name()), new AST_assign_statement(new AST_ctor_call(new AST_identifier(
                __link_variable_def.type_name()), new AST_elist(new AST_null(), new AST_literal(
                quantity)))))),
        // new AST_block(
                new AST_slist(new AST_expression(new AST_method_call(new AST_dot(
                        new AST_identifier(__link_variable_def.variable_name()),
                        new AST_identifier("connect") //$NON-NLS-1$
                        ), new AST_elist(new AST_comma(new AST_identifier(__out_variable_def
                                .variable_name()), new AST_method_call(new AST_dot(
                                new AST_identifier(__out_variable_def.variable_name()),
                                new AST_identifier("getOutputExchangeItem") //$NON-NLS-1$
                                ), new AST_elist(new AST_number_integer(item1))),
                                new AST_identifier(__in_variable_def.variable_name()),
                                new AST_method_call(new AST_dot(new AST_identifier(
                                        __in_variable_def.variable_name()), new AST_identifier(
                                        "getInputExchangeItem") //$NON-NLS-1$
                                        ), new AST_elist(new AST_number_integer(item2))))))),
                        new AST_if(new AST_condition(new AST_logical_equal(new AST_bool_false(),
                                new AST_method_call(new AST_dot(new AST_identifier(
                                        __link_variable_def.variable_name()), new AST_identifier(
                                        "isConnected") //$NON-NLS-1$
                                        ), new AST_elist()))), new AST_then(new AST_slist(
                                new AST_expression(new AST_throw_call(new AST_ctor_call(
                                        new AST_identifier("RuntimeException") //$NON-NLS-1$
                                        , new AST_elist(new AST_plus(new AST_literal("Link ") //$NON-NLS-1$
                                                , new AST_method_call(new AST_dot(
                                                        new AST_identifier(__link_variable_def
                                                                .variable_name()),
                                                        new AST_identifier("getID") //$NON-NLS-1$
                                                        ), new AST_elist()), new AST_literal(
                                                        " not created.") //$NON-NLS-1$
                                                )))))))))
        // )
        );
    } // __linkage

    /**
     * <p>
     * Translates a linkable input exchange component - "input" - of a model into a tree-like
     * intermediate representation of the target code.
     * </p>
     * 
     * @param symtable - the symbol table.
     * @param root - the root of the syntax tree.
     * @param operator - a "input" operator.
     */
    @SuppressWarnings("unchecked")
    private void __model_input( Symtable<SYMs> symtable, AST_root root, APT_input operator ) {

        // Gathering required informations
        final APT_variable_definition __linkable_variable_def;
        __linkable_variable_def = operator.linkable_variable_def();
        final APT_variable_definition __link_variable_def;
        __link_variable_def = operator.link_variable_def();
        final APT_statement __modeldef;
        __modeldef = (APT_statement) operator.lookup(APTs.APT_STATEMENT);
        final APT_java_model __model = (APT_java_model) __modeldef.__model();
        final APT_variable_definition __model_variable_def;
        __model_variable_def = __model.linkable_variable_def();
        final SYM_type_java_model __model_typedef;
        __model_typedef = (SYM_type_java_model) symtable.lookup(__model_variable_def
                .variable_name());
        final boolean bIsNested = (__is_nested(operator) && TOKs.CHARACTER_ASTERISK == operator
                .token_value().identifier())
                || (null != operator.nested_statement() && null != operator.nested_statement()
                        .__model());

        if (false == bIsNested) {

            final SYM_type_array __typedef_argv = new SYM_type_array("Argument"); //$NON-NLS-1$
            final String __variable_argv;
            symtable.register(__variable_argv = symtable.autoCreateIdentifier("__argv_" //$NON-NLS-1$
                    ), __typedef_argv);

            root
                    .__ctor_call_seg()
                    .addChild(
                            new AST_slist(
                                    new AST_expression(new AST_variable_definition(new AST_type(
                                            __linkable_variable_def.type_name()),
                                            new AST_identifier(__linkable_variable_def
                                                    .variable_name()), new AST_assign_statement(
                                                    new AST_ctor_call(new AST_identifier(
                                                            __linkable_variable_def.type_name()),
                                                            new AST_elist(new AST_method_call(
                                                                    new AST_dot(new AST_identifier(
                                                                            "out") //$NON-NLS-1$
                                                                    )), new AST_method_call(
                                                                    new AST_dot(new AST_identifier(
                                                                            "err") //$NON-NLS-1$
                                                                    ))))))),
                                    // new AST_block(
                                    new AST_slist(
                                    // new AST_expression(
                                            new AST_variable_definition(
                                                    new AST_type(__typedef_argv.type()),
                                                    new AST_identifier(__variable_argv),
                                                    new AST_assign_statement(
                                                            new AST_array(
                                                                    __argument_definition((APT_argument_definition) operator
                                                                            .argument_def()),
                                                                    new AST_type(__typedef_argv
                                                                            .type()),
                                                                    new AST_identifier(
                                                                            __variable_argv))

                                                    // new AST_block(
                                                    // __argument_definition((APT_argument_definition
                                                    // ) operator
                                                    // .argument_def()))

                                                    ))
                                            // )
                                            , new AST_expression(new AST_method_call(new AST_dot(
                                                    new AST_identifier(__linkable_variable_def
                                                            .variable_name()), new AST_identifier(
                                                            "initialize") //$NON-NLS-1$
                                                    ), new AST_elist(new AST_identifier(
                                                            __variable_argv)))))
                                    // )
                                    , __linkage(operator.token_flag().__quantity(), operator, "0" //$NON-NLS-1$
                                            , __modeldef.__model(), __model_typedef
                                                    .exchangeItem(operator.token_flag()
                                                            .__quantity()))));

            root.__ptor_call_seg().addChild(
                    0,
                    new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                            __linkable_variable_def.variable_name()), new AST_identifier("prepare") //$NON-NLS-1$
                            ), new AST_elist())));

            if (true == operator.token_flag().__asterisk()
                    || TOKs.CHARACTER_ASTERISK == operator.token_value().identifier()) {

                final AST<ASTs> param2;
                final AST<ASTs> __astetor;
                switch( operator.token_value().identifier() ) {
                case CHARACTER_ASTERISK:
                    AST<ASTs> __lvalue = null;
                    if (null != __modeldef.__external_variable())
                        __lvalue = new AST_identifier(__modeldef.__external_variable()
                                .variable_name());
                    AST<ASTs> __rvalue = new AST_expression(new AST_method_call(new AST_dot(
                            new AST_identifier(__model_variable_def.variable_name()),
                            new AST_identifier("getValues") //$NON-NLS-1$
                            ), new AST_elist(new AST_null(), param2 = new AST_method_call(
                                    new AST_dot(new AST_identifier(__link_variable_def
                                            .variable_name()), new AST_identifier("getID") //$NON-NLS-1$
                                    ), new AST_elist()))));
                    if (null == __lvalue) {

                        __astetor = __rvalue;
                    } else {

                        __astetor = new AST_assign_statement(__lvalue, __rvalue);
                    }
                    break;

                case LITERAL:
                case VARIABLE:
                default:
                    __astetor = new AST_expression(new AST_method_call(new AST_dot(
                            new AST_identifier(__linkable_variable_def.variable_name()),
                            new AST_identifier("getValues") //$NON-NLS-1$
                            ), new AST_elist(new AST_null(), param2 = new AST_null())));
                }

                String szEndingup = projectSpace().getArgVerbValue(
                        Projectspace.ARG_VERB_TIME_ENDING_UP);
                String szStartup = projectSpace().getArgVerbValue(
                        Projectspace.ARG_VERB_TIME_START_UP);
                String szDelta = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_DELTA);

                if (null == szDelta || null == szStartup || null == szEndingup) {

                    root.__gtor_call_seg().addChild(0, __astetor);
                } else {

                    root.__gtor_call_seg().addChild(0,
                            __time_loop(symtable, __linkable_variable_def.variable_name(), param2));
                }
            }

            root.__dtor_call_seg().addChild(
                    0,
                    new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                            __linkable_variable_def.variable_name()), new AST_identifier("finish") //$NON-NLS-1$
                            ), new AST_elist())));
        }
    } // __model_input

    /**
     * <p>
     * Translates a linkable output exchange component - "output" - of a model into a tree-like
     * intermediate representation of the target code.
     * </p>
     * 
     * @param symtable - the symbol table.
     * @param root - the root of the syntax tree.
     * @param operator - a "output" operator.
     */
    @SuppressWarnings("unchecked")
    private void __model_output( Symtable<SYMs> symtable, AST_root root, APT_output operator ) {

        // Gathering required informations
        final APT_variable_definition __linkable_variable_def;
        __linkable_variable_def = operator.linkable_variable_def();
        final APT_statement __modeldef;
        __modeldef = (APT_statement) operator.lookup(APTs.APT_STATEMENT);

        final boolean bIsNested = (__is_nested(operator) && TOKs.CHARACTER_ASTERISK == operator
                .token_value().identifier())
                || (null != operator.nested_statement() && null != operator.nested_statement()
                        .__model());

        if (false == bIsNested) {

            final SYM_type_array __typedef_argv = new SYM_type_array("Argument"); //$NON-NLS-1$
            final String __variable_argv;
            symtable.register(__variable_argv = symtable.autoCreateIdentifier("__argv_" //$NON-NLS-1$
                    ), __typedef_argv);
            final APT_java_model __model;
            __model = (APT_java_model) __modeldef.__model();
            final APT_variable_definition __model_variable_def;
            __model_variable_def = __model.linkable_variable_def();
            final SYM_type_java_model __model_typedef;
            __model_typedef = (SYM_type_java_model) symtable.lookup(__model_variable_def
                    .variable_name());

            root
                    .__ctor_call_seg()
                    .addChild(
                            new AST_slist(
                                    new AST_expression(new AST_variable_definition(new AST_type(
                                            __linkable_variable_def.type_name()),
                                            new AST_identifier(__linkable_variable_def
                                                    .variable_name()), new AST_assign_statement(
                                                    new AST_ctor_call(new AST_identifier(
                                                            __linkable_variable_def.type_name()),
                                                            new AST_elist(new AST_method_call(
                                                                    new AST_dot(new AST_identifier(
                                                                            "out") //$NON-NLS-1$
                                                                    )), new AST_method_call(
                                                                    new AST_dot(new AST_identifier(
                                                                            "err") //$NON-NLS-1$
                                                                    ))))))),
                                    // new AST_block(
                                    new AST_slist(
                                    // new AST_expression(
                                            new AST_variable_definition(
                                                    new AST_type(__typedef_argv.type()),
                                                    new AST_identifier(__variable_argv),
                                                    new AST_assign_statement(
                                                            new AST_array(
                                                                    __argument_definition((APT_argument_definition) operator
                                                                            .argument_defs()),
                                                                    new AST_type(__typedef_argv
                                                                            .type()),
                                                                    new AST_identifier(
                                                                            __variable_argv))
                                                    // new AST_block(
                                                    // __argument_definition((APT_argument_definition
                                                    // ) operator
                                                    // .argument_defs()))
                                                    //                                                                    
                                                    ))
                                            // )
                                            , new AST_expression(new AST_method_call(new AST_dot(
                                                    new AST_identifier(__linkable_variable_def
                                                            .variable_name()), new AST_identifier(
                                                            "initialize") //$NON-NLS-1$
                                                    ), new AST_elist(new AST_identifier(
                                                            __variable_argv)))))
                                    // )
                                    , __linkage(operator.token_flag().__quantity(), __modeldef
                                            .__model(), __model_typedef.exchangeItem(operator
                                            .token_flag().__quantity()), operator, "0" //$NON-NLS-1$
                                    )));

            root.__ptor_call_seg().addChild(
                    0,
                    new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                            __linkable_variable_def.variable_name()), new AST_identifier("prepare") //$NON-NLS-1$
                            ), new AST_elist())));

        }
    } // __model_output

    /**
     * <p>
     * Creates as output the tree-like intermediate representation of the target code for the loop
     * of time-dependent model statement.
     * </p>
     * 
     * @param varname - a variable name.
     * @return Returns the produced tree-like intermediate representation of the target code.
     */
    @SuppressWarnings("unchecked")
    private AST<ASTs> __time_loop( Symtable<SYMs> symtable, String varname, AST<ASTs> param2 ) {

        final SYM_type_class __typedef_dateformat = new SYM_type_class("DateFormat"); //$NON-NLS-1$
        final String __variable_dateformat;
        symtable.register(__variable_dateformat = symtable.autoCreateIdentifier("__dateformat_" //$NON-NLS-1$
                ), __typedef_dateformat);

        final SYM_type_class __typedef_date = new SYM_type_class("Date"); //$NON-NLS-1$
        final String __variable_date;
        symtable.register(__variable_date = symtable.autoCreateIdentifier("__date_" //$NON-NLS-1$
                ), __typedef_date);

        final SYM_type_primitive __typedef_long = new SYM_type_long("long"); //$NON-NLS-1$
        final String __variable_delta;
        symtable.register(__variable_delta = symtable.autoCreateIdentifier("__delta_" //$NON-NLS-1$
                ), __typedef_long);
        final SYM_type_class __typedef_hdate = new SYM_type_class("HydrologisDate"); //$NON-NLS-1$
        final String __variable_startup;
        symtable.register(__variable_startup = symtable.autoCreateIdentifier("__startup_" //$NON-NLS-1$
                ), __typedef_hdate);
        final String __variable_endingup;
        symtable.register(__variable_endingup = symtable.autoCreateIdentifier("__endingup_" //$NON-NLS-1$
                ), __typedef_hdate);

        AST<ASTs> retval = new AST_slist();
        retval.addChild(new AST_slist(new AST_expression(new AST_variable_definition(new AST_type(
                __typedef_dateformat.type()), new AST_identifier(__variable_dateformat),
                new AST_assign_statement(new AST_method_call(new AST_dot(new AST_identifier(
                        __typedef_dateformat.type()), new AST_identifier("getDateTimeInstance") //$NON-NLS-1$
                        ), new AST_elist(new AST_dot(
                                new AST_identifier(__typedef_dateformat.type()),
                                new AST_identifier("MEDIUM") //$NON-NLS-1$
                                ), new AST_dot(new AST_identifier(__typedef_dateformat.type()),
                                        new AST_identifier("MEDIUM") //$NON-NLS-1$
                                ), new AST_method_call(new AST_dot(new AST_identifier("Locale") //$NON-NLS-1$
                                        , new AST_identifier("getDefault") //$NON-NLS-1$
                                        ), new AST_elist())))))), new AST_expression(
                new AST_variable_definition(new AST_type(__typedef_date.type()),
                        new AST_identifier(__variable_date))), new AST_expression(
                new AST_assign_statement(new AST_identifier(__variable_date), new AST_method_call(
                        new AST_dot(new AST_identifier(__variable_dateformat), new AST_identifier(
                                "parse") //$NON-NLS-1$
                        ), new AST_elist(new AST_literal(projectSpace().getArgVerbValue(
                                Projectspace.ARG_VERB_TIME_ENDING_UP)))))), new AST_expression(
                new AST_variable_definition(new AST_type(__typedef_hdate.type()),
                        new AST_identifier(__variable_endingup), new AST_assign_statement(
                                new AST_ctor_call(new AST_identifier(__typedef_hdate.type()),
                                        new AST_elist())))), new AST_expression(
                new AST_method_call(new AST_dot(new AST_identifier(__variable_endingup),
                        new AST_identifier("setTime") //$NON-NLS-1$
                        ), new AST_elist(new AST_method_call(new AST_dot(new AST_identifier(
                                __variable_date), new AST_identifier("getTime") //$NON-NLS-1$
                                ), new AST_elist())))), new AST_expression(
                new AST_assign_statement(new AST_identifier(__variable_date), new AST_method_call(
                        new AST_dot(new AST_identifier(__variable_dateformat), new AST_identifier(
                                "parse") //$NON-NLS-1$
                        ), new AST_elist(new AST_literal(projectSpace().getArgVerbValue(
                                Projectspace.ARG_VERB_TIME_START_UP)))))), new AST_expression(
                new AST_variable_definition(new AST_type(__typedef_hdate.type()),
                        new AST_identifier(__variable_startup), new AST_assign_statement(
                                new AST_ctor_call(new AST_identifier(__typedef_hdate.type()),
                                        new AST_elist())))), new AST_expression(
                new AST_method_call(new AST_dot(new AST_identifier(__variable_startup),
                        new AST_identifier("setTime") //$NON-NLS-1$
                        ), new AST_elist(new AST_method_call(new AST_dot(new AST_identifier(
                                __variable_date), new AST_identifier("getTime") //$NON-NLS-1$
                                ), new AST_elist())))), new AST_expression(
                new AST_variable_definition(new AST_type(__typedef_long.type()),
                        new AST_identifier(__variable_delta), new AST_assign_statement(
                                new AST_number_integer(projectSpace().getArgVerbValue(
                                        Projectspace.ARG_VERB_TIME_DELTA))))),
        // the while condition if(true == enddate.after.startdate)
                new AST_while(

                new AST_condition(new AST_logical_equal(new AST_bool_true(), new AST_method_call(
                        new AST_dot(new AST_identifier(__variable_endingup), new AST_identifier(
                                "after") //$NON-NLS-1$
                        ), new AST_elist(new AST_identifier(__variable_startup)))))
                // AST_block creates the curled brackets {}
                        , new AST_block(new AST_slist(new AST_expression(new AST_method_call(
                                new AST_dot(new AST_identifier(varname), new AST_identifier(
                                        "getValues") //$NON-NLS-1$
                                ), new AST_elist(new AST_identifier(__variable_startup), param2))),
                                new AST_expression(new AST_method_call(new AST_dot(
                                        new AST_identifier(__variable_startup), new AST_identifier(
                                                "setTime") //$NON-NLS-1$
                                        ), new AST_elist(new AST_plus(new AST_method_call(
                                                new AST_dot(new AST_identifier(__variable_startup),
                                                        new AST_identifier("getTime") //$NON-NLS-1$
                                                ), new AST_elist()), new AST_identifier(
                                                __variable_delta))))))

                        ) // end of AST_block

                )));

        return retval;
    } // __time_loop

    @SuppressWarnings("unchecked")
    private AST<ASTs> __output_time_loop( Symtable<SYMs> symtable, String[] outputVarnames,
            AST<ASTs> param2 ) {

        String formatterVariable = symtable.autoCreateIdentifier("__dateFormatter_" //$NON-NLS-1$
                );
        String formatter = "SimpleDateFormat " + formatterVariable
                + " = new SimpleDateFormat(\"yyyy-MM-dd HH:mm\");";
        final SYM_type_class __typedef_dateformat = new SYM_type_class("DateFormat"); //$NON-NLS-1$
        final String __variable_dateformat;
        symtable.register(__variable_dateformat = symtable.autoCreateIdentifier("__dateformat_" //$NON-NLS-1$
                ), __typedef_dateformat);

        final SYM_type_class __typedef_date = new SYM_type_class("Date"); //$NON-NLS-1$
        final String __variable_date;
        symtable.register(__variable_date = symtable.autoCreateIdentifier("__date_" //$NON-NLS-1$
                ), __typedef_date);

        final SYM_type_primitive __typedef_long = new SYM_type_long("long"); //$NON-NLS-1$
        final String __variable_delta;
        symtable.register(__variable_delta = symtable.autoCreateIdentifier("__delta_" //$NON-NLS-1$
                ), __typedef_long);
        final SYM_type_class __typedef_hdate = new SYM_type_class("HydrologisDate"); //$NON-NLS-1$
        final String __variable_startup;
        symtable.register(__variable_startup = symtable.autoCreateIdentifier("__startup_" //$NON-NLS-1$
                ), __typedef_hdate);
        final String __variable_endingup;
        symtable.register(__variable_endingup = symtable.autoCreateIdentifier("__endingup_" //$NON-NLS-1$
                ), __typedef_hdate);

        List astList = new ArrayList();
        for( int i = 0; i < outputVarnames.length; i++ ) {
            astList.add(new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                    outputVarnames[i]), new AST_identifier("getValues") //$NON-NLS-1$
                    ), new AST_elist(new AST_identifier(__variable_startup), param2))));
        }
        AST[] outputAstsArray = new AST[astList.size()];
        for( int i = 0; i < outputAstsArray.length; i++ ) {
            outputAstsArray[i] = (AST) astList.get(i);
        }

        AST<ASTs> retval = new AST_slist();
        retval.addChild(new AST_slist(
                // the date formatter for the cyclic output
                new AST_expression(new AST_identifier(formatter)), new AST_expression(
                        new AST_variable_definition(new AST_type(__typedef_dateformat.type()),
                                new AST_identifier(__variable_dateformat),
                                new AST_assign_statement(new AST_method_call(new AST_dot(
                                        new AST_identifier(__typedef_dateformat.type()),
                                        new AST_identifier("getDateTimeInstance") //$NON-NLS-1$
                                        ), new AST_elist(new AST_dot(new AST_identifier(
                                                __typedef_dateformat.type()), new AST_identifier(
                                                "MEDIUM") //$NON-NLS-1$
                                                ), new AST_dot(new AST_identifier(
                                                        __typedef_dateformat.type()),
                                                        new AST_identifier("MEDIUM") //$NON-NLS-1$
                                                ), new AST_method_call(new AST_dot(
                                                        new AST_identifier("Locale") //$NON-NLS-1$
                                                        , new AST_identifier("getDefault") //$NON-NLS-1$
                                                        ), new AST_elist())))))),
                new AST_expression(new AST_variable_definition(new AST_type(__typedef_date.type()),
                        new AST_identifier(__variable_date))), new AST_expression(
                        new AST_assign_statement(new AST_identifier(__variable_date),
                                new AST_method_call(new AST_dot(new AST_identifier(
                                        __variable_dateformat), new AST_identifier("parse") //$NON-NLS-1$
                                        ), new AST_elist(new AST_literal(projectSpace()
                                                .getArgVerbValue(
                                                        Projectspace.ARG_VERB_TIME_ENDING_UP)))))),
                new AST_expression(new AST_variable_definition(
                        new AST_type(__typedef_hdate.type()), new AST_identifier(
                                __variable_endingup), new AST_assign_statement(new AST_ctor_call(
                                new AST_identifier(__typedef_hdate.type()), new AST_elist())))),
                new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                        __variable_endingup), new AST_identifier("setTime") //$NON-NLS-1$
                        ), new AST_elist(new AST_method_call(new AST_dot(new AST_identifier(
                                __variable_date), new AST_identifier("getTime") //$NON-NLS-1$
                                ), new AST_elist())))), new AST_expression(
                        new AST_assign_statement(new AST_identifier(__variable_date),
                                new AST_method_call(new AST_dot(new AST_identifier(
                                        __variable_dateformat), new AST_identifier("parse") //$NON-NLS-1$
                                        ), new AST_elist(new AST_literal(projectSpace()
                                                .getArgVerbValue(
                                                        Projectspace.ARG_VERB_TIME_START_UP)))))),
                new AST_expression(new AST_variable_definition(
                        new AST_type(__typedef_hdate.type()),
                        new AST_identifier(__variable_startup), new AST_assign_statement(
                                new AST_ctor_call(new AST_identifier(__typedef_hdate.type()),
                                        new AST_elist())))), new AST_expression(
                        new AST_method_call(new AST_dot(new AST_identifier(__variable_startup),
                                new AST_identifier("setTime") //$NON-NLS-1$
                                ), new AST_elist(new AST_method_call(new AST_dot(
                                        new AST_identifier(__variable_date), new AST_identifier(
                                                "getTime") //$NON-NLS-1$
                                        ), new AST_elist())))), new AST_expression(
                        new AST_variable_definition(new AST_type(__typedef_long.type()),
                                new AST_identifier(__variable_delta), new AST_assign_statement(
                                        new AST_number_integer(projectSpace().getArgVerbValue(
                                                Projectspace.ARG_VERB_TIME_DELTA))))),
                // the while condition if(true == enddate.after.startdate)
                new AST_while(

                new AST_condition(new AST_logical_equal(new AST_bool_true(), new AST_method_call(
                        new AST_dot(new AST_identifier(__variable_endingup), new AST_identifier(
                                "after") //$NON-NLS-1$
                        ), new AST_elist(new AST_identifier(__variable_startup)))))
                // AST_block creates the curled brackets {}
                        , new AST_block(
                        /*
                         * out.println();
                         * out.println(formatter.format(currentDate));
                         * out.println("----------------------------------------------");
                         */
                        new AST_slist(new AST_expression(new AST_identifier("out.println()")),
                                new AST_expression(new AST_identifier("out.println("
                                        + "\"Current time step: \" + " + formatterVariable
                                        + ".format(" + __variable_startup + "))")),
                                new AST_expression(new AST_identifier("out.println("
                                        + "\"----------------------------------------------\")"))),
                        /*
                         * the list of output components that need to be triggered
                         */
                        new AST_slist(outputAstsArray),

                        /*
                         * the go-one-timestep-forward 
                         */
                        new AST_slist(

                        // new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                                //                                varname), new AST_identifier("getValues") //$NON-NLS-1$
                                // ), new AST_elist(
                                // new AST_identifier(__variable_startup), param2)))

                                new AST_expression(new AST_method_call(new AST_dot(
                                        new AST_identifier(__variable_startup), new AST_identifier(
                                                "setTime") //$NON-NLS-1$
                                        ), new AST_elist(new AST_plus(new AST_method_call(
                                                new AST_dot(new AST_identifier(__variable_startup),
                                                        new AST_identifier("getTime") //$NON-NLS-1$
                                                ), new AST_elist()), new AST_identifier(
                                                __variable_delta))))))

                        ) // end of AST_block

                )));

        return retval;
    } // __time_loop
    /**
     * <p>
     * Translates the usage into the tree-like intermediate representation of the target code - .
     * </p>
     * 
     * @param syntaxTree - the root of the syntax tree.
     * @param operator - a "input", "output" or a "java_model" operator.
     */
    @SuppressWarnings("unchecked")
    private void __usage( AST_root syntaxTree, APT<APTs> operator ) {

        final APT_variable_definition __variable_def;
        switch( operator.identifier() ) {
        case APT_INPUT:
            __variable_def = ((APT_input) operator).linkable_variable_def();
            break;

        case APT_JAVA_MODEL:
            __variable_def = ((APT_java_model) operator).linkable_variable_def();
            break;

        case APT_OUTPUT:
            __variable_def = ((APT_output) operator).linkable_variable_def();
            break;

        default:
            __variable_def = null;
        }

        if (null != __variable_def) {

            syntaxTree.__ctor_call_seg().addChild(
                    new AST_slist(new AST_expression(new AST_variable_definition(
                            new AST_identifier("def "), new AST_identifier(__variable_def
                                    .variable_name()), new AST_assign_statement(
                                    new AST_ctor_call(
                                            new AST_identifier(__variable_def.type_name()),
                                            new AST_elist()))))));
            syntaxTree
                    .__gtor_call_seg()
                    .addChild(
                            new AST_slist(
                                    new AST_expression(new AST_variable_definition(new AST_type(
                                            "String") //$NON-NLS-1$
                                            , new AST_identifier("__usage") //$NON-NLS-1$
                                            , new AST_assign_statement(new AST_method_call(
                                                    new AST_dot(new AST_identifier(__variable_def
                                                            .variable_name()), new AST_identifier(
                                                            "getModelDescription") //$NON-NLS-1$
                                                    ), new AST_elist())))),
                                    new AST_if(
                                            new AST_condition(new AST_logical_equal(new AST_null(),
                                                    new AST_identifier("__usage") //$NON-NLS-1$
                                                    )),
                                            new AST_then(
                                                    new AST_slist(
                                                            new AST_expression(
                                                                    new AST_method_call(
                                                                            new AST_identifier(
                                                                                    "println") //$NON-NLS-1$
                                                                            ,
                                                                            new AST_elist(
                                                                                    new AST_literal(
                                                                                            "Usage hints of \"" + __variable_def.type_name() + "\" not available.") //$NON-NLS-2$ //$NON-NLS-1$
                                                                            ))))), new AST_else(
                                                    new AST_slist(new AST_expression(
                                                            new AST_method_call(new AST_identifier(
                                                                    "println") //$NON-NLS-1$
                                                                    , new AST_elist(
                                                                            new AST_identifier(
                                                                                    "__usage") //$NON-NLS-1$
                                                                    ))))))));
        }
    } // __usage

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor#translate()
     */
    protected void translate( Symtable<SYMs> symtable, APT<APTs> parseTree, AST<ASTs> syntaxTree ) {

        try {

            AST_root __astroot = (AST_root) syntaxTree;
            switch( parseTree.identifier() ) {
            case APT_JAVA_MODEL:
                if (true == ((APT_java_model) parseTree).m_usage)
                    __usage(__astroot, (APT_java_model) parseTree);
                else if (false == ((APT_root) parseTree.root()).m_usage)
                    __java_model(symtable, __astroot, (APT_java_model) parseTree);
                break;

            case APT_INPUT:
                APT_input __input = (APT_input) parseTree;
                if (true == __input.m_usage)
                    __usage(__astroot, __input);
                else if (false == ((APT_root) __input.root()).m_usage)
                    __model_input(symtable, __astroot, __input);
                if (null != __input.nested_statement()
                        && null != __input.nested_statement().__model())
                    translate(symtable, __input.nested_statement(), __astroot);
                break;

            case APT_IO_DEFINITION:
                final APT_io_definition __io_def;
                __io_def = (APT_io_definition) parseTree;
                for( int i = 0; i < __io_def.size(); ++i )
                    translate(symtable, __io_def.getChild(i), __astroot);
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
                    translate(symtable, __aptroot.getChild(i), syntaxTree);
                if (0 < __aptroot.size() && false == __aptroot.m_usage)
                    __initialize(symtable, __astroot, __aptroot);

                closeBlock(symtable, __astroot);
                break;

            case APT_OUTPUT:
                APT_output __output = (APT_output) parseTree;
                if (true == __output.m_usage)
                    __usage(__astroot, __output);
                else if (false == ((APT_root) __output.root()).m_usage)
                    __model_output(symtable, __astroot, __output);
                if (null != __output.nested_statement()
                        && null != __output.nested_statement().__model()) {
                    translate(symtable, __output.nested_statement(), __astroot);
                }

                switch( __output.token_value().identifier() ) {
                case CHARACTER_ASTERISK:
                    outputAsteriscTrees.add(__output.linkable_variable_def().variable_name());
                    outputAsteriscParserTrees.add(__output);
                    break;
                default:
                    outputTrees.add(__output.linkable_variable_def().variable_name());
                    outputParserTrees.add(__output);
                }
                break;
            }
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            e.printStackTrace();
        }
    } // translate

    /**
     * Method that closes the openmi chain with the while loop.
     * 
     * <p>The while loop is created with all the output 
     * {@link LinkableComponent}s inside the loop. A check 
     * on time dependency is also done.
     * </p>
     * 
     * @param symtable the {@link Symtable symbol table}.
     * @param root the {@link AST_root root abstract parser tree}.
     */
    @SuppressWarnings("unchecked")
    private void closeBlock( Symtable<SYMs> symtable, AST_root root ) {

        String szEndingup = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_ENDING_UP);
        String szStartup = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_START_UP);
        String szDelta = projectSpace().getArgVerbValue(Projectspace.ARG_VERB_TIME_DELTA);

        /*
         * with right we assume this is the closing block.
         * This means that we can take the first output 
         * item in the case of no time loop. In that case
         * the model returns the object of that one. Anyway
         * the model can return only one result from the 
         * openmi chain. In the case of time chain, this 
         * doesn't make any sense anyway.
         */
        APT_output operator = null;
        if (outputParserTrees.size() != 0) {
            operator = outputParserTrees.get(0);
        } else if (outputAsteriscParserTrees.size() != 0) {
            operator = outputAsteriscParserTrees.get(0);
        } else {
            return;
        }

        // Gathering required informations
        final APT_variable_definition __linkable_variable_def;
        __linkable_variable_def = operator.linkable_variable_def();
        final APT_variable_definition __link_variable_def;
        __link_variable_def = operator.link_variable_def();
        final APT_statement __modeldef;
        __modeldef = (APT_statement) operator.lookup(APTs.APT_STATEMENT);
        final APT_java_model __model;
        __model = (APT_java_model) __modeldef.__model();
        final APT_variable_definition __model_variable_def;
        __model_variable_def = __model.linkable_variable_def();

        AST<ASTs> astetor;
        switch( operator.token_value().identifier() ) {
        case CHARACTER_ASTERISK:
            AST<ASTs> __lvalue = null;
            if (null != __modeldef.__external_variable())
                __lvalue = new AST_identifier(__modeldef.__external_variable().variable_name());
            AST<ASTs> __rvalue = new AST_expression(new AST_method_call(new AST_dot(
                    new AST_identifier(__model_variable_def.variable_name()), new AST_identifier(
                            "getValues") //$NON-NLS-1$
                    ), new AST_elist(new AST_null(), new AST_method_call(new AST_dot(
                            new AST_identifier(__link_variable_def.variable_name()),
                            new AST_identifier("getID") //$NON-NLS-1$
                            ), new AST_elist()))));
            if (null == __lvalue) {

                astetor = __rvalue;
            } else {

                astetor = new AST_assign_statement(__lvalue, __rvalue);
            }
            break;

        case LITERAL:
        case VARIABLE:
        default:
            astetor = new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                    __linkable_variable_def.variable_name()), new AST_identifier("getValues") //$NON-NLS-1$
                    ), new AST_elist(new AST_null(), new AST_null())));
        }

        if (null == szDelta || null == szStartup || null == szEndingup) {

            for( String varName : outputTrees ) {
                astetor = new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(
                        varName), new AST_identifier("getValues") //$NON-NLS-1$
                        ), new AST_elist(new AST_null(), new AST_null())));
                root.__gtor_call_seg().addChild(
                        (true == operator.token_flag().__asterisk()) ? 0 : root.__gtor_call_seg()
                                .size(), astetor);
            }
            /*
             *  if there is an asterisc, assign astetor 
             *  that has the proper left value.
             */
            if (outputAsteriscTrees.size() > 0) {
                root.__gtor_call_seg().addChild(0, astetor);
            }
        } else {
            root.__gtor_call_seg().addChild(
                    root.__gtor_call_seg().size(),
                    __output_time_loop(symtable, (String[]) outputTrees
                            .toArray(new String[outputTrees.size()]), new AST_null()));
        }

        for( String varName : outputTrees ) {
            root.__dtor_call_seg().addChild(
                    0,
                    new AST_expression(new AST_method_call(new AST_dot(new AST_identifier(varName),
                            new AST_identifier("finish") //$NON-NLS-1$
                            ), new AST_elist())));
        }

    }

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor#intermediate()
     */
    public final APT<APTs> intermediate( Symtable<SYMs> symtable, Reader sourceCode, int line ) {

        APT<APTs> retval = null;
        try {

            Lexer<TOKs> scanner = new JavaMLScanner(projectSpace(), sourceCode, line);
            Parser<APTs, TOKs, SYMs> parser = new JavaMLParser(projectSpace(), scanner);
            retval = parser.parse(symtable, new APT_root());
        } catch (Exception e) {

            final Throwable theCause = e.getCause();
            if (false == theCause instanceof CompilerWarning
                    && false == theCause instanceof CompilerError) {

                if (true == Projectspace.isErrorEnabled())
                    projectSpace().err.println(e);

                e.printStackTrace();
            }
        }

        return retval;
    } // intermediate

} // JavaML4j
