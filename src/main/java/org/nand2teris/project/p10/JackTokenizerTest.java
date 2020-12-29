package org.nand2teris.project.p10;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JackTokenizerTest {
    public static void main(String[]args) throws IOException {
        JackTokenizer tokenizer = new JackTokenizer(args[0]);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[0]+ "-tokens.xml")));

        writer.println("<tokens>");

        while(tokenizer.hasMoreTokens()){
            tokenizer.advance();
           switch(tokenizer.tokenType()){
               case KEYWORD: writer.println("<keyword> "+ StringEscapeUtils.escapeXml(tokenizer.keyWord()) + " </keyword>");break;
               case SYMBOL: writer.println("<symbol> "+ StringEscapeUtils.escapeXml(tokenizer.symbol()+"" )+ " </symbol>"); break;
               case INT_CONST: writer.println("<integerConstant> "+ tokenizer.intVal() + " </integerConstant>"); break;
               case STRING_CONST: writer.println("<stringConstant> "+ StringEscapeUtils.escapeXml(tokenizer.stringVal()) + " </stringConstant>"); break;
               case IDENTIFIER: writer.println("<identifier> "+ StringEscapeUtils.escapeXml(tokenizer.identifier()) + " </identifier>");break;
               default:
                   break;
           }
           writer.flush();
        }
        writer.println("</tokens>");
        writer.flush();
    }
}
