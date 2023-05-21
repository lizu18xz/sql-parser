package calcite.demo;

import java.util.HashSet;
import java.util.Set;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlInsert;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.util.SqlBasicVisitor;

/**
 * @author lizu
 * @since 2023/5/21
 */
public class SqlParseDemo {

    public static void main(String[] args) {
        String sqlStr = "select * from abc as A  where id>100";
        Config build = SqlParser.configBuilder().build();

        try {
            SqlNode sqlNode = SqlParser.create(sqlStr, build).parseQuery();
            sqlNode.accept(new SqlBasicVisitor<String>() {
                @Override
                public String visit(SqlCall call) {
                    if (call.getKind().equals(SqlKind.SELECT)) {
                        SqlSelect select = (SqlSelect) call;
                        System.out
                            .println("--------------查询列名----------------------------------------");
                        select.getSelectList().forEach(colum -> {
                            if (SqlKind.AS.equals(colum.getKind())) {
                                SqlBasicCall basicCall = (SqlBasicCall) colum;
                                System.out.println(
                                    basicCall.getOperandList().get(0).toString() + " as "
                                        + basicCall.getOperandList().get(1).toString());
                            } else if (SqlKind.IDENTIFIER.equals(colum.getKind())) {
                                System.out.println(colum.toString());
                            }
                        });
                        System.out.println(
                            "--------------From Table Info----------------------------------------");
                        select.getFrom().accept(new SqlBasicVisitor<String>() {
                            @Override
                            public String visit(SqlCall call) {
                                if (call.getKind().equals(SqlKind.JOIN)) {
                                    SqlJoin join = (SqlJoin) call;
                                    System.out
                                        .println("join.getRight:" + join.getRight().toString()
                                            + " ,join.getCondition{}" + join.getCondition()
                                            .toString());
                                    if (!join.getLeft().getKind().equals(SqlKind.JOIN)) {
                                        System.out
                                            .println("join.getLeft:" + join.getLeft().toString());
                                    }
                                }
                                return call.getOperator().acceptCall(this, call);
                            }
                        });
                        System.out
                            .println(
                                "--------------Where  Info-----------------------------------------");
                        if (select.getWhere() != null) {
                            select.getWhere().accept(new SqlBasicVisitor<String>() {
                                @Override
                                public String visit(SqlCall call) {
                                    if (call.getKind().equals(SqlKind.AND) || call.getKind()
                                        .equals(SqlKind.OR)) {
                                        SqlBasicCall sql = (SqlBasicCall) call;
                                        SqlBasicCall left = (SqlBasicCall) sql.getOperandList()
                                            .get(0);
                                        SqlBasicCall right = (SqlBasicCall) sql.getOperandList()
                                            .get(1);
                                        System.out
                                            .println("kind:" + sql.getKind() + ",right:" + right);
                                        if (!left.getKind().equals(SqlKind.AND) && !left.getKind()
                                            .equals(SqlKind.OR)) {
                                            System.out.println("left:" + left);
                                        }
                                    } else {
                                        SqlBasicCall sql = (SqlBasicCall) call;
                                        SqlOperator operator = sql.getOperator();
                                        String operatorName = operator.getName();
                                        SqlIdentifier leftOperands = (SqlIdentifier) sql
                                            .getOperands()[0];

                                        SqlLiteral rightOperand = (SqlLiteral) sql.getOperands()[1];
                                        Object value = rightOperand.getValue();
                                        System.out.println(
                                            leftOperands.names.get(0) + " " + operatorName + " "
                                                + value);

                                    }
                                    return call.getOperator().acceptCall(this, call);
                                }
                            });
                        }

                    }
                    return call.getOperator().acceptCall(this, call);
                }
            });
        } catch (SqlParseException e) {
            e.printStackTrace();
        }


    }


    private static Set extractSourceTableInSelectSql(SqlNode sqlNode, boolean fromOrJoin) {
        if (sqlNode == null) {
            return new HashSet<>();
        }
        final SqlKind sqlKind = sqlNode.getKind();
        if (SqlKind.SELECT.equals(sqlKind)) {
            SqlSelect selectNode = (SqlSelect) sqlNode;
            Set selectList = new HashSet<>(
                extractSourceTableInSelectSql(selectNode.getFrom(), true));
            selectNode.getSelectList().getList().stream().filter(node -> node instanceof SqlCall)
                .forEach(node -> selectList.addAll(extractSourceTableInSelectSql(node, false)));
            selectList.addAll(extractSourceTableInSelectSql(selectNode.getWhere(), false));
            selectList.addAll(extractSourceTableInSelectSql(selectNode.getHaving(), false));
            return selectList;
        }
        if (SqlKind.JOIN.equals(sqlKind)) {
            SqlJoin sqlJoin = (SqlJoin) sqlNode;
            Set joinList = new HashSet<>();
            joinList.addAll(extractSourceTableInSelectSql(sqlJoin.getLeft(), true));
            joinList.addAll(extractSourceTableInSelectSql(sqlJoin.getRight(), true));
            return joinList;
        }

        if (SqlKind.AS.equals(sqlKind)) {
            SqlCall sqlCall = (SqlCall) sqlNode;
            return extractSourceTableInSelectSql(sqlCall.getOperandList().get(0),
                fromOrJoin);

        }
        if (SqlKind.IDENTIFIER.equals(sqlKind)) {
            Set identifierList = new HashSet<>();
            if (fromOrJoin) {
                SqlIdentifier sqlIdentifier = (SqlIdentifier) sqlNode;
                identifierList.add(sqlIdentifier.toString());
            }
            return identifierList;
        }

        Set defaultList = new HashSet<>();
        if (sqlNode instanceof SqlCall) {
            SqlCall call = (SqlCall) sqlNode;
            call.getOperandList()
                .forEach(node -> defaultList
                    .addAll(extractSourceTableInSelectSql(node, false)));
            return defaultList;

        }
        return null;
    }

    private static Set extractSourceTableInInsertSql(SqlNode sqlNode, boolean fromOrJoin) {
        SqlInsert sqlInsert = (SqlInsert) sqlNode;
        Set insertList = new HashSet<>(extractSourceTableInSelectSql(sqlInsert.getSource(), false));
        final SqlNode targetTable = sqlInsert.getTargetTable();
        if (targetTable instanceof SqlIdentifier) {
            insertList.add(((SqlIdentifier) targetTable).toString());
        }
        return insertList;
    }

}
