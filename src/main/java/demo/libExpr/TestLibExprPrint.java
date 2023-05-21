package demo.libExpr;

import demo.g4.LibExprLexer;
import demo.g4.LibExprParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.antlr.v4.runtime.*;

/**
 * 打印语法树
 */
public class TestLibExprPrint {

    // 打印语法树 input -> lexer -> tokens -> parser -> tree -> print
    public static void main(String args[]){
        printTree("/Users/lizu/idea/github/sql-parser/src/main/java/demo/libExpr/testCase.txt");
    }


    /**
     * 打印语法树 input -> lexer -> token -> parser -> tree
     * @param fileName
     */
    private static void printTree(String fileName){
        // 定义输入流
        ANTLRInputStream input = null;

        // 判断文件名是否为空,若不为空，则读取文件内容，若为空，则读取输入流
        if(fileName!=null){
            try{
                input = new ANTLRFileStream(fileName);
            }catch(FileNotFoundException fnfe){
                System.out.println("文件不存在，请检查后重试！");
            }catch(IOException ioe){
                System.out.println("文件读取异常，请检查后重试！");
            }
        }else{
            try{
                input = new ANTLRInputStream(System.in);
            }catch(FileNotFoundException fnfe){
                System.out.println("文件不存在，请检查后重试！");

            }catch(IOException ioe){
                System.out.println("文件读取异常，请检查后重试！");
            }
        }

        // 定义词法规则分析器
        LibExprLexer lexer = new LibExprLexer(input);

        // 生成通用字符流
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 语法解析
        LibExprParser parser = new LibExprParser(tokens);

        // 生成语法树
        ParseTree tree = parser.prog();

        // 打印语法树
        // System.out.println(tree.toStringTree(parser));

        // 生命访问器
        LibExprVisitorImpl visitor = new LibExprVisitorImpl();
        visitor.visit(tree);

    }

}