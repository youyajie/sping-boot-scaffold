package com.yyj.springbootscaffold.mybatis.query;

import java.util.regex.Pattern;

/**
 * Created by yyj on 2018/12/20.
 */
public enum QueryOperatorEnum {
    none("none", "none", "none") // 表示不支持所有操作
    , all("all", "all", "all") // 表示支持所有操作
    , lt("lt", "<", "小于")
    , lte("lte", "<=", "小于等于")
    , gt("gt", ">", "大于等于")
    , gte("gte", ">=", "大于等于")
    , eq("eq", "=", "等于")
    , ne("ne", "!=", "不等于")
    , in("in", "in", "在xx之间")
    , nn("nn", "is not null", "不是空")
    , isnull("isnull", "is null", "不是空")
    , contains("contains", "like", "模糊匹配")
    , containsList("containsList", "containsList", "模糊匹配");
    private String op;
    private String desc;
    private String dbOp;

    static Pattern pattern = Pattern.compile("(_){1,2}(eq|lt|lte|gt|gte||ne|in|isnull|nn|contains|containsList)$");
    QueryOperatorEnum(String op, String dbOp, String desc) {
        this.op = op;
        this.desc = desc;
        this.dbOp = dbOp;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDbOp() {
        return dbOp;
    }

    public void setDbOp(String dbOp) {
        this.dbOp = dbOp;
    }

    public static Pattern getAllOperationPattern() {
        return pattern;
    }
}

