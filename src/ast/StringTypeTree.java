package ast;

import visitor.*;

public class StringTypeTree extends AST {

    public StringTypeTree() {
    }

    public Object accept(ASTVisitor v) {
        return v.visitStringTypeTree(this);
    }

}
