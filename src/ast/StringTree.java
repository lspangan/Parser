package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class StringTree extends AST {
    private Symbol symbol;

/**
 *  @param tok is the Token containing the String representation of the String
 *  literal; we keep the String rather than converting to an integer value
 *  so we don't introduce any machine dependencies with respect to String
 *  representations
*/
    public StringTree(Token tok) {
        this.symbol = tok.getSymbol();
    }

    public Object accept(ASTVisitor v) {
        return v.visitStringTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

}
