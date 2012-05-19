package unittest;

import halo.dal.analysis.PartitionTableInfo;
import halo.dal.analysis.SQLAnalyzer;
import halo.dal.analysis.SQLInfo;
import halo.dal.analysis.SQLStruct;
import halo.dal.analysis.def.CachedSQLAnalyzer;
import halo.dal.analysis.def.DefSQLAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class SQLAnalyzerTest {

    SQLAnalyzer sqlAnalyzer = new CachedSQLAnalyzer(new DefSQLAnalyzer());

    // SQLAnalyzer sqlAnalyzer = new DefSQLAnalyzer();
    Map<String, Object> context;

    @Before
    public void before() {
        context = new HashMap<String, Object>();
    }

    @Test
    public void delete() {
        String sql = "delete from user where uid=? and (age>=? or age<=?) and (sex=? or sex=?) and time<=sysdate()";
        Object[] values = new Object[] { 1, 50, 10, 1, 2 };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(sql, sql2);
        // no where
        sql = "delete from user";
        sqlStruct = sqlAnalyzer.parse(sql, context);
        sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, null, context);
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(sql, sql2);
    }

    @Test
    public void delete2() {
        String sql = "delete from user where uid=? and (age>=? or age<=?) and (sex=? or sex=?) and time<=sysdate()";
        Object[] values = new Object[] { 1, 50, 10, 1, 2 };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("user", "user1");
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user1", partitionTableInfo.getRealTable("user"));
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(
                "delete from user1 where uid=? and (age>=? or age<=?) and (sex=? or sex=?) and time<=sysdate()",
                sql2);
        // no where
        sql = "delete from user";
        sqlStruct = sqlAnalyzer.parse(sql, context);
        sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, null, context);
        partitionTableInfo.setRealTable("user", "user2");
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        Assert.assertEquals("user2", partitionTableInfo.getRealTable("user"));
        sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals("delete from user2", sql2);
    }

    @Test
    public void insert() {
        String sql = "insert into user(userid,nickname,sex) vlaues(?,?,?)";
        Object[] values = new Object[] { 4, "jack", 22 };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        Assert.assertEquals(sqlStruct.getTableNames().size(), 1);
        Assert.assertEquals(sqlStruct.getTableNames().get(0), "user");
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(sql, sql2);
        Assert.assertEquals("userid",
                sqlInfo.getSQLExpressions("user.userid")[0].getColumn());
        Assert.assertEquals(4,
                sqlInfo.getSQLExpressions("user.userid")[0].getValue());
        Assert.assertEquals("nickname",
                sqlInfo.getSQLExpressions("user.nickname")[0].getColumn());
        Assert.assertEquals("jack",
                sqlInfo.getSQLExpressions("user.nickname")[0].getValue());
        Assert.assertEquals("sex",
                sqlInfo.getSQLExpressions("user.sex")[0].getColumn());
        Assert.assertEquals(22,
                sqlInfo.getSQLExpressions("user.sex")[0].getValue());
    }

    @Test
    public void insert2() {
        String sql = "insert into user(userid,nickname,sex) vlaues(?,?,?)";
        Object[] values = new Object[] { 4, "jack", 22 };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("user", "user2");
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        Assert.assertEquals("user2", partitionTableInfo.getRealTable("user"));
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(
                "insert into user2(userid,nickname,sex) vlaues(?,?,?)", sql2);
        Assert.assertEquals("userid",
                sqlInfo.getSQLExpressions("user.userid")[0].getColumn());
        Assert.assertEquals(4,
                sqlInfo.getSQLExpressions("user.userid")[0].getValue());
        Assert.assertEquals("nickname",
                sqlInfo.getSQLExpressions("user.nickname")[0].getColumn());
        Assert.assertEquals("jack",
                sqlInfo.getSQLExpressions("user.nickname")[0].getValue());
        Assert.assertEquals("sex",
                sqlInfo.getSQLExpressions("user.sex")[0].getColumn());
        Assert.assertEquals(22,
                sqlInfo.getSQLExpressions("user.sex")[0].getValue());
    }

    @Test
    public void update() {
        String sql = "update user set uid=? , o=? where name=?";
        Object[] values = new Object[] { 1, 3, "43" };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        Assert.assertEquals(sqlStruct.getTableNames().size(), 1);
        Assert.assertEquals(sqlStruct.getTableNames().get(0), "user");
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(sql, sql2);
        Assert.assertEquals(
                sqlInfo.getSQLExpressions("user.uid")[0].getValue(), 1);
        Assert.assertEquals(sqlInfo.getSQLExpressions("user.o")[0].getValue(),
                3);
        Assert.assertEquals(
                sqlInfo.getSQLExpressions("user.name")[0].getValue(), "43");
    }

    @Test
    public void update2() {
        String sql = "update user set uid=? , o=? where name=?";
        Object[] values = new Object[] { 1, 3, "43" };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("user", "user2");
        Assert.assertEquals(1, sqlStruct.getTableNames().size());
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        Assert.assertEquals("user2", partitionTableInfo.getRealTable("user"));
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals("update user2 set uid=? , o=? where name=?", sql2);
        Assert.assertEquals(
                sqlInfo.getSQLExpressions("user.uid")[0].getValue(), 1);
        Assert.assertEquals(sqlInfo.getSQLExpressions("user.o")[0].getValue(),
                3);
        Assert.assertEquals(
                sqlInfo.getSQLExpressions("user.name")[0].getValue(), "43");
    }

    @Test
    public void select() {
        String sql = "select * from user u,member as m "
                + "where u.uid=m.uid and u.sex=? or m.age>? " + "group by sex "
                + "order by sex " + "having name=?";
        Object[] values = new Object[] { 1, 5, "akwei" };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        Assert.assertEquals("member", sqlStruct.getTableNames().get(1));
        Assert.assertEquals(1,
                sqlInfo.getSQLExpressions("user.sex")[0].getValue());
        Assert.assertEquals(5,
                sqlInfo.getSQLExpressions("member.age")[0].getValue());
    }

    @Test
    public void selectForIn() {
        String sql = "select * from user u,member as m "
                + "where u.uid=m.uid and u.sex=? or m.age>? id in(1,2,3)"
                + "group by sex " + "order by sex " + "having name=?";
        Object[] values = new Object[] { 1, 5, "akwei" };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(sql, sql2);
    }

    @Test
    public void selectNoWhere() {
        String sql = "select * from user";
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, null, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("user", "user2");
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals("select * from user2", sql2);
    }

    @Test
    public void select2() {
        String sql = "select * from user u,member as m "
                + "where u.uid=m.uid and u.sex=? or m.age>? " + "group by sex "
                + "order by sex " + "having name=?";
        Object[] values = new Object[] { 1, 5, "akwei" };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("user", "user2");
        partitionTableInfo.setRealTable("member", "member5");
        Assert.assertEquals("user", sqlStruct.getTableNames().get(0));
        Assert.assertEquals("user2", partitionTableInfo.getRealTable("user"));
        Assert.assertEquals("member", sqlStruct.getTableNames().get(1));
        Assert.assertEquals("member5",
                partitionTableInfo.getRealTable("member"));
        Assert.assertEquals(1,
                sqlInfo.getSQLExpressions("user.sex")[0].getValue());
        Assert.assertEquals(5,
                sqlInfo.getSQLExpressions("member.age")[0].getValue());
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals("select * from user2 u,member5 as m "
                + "where u.uid=m.uid and u.sex=? or m.age>? " + "group by sex "
                + "order by sex " + "having name=?", sql2);
    }

    @Test
    public void selectForHibernate() {
        String sql = "select gatewayeve0_.ID as ID1_, gatewayeve0_.ADAPTER_ID as ADAPTER2_1_, "
                + "gatewayeve0_.ADAPTER_MEMO as ADAPTER3_1_, gatewayeve0_.ADAPTER_NAME as ADAPTER4_1_, "
                + "gatewayeve0_.CREATETIME as CREATETIME1_, gatewayeve0_.END_DATE as END6_1_, "
                + "gatewayeve0_.EVENT_ID as EVENT7_1_, gatewayeve0_.EVENT_STATUS as EVENT8_1_, "
                + "gatewayeve0_.EVENT_TYPE as EVENT9_1_, gatewayeve0_.LASTUPDTIME as LASTUPD10_1_, "
                + "gatewayeve0_.MERCHANT_ID as MERCHANT11_1_, gatewayeve0_.MERCHANT_NAME as MERCHANT12_1_, "
                + "gatewayeve0_.NAME as NAME1_, gatewayeve0_.OPRID as OPRID1_, "
                + "gatewayeve0_.START_DATE as START15_1_ "
                + "from gateway_event gatewayeve0_ "
                + "where 1=1 and gatewayeve0_.EVENT_STATUS=?";
        Object[] values = new Object[] { 5 };
        SQLStruct sqlStruct = sqlAnalyzer.parse(sql, context);
        SQLInfo sqlInfo = sqlAnalyzer.analyse(sql, sqlStruct, values, context);
        PartitionTableInfo partitionTableInfo = new PartitionTableInfo();
        partitionTableInfo.setRealTable("gateway_event", "gateway_event1");
        Assert.assertEquals("gateway_event", sqlStruct.getTableNames().get(0));
        Assert.assertEquals(5, sqlInfo
                .getSQLExpressions("gateway_event.EVENT_STATUS")[0].getValue());
        String sql2 = sqlAnalyzer.outPutSQL(sql, sqlStruct, sqlInfo,
                partitionTableInfo);
        Assert.assertEquals(
                "select gatewayeve0_.ID as ID1_, gatewayeve0_.ADAPTER_ID as ADAPTER2_1_, "
                        + "gatewayeve0_.ADAPTER_MEMO as ADAPTER3_1_, gatewayeve0_.ADAPTER_NAME as ADAPTER4_1_, "
                        + "gatewayeve0_.CREATETIME as CREATETIME1_, gatewayeve0_.END_DATE as END6_1_, "
                        + "gatewayeve0_.EVENT_ID as EVENT7_1_, gatewayeve0_.EVENT_STATUS as EVENT8_1_, "
                        + "gatewayeve0_.EVENT_TYPE as EVENT9_1_, gatewayeve0_.LASTUPDTIME as LASTUPD10_1_, "
                        + "gatewayeve0_.MERCHANT_ID as MERCHANT11_1_, gatewayeve0_.MERCHANT_NAME as MERCHANT12_1_, "
                        + "gatewayeve0_.NAME as NAME1_, gatewayeve0_.OPRID as OPRID1_, "
                        + "gatewayeve0_.START_DATE as START15_1_ "
                        + "from gateway_event1 gatewayeve0_ "
                        + "where 1=1 and gatewayeve0_.EVENT_STATUS=?", sql2);
    }

    @Test
    public void performance() {
        long begin = System.currentTimeMillis();
        String sql = "select gatewayeve0_.ID as ID1_, gatewayeve0_.ADAPTER_ID as ADAPTER2_1_, "
                + "gatewayeve0_.ADAPTER_MEMO as ADAPTER3_1_, gatewayeve0_.ADAPTER_NAME as ADAPTER4_1_, "
                + "gatewayeve0_.CREATETIME as CREATETIME1_, gatewayeve0_.END_DATE as END6_1_, "
                + "gatewayeve0_.EVENT_ID as EVENT7_1_, gatewayeve0_.EVENT_STATUS as EVENT8_1_, "
                + "gatewayeve0_.EVENT_TYPE as EVENT9_1_, gatewayeve0_.LASTUPDTIME as LASTUPD10_1_, "
                + "gatewayeve0_.MERCHANT_ID as MERCHANT11_1_, gatewayeve0_.MERCHANT_NAME as MERCHANT12_1_, "
                + "gatewayeve0_.NAME as NAME1_, gatewayeve0_.OPRID as OPRID1_, "
                + "gatewayeve0_.START_DATE as START15_1_ "
                + "from gateway_event gatewayeve0_ "
                + "where 1=1 and gatewayeve0_.EVENT_STATUS=? or gatewayeve0_.NAME =? and "
                + "(gatewayeve0_.EVENT_ID>=? and gatewayeve0_.EVENT_ID<=?) "
                + "order by gatewayeve0_.EVENT_TYPE desc";
        for (int i = 0; i < 1000 * 1000; i++) {
            this.parse(sql, null);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - begin);
    }

    @Test
    public void multperformance() {
        ExecutorService executorService = Executors.newFixedThreadPool(600,
                new ThreadFactory() {

                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("thread " + t.getId());
                        t.setDaemon(false);
                        return t;
                    }
                });
        final String sql = "select gatewayeve0_.ID as ID1_, gatewayeve0_.ADAPTER_ID as ADAPTER2_1_, "
                + "gatewayeve0_.ADAPTER_MEMO as ADAPTER3_1_, gatewayeve0_.ADAPTER_NAME as ADAPTER4_1_, "
                + "gatewayeve0_.CREATETIME as CREATETIME1_, gatewayeve0_.END_DATE as END6_1_, "
                + "gatewayeve0_.EVENT_ID as EVENT7_1_, gatewayeve0_.EVENT_STATUS as EVENT8_1_, "
                + "gatewayeve0_.EVENT_TYPE as EVENT9_1_, gatewayeve0_.LASTUPDTIME as LASTUPD10_1_, "
                + "gatewayeve0_.MERCHANT_ID as MERCHANT11_1_, gatewayeve0_.MERCHANT_NAME as MERCHANT12_1_, "
                + "gatewayeve0_.NAME as NAME1_, gatewayeve0_.OPRID as OPRID1_, "
                + "gatewayeve0_.START_DATE as START15_1_ "
                + "from gateway_event gatewayeve0_ "
                + "where 1=1 and gatewayeve0_.EVENT_STATUS=? or gatewayeve0_.NAME =? and "
                + "(gatewayeve0_.EVENT_ID>=? and gatewayeve0_.EVENT_ID<=?) "
                + "order by gatewayeve0_.EVENT_TYPE desc";
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
        for (int i = 0; i < 1000 * 1000; i++) {
            Callable<Boolean> task = new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    parse(sql, null);
                    return true;
                }
            };
            tasks.add(task);
        }
        try {
            long begin = System.currentTimeMillis();
            executorService.invokeAll(tasks);
            long end = System.currentTimeMillis();
            System.out.println(end - begin);
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void parse(String sql, Object[] values) {
        SQLAnalyzer analyzer = sqlAnalyzer;
        analyzer.parse(sql, context);
    }
}
