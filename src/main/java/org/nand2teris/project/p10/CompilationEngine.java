package org.nand2teris.project.p10;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.swing.plaf.IconUIResource;
import java.io.*;
import java.util.stream.Stream;

/**
 * Generate compiler output
 */
public class CompilationEngine {
    private JackTokenizer tokenizer;
    private PrintWriter writer;

    /**
     * Create a new CompilationEngine with given input and output.
     * The next routine call must be compileClass.
     *
     * @param inputFilePath
     * @param outputFilePath
     */
    public CompilationEngine(String inputFilePath, String outputFilePath) throws Exception {
        tokenizer = new JackTokenizer(inputFilePath);
        writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFilePath)));
    }

    /**
     * Compile a complete class
     * grammar --> 'class' className '{' classVarDec* subroutineDec* '}'
     */
    public void compileClass() throws Exception {
        writer.println("<class>");
        if (tokenizer.hasMoreTokens()) {
            //handle 'class'
            tokenizer.advance();
            if (!"class".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
            writer.println("<keyword> class </keyword>");

            //handle className
            tokenizer.advance();
            if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
            printTerminalElement();

            //handle '{'
            tokenizer.advance();
            if ('{' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();

            //handle classVarDec*
            tokenizer.advance();
            while ("static".equals(tokenizer.stringVal()) || "field".equals(tokenizer.stringVal())) {
                compileClassVarDec();
                tokenizer.advance();
            }

            //handle subroutineDec*
            while ("constructor".equals(tokenizer.stringVal()) || "function".equals(tokenizer.stringVal()) || "method".equals(tokenizer.stringVal())) {
                compileSubroutineDec();
                tokenizer.advance();
            }

            //handle '}'
            if ('}' != tokenizer.symbol())
                throw new InvalidSyntaxException();
            printTerminalElement();
        }
        writer.println("</class>");
    }

    /**
     * Compiles a static variable declaration, or a field declaration.
     * <p>
     * this method uses current token
     * grammar --> ('static' | 'field') type varName (',' varName)* ';'
     */
    public void compileClassVarDec() throws Exception {
        writer.println("<classVarDec>");
        //handle ('static' | 'field')
        if (!"static".equals(tokenizer.stringVal()) && !"field".equals(tokenizer.stringVal()))
            throw new InvalidSyntaxException();
        printTerminalElement();

        //handle type
        tokenizer.advance();
        compileTypeDeclaration();

        //handle '}'
        if (';' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</classVarDec>");
    }

    /**
     * this method uses current token
     * this method advances next token at the end
     * grammar --> type varName (',' varName)*
     *
     * @throws InvalidSyntaxException
     * @throws IOException
     */
    private void compileTypeDeclaration() throws InvalidSyntaxException, IOException {
        if (!isType()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle varName
        tokenizer.advance();
        if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle (',' varName)*
        tokenizer.advance();
        while (',' == tokenizer.symbol()) {
            printTerminalElement();
            tokenizer.advance();
            if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
        }
    }

    /**
     * Compiles a complete method, function or constructor.
     * <p>
     * this method uses current token
     * ('constructor'|'function'|'method') (void|type) subroutineName '('parameterList')' subroutineBody
     */
    public void compileSubroutineDec() throws InvalidSyntaxException, IOException {
        writer.println("<subroutineDec>");

        //handle ('constructor'|'function'|'method')
        if (!"constructor".equals(tokenizer.stringVal()) && !"function".equals(tokenizer.stringVal()) && !"method".equals(tokenizer.stringVal()))
            throw new InvalidSyntaxException();
        printTerminalElement();

        //handle (void|type)
        tokenizer.advance();
        if (!"void".equals(tokenizer.stringVal()) && !isType()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle subroutineName
        tokenizer.advance();
        if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle '('
        tokenizer.advance();
        if ('(' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();


        //handle parameterList
        tokenizer.advance();
        if (')' != tokenizer.symbol())
            compileParameterList();

        //handle ')'
        if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle subroutineBody
        compileSubroutineBody();

        writer.println("</subroutineDec>");
    }

    /**
     * Compiles a (possibly empty) parameter list. Does not handle enclosing "()".
     * <p>
     * this method uses current token
     * this method advances next token at the end
     * grammar --> ((type varName) (','type varName)*)?
     */
    public void compileParameterList() throws InvalidSyntaxException, IOException {
        writer.println("<parameterList>");

        //handle (type varName)
        if (!isType()) throw new InvalidSyntaxException();
        printTerminalElement();
        tokenizer.advance();
        if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle (','type varName)*
        tokenizer.advance();
        while (',' == tokenizer.symbol()) {
            printTerminalElement();

            tokenizer.advance();
            if (!isType()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
            if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
            printTerminalElement();

            tokenizer.advance(); //read next
        }

        writer.println("</parameterList>");
    }

    /**
     * Compiles a subroutine's body.
     * grammar --> '{' varDec* statements'}'
     */
    public void compileSubroutineBody() throws IOException, InvalidSyntaxException {
        writer.println("<subroutineBody>");

        //handle '{'
        tokenizer.advance();
        if ('{' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle varDec*
        tokenizer.advance();
        while ("var".equals(tokenizer.stringVal())) {
            compileVarDec();
            tokenizer.advance();
        }

        //handle statements
        compileStatements();

        //handle '}'
        if ('}' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</subroutineBody>");
    }

    /**
     * Compiles a var declaration.
     * <p>
     * this method uses current token
     * grammar --> 'var' type varName (','varName)*';'
     */
    public void compileVarDec() throws InvalidSyntaxException, IOException {
        writer.println("<varDec>");

        //handle 'var'
        if (!"var".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle type varName (','varName)*
        tokenizer.advance();
        compileTypeDeclaration();

        //handle ';'
        if (';' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</varDec>");
    }

    /**
     * Compiles a sequence of statements. Does not handle enclosing "()".
     * this method uses current token
     * this method advances next token at the end
     * grammar --> statement*
     * statement --> letStatement|ifStatement|whileStatement|doStatement|returnStatement
     */
    public void compileStatements() throws IOException, InvalidSyntaxException {
        writer.println("<statements>");


        while ("let".equals(tokenizer.stringVal()) || "if".equals(tokenizer.stringVal()) || "while".equals(tokenizer.stringVal())
                || "do".equals(tokenizer.stringVal()) || "return".equals(tokenizer.stringVal())) {
            switch (tokenizer.stringVal()) {
                case "let":
                    compileLet();
                    tokenizer.advance();
                    break;
                case "do":
                    compileDo();
                    tokenizer.advance();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    tokenizer.advance();
                    break;
                case "return":
                    compileReturn();
                    tokenizer.advance();
                    break;
            }
        }

        writer.println("</statements>");
    }

    /**
     * Compiles a let statement
     * <p>
     * this method uses current token
     * grammar --> 'let' varName('['expression']')?'='expression';'
     */
    public void compileLet() throws IOException, InvalidSyntaxException {
        writer.println("<letStatement>");

        //handle 'let'
        if (!"let".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle varName
        tokenizer.advance();
        if (!TokenType.IDENTIFIER.equals(tokenizer.tokenType())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle ('['expression']')?
        tokenizer.advance();

        if ('[' == tokenizer.symbol()) {
            //handle '['expression']'
            printTerminalElement();
            compileExpression();
            if (']' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
        }

        if ('=' != tokenizer.symbol())
            throw new InvalidSyntaxException();
        printTerminalElement();

        compileExpression();

        if (';' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</letStatement>");
    }

    /**
     * Compiles an if statement, possibly with a trailing else clause.
     * <p>
     * this method uses current token
     * this method advances next token at the end
     * grammar --> 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?
     */
    public void compileIf() throws InvalidSyntaxException, IOException {
        writer.println("<ifStatement>");

        //handle 'if'
        if (!"if".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle '('
        tokenizer.advance();
        if ('(' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle expression
        compileExpression();

        //handle ')'
        if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle '{'
        tokenizer.advance();
        if ('{' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle statements
        tokenizer.advance();    //this is needed because compileStatements() uses current token
        compileStatements();

        //token.advance() is not needed since compileStatements() advances next token
        if ('}' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        tokenizer.advance();
        if ("else".equals(tokenizer.stringVal())) {
            //handle 'else' '{' statements '}'
            printTerminalElement();
            tokenizer.advance();
            if ('{' != tokenizer.symbol())
                throw new InvalidSyntaxException();
            printTerminalElement();

            //handle statements
            tokenizer.advance();    //this is needed because compileStatements() uses current token
            compileStatements();

            //token.advance() is not needed since compileStatements() advances next token
            if ('}' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
        }

        writer.println("</ifStatement>");
    }

    /**
     * Compiles a while statement.
     * <p>
     * this method uses current token
     * grammar --> 'while' '(' expression ')' '{' statements '}'
     */
    public void compileWhile() throws InvalidSyntaxException, IOException {
        writer.println("<whileStatement>");

        //handle 'while'
        if (!"while".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle '('
        tokenizer.advance();
        if ('(' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle expression
        compileExpression();

        //handle ')'
        if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle '{'
        tokenizer.advance();
        if ('{' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle statements
        tokenizer.advance();
        compileStatements();

        //handle '}'
        if ('}' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</whileStatement>");
    }

    /**
     * Compiles a do statement.
     * <p>
     * this method uses current token
     * grammar --> 'do' subroutineCall ';'
     */
    public void compileDo() throws InvalidSyntaxException, IOException {
        writer.println("<doStatement>");

        if (!"do".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        //handle subroutineCall
        //subroutineCall --> subroutineName '(' expressionList ')' | (className|varName)'.'subroutineName'('expressionList')'

        //handle subroutineName or (className|varName)
        tokenizer.advance();
        if(TokenType.IDENTIFIER != tokenizer.tokenType()) throw new InvalidSyntaxException();
        printTerminalElement();

        tokenizer.advance();
        //handle '(' expressionList ')'
        if ('(' == tokenizer.symbol()) {
            printTerminalElement();
            tokenizer.advance();
            if (isTerm()) compileExpressionList();

            if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();

        }else if ('.' == tokenizer.symbol()) { //'.'subroutineName'('expressionList')'
            printTerminalElement();
            tokenizer.advance();
            if (TokenType.IDENTIFIER != tokenizer.tokenType()) throw new InvalidSyntaxException();
            printTerminalElement();

            tokenizer.advance();
            if ('(' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
            if (isTerm()) compileExpressionList();

            if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
        }

        if (';' != tokenizer.symbol()) throw new InvalidSyntaxException();
        printTerminalElement();

        writer.println("</doStatement>");


    }

    /**
     * Compiles a return statement.
     * <p>
     * this method uses current token
     * grammar --> 'return' expression? ';'
     */
    public void compileReturn() throws InvalidSyntaxException, IOException {
        writer.println("<returnStatement>");

        if (!"return".equals(tokenizer.stringVal())) throw new InvalidSyntaxException();
        printTerminalElement();

        tokenizer.advance();
        if (';' == tokenizer.symbol()) {
            printTerminalElement();
        } else {
            compileExpressionWithCurrentToken();
            if (';' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
        }

        writer.println("</returnStatement>");
    }

    /**
     * Compiles an expression.
     *
     * this method advances next token
     */
    public void compileExpression() throws IOException, InvalidSyntaxException {
        tokenizer.advance();
        compileExpressionWithCurrentToken();
    }

    /**
     * Compiles an expression.
     *
     * This method uses current token
     * this method advances next token
     * grammar --> term (op term)*
     */
    private void compileExpressionWithCurrentToken() throws IOException, InvalidSyntaxException {
        writer.println("<expression>");

        compileTerm();

        while (isOp()) {
            printTerminalElement();
            tokenizer.advance();
            compileTerm();
        }

        writer.println("</expression>");
    }


    /**
     * Compile a term. If the current token is an identifier, the routine must distinguish between a variable,
     * an array entry, or a subroutine call. A single look-ahead token which may be one of "[", "(", or ".", suffices
     * to distinguish between the possibilities. Any other token is not part of the term and should not be advanced over.
     * <p>
     * This method uses current token
     * This method advances next token
     * grammar -->  integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
     */
    public void compileTerm() throws InvalidSyntaxException, IOException {
        writer.println("<term>");

        TokenType type = tokenizer.tokenType();
        //handle integerConstant | stringConstant | keywordConstant
        if (type == TokenType.INT_CONST || type == TokenType.STRING_CONST || type == TokenType.KEYWORD) {
            printTerminalElement();
            tokenizer.advance();
        } else if (type == TokenType.IDENTIFIER) { //handle varName | varName '[' expression ']' | subroutineCall
            printTerminalElement();
            tokenizer.advance();

            //handle varName '[' expression ']'
            if ('[' == tokenizer.symbol()) {
                printTerminalElement();
                compileExpression();
                if (']' != tokenizer.symbol()) throw new InvalidSyntaxException();
                printTerminalElement();
                tokenizer.advance();
            }

            //handle subroutineCall
            //subroutineCall --> subroutineName '(' expressionList ')' | (className|varName)'.'subroutineName'('expressionList')'
            else if ('(' == tokenizer.symbol()) {
                printTerminalElement();
                tokenizer.advance();
                if (isTerm()) compileExpressionList();

                if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
                printTerminalElement();
                tokenizer.advance();

            } else if ('.' == tokenizer.symbol()) {
                printTerminalElement();
                tokenizer.advance();
                if (TokenType.IDENTIFIER != tokenizer.tokenType()) throw new InvalidSyntaxException();
                printTerminalElement();

                tokenizer.advance();
                if ('(' != tokenizer.symbol()) throw new InvalidSyntaxException();
                printTerminalElement();
                tokenizer.advance();
                if (isTerm()) compileExpressionList();

                if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
                printTerminalElement();
                tokenizer.advance();
            }

        } else if ('(' == tokenizer.symbol()) { //handle '(' expression ')'
            printTerminalElement();
            compileExpression();
            if (')' != tokenizer.symbol()) throw new InvalidSyntaxException();
            printTerminalElement();
            tokenizer.advance();
        } else if ('-' == tokenizer.symbol() || '~' == tokenizer.symbol()) { //handle unaryOp term
            printTerminalElement();
            tokenizer.advance();
            compileTerm();
        } else {
            throw new InvalidSyntaxException();
        }

        writer.println("</term>");
    }

    /**
     * this method uses current token
     * grammar -->  integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
     * @return
     */
    private boolean isTerm() {
        TokenType type = tokenizer.tokenType();
        if( TokenType.INT_CONST == type || TokenType.STRING_CONST == type || TokenType.KEYWORD == type
                || TokenType.IDENTIFIER == type || '(' == tokenizer.symbol() || '-' == tokenizer.symbol()
                || '~' == tokenizer.symbol())
            return true;

        return false;
    }

    /**
     * Compiles a (possibly empty) comma separated list of expressions.
     *
     * this method use current token
     * this method advances next token
     * grammar --> (expression (',' expression)*)?
     */
    public void compileExpressionList() throws IOException, InvalidSyntaxException {
        writer.println("<expressionList>");

        if(isTerm()) compileExpressionWithCurrentToken();
        while(',' == tokenizer.symbol()){
            printTerminalElement();
            compileExpression();
        }

        writer.println("</expressionList>");
    }

    /**
     * check if current token is a type
     * grammar --> type: 'int' | 'char' | 'boolean' | className
     * grammar --> className: identifier
     *
     * @return
     */
    private boolean isType() {
        switch (tokenizer.stringVal()) {
            case "int":
            case "char":
            case "boolean":
                return true;
            default:
                return TokenType.IDENTIFIER.equals(tokenizer.tokenType());
        }
    }

    /**
     * this method uses current token
     * grammar --> '+'|'-'|'*'|'/'|'&'|'|'|'<'|'>'|'='
     *
     * @return
     */
    private boolean isOp() {
        char[] ops = new char[]{'+', '-', '*', '/', '&', '|', '<', '>', '='};
        return tokenizer.tokenType() == TokenType.SYMBOL && contains(ops, tokenizer.symbol());
    }

    private boolean contains(final char[] array, final char v) {

        boolean result = false;

        for (int i : array) {
            if (i == v) {
                result = true;
                break;
            }
        }

        return result;
    }

    private void printTerminalElement() {
        switch (tokenizer.tokenType()) {
            case KEYWORD:
                writer.println("<keyword> " + StringEscapeUtils.escapeXml(tokenizer.keyWord()) + " </keyword>");
                break;
            case SYMBOL:
                writer.println("<symbol> " + StringEscapeUtils.escapeXml(tokenizer.symbol() + "") + " </symbol>");
                break;
            case INT_CONST:
                writer.println("<integerConstant> " + tokenizer.intVal() + " </integerConstant>");
                break;
            case STRING_CONST:
                writer.println("<stringConstant> " + StringEscapeUtils.escapeXml(tokenizer.stringVal()) + " </stringConstant>");
                break;
            case IDENTIFIER:
                writer.println("<identifier> " + StringEscapeUtils.escapeXml(tokenizer.identifier()) + " </identifier>");
                break;
            default:
                break;
        }
    }

    public void flush() {
        writer.flush();
    }
}
