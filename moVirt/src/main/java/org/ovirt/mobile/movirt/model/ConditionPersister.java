package org.ovirt.mobile.movirt.model;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.field.types.LongStringType;
import com.j256.ormlite.support.DatabaseResults;

import org.ovirt.mobile.movirt.util.JsonUtils;

import java.sql.SQLException;

public class ConditionPersister extends BaseDataType {
    private static ConditionPersister instance = new ConditionPersister();

    private ConditionPersister() {
        super(SqlType.LONG_STRING, new Class<?>[0]);
    }

    public static ConditionPersister getSingleton() {
        return instance;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        return JsonUtils.objectToString(javaObject);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return JsonUtils.stringToObject(sqlArg.toString(), Condition.class);
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        return JsonUtils.stringToObject(defaultStr, Condition.class);
    }

    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    @Override
    public Class<?> getPrimaryClass() {
        return Condition.class;
    }


}
