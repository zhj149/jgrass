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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Vector;

import eu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor;
import eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor;
import eu.hydrologis.jgrass.console.core.internal.nodes.APT;
import eu.hydrologis.jgrass.console.core.internal.nodes.AST;
import eu.hydrologis.jgrass.console.core.internal.nodes.Symtable;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace.JavaExchangeWordDescriptor;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace.JavaModelWordDescriptor;
import eu.hydrologis.jgrass.console.core.prefs.Projectspace.NativeModelWordDescriptor;
import eu.hydrologis.jgrass.console.core.runtime.analysis.APTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.ASTs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.SYMs;
import eu.hydrologis.jgrass.console.core.runtime.analysis.TOKs;
import eu.hydrologis.jgrass.console.core.runtime.nodes.AST_root;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_constant_value;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_class;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_java_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_native_model;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_primitive;
import eu.hydrologis.jgrass.console.core.runtime.nodes.SYM_type_reference;

/**
 * <p>
 * The class <code>AbstractML4j</code> provides default implementation for the
 * <code>Preprocessor</code> interface and defines standard behavior for the methods:
 * <code>projectSymbols</code>, <code>generate</code> and <code>translate</code>. The developer
 * subclasses this abstract class, defines the method <code>intermediate</code> and the protected
 * abstract method <code>translate</code> provided by this abstract class.
 * </p>
 * <p>
 * However, the method <code>translate(Symtable&lt;SYMs&gt;,APT&lt;APTs&gt;,AST&lt;ASTs&gt;)</code>
 * is called by the default implementation of the
 * <code>translate(Symtable&lt;SYMs&gt;,APT&lt;APTs&gt;)</code> method to translate the tree-like
 * intermediate representation of the source code into the tree-like intermediate representation of
 * the target code.
 * </p>
 * 
 * @see eu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor
 * @see eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor
 * @since 2.0.0
 * @version 1.0.0.1
 * @author Andreas Hamm (aha), ahamm@andreas-hamm.de
 */
public abstract class AbstractML4j extends AbstractPreprocessor<ASTs, APTs, TOKs, SYMs>
        implements
            Preprocessor<ASTs, APTs, TOKs, SYMs> {

    private static int labelIndex = 0;

    // Construction
    /**
     * <p>
     * The copy constructor <code>AbstractML4j</code> defines this preprocessor object using the
     * specified project-space.
     * </p>
     * 
     * @param projectSpace - the project-space.
     */
    public AbstractML4j( Projectspace projectSpace ) {

        super(projectSpace);
    } // AbstractML4j

    // Operations
    /**
     * <p>
     * This is the helper of the method <code>translate(Symtable&lt;SYMs&gt;,APTs&lt;S&gt;)</code>;
     * the developer realizes the <i>intermediate code generation</i> - the translation from the
     * tree-like intermediate representation of the source code into the equivalent tree-like
     * intermediate representation of the target code - in this method.
     * </p>
     * <p>
     * Simply stated, the developer uses the <i>abstract symbol</i> to evaluate the source
     * programming construct specified by the parse tree - either a operator or a operand -
     * currently being processed and then generates its equivalent tree-like intermediate
     * representation of the target code; the tree-like intermediate representation of the target
     * code then is added as operand to the tree-like intermediate representation of the target
     * code. The developer then performs a recursive call and passes the operands - one at a time -
     * of the specified parse tree.
     * </p>
     * 
     * @param symtable - the symtable.
     * @param parseTree - the source; a tree-like intermediate representation of the source code.
     * @param syntaxTree - the target; a tree-like intermediate representation of the target code.
     */
    protected abstract void translate( Symtable<SYMs> symtable, APT<APTs> parseTree,
            AST<ASTs> syntaxTree );

    /*
     * (non-Javadoc)
     * @seeeu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor#projectSymbols(
     * Projectspace)
     */
    protected Symtable<SYMs> projectSymbols( Projectspace projectSpace ) {

        Symtable<SYMs> retval = new Symtable<SYMs>();

        // Imported classes...
        Vector<String> importedClasses = projectSpace.importedClasses();
        for( int i = 0; i < importedClasses.size(); ++i ) {

            SYM_type_class symbole = new SYM_type_class(importedClasses.elementAt(i));
            retval.register(symbole.type(), symbole);
        }

        // Imported native commands...
        Vector<NativeModelWordDescriptor> importedNativeModelClasses = projectSpace
                .describeNativeModelWords();
        for( int i = 0; i < importedNativeModelClasses.size(); ++i ) {

            NativeModelWordDescriptor model = importedNativeModelClasses.elementAt(i);
            SYM_type_native_model symbole = new SYM_type_native_model(model.executable());
            retval.register(SYM_type_primitive.qualifier(model.identifier()), symbole);
        }

        // Imported i/o exchange components...
        Vector<JavaExchangeWordDescriptor> importedExchangeClasses = projectSpace
                .describeJavaExchangeWords();
        for( int i = 0; i < importedExchangeClasses.size(); ++i ) {

            JavaExchangeWordDescriptor exchange = importedExchangeClasses.elementAt(i);
            SYM_type_class symbole = new SYM_type_class(exchange.fullQualifiedName());
            retval.register(SYM_type_reference.qualifier(exchange.identifier()), symbole);
        }

        // Imported java models...
        Vector<JavaModelWordDescriptor> importedJavaModelClasses = projectSpace
                .describeJavaModelWords();
        for( int i = 0; i < importedJavaModelClasses.size(); ++i ) {

            JavaModelWordDescriptor model = importedJavaModelClasses.elementAt(i);
            SYM_type_java_model symbole = new SYM_type_java_model(model.fullQualifiedName(), model
                    .defaultKey(), model.quantities());
            retval.register(SYM_type_reference.qualifier(model.identifier()), symbole);
        }

        retval.register(Projectspace.ARG_VERB_GRASSDB, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_GRASSDB)));
        retval.register(Projectspace.ARG_VERB_LOCATION, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_LOCATION)));
        retval.register(Projectspace.ARG_VERB_MAPSET, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_MAPSET)));

        // START ADDITION Andrea Antonello - andrea.antonello@gmail.com
        retval.register(Projectspace.ARG_VERB_TIME_START_UP, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_TIME_START_UP)));
        retval.register(Projectspace.ARG_VERB_TIME_ENDING_UP, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_TIME_ENDING_UP)));
        retval.register(Projectspace.ARG_VERB_TIME_DELTA, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_TIME_DELTA)));
        retval.register(Projectspace.ARG_VERB_REMOTEDB, new SYM_constant_value(projectSpace
                .getArgVerbValue(Projectspace.ARG_VERB_REMOTEDB)));
        // STOP ADDITION
        return retval;
    } // projectSymbols

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.Preprocessor#generate()
     */
    public void generate( int indentCount, AST<ASTs> op, Writer targetCode ) {

        try {

            final String __indentString = indentions(indentCount);
            switch( op.identifier() ) {
            case AST_ASSIGN_STATEMENT:
                if (1 == op.size()) {

                    targetCode.append(" " + op.expression() + " "); //$NON-NLS-2$ //$NON-NLS-1$
                    for( int i = 0; i < op.size(); ++i )
                        generate(indentCount, op.getChild(i), targetCode);
                } else {

                    for( int i = 0; i < op.size(); ++i ) {

                        generate(indentCount, op.getChild(i), targetCode);
                        if (0 == i)
                            targetCode.append(" " + op.expression() + " "); //$NON-NLS-2$ //$NON-NLS-1$
                    }
                }
                break;

            case AST_ARRAY:
                /*
                 * this should be a set of strings (also concatenated) divided by commas
                 */

                targetCode.append("new "); //$NON-NLS-1$

                AST<ASTs> typeChild = null;
                AST<ASTs> commaChild = null;
                AST<ASTs> variableChild = null;
                for( int i = 0; i < op.size(); i++ ) {
                    AST<ASTs> child = op.getChild(i);
                    if (child.identifier().annotation().equals(ASTs.AST_TYPE.annotation())) {
                        typeChild = child;
                    }
                    if (child.identifier().annotation().equals(ASTs.AST_COMMA.annotation())) {
                        commaChild = child;
                    }
                    if (child.identifier().annotation().equals(ASTs.AST_IDENTIFIER.annotation())) {
                        variableChild = child;
                    }
                }
                if (typeChild != null && commaChild != null && variableChild != null) {
                    int nums = commaChild.size();
                    String arrayString = typeChild.expression();
                    arrayString = arrayString.substring(0, arrayString.length() - 1) + nums + "]";
                    targetCode.append(arrayString).append(";\n"); //$NON-NLS-1$

                    // now the arrays
                    String varName = variableChild.expression();
                    for( int i = 0; i < nums; i++ ) {
                        for( int j = 0; j < indentCount; j++ ) {
                            targetCode.append(__indentString);
                        }
                        targetCode.append(varName).append("[").append("" + i).append("] = "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        AST<ASTs> child = commaChild.getChild(i);
                        generate(indentCount + 1, child, targetCode);
                        targetCode.append(";\n"); //$NON-NLS-1$
                    }
                }

                break;
            case AST_BLOCK:
                targetCode.append("{\n"); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount + 1, op.getChild(i), targetCode);
                }
                targetCode.append(__indentString + "}"); //$NON-NLS-1$
                break;

            case AST_BOOL_FALSE:
            case AST_BOOL_TRUE:
                targetCode.append(op.expression());
                break;

            case AST_CATCH:
                targetCode.append(op.expression());
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    targetCode.append(" "); //$NON-NLS-1$
                }
                break;

            case AST_COMMA:
                for( int i = 0; i < op.size(); ++i ) {

                    targetCode.append(__indentString);
                    if (0 < i)
                        targetCode.append(op.expression() + " "); //$NON-NLS-1$
                    generate(indentCount, op.getChild(i), targetCode);
                    targetCode.append("\n"); //$NON-NLS-1$
                }
                break;

            case AST_CONDITION:
                targetCode.append("( "); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                targetCode.append(" ) "); //$NON-NLS-1$
                break;

            case AST_CONDITIONAL_AND:
            case AST_CONDITIONAL_OR:
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    if (i < op.size() - 1)
                        targetCode.append(" " //$NON-NLS-1$
                                + op.expression() + " " //$NON-NLS-1$
                        );
                }
                break;

            case AST_CTOR_CALL:
                targetCode.append("new "); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_DOT:
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    if (i < op.size() - 1)
                        targetCode.append(op.expression());
                }
                break;

            case AST_ELIST:
                if (0 >= op.size()) {

                    targetCode.append("()"); //$NON-NLS-1$
                } else {

                    targetCode.append("("); //$NON-NLS-1$
                    final ASTs identifier = op.getChild(0).identifier();
                    if (ASTs.AST_COMMA != identifier)
                        targetCode.append(" "); //$NON-NLS-1$
                    else
                        targetCode.append("\n"); //$NON-NLS-1$
                    for( int i = 0; i < op.size(); ++i ) {

                        generate(indentCount + 2, op.getChild(i), targetCode);
                        if (i < op.size() - 1)
                            targetCode.append(", "); //$NON-NLS-1$
                    }
                    if (ASTs.AST_COMMA != identifier)
                        targetCode.append(" "); //$NON-NLS-1$
                    else
                        targetCode.append("\n\t" + __indentString); //$NON-NLS-1$
                    targetCode.append(")"); //$NON-NLS-1$
                }
                break;

            case AST_ELSE:
                targetCode.append("\n" + __indentString + "else {\n"); //$NON-NLS-2$ //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount + 1, op.getChild(i), targetCode);
                targetCode.append(__indentString + "}"); //$NON-NLS-1$
                break;

            case AST_EXPRESSION:
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    targetCode.append(";"); //$NON-NLS-1$
                }
                break;

            case AST_FINALLY:
                targetCode.append(op.expression() + " "); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_IDENTIFIER:
                targetCode.append(op.expression());
                break;

            case AST_IF:
                targetCode.append(op.expression());
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_LITERAL:
                targetCode.append("\"" + op.expression() + "\""); //$NON-NLS-2$ //$NON-NLS-1$
                break;

            case AST_LOGICAL_EQUAL:
            case AST_LOGICAL_UNEQUAL:
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    if (i < op.size() - 1)
                        targetCode.append(" " + op.expression() + " " //$NON-NLS-2$ //$NON-NLS-1$
                        );
                }
                break;

            case AST_METHOD_CALL:
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_NULL:
                targetCode.append(op.expression());
                break;

            case AST_NUMBER_INTEGER:
                targetCode.append(op.expression());
                break;

            case AST_PLUS:
                for( int i = 0; i < op.size(); ++i ) {

                    generate(indentCount, op.getChild(i), targetCode);
                    if (i < op.size() - 1)
                        targetCode.append(" " + op.expression() + " "); //$NON-NLS-2$ //$NON-NLS-1$
                }
                break;

            case AST_ROOT:
                if (0 < op.size()) {

                    targetCode
                            .append("ROOT").append(String.valueOf(labelIndex++)).append(":").append("{\n"); //$NON-NLS-1$
                    for( int i = 0; i < op.size(); ++i ) {

                        AST<ASTs> operand = op.getChild(i);
                        if (0 < operand.size())
                            generate(indentCount + 1, operand, targetCode);
                    }
                    targetCode.append(__indentString + "}\n"); //$NON-NLS-1$
                }
                break;

            case AST_SLIST:
                if (0 < op.size()) {

                    for( int i = 0; i < op.size(); ++i ) {

                        AST<ASTs> operand = op.getChild(i);
                        if (ASTs.AST_SLIST != operand.identifier())
                            targetCode.append(__indentString);
                        generate(indentCount, operand, targetCode);
                        if (ASTs.AST_SLIST != operand.identifier())
                            targetCode.append("\n"); //$NON-NLS-1$
                    }
                }
                break;

            case AST_THEN:
                targetCode.append("{\n"); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount + 1, op.getChild(i), targetCode);
                targetCode.append(__indentString + "}"); //$NON-NLS-1$
                break;

            case AST_THROW_CALL:
                targetCode.append("throw "); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_TRY:
                targetCode.append(op.expression() + " "); //$NON-NLS-1$
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_TYPE:
                targetCode.append(op.expression() + " "); //$NON-NLS-1$
                break;

            case AST_VARIABLE_DEFINITION:
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            case AST_WHILE:
                targetCode.append(op.expression());
                for( int i = 0; i < op.size(); ++i )
                    generate(indentCount, op.getChild(i), targetCode);
                break;

            default:
                throw new IOException(op.identifier().name());
            }
        } catch (IOException e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            e.printStackTrace();
        }
    } // generate

    /*
     * (non-Javadoc)
     * @see eu.hydrologis.jgrass.console.core.internal.compiler.AbstractPreprocessor#translate()
     */
    public final AST<ASTs> translate( Symtable<SYMs> symtable, APT<APTs> parseTree ) {

        AST<ASTs> retval = new AST_root();
        try {

            if (null != parseTree)
                translate(symtable, parseTree, retval);
        } catch (Exception e) {

            if (true == Projectspace.isErrorEnabled())
                projectSpace().err.println(e);

            e.printStackTrace();
        }

        return retval;
    } // translate

} // AbstractML4j
