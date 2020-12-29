package org.nand2teris.project.p10;

public enum Keyword {
    CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET , DO, IF, ELSE, WHILE, RETURN,
    TRUE, FALSE, NULL, THIS;

    public static boolean isKeyword(String value){
        for(Keyword keyword: Keyword.values()){
            if(keyword.name().toLowerCase().equals(value)) return true;
        }
        return false;
    }

    public static Keyword convert(String keyword){
        return Keyword.valueOf(keyword.toUpperCase());
    }
}
