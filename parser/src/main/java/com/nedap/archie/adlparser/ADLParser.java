package com.nedap.archie.adlparser;

import com.nedap.archie.adlparser.antlr.*;
import com.nedap.archie.adlparser.treewalkers.ADLListener;
import com.nedap.archie.aom.Archetype;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.InputStream;


/**
 * Parses ADL files to Archetype objects.
 *
 */
public class ADLParser {

    private ADLParserErrors errors;

    private Lexer lexer;
    private AdlParser parser;
    private ADLListener listener;
    private ParseTreeWalker walker;
    private AdlParser.AdlContext tree;
    public ADLErrorListener errorListener;


    public Archetype parse(String adl) throws IOException {
        return parse(new ANTLRInputStream(adl));
    }

    public Archetype parse(InputStream stream) throws IOException {
        return parse(new ANTLRInputStream(stream));
    }

    public Archetype parse(CharStream stream) {
        errors = new ADLParserErrors();
        errorListener = new ADLErrorListener(errors);

        lexer = new AdlLexer(stream);
        lexer.addErrorListener(errorListener);
        parser = new AdlParser(new CommonTokenStream(lexer));
        parser.addErrorListener(errorListener);
        tree = parser.adl(); // parse

        ADLListener listener = new ADLListener(errors);
        walker= new ParseTreeWalker();
        walker.walk(listener, tree);
        return listener.getArchetype();

    }

    public ADLParserErrors getErrors() {
        return errors;
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public AdlParser getParser() {
        return parser;
    }

    public void setParser(AdlParser parser) {
        this.parser = parser;
    }

    public ADLListener getListener() {
        return listener;
    }

    public void setListener(ADLListener listener) {
        this.listener = listener;
    }

    public ParseTreeWalker getWalker() {
        return walker;
    }

    public void setWalker(ParseTreeWalker walker) {
        this.walker = walker;
    }

    public AdlParser.AdlContext getTree() {
        return tree;
    }

    public void setTree(AdlParser.AdlContext tree) {
        this.tree = tree;
    }
}