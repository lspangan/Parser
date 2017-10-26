package lexer;
/**
 *  The Lexer class is responsible for scanning the source file
 *  which is a stream of characters and returning a stream of 
 *  tokens; each token object will contain the string (or access
 *  to the string) that describes the token along with an
 *  indication of its location in the source program to be used
 *  for error reporting; we are tracking line numbers; white spaces
 *  are space, tab, newlines
*/
public class Lexer {
    private boolean atEOF = false;
    private char ch;     // next character to process
    private static SourceReader source;
    // positions in line of current token
    private int startPosition, endPosition, lineNum; 
    boolean flag;

    public Lexer(String sourceFile) throws Exception {
        new TokenType();  // init token table
        source = new SourceReader(sourceFile);
        ch = source.read();
    }


    public static void main(String args[]) {
        Token tok;
        try {
            Lexer lex = new Lexer(args[0]);
            while (true) {
                tok = lex.nextToken();
                String p = "" + TokenType.tokens.get(tok.getKind()) + " ";
                if ((tok.getKind() == Tokens.Identifier) ||
                    (tok.getKind() == Tokens.INTeger) ||
                    (tok.getKind() == Tokens.Float) ||
                    (tok.getKind() == Tokens.ScientificN) ||
                    (tok.getKind() == Tokens.Char) ||
                    (tok.getKind() == Tokens.String)) 
                    p += tok.toString();
                p += "\tL: " + tok.getLeftPosition() +
                   "\tR: " + tok.getRightPosition() + "\tLine: " + lex.source.getLineno() + "\t";                                      
                System.out.println(p);
            }
        } catch (Exception e) {}     
        System.out.println("\n");
        source.printFile();
    }
  
 
/**
 *  newIdTokens are either ids or reserved words; new id's will be inserted
 *  in the symbol table with an indication that they are id's
 *  @param id is the String just scanned - it's either an id or reserved word
 *  @param startPosition is the column in the source file where the token begins
 *  @param endPosition is the column in the source file where the token ends
 *  @return the Token; either an id or one for the reserved words
*/
    public Token newIdToken(String id,int startPosition,int endPosition) {
        return new Token(startPosition,endPosition,Symbol.symbol(id,Tokens.Identifier));
    }

/**
 *  number tokens are inserted in the symbol table; we don't convert the 
 *  numeric strings to numbers until we load the bytecodes for interpreting;
 *  this ensures that any machine numeric dependencies are deferred
 *  until we actually run the program; i.e. the numeric constraints of the
 *  hardware used to compile the source program are not used
 *  @param number is the int String just scanned
 *  @param startPosition is the column in the source file where the intr begins
 *  @param endPosition is the column in the source file where the int ends
 *  @return the number Token
*/
    
    public Token newNumberToken(String number,int startPosition,int endPosition) {
            return new Token(startPosition,endPosition,
                Symbol.symbol(number, Tokens.INTeger));
    }
    
    public Token newFloatToken(String number, int startPosition, int endPosition) {
            return new Token(startPosition,endPosition,
                Symbol.symbol(number,Tokens.FLOAT));        
    }
    
    public Token newSciNumToken(String number, int startPosition, int endPosition) {
        return new Token(startPosition,endPosition,
                Symbol.symbol(number, Tokens.ScientificN));
    }
    
    public Token newCharToken(String ch, int startPosition, int endPosition) {
        return new Token(startPosition,endPosition,
                Symbol.symbol(ch, Tokens.CHAR));
    }
    
    public Token newStrToken(String str, int startPosition, int endPosition) {
        return new Token(startPosition,endPosition,
                Symbol.symbol(str, Tokens.STRING));
    }

/**
 *  build the token for operators (+ -) or separators (parens, braces)
 *  filter out comments which begin with two slashes
 *  @param s is the String representing the token
 *  @param startPosition is the column in the source file where the token begins
 *  @param endPosition is the column in the source file where the token ends
 *  @return the Token just found
*/
    public Token makeToken(String s,int startPosition,int endPosition) {
        if (s.equals("//")) {  // filter comment
            try {
               int oldLine = source.getLineno();
               do {
                   ch = source.read();
               } while (oldLine == source.getLineno());
            } catch (Exception e) {
                    atEOF = true;
            }
            return nextToken();
        }
        Symbol sym = Symbol.symbol(s,Tokens.BogusToken); // be sure it's a valid token
        if (sym == null) {
             System.out.println("******** illegal character: " + s);
             System.out.println("******** Line error: " + lineNum);
             atEOF = true;
        }
        return new Token(startPosition,endPosition,sym);
        }

/**
 *  @return the next Token found in the source file
*/
    
    public Token nextToken() { // ch is always the next char to process
        if (atEOF) {
            if (source != null) {
                source.close();
                source = null;
            }
            return null;
        }
        try {
            while (Character.isWhitespace(ch)) {  // scan past whitespace
                ch = source.read();
            }
        } catch (Exception e) {
            atEOF = true;
            return nextToken();
        }
        startPosition = source.getPosition();
        endPosition = startPosition - 1;
        lineNum = source.getLineno();

        if (Character.isJavaIdentifierStart(ch)) {
            // return tokens for ids and reserved words
            String id = "";
            try {
                do {
                    endPosition++;
                    id += ch;
                    ch = source.read();
                } while (Character.isJavaIdentifierPart(ch));
            } catch (Exception e) {                
                atEOF = true;               
            }
            return newIdToken(id,startPosition,endPosition);
        }
        if (Character.isDigit(ch)) {
            // return number tokens checking for floats, scientificN, and digits
            boolean fl = false, sciNum = false, digit = false;
            String number = "";
            try {
                endPosition++;
                number += ch;
                ch = source.read();
                if (Character.isDigit(ch)) { // second char is digit
                    do {
                        endPosition++;
                        number += ch;
                        ch = source.read();
                    } while (Character.isDigit(ch));
                    if (ch == '.') {
                        do {
                            endPosition++;
                            number += ch;
                            ch = source.read();
                        } while (Character.isDigit(ch));
                        fl = true;
                    } else {
                        digit = true;
                    }
                } else if (ch == '.') { // second char is '.'
                    do {
                        endPosition++;
                        number += ch;
                        ch = source.read();
                    } while (Character.isDigit(ch));
                    if (ch =='e' || ch == 'E') { // checks for scientific notation
                        endPosition++;
                        number += ch;
                        ch = source.read();
                        if (ch == '+' || ch == '-') {
                            endPosition++;
                            number += ch;
                            ch = source.read();                
                            if (Character.isDigit(ch)) {
                                do {
                                    endPosition++;
                                    number += ch;
                                    ch = source.read();
                                } while (Character.isDigit(ch));
                                sciNum = true;
                            } else {
                                System.out.println("Digit error on line : " + lineNum);
                                atEOF = true;
                                return nextToken();
                            }
                        } else if (Character.isDigit(ch)) { // if no '+' or '-' after 'e' or 'E'
                            do {
                                endPosition++;
                                number += ch;
                                ch = source.read();
                            } while (Character.isDigit(ch));
                            sciNum = true;
                        } else {
                            System.out.println("Digit error on line: " + lineNum);
                            atEOF = true;
                            return nextToken();
                        }
                    } else {
                        fl = true;
                    }
                } else {
                    digit = true; 
                }
            } catch (Exception e) {
                atEOF = true;
            }
            if (sciNum == true) {
                return newSciNumToken(number,startPosition,endPosition);
            } else if (fl == true) {
                return newFloatToken(number,startPosition,endPosition);
            } else if (digit == true) {
                return newNumberToken(number,startPosition,endPosition);
            }      
        }
        
        if (ch == '.') { // check for digit after '.'             
            boolean isFloat = false;
            String number = "";
            try {
                do {                 
                    endPosition++;
                    number += ch;
                    ch = source.read();
                    if ((ch == 'e' || ch == 'E' || ch == '.' || ch == ' ') && isFloat == false) {
                        System.out.println("Float error on line: " + lineNum);
                        atEOF = true;
                        return nextToken();
                    } else {
                        isFloat = true; // float exists
                    }
                } while (Character.isDigit(ch));
            } catch (Exception e) {
                atEOF = true;
            }
            return newFloatToken(number,startPosition,endPosition);
        }
        /*
        if (ch == '\'') { // checks for char token
            boolean isChar = false;
            String tempChar = "";
            try {                
                endPosition++;
                tempChar += ch;
                ch = source.read();
                if (ch == '\'') {
                    System.out.println("Char error on line: " + lineNum);
                    atEOF = true;
                    return nextToken();
                } else {
                    endPosition++;
                    tempChar += ch;
                    ch = source.read();
                    if (ch == '\'') {
                        endPosition++;
                        tempChar += ch;
                        ch = source.read();
                        isChar = true;
                    } else {
                        System.out.println("Missing single quote on line: " + lineNum);
                        atEOF = true;
                        return nextToken();
                    }
                }
                
            } catch (Exception e) {
                atEOF = true;
            }
            if (isChar == true) {
                return newCharToken(tempChar,startPosition,endPosition);
            }
        }*/
        /*if (ch == '"') { // checks for string token
            boolean isStr = false;
            String str = "";
            try {
                endPosition++;
                str += ch;
                ch += source.read();
                if (ch == '"') {
                    System.out.println("String error on line: " + lineNum);
                    atEOF = true;
                    return nextToken();
                } else {
                    while (source.getNextChar() != '"') { // reads each character
                        endPosition++;
                        str += ch;
                        ch = source.read();
                    }
                    if (source.getNextChar() == '"') {
                        endPosition++;
                        str += ch;
                        ch = source.read();
                        isStr = true;
                    } else {
                        System.out.println("Missing double quote on line: " + lineNum);
                        atEOF = true;
                        return nextToken();
                    }
                    ch = source.read();
                }
            } catch (Exception e) {
                atEOF = true;
            }
            if (isStr == true) {
                return newStrToken(str,startPosition,endPosition);
            }
        }*/
        if (ch == '\'') {
            // return character token - either float or scientificN
            String charString = "";
            try {
                // Character string starts with single quote ' 
                charString = "" + ch;
                endPosition++;
                int oldLine = source.getLineno();  // capture line # of open '
                
                ch = source.read();  // read the single character.
            
                charString += ch;
                endPosition++;
                ch = source.read();  // should be closing single quote. 
                
                charString += ch;  // Add in char that should be single quote.
                endPosition++;  // This code is here to help with error msg. 
                
                // verify closing single quote ' and same line. 
                if ((ch != '\'') || (oldLine != source.getLineno())) 
                    throw new Exception();
                
            } catch (Exception e) {
                System.out.println("******* illegal input: " + charString);
                atEOF = true;
                return nextToken();
            }
            try {                
                // read next character for next Token. 
                ch = source.read();
            } catch (Exception e) {
                atEOF = true;
            }
            return newCharToken(charString,startPosition,endPosition);
        }
                if (ch == '\"') {
            String inputString = "";
            try {
                inputString = "" + ch;
                endPosition++;
                int oldLine = source.getLineno();  // capture line # of open "
                
                ch = source.read();  // read a character
                
                // loop until closing " or switch to new line 
                while ((ch != '\"') && (oldLine == source.getLineno())) {  
                    inputString += ch;
                    endPosition++;
                    ch = source.read();
                }
                // If open " is on different line than close " throw 
                // Exception to print out error message. 
                if (oldLine != source.getLineno())
                    throw new Exception(); 
                inputString += ch;
                endPosition++;
            } catch (Exception e) {
                System.out.println("******* illegal input: " + inputString);
                atEOF = true;
                return nextToken();
            }
            try {  // read next character for next Token. 
                ch = source.read();
            } catch (Exception e) {
                atEOF = true;
            }
            return newStrToken(inputString,startPosition,endPosition);
        }
        
        // At this point the only tokens to check for are one or two
        // characters; we must also check for comments that begin with
        // 2 slashes
        String charOld = "" + ch;
        String op = charOld;
        Symbol sym;
        try {
            endPosition++;
            ch = source.read();
            op += ch;
            // check if valid 2 char operator; if it's not in the symbol
            // table then don't insert it since we really have a one char
            // token
            sym = Symbol.symbol(op, Tokens.BogusToken); 
            if (sym == null) {  // it must be a one char token
                return makeToken(charOld,startPosition,endPosition);
            }
            endPosition++;
            ch = source.read();
            return makeToken(op,startPosition,endPosition);
        } catch (Exception e) {}
        atEOF = true;
        if (startPosition == endPosition) {
            op = charOld;
        }
        return makeToken(op,startPosition,endPosition);
    }
}
