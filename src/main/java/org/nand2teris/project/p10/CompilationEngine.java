package org.nand2teris.project.p10;

/**
 * Generate compiler output
 */
public class CompilationEngine {
    /**
     * Create a new CompilationEngine with given input and output.
     * The next routine call must be compileClass.
     * @param inputFilePath
     * @param outputFilePath
     */
    public CompilationEngine(String inputFilePath, String outputFilePath){

    }

    /**
     * Compile a complete class
     */
    public void compileClass(){

    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     */
    public void compileClassVarDec(){

    }

    /**
     * Compiles a complete method, function or constructor
     */
    public void compileSubroutineDec(){

    }

    /**
     * Compiles a (possibly empty) parameter list. Does not handle enclosing "()".
     */
    public void compileParameterList(){

    }

    /**
     * Compiles a subroutine's body.
     */
    public void compileSubroutineBody(){

    }

    /**
     * Compiles a var declaration.
     */
    public void compileVarDec(){

    }

    /**
     * Compiles a sequence of statements. Does not handle enclosing "()".
     */
    public void compileStatements(){

    }

    /**
     * Compiles a let statement
     */
    public void compileLet(){

    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     */
    public void compileIf(){

    }

    /**
     * Compiles a while statement.
     */
    public void compileWhile(){}

    /**
     * Compiles a do statement.
     */
    public void compileDo(){}

    /**
     * Compiles a return statement.
     */
    public void compileReturn(){}

    /**
     * Compiles an expression.
     */
    public void compileExpression(){}

    /**
     * Compile a term. If the current token is an identifier, the routine must distinguish between a variable,
     * an array entry, or a subroutine call. A single look-ahead token which may be one of "[", "(", or ".", suffices
     * to distinguish between the possibilities. Any other token is not part of the term and should not be advanced over.
     */
    public void compileTerm(){}

    /**
     * Compiles a (possibly empty) comma separated list of expressions.
     */
    public void compileExpressionList(){}
}
