package ast;

import visitor.*;

public class EHeadTree extends AST {

    public EHeadTree() {
    }

    public Object accept(ASTVisitor v) {
        return v.visitEHeadTree(this);
    }

}
