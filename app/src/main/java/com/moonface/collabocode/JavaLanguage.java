package com.moonface.collabocode;

import com.github.ahmadaghazadeh.editor.processor.language.Language;
import com.github.ahmadaghazadeh.editor.processor.utils.text.ArrayUtils;

import java.util.regex.Pattern;

public class JavaLanguage extends Language {
    private static final Pattern SYNTAX_NUMBERS = Pattern.compile("(\\b(\\d*[.]?\\d+)\\b)");

    public final Pattern getSyntaxNumbers() {
        return SYNTAX_NUMBERS;
    }

    private static final Pattern SYNTAX_SYMBOLS = Pattern.compile("(!|\\+|-|\\*|<|>|=|\\?|\\||:|%|&)");

    public final Pattern getSyntaxSymbols() {
        return SYNTAX_SYMBOLS;
    }

    private static final Pattern SYNTAX_BRACKETS = Pattern.compile("(\\(|\\)|\\{|\\}|\\[|\\])");

    public final Pattern getSyntaxBrackets() {
        return SYNTAX_BRACKETS;
    }

    private static final Pattern SYNTAX_KEYWORDS = Pattern.compile(
            "(?<=\\b)((abstract)|(assert)|(boolean)|(break)|(byte)|(case)|(catch)|(char)" +
                    "|(class)|(continue)|(default)|(do)|(double)|(else)|(enum)|(extends)" +
                    "|(final)|(finally)|(float)|(for)|(if)|(implements)|(import)" +
                    "|(instanceof)|(int)|(interface)|(long)|(native)|(new)|(package)|(private)"+
                    "|(protected)|(public)|(return)|(short)|(static)|(strictfp)|(super)|(switch)"+
                    "|(sychronized)|(this)|(throw)|(throws)|(transient)|(try)|(void)|(volatile)|(while)"+
                    "|(true)|(false)|(null)|(const)|(goto))(?=\\b)");

    public final Pattern getSyntaxKeywords() {
        return SYNTAX_KEYWORDS;
    }

    private static final Pattern SYNTAX_METHODS = Pattern.compile(
            "", Pattern.CASE_INSENSITIVE);

    public final Pattern getSyntaxMethods() {
        return SYNTAX_METHODS;
    }

    private static final Pattern SYNTAX_STRINGS = Pattern.compile("\"(.*?)\"|'(.*?)'");

    public final Pattern getSyntaxStrings() {
        return SYNTAX_STRINGS;
    }

    private static final Pattern SYNTAX_COMMENTS = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");

    public final Pattern getSyntaxComments() {
        return SYNTAX_COMMENTS;
    }

    private static final char[] LANGUAGE_BRACKETS = new char[]{'{', '[', '(', '}', ']', ')'}; //do not change

    public final char[] getLanguageBrackets() {
        return LANGUAGE_BRACKETS;
    }

    private static final String[] ALL_KEYWORDS = ArrayUtils.join(String.class);

    public final String[] getAllCompletions() {
        return ALL_KEYWORDS;
    }
}
