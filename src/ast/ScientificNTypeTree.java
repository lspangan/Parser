package ast;

import visitor.*;

public class ScientificNTypeTree extends AST {

    public ScientificNTypeTree() {
    }

    public Object accept(ASTVisitor v) {
        return v.visitScientificNTypeTree(this);
    }

}
