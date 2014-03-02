package org.ovirt.mobile.movirt.model;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;
import com.j256.ormlite.support.DatabaseResults;

import org.ovirt.mobile.movirt.util.JsonUtils;

import java.sql.SQLException;

public class ConditionPersister extends LongStringType {
    private static ConditionPersister instance = new ConditionPersister();

    private ConditionPersister() {
        super(SqlType.LONG_STRING, new Class<?>[] { Condition.class });
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
}
