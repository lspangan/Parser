package parser;

import java.util.*;
import lexer.*;
import ast.*;

/** 
 *  The Parser class performs recursive-descent parsing; as a
 *  by-product it will build the Abstract Syntax Tree representation
 *  for the source program
 *  Following is the Grammar we are using:
 *  
 *  PROGRAM - �program� BLOCK == program
 *  
 *  BLOCK - �{� D* S* �}�  == block
 *  
 *  D - TYPE NAME                    == decl
 *    - TYPE NAME FUNHEAD BLOCK      == functionDecl
 *  
 *  TYPE  -  �int�
 *        -  �boolean�
 *
 *  FUNHEAD  - '(' (D list ',')? ')'  == formals
 *
 *  S - �if� E �then� BLOCK �else� BLOCK  == if
 *    - �while� E BLOCK               == while
 *    - �return� E                    == return
 *    - BLOCK
 *    - NAME �=� E                    == assign
 *  
 *  E - SE
 *    - SE �==� SE   == =
 *    - SE �!=� SE   == !=
 *    - SE �less than  SE   == less than
 *    - SE �less than or equal to SE   == less than or equal to
 *  
 *  SE  -  T
 *      -  SE �+� T  == +
 *      -  SE �-� T  == -
 *      -  SE �|� T  == or
 *  
 *  T  - F
 *     - T �*� F  == *
 *     - T �/� F  == /
 *     - T �and� F  == and
 *  
 *  F  - �(� E �)�
 *     - NAME
 *     - int
 *     - NAME '(' (E list ',')? ')' == call
 *  
 *  NAME  - id
 * 
*/
public class Parser {
    private Token currentToken;
    private Lexer lex;
    private EnumSet<Tokens> relationalOps = 
    	EnumSet.of(Tokens.Equal,Tokens.NotEqual,Tokens.Less,Tokens.LessEqual,
                   Tokens.Greater, Tokens.GreaterEqual);
    private EnumSet<Tokens> addingOps = 
    	EnumSet.of(Tokens.Plus,Tokens.Minus,Tokens.Or);    
    private EnumSet<Tokens> multiplyingOps = 
    	EnumSet.of(Tokens.Multiply,Tokens.Divide,Tokens.And);
    private EnumSet<Tokens> powerOp =
        EnumSet.of(Tokens.Power);
    private EnumSet<Tokens> unaryOps =
        EnumSet.of(Tokens.Minus, Tokens.Negation);
/**
 *  Construct a new Parser; 
 *  @param sourceProgram - source file name
 *  @exception Exception - thrown for any problems at startup (e.g. I/O)
*/
    public Parser(String sourceProgram) throws Exception {
        try {
            lex = new Lexer(sourceProgram);
            scan();
        }
         catch (Exception e) {
            System.out.println("********exception*******"+e.toString());
            throw e;
         };
    }
    
    public Lexer getLex() { return lex; }
    
/**
 *  Execute the parse command
 *  @return the AST for the source program
 *  @exception Exception - pass on any type of exception raised
*/
    public AST execute() throws Exception {
        try {
            return rProgram();
        }catch (SyntaxError e) {
            e.print();
            throw e;
            }
    }
    
/** <pre>
 *  Program - 'program' block == program
 *  </pre>
 *  @return the program tree
 *  @exception SyntaxError - thrown for any syntax error
*/    
    public AST rProgram() throws SyntaxError {
        // note that rProgram actually returns a ProgramTree; we use the 
        // principle of substitutability to indicate it returns an AST
       AST t = new ProgramTree();
        expect(Tokens.Program);
        t.addKid(rBlock());
        return t;
    }

/** <pre>
 *  block - '{' d* s* '}'  == block
 *  </pre>
 *  @return  block tree
 *  @exception SyntaxError - thrown for any syntax error
 *         e.g. an expected left brace isn't found
*/
    public AST rBlock() throws SyntaxError {
        expect(Tokens.LeftBrace);
        AST t = new BlockTree();
        while (startingDecl()) {  // get decls
            t.addKid(rDecl());
        }
        while (startingStatement()) {  // get statements
            t.addKid(rStatement());
        }
        expect(Tokens.RightBrace);
        return t;
    }

    boolean startingDecl() {
        if (isNextTok(Tokens.Int) || isNextTok(Tokens.BOOLean) || isNextTok(Tokens.Float)
                || isNextTok(Tokens.Char) || isNextTok(Tokens.String)) {
            return true;
        }
        return false;
    }

    boolean startingStatement() {
        if (isNextTok(Tokens.If) || isNextTok(Tokens.While) || isNextTok(Tokens.Return)
                || isNextTok(Tokens.LeftBrace) || isNextTok(Tokens.Identifier)
                || isNextTok(Tokens.Do) || isNextTok(Tokens.For)) {
            return true;
        }
        return false;
    }

/** <pre>
 *  d - type name                 == decl
 *    - type name funcHead block  == functionDecl
 *  </pre>
 *  @return either the decl tree or the functionDecl tree
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rDecl() throws SyntaxError {
        AST t,t1;
        t = rType();
        t1 = rName();
        if (isNextTok(Tokens.LeftParen)) { // function
            t = (new FunctionDeclTree()).addKid(t).addKid(t1);
            t.addKid(rFunHead());
            t.addKid(rBlock());
            return t;
        }
        t = (new DeclTree()).addKid(t).addKid(t1);
        return t;
    }

/** <pre>
 *  type  -  'int'
 *  type  -  'bool'
 *  </pre>
 *  @return either the intType or boolType tree
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rType() throws SyntaxError {
        AST t;
        if (isNextTok(Tokens.Int)) {
            t = new IntTypeTree();
            scan();
        } else if (isNextTok(Tokens.String)){ // added string type
            t = new StringTypeTree();
            scan();
        } else if (isNextTok(Tokens.Float)) { // added float type
            t = new FloatTypeTree();
            scan();
        } else if (isNextTok(Tokens.Char)) { // added char type
            t = new CharTypeTree();
            scan();
        } else {
            expect(Tokens.BOOLean); 
            t = new BoolTypeTree();
        }
        return t;
    }

/** <pre>
 *  funHead  -  '(' (decl list ',')? ')'  == formals
 *  note a funhead is a list of zero or more decl's
 *  separated by commas, all in parens
 *  </pre>
 *  @return the formals tree describing this list of formals
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rFunHead() throws SyntaxError {
        AST t = new FormalsTree();
        expect(Tokens.LeftParen);
        if (!isNextTok(Tokens.RightParen)) {
            do {
                t.addKid(rDecl());
                if (isNextTok(Tokens.Comma)) {
                    scan();
                } else {
                    break;
                }
            } while (true);
        }
        expect(Tokens.RightParen);
        return t;
    }
    
/** <pre>
 *  EHEAD - ‘(‘ (ASSIGN)? ‘;’ (E)? ‘;’ (ASSIGN)? ‘)’ -------------NEW RULE
 *  separated by semicolons, all in parens
 *  </pre>
 *  @return the Ehead tree describing this list
 *  @exception SyntaxError - thrown for any syntax error
*/     
    public AST rEHEAD() throws SyntaxError {
        AST t = new EHeadTree();
        expect(Tokens.LeftParen);
        do {
            if (isNextTok(Tokens.Semicolon)) {
                scan();
            } else {
                t.addKid(rASSIGN());
                if (isNextTok(Tokens.Semicolon)) {
                   scan();
                }
            }
            if (isNextTok(Tokens.Semicolon)) {
                scan();
            } else {
                t.addKid(rExpr());
                if (isNextTok(Tokens.Semicolon)) {
                    scan();
                }
            }
            if (isNextTok(Tokens.RightParen)) {
                scan();
                return t;
            } else {            
                t.addKid(rASSIGN());
                if (isNextTok(Tokens.RightParen)) {                   
                }
            }    
        } while (!isNextTok(Tokens.RightParen));
        expect(Tokens.RightParen);
        return t;
    }
    
/** <pre>
 *  ASSIGN - NAME ‘=’ E == assign ------------NEW RULE
 *  </pre>
 *  @return the Assign tree describing this statement
 *  @exception SyntaxError - thrown for any syntax error
*/ 
    public AST rASSIGN() throws SyntaxError {
        AST t = rName();
        if (isNextTok(Tokens.Assign)) {
            t = new AssignTree().addKid(t);
            scan();
            return t.addKid(rExpr());
        }
        return t;
    }

/** <pre>
 *      S - 'if' e 'then' block 'else' block  == if
 *        - ‘if’ E ‘then’ BLOCK == if ------------NEW RULE 
 *        - ‘do’ BLOCK ‘while’ E ---NEW RULE
 *        - 'while' e block  == while
 *        - ‘for’ EHEAD BLOCK -------------NEW RULE
 *        - 'return' e  == return
 *        - block
 *        - name '=' e  == assign
 *  </pre>
 *  @return the tree corresponding to the statement found
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rStatement() throws SyntaxError {
        AST t;
        if (isNextTok(Tokens.If)) { // added 'if' E 'then' BLOCK
            scan();
            t = new IfTree();
            t.addKid(rExpr());
            expect(Tokens.Then);
            t.addKid(rBlock());
            if (isNextTok(Tokens.Else)) {
                scan();
                t.addKid(rBlock());
                return t;
            }
            return t; 
        }
        if (isNextTok(Tokens.Do)) { // added 'do' BLOCK 'while' E
            scan();
            t = new DoTree();
            t.addKid(rBlock());
            expect(Tokens.While);
            t.addKid(rExpr());
            return t;
        }
        if (isNextTok(Tokens.While)) {
            scan();
            t = new WhileTree();
            t.addKid(rExpr());
            t.addKid(rBlock());
            return t;
        }
        if (isNextTok(Tokens.For)) { // added ‘for’ EHEAD BLOCK 
            scan();
            t = new ForTree();
            t.addKid(rEHEAD());
            t.addKid(rBlock());
            return t;
        }
        if (isNextTok(Tokens.Return)) {
            scan();
            t = new ReturnTree();
            t.addKid(rExpr());
            return t;
        }
        if (isNextTok(Tokens.LeftBrace)) {
            return rBlock();
        }
        t = rASSIGN();
        return t;
    }
    

/** <pre>
 *      e - se
 *        - se '==' se  == =
 *        - se '!=' se  == !=
 *        - se 'less than'  se  == less than
 *        - se 'less than or equal to' se  == less than or equal to
 *        - se 'greater than' se == greater than ----------- ADDED
 *        - se 'greater than or equal to' se == greater than or equal to ---------- ADDED
 *  </pre>
 *  @return the tree corresponding to the expression
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rExpr() throws SyntaxError {
        AST t, kid = rSimpleExpr();
        t = getRelationTree();
        if (t == null) {
            return kid;
        }
        t.addKid(kid);
        t.addKid(rSimpleExpr());
        return t;
    }

/** <pre>
 *      se  -  t
 *          -  se '+' t  == +
 *          -  se '-' t  == -
 *          -  se '|' t  == or
 *  This rule indicates we should pick up as many <i>t</i>'s as
 *  possible; the <i>t</i>'s will be left associative
 *  </pre>
 *  @return the tree corresponding to the adding expression
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rSimpleExpr() throws SyntaxError {
        AST t, kid = rTerm();
        while ( (t = getAddOperTree()) != null) {
            t.addKid(kid);
            t.addKid(rTerm());
            kid = t;
        }
        return kid;
    }

/** <pre>
 *      t  - f
 *         - t '*' f  == *
 *         - t '/' f  == /
 *         - t 'and' f  == and
 *  This rule indicates we should pick up as many <i>f</i>'s as
 *  possible; the <i>f</i>'s will be left associative
 *  </pre>
 *  @return the tree corresponding to the multiplying expression
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rTerm() throws SyntaxError {
        AST t, kid = rFactor();
        while ( (t = getMultOperTree()) != null) {
            t.addKid(kid);
            t.addKid(rFactor());
            kid = t;
        }
        return kid;
    }
    
/** <pre>
 *      f  - ff
 *         - ff '^' f  == ^
 *  This rule indicates we should pick up as many <i>ff</i>'s as
 *  possible; the <i>ff</i>'s will be right associative
 *  </pre>
 *  @return the tree corresponding to the power expression
 *  @exception SyntaxError - thrown for any syntax error
*/    
    public AST rFactor() throws SyntaxError {
        AST t, kid = rFFactor();
        t = getPowerOperTree();
        if (t == null) {
            return kid;
        }
        t.addKid(kid);
        t.addKid(rFactor());
        return t;
    }
    

/** <pre>
 *      ff - '(' e ')'
 *         - name
 *         - int
 *         - name '(' (e list ',')? ')' == call
 *  </pre>
 *  @return the tree corresponding to the ffactor expression
 *  @exception SyntaxError - thrown for any syntax error
*/
    public AST rFFactor() throws SyntaxError {
        AST t;
        if (isNextTok(Tokens.LeftParen)) { // -> (e)
            scan();
            t = rExpr();
            expect(Tokens.RightParen);
            return t;
        }
        if (isNextTok(Tokens.INTeger)) {  //  -> <int>
            t = new IntTree(currentToken);
            scan();
            return t;
        }
        if (isNextTok(Tokens.FLOAT)) { // -> <float>
            t = new FloatTree(currentToken);
            scan();
            return t;
        }
        if (isNextTok(Tokens.ScientificN)) { // -> <scientificN>
            t = new ScientificNTree(currentToken);
            scan();
            return t;
        }         
        if (isNextTok(Tokens.CHAR)) { // -> <char>
            t = new CharTree(currentToken);
            scan();
            return t;
        }
        if (isNextTok(Tokens.STRING)) { // -> <string>
            t = new StringTree(currentToken);
            scan();
            return t;
        }       
        Tokens kind = currentToken.getKind(); // -> '-' || '!'
       	if (unaryOps.contains(kind)) {
            t = new UnaryOpTree(currentToken);
            scan();
            t.addKid(rExpr());
            return t;   
        }
        t = rName();
        if (!isNextTok(Tokens.LeftParen)) {  //  -> name
            return t;
        }
        scan();     // -> name '(' (e list ',')? ) ==> call
        t = (new CallTree()).addKid(t);    
        if (!isNextTok(Tokens.RightParen)) {
            do {
                t.addKid(rExpr());
                if (isNextTok(Tokens.Comma)) {
                    scan();
                } else {
                    break;
                }
            } while (true);
        }
        expect(Tokens.RightParen);
        return t;
    }

/** <pre>
 *      name  - id
 *  </pre>
 *  @return the id tree
 *  @exception SyntaxError - thrown for any syntax error
*/        
    public AST rName() throws SyntaxError {
        AST t;
        if (isNextTok(Tokens.Identifier)) {
            t = new IdTree(currentToken);
            scan();
            return t;
        }
        throw new SyntaxError(currentToken,Tokens.Identifier);
    }
    
    AST getRelationTree() {  // build tree with current token's relation
    	Tokens kind = currentToken.getKind();
    	if (relationalOps.contains(kind)) {
    		AST t = new RelOpTree(currentToken);
    		scan();
    		return t;
    	} else {
    		return null;
    	}
     }
    
    private AST getAddOperTree() {
    	Tokens kind = currentToken.getKind();
    	if (addingOps.contains(kind)) {
    		AST t = new AddOpTree(currentToken);
    		scan();
    		return t;
    	} else {
    		return null;
    	}
    }

    private AST getMultOperTree() {
    	Tokens kind = currentToken.getKind();
       	if (multiplyingOps.contains(kind)) {
    		AST t = new MultOpTree(currentToken);
    		scan();
    		return t;
    	} else {
    		return null;
    	}
    }
    
    private AST getPowerOperTree() { // added power tree
    	Tokens kind = currentToken.getKind();
       	if (powerOp.contains(kind)) {
    		AST t = new PowerOpTree(currentToken);
                scan();
    		return t;
    	} else {
    		return null;
    	}
    }    

    private boolean isNextTok(Tokens kind) {
        if ((currentToken == null) || (currentToken.getKind() != kind)) {
            return false;
        }
        return true;
    }

    private void expect(Tokens kind) throws SyntaxError {
        if (isNextTok(kind)) {
            scan();
            return;
        }
        throw new SyntaxError(currentToken,kind);
    }

    private void scan() {
        currentToken = lex.nextToken();
        /*if (currentToken != null) {
            currentToken.print();   // debug printout
        }
        return;*/
    }
}

class SyntaxError extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Token tokenFound;
    private Tokens kindExpected;
    
/**
 *  record the syntax error just encountered
 *  @param tokenFound is the token just found by the parser
 *  @param kindExpected is the token we expected to find based on 
 *  the current context
*/
    public SyntaxError(Token tokenFound, Tokens kindExpected) {
        this.tokenFound = tokenFound;
        this.kindExpected = kindExpected;
    }
    
    void print() {
        System.out.println("Expected: " + 
           kindExpected);
        return;
    }
}
