package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class CharTree extends AST {
    private Symbol symbol;

/**
 *  @param tok is the Token containing the String representation of the char
 *  literal; we keep the String rather than converting to an integer value
 *  so we don't introduce any machine dependencies with respect to char
 *  representations
*/
    public CharTree(Token tok) {
        this.symbol = tok.getSymbol();
    }

    public Object accept(ASTVisitor v) {
        return v.visitCharTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

}
