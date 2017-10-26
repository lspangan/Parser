package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class UnaryOpTree extends AST {
    private Symbol symbol;

/**
 *  @param tok contains the Symbol which indicates the specific FF operator ('!', '-')
*/
    public UnaryOpTree(Token tok) {
        this.symbol = tok.getSymbol();
    }

    public Object accept(ASTVisitor v) {
    return v.visitUnaryOpTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

}
