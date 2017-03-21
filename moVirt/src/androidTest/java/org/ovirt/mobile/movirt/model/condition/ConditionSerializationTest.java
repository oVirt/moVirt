package org.ovirt.mobile.movirt.model.condition;

import junit.framework.TestCase;

import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.util.JsonUtils;

public class ConditionSerializationTest extends TestCase {

    public void testCpuCondition() {
        CpuThresholdCondition condition = new CpuThresholdCondition(42);
        CpuThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getPercentageLimit(), condition2.getPercentageLimit());
    }

    public void testMemoryCondition() {
        MemoryThresholdCondition condition = new MemoryThresholdCondition(42);
        MemoryThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getPercentageLimit(), condition2.getPercentageLimit());
    }

    public void testStatusCondition() {
        StatusCondition condition = new StatusCondition(VmStatus.DOWN);
        StatusCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getStatus(), condition2.getStatus());
    }

    @SuppressWarnings("unchecked")
    private <T extends Condition> T serializationTransform(Condition condition) {
        return (T) JsonUtils.stringToObject(JsonUtils.objectToString(condition), Condition.class);
    }
}
