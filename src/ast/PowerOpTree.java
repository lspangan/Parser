package ast;

import lexer.Symbol;
import lexer.Token;
import visitor.*;

public class PowerOpTree extends AST {
    private Symbol symbol;

/**
 *  @param tok contains the Symbol that indicates the specific power operator
*/
    public PowerOpTree(Token tok) {
        this.symbol = tok.getSymbol();
    }

    public Object accept(ASTVisitor v) {
        return v.visitPowerOpTree(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

}
