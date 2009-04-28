package org.openbravo.test.javascript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.FunctionNode;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ScriptOrFnNode;
import org.mozilla.javascript.Token;

public class JavaScriptParser {

  private File jsFile = null;
  private ScriptOrFnNode nodeTree = null;
  private StringBuffer details = null;

  public JavaScriptParser() {
  }

  /**
   * Sets the file to parse
   * 
   * @param f
   *          a File object of the JavaScript to parse
   */
  public void setFile(File f) {
    jsFile = f;
    nodeTree = null;
    details = null;
  }

  /**
   * Returns a tree representation of the parsed JavaScript file
   * 
   * @return
   * @throws IOException
   */
  public ScriptOrFnNode parse() throws IOException {

    if (nodeTree == null) {
      Reader reader = new FileReader(jsFile);

      CompilerEnvirons compilerEnv = new CompilerEnvirons();
      ErrorReporter errorReporter = compilerEnv.getErrorReporter();

      Parser parser = new Parser(compilerEnv, errorReporter);

      String sourceURI;

      try {
        sourceURI = jsFile.getCanonicalPath();
      } catch (IOException e) {
        sourceURI = jsFile.toString();
      }

      nodeTree = parser.parse(reader, sourceURI, 1);
    }
    return nodeTree;
  }

  /**
   * Returns a string with the global variables and function definitions
   * 
   * @return
   */
  public String getStringDetails() {
    if (jsFile == null) {
      throw new RuntimeException("You need to specify the file to parse");
    }
    if (details == null) {
      details = new StringBuffer();
      try {
        parse();
      } catch (IOException e) {
        e.printStackTrace();
      }
      for (Node cursor = nodeTree.getFirstChild(); cursor != null; cursor = cursor.getNext()) {
        if (cursor.getType() == Token.FUNCTION) {
          int fnIndex = cursor.getExistingIntProp(Node.FUNCTION_PROP);
          FunctionNode fn = nodeTree.getFunctionNode(fnIndex);
          Iterator<String> iter = null;
          StringBuffer sbParam = new StringBuffer();
          if (fn.getSymbolTable() != null) {
            iter = fn.getSymbolTable().keySet().iterator();
            while (iter.hasNext()) {
              sbParam.append(iter.next());
              sbParam.append(" ");
            }
          }
          details.append("FUNCTION: " + fn.getFunctionName() + " [ " + sbParam + "]\n");
        } else if (cursor.getType() == Token.VAR) {
          Node vn = cursor.getFirstChild();
          details.append("VAR: " + vn.getString() + "\n");
        }
      }
    }
    return details.toString();
  }

  /**
   * Write the details of the js file into a file
   * 
   * @param f
   */
  public void toFile(File fileName) throws IOException {
    if (jsFile == null) {
      throw new RuntimeException("You need to specify the file to parse");
    }
    BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
    out.write(getStringDetails());
    out.close();
  }
}
