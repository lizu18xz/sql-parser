// 定于语法规则
grammar LibExpr;

// 词法根
prog:stat+ EOF?;

// 定义声明
stat:expr (NEWLINE)?         # printExpr
    | ID '=' expr (NEWLINE)? # assign
    | NEWLINE                # blank
    ;

// 定义表达式
expr:expr op=('*'|'/') expr # MulDiv
    |expr op=('+'|'-') expr # AddSub
    |'(' expr ')'           # Parens
    |ID                     # Id
    |INT                    # Int
    ;

//////// 定义词法
// 匹配ID
ID     : [a-zA-Z]+ ;
// 匹配INT
INT    : [0-9]+    ;
// 匹配换行符
NEWLINE: '\n'('\r'?);
// 跳过空格、跳格、换行符
WS     : [ \t\n\r]+ -> skip;

//////// 运算符
DIV:'/';
MUL:'*';
ADD:'+';
SUB:'-';
EQU:'=';