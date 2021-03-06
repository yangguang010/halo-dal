package halo.dal.analysis.antlr;

import halo.dal.analysis.SQLExpression;
import halo.dal.analysis.SQLInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLInfoImpl implements SQLInfo {

    private final Map<String, SQLExpression[]> sqlExpressionMap = new HashMap<String, SQLExpression[]>();

    private final Map<String, String> logic_realMap = new HashMap<String, String>();

    public SQLExpression[] getSQLExpressions(String columnName) {
        String lowerColumnName = columnName.toLowerCase();
        SQLExpression[] lowerArr = sqlExpressionMap.get(lowerColumnName);
        int idx = columnName.indexOf(".");
        List<SQLExpression> list = new ArrayList<SQLExpression>();
        if (idx == -1) {
            if (lowerArr == null) {
                return null;
            }
            for (SQLExpression e : lowerArr) {
                list.add(e);
            }
            return list.toArray(new SQLExpression[list.size()]);
        }
        String subName = lowerColumnName.substring(idx + 1);
        SQLExpression[] lowerArr1 = sqlExpressionMap.get(subName);
        if (lowerArr != null) {
            for (SQLExpression e : lowerArr) {
                list.add(e);
            }
        }
        if (lowerArr1 != null) {
            for (SQLExpression e : lowerArr1) {
                list.add(e);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new SQLExpression[list.size()]);
    }

    public void addSQLExpression(String logicTableName,
            SQLExpression sqlExpression) {
        SQLExpression[] sqlExpressions = this.getSQLExpressions(sqlExpression
                .getColumn());
        if (sqlExpressions == null) {
            sqlExpressions = new SQLExpression[] { sqlExpression };
        }
        else {
            int len = sqlExpressions.length + 1;
            SQLExpression[] temp = new SQLExpression[len];
            for (int i = 0; i < sqlExpressions.length; i++) {
                temp[i] = sqlExpressions[i];
            }
            temp[len - 1] = sqlExpression;
            sqlExpressions = temp;
        }
        if (logicTableName == null) {
            sqlExpressionMap.put(sqlExpression.getColumn(), sqlExpressions);
        }
        else {
            sqlExpressionMap.put(logicTableName.toLowerCase() + "."
                    + sqlExpression.getColumn(), sqlExpressions);
        }
    }

    public String getRealTable(String logic) {
        return logic_realMap.get(logic);
    }

    public void setRealTable(String logic, String real) {
        logic_realMap.put(logic, real);
    }
}
