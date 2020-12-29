package org.nand2teris.project.p10;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JackTokenizer: Ignores all comments and white space in the input stream, serializes it into Jack-language tokens. The
 * token types are specified according to Jack grammar.
 */

public class JackTokenizer {
    private static final Pattern p = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
    private StringBuilder currentData = new StringBuilder();
    private StringBuilder nextData = new StringBuilder();
    private TokenType nextTokenType;
    private TokenType currentTokenType;
    private char currentDeliminator;
    private BufferedReader br;
    private char ch = (char)0;
    private char ch2= (char)-1;

    /**
     * Open the input .jack file and get ready to tokenize it.
     * @param filePath
     */
    public JackTokenizer(String filePath) throws IOException {
        br = Files.newBufferedReader(Paths.get(filePath));
        ch2 = (char) br.read();
        advance();
    }

    /**
     * Are there more tokens in the input?
     * @return
     */
    public boolean hasMoreTokens(){
        return nextData.length() > 0;
    }

    /**
     * Get the next token from the input and make it current token.
     * The method should be called only if hasMoreTokens is true.
     * Initially there is no current token.
     */
    public void advance() throws IOException {
        currentData.setLength(0);
        currentData.append(nextData.toString());
        currentTokenType = nextTokenType;

        readNext();
    }

    private void readNext() throws IOException {

        if(isSymbol(currentDeliminator)){
            nextTokenType = TokenType.SYMBOL;
            nextData.setLength(0);
            nextData.append(currentDeliminator);
            currentDeliminator= (char) -1;
            return;
        }

        if(currentDeliminator == '"') {
            nextTokenType = TokenType.STRING_CONST;
            nextData.setLength(0);
            nextData.append(eatString());
            currentDeliminator= (char) -1;
            return;
        }

        nextData.setLength(0);
        ch = ch2;
        ch2 = (char) br.read();
        while( ch != (char)-1){
            if('/' == ch && '/' == ch2) {
                nextData.setLength(0);
                eatSingleLineComment();
            }else if('/' == ch && '*' == ch2) {
                nextData.setLength(0);
                eatMultiLineComment();
            }else if(isSymbol(ch)  || ch == '"' || (ch == ' ' && nextData.length()>0) || (ch == '\t' && nextData.length()>0) ){
                currentDeliminator = ch;
                if((currentDeliminator == '"' || isSymbol(ch)) && nextData.length() == 0)readNext();
                break;
            }else if(ch != ' ' && ch != '\n' && ch != '\t'){
                nextData.append(ch);
            }

            ch = ch2;
            ch2 = (char)br.read();
        }
        decideTokenType();
    }

    private void decideTokenType() throws IOException {

        if(Keyword.isKeyword(nextData.toString())){
            nextTokenType = TokenType.KEYWORD;
        }else if(isInteger(nextData.toString())){
            nextTokenType = TokenType.INT_CONST;
        }else if(isIdentifier(nextData.toString())){
            nextTokenType = TokenType.IDENTIFIER;
        }
    }


    /**
     * Return the type of current token, as a constant
     * @return
     */
    public TokenType tokenType(){
        return currentTokenType;
    }

    /**
     * Return the keyword which is the current token as a constant.
     * This method should be called only if tokenType is KEYWORD
     * @return
     */
    public String keyWord(){
        return currentData.toString();
    }

    /**
     * Return the character which is the current token.Should be called only if tokenType is SYMBOL.
     * @return
     */
    public char symbol(){
        return currentData.toString().charAt(0);
    }

    /**
     * Return the identifier which is the current token. Should be called only if tokenType is IDENTIFIER.
     * @return
     */
    public String identifier(){
        return currentData.toString();
    }

    /**
     * Return the integer value of the current token. Should be called only if tokenType is INT_CONST
     * @return
     */
    public int intVal(){
        return Integer.parseInt(currentData.toString());
    }

    /**
     * Return the string value of the current token. Should be called only if tokenType is STRING_CONST
     * @return
     */
    public String stringVal(){
        return currentData.toString();
    }

    private boolean isSymbol(char ch){
        for(char c: new char[]{'{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~'}){
            if(c == ch) return true;
        }
        return false;
    }

    private boolean isInteger(String number){
        try { //Try to make the input into an integer
            Integer.parseInt( number );
            return true; //Return true if it works
        }
        catch( Exception e ) {
            return false; //If it doesn't work return false
        }
    }


    private void eatSingleLineComment() throws IOException {
        System.out.println("single comment -> " + br.readLine());
        this.ch2 = (char) br.read();
    }

    private void eatMultiLineComment() throws IOException {
        System.out.print("multiline comment -> " + ch + ch2);
        ch = (char) br.read();
        ch2 = (char) br.read();
        while('*' != ch || '/' != ch2){
            ch = ch2;
            ch2 = (char)br.read();
            System.out.print(ch2);
        }
        ch2 = (char) br.read();
    }

    private boolean isIdentifier(String val){
        Matcher m = p.matcher(val);
        return m.matches();
    }


    private String eatString() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        while(ch2 != '"'){
            stringBuilder.append(ch2);
            ch2 = (char) br.read();
        }
        ch2 = (char) br.read();
        return stringBuilder.toString();
    }
}
