/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package visitor;

import ast.AST;
import ast.AddOpTree;
import ast.CharTree;
import ast.DoTree;
import ast.EHeadTree;
import ast.FloatTree;
import ast.ForTree;
import ast.IdTree;
import ast.IntTree;
import ast.MultOpTree;
import ast.PowerOpTree;
import ast.RelOpTree;
import ast.StringTree;
import ast.ScientificNTree;
import ast.UnaryOpTree;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lowell Milliken
 */
public class DrawVisitor extends ASTVisitor {
    
    private final int nodew = 100;
    private final int nodeh = 30;
    private final int vertSep = 50;
    private final int horizSep = 10;
    
    private int width;
    private int height;
    
    private int [] nCount;
    private int [] progress;
    private int depth = 0;
    private BufferedImage bimg;
    private Graphics2D g2;
    
    public DrawVisitor(int [] nCount) {
        this.nCount = nCount;
        progress = new int [nCount.length];

        width = max(nCount)*(nodew + horizSep);
        height = nCount.length*(nodeh + vertSep);

        g2 = createGraphics2D(width,height);
    }

    private int max(int [] array) {
        int max = array[0];
        for(int i = 1; i < array.length; i++)
            if(max < array[i]) max = array[i];
        return max;
    }
    
    public void draw(String s, AST t) {
        int hstep = nodew + horizSep;
        int vstep = nodeh + vertSep;

        //  x value needs to be calculated considering progress[depth] and nCount[depth] and relative hstep.
        int x =  //t.getXLoc();
                (width/2 + (progress[depth])*hstep - nCount[depth]*hstep/2 + nodew/2)-vertSep;
        // y value should be calculated using depth and vstep
        int y = vstep * depth;

        g2.setColor(Color.black);
        g2.drawOval(x, y, nodew, nodeh);
        g2.setColor(Color.BLACK);
        g2.drawString(s, x+10, y+2*nodeh/3);

        // Think what should be startx and starty
        int startx = x + nodew/2;
        int starty = y + nodeh;

        int endx;
        int endy;
        g2.setColor(Color.black);
        for(int i = 0; i < t.kidCount(); i++) {
            endx = //width/2 + t.getKid(i).getXLoc()*hstep - nCount[depth + 1]*hstep/2 + nodew/2;
           width/2 + (progress[depth+1]+i)*hstep - nCount[depth+1]*hstep/2 + nodew/2;
            endy = (depth+1)*vstep;
            g2.drawLine(startx, starty, endx, endy);
        }

        progress[depth]++;
        depth++;
        visitKids(t);
        depth--;
        
        // use post-processing = process child then parent
        // keep track of x,y coordinates
        // check node if child
        // if child, then traverse children first
        // need to know how much has been drawn (progress) in the depth
        // assign coordinates to children, then parent should be half point of children's coordinate (left and right)
        // if space is accepted = shift parent to right and adjust children's position by the amount you shifted
        // get coordinates then draw NOTE- DRAW NODES LAST
    }
    
    private Graphics2D createGraphics2D(int w, int h)
    {
        Graphics2D g2;
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h)
        {
            bimg = new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
        }
        g2 = bimg.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0, 0, w, h);
        return g2;
    }

    public BufferedImage getImage() {
        return bimg;
    }

    public Object visitProgramTree(AST t) { draw("Program",t);  return null; }
    public Object visitBlockTree(AST t) { draw("Block",t);  return null; }
    public Object visitFunctionDeclTree(AST t) { draw("FunctionDecl",t);  return null; }
    public Object visitCallTree(AST t) { draw("Call",t);  return null; }
    public Object visitDeclTree(AST t) { draw("Decl",t);  return null; }
    public Object visitIntTypeTree(AST t) { draw("IntType",t);  return null; }
    public Object visitBoolTypeTree(AST t) { draw("BoolType",t);  return null; }
    public Object visitFormalsTree(AST t) { draw("Formals",t);  return null; }
    public Object visitActualArgsTree(AST t) { draw("ActualArgs",t);  return null; }
    public Object visitIfTree(AST t) { draw("If",t);  return null; }
    public Object visitWhileTree(AST t) { draw("While",t);  return null; }
    public Object visitReturnTree(AST t) { draw("Return",t);  return null; }
    public Object visitAssignTree(AST t) { draw("Assign",t);  return null; }
    public Object visitIntTree(AST t) { draw("Int: "+((IntTree)t).getSymbol().toString(),t);  return null; }
    public Object visitIdTree(AST t) { draw("Id: "+((IdTree)t).getSymbol().toString(),t);  return null; }
    public Object visitRelOpTree(AST t) { draw("RelOp: "+((RelOpTree)t).getSymbol().toString(),t);  return null; }
    public Object visitAddOpTree(AST t) { draw("AddOp: "+((AddOpTree)t).getSymbol().toString(),t);  return null; }
    public Object visitMultOpTree(AST t) { draw("MultOp: "+((MultOpTree)t).getSymbol().toString(),t);  return null; }
    
    //new methods here
    public Object visitFloatTree(AST t) { draw("Float: "+((FloatTree)t).getSymbol().toString(),t);  return null; }
    public Object visitFloatTypeTree(AST t) { draw("FloatType",t);  return null;  }
    public Object visitCharTree(AST t) { draw("Char"+((CharTree)t).getSymbol().toString(),t);  return null;  }
    public Object visitCharTypeTree(AST t) { draw("CharType",t);  return null;  }
    public Object visitStringTree(AST t) { draw("String"+((StringTree)t).getSymbol().toString(),t);  return null;  }
    public Object visitStringTypeTree(AST t) { draw("StringType",t);  return null;  }
    public Object visitScientificNTree(AST t) { draw("ScientificN"+((ScientificNTree)t).getSymbol().toString(),t);  return null;  }
    public Object visitScientificNTypeTree(AST t) { draw("ScientificNType",t);  return null;  }
    public Object visitDoTree(AST t) { draw("Do",t);  return null;  }
    public Object visitForTree(AST t) { draw("For",t);  return null;  }
    public Object visitEHeadTree(AST t) { draw("EHead",t);  return null;  }
    public Object visitPowerOpTree(AST t) { draw("PowerOp"+((PowerOpTree)t).getSymbol().toString(),t);  return null;  }
    public Object visitUnaryOpTree(AST t) { draw("UnaryOp"+((UnaryOpTree)t).getSymbol().toString(),t);  return null;  }
    
}

