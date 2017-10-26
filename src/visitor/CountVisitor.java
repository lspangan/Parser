package visitor;

import ast.AST;
import java.util.ArrayList;
import java.util.List;


public class CountVisitor extends ASTVisitor {
    
    private int [] nCount = new int[100]; // holds the max x coordinate so far for each depth
    private int depth = 0; // holds the current depth
    private int maxDepth = 0;
    private boolean shift = false; // indicates if this pass was to calculate x coordinate (false) or shift it by the amt necessary to eliminate overlap (true
    private int shiftAmt = 0; // amt to shift tree to the right
    
    
    // sets maxDepth according to the input argument
    // nCount array is created for the correct size and initialized
    private void CountVisitor(int inpDepth) {
        maxDepth = inpDepth;
        nCount = new int[maxDepth + 1];
        for (int i = 0; i < maxDepth; i++) {
            nCount[i] = 0;
        }
    }
    
    // TODO calculates the x coordinate for each AST sub-tree node
    private void count(AST t) {
        
        //nCount[depth] ++;
        //t.setXLoc(nCount[depth]);
        depth++;
        visitKids(t);
        depth--;
        if(depth > maxDepth) {maxDepth = depth;}
        if (t.kidCount() == 0) {
            nCount[depth] += 2;
            t.setXLoc(t.getXLoc() + nCount[depth]);   
            
        }        

        if (t.kidCount() > 0) {       
            int trialX = (t.getKid(1).getXLoc() + t.getKid(t.kidCount()).getXLoc()) / 2;
                   
            if (trialX < nCount[depth]) {
                shift = true;
                shiftAmt ++;
                
                trialX += shiftAmt; 
                depth++;
                visitKids(t);
                depth--;
                //nCount[depth] ++;
                //t.setXLoc(trialX);
                
            } 
            t.setXLoc(trialX);
            
            nCount[depth] ++;
            
        }
        if (shift == true) {
            t.setXLoc(t.getXLoc() + shiftAmt);
            nCount[depth] ++;
            //depth++;
            //visitKids(t);
            //depth--;
            shift = false;

        }        
        //nCount[depth]++;
        //if t.leafnode
        //if t.leafnode is false
            // trialx < ncount[depth]
        /*nCount[depth]++;
        if(depth > maxDepth) {maxDepth = depth;}
        depth++;
        visitKids(t);
        depth--;*/
       
    }
    
    public int[] getCount() {
        int [] count = new int[maxDepth + 1];
        
        for(int i = 0; i <= maxDepth; i++) {
            count[i] = nCount[i];
        }
        
        return count;
    }
    
    public void printCount() {
        for(int i = 0; i <= maxDepth; i++) {
            System.out.println("Depth: " + i + " Nodes: " + nCount[i]);
        }
    }

    public Object visitProgramTree(AST t) {count(t); return null;}
    public Object visitBlockTree(AST t) {count(t); return null;}
    public Object visitFunctionDeclTree(AST t) {count(t); return null;    }
    public Object visitCallTree(AST t) {count(t); return null;    }
    public Object visitDeclTree(AST t) {count(t); return null;    }
    public Object visitIntTypeTree(AST t) {count(t); return null;    }
    public Object visitBoolTypeTree(AST t) {count(t); return null;    }
    public Object visitFormalsTree(AST t) {count(t); return null;    }
    public Object visitActualArgsTree(AST t) {count(t); return null;    }
    public Object visitIfTree(AST t) {count(t); return null;    }
    public Object visitWhileTree(AST t) {count(t); return null;    }
    public Object visitReturnTree(AST t) {count(t); return null;    }
    public Object visitAssignTree(AST t) {count(t); return null;    }
    public Object visitIntTree(AST t) {count(t); return null;    }
    public Object visitIdTree(AST t) {count(t); return null;    }
    public Object visitRelOpTree(AST t) {count(t); return null;    }
    public Object visitAddOpTree(AST t) {count(t); return null;    }
    public Object visitMultOpTree(AST t) {count(t); return null;    }

    //new methods here
    public Object visitFloatTree(AST t) {count(t); return null;    }
    public Object visitFloatTypeTree(AST t) {count(t); return null;    }
    public Object visitCharTree(AST t) {count(t); return null;    }
    public Object visitCharTypeTree(AST t) {count(t); return null;    }
    public Object visitStringTree(AST t) {count(t); return null;    }
    public Object visitStringTypeTree(AST t) {count(t); return null;    }
    public Object visitScientificNTree(AST t) {count(t); return null;    }
    public Object visitScientificNTypeTree(AST t) {count(t); return null;    }
    public Object visitDoTree(AST t) {count(t); return null;    }
    public Object visitForTree(AST t) {count(t); return null;    }
    public Object visitEHeadTree(AST t) {count(t); return null;    }
    public Object visitPowerOpTree(AST t) {count(t); return null;    }
    public Object visitUnaryOpTree(AST t) {count(t); return null;    }
    
    
}

