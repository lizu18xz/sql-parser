package demo.libExpr;

import demo.g4.LibExprBaseVisitor;
import demo.g4.LibExprParser;
import java.util.HashMap;
import java.util.Map;

/**
 * 重写访问器规则，实现数据计算功能
 * 目标：
 *     1+2 => 1+2=3
 *     1+2*4 => 1+2*4=9
 *     1+2*4-5 => 1+2*4-5=4
 *     1+2*4-5+20/5 => 1+2*4-5+20/5=8
 *     (1+2)*4 => (1+2)*4=12
 */
public class LibExprVisitorImpl extends LibExprBaseVisitor<Integer> {
    // 定义数据
    Map<String,Integer> data = new HashMap<String,Integer>();

    // expr (NEWLINE)?         # printExpr
    @Override
    public Integer visitPrintExpr(LibExprParser.PrintExprContext ctx) {
        System.out.println("visitPrintExpr："+ctx.expr().getText()+"="+visit(ctx.expr()));
        return visit(ctx.expr());
    }

    // ID '=' expr (NEWLINE)? # assign
    @Override
    public Integer visitAssign(LibExprParser.AssignContext ctx) {
        // 获取id
        String id = ctx.ID().getText();
        // // 获取value
        int value = Integer.valueOf(visit(ctx.expr()));

        // 缓存ID数据
        data.put(id,value);

        // 打印日志
        System.out.println("visitAssign:"+id+"="+value);

        return value;
    }

    // NEWLINE                # blank
    @Override
    public Integer visitBlank(LibExprParser.BlankContext ctx) {
        return 0;
    }

    // expr op=('*'|'/') expr # MulDiv
    @Override
    public Integer visitMulDiv(LibExprParser.MulDivContext ctx) {
        // 左侧数字
        int left = Integer.valueOf(visit(ctx.expr(0)));
        // 右侧数字
        int right = Integer.valueOf(visit(ctx.expr(1)));
        // 操作符号
        int opType = ctx.op.getType();

        // 调试
        // System.out.println("visitMulDiv>>>>> left:"+left+",opType:"+opType+",right:"+right);

        // 判断是否为乘法
        if(LibExprParser.MUL==opType){
            return left*right;
        }

        // 判断是否为除法
        return left/right;

    }

    // expr op=('+'|'-') expr # AddSub
    @Override
    public Integer visitAddSub(LibExprParser.AddSubContext ctx) {
        // 获取值和符号

        // 左侧数字
        int left = Integer.valueOf(visit(ctx.expr(0)));
        // 右侧数字
        int right = Integer.valueOf(visit(ctx.expr(1)));
        // 操作符号
        int opType = ctx.op.getType();

        // 调试
         System.out.println("visitAddSub>>>>> left:"+left+",opType:"+opType+",right:"+right);

        // 判断是否为加法
        if(LibExprParser.ADD==opType){
            return left+right;
        }

        // 判断是否为减法
        return left-right;

    }

    // '(' expr ')'           # Parens
    @Override
    public Integer visitParens(LibExprParser.ParensContext ctx) {
        // 递归下调
        return visit(ctx.expr());
    }

    // ID                     # Id
    @Override
    public Integer visitId(LibExprParser.IdContext ctx) {
        // 获取id
        String id = ctx.ID().getText();
        // 判断ID是否被定义
        if(data.containsKey(id)){
            // System.out.println("visitId>>>>> id:"+id+",value:"+data.get(id));
            return data.get(id);
        }
        return 0;
    }

    // INT                    # Int
    @Override
    public Integer visitInt(LibExprParser.IntContext ctx) {
        //System.out.println("visitInt>>>>> int:"+ctx.INT().getText());
        return Integer.valueOf(ctx.INT().getText());
    }

}