package org.ovirt.mobile.movirt.model.condition;

import junit.framework.TestCase;

import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.util.JsonUtils;

public class ConditionSerializationTest extends TestCase {

    public void testCpuCondition() {
        CpuThresholdCondition condition = new CpuThresholdCondition(42);
        CpuThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.percentageLimit, condition2.percentageLimit);
    }

    public void testMemoryCondition() {
        MemoryThresholdCondition condition = new MemoryThresholdCondition(42);
        MemoryThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.percentageLimit, condition2.percentageLimit);
    }

    public void testStatusCondition() {
        StatusCondition condition = new StatusCondition(Vm.Status.DOWN);
        StatusCondition condition2 = serializationTransform(condition);

        assertEquals(condition.status, condition2.status);
    }

    @SuppressWarnings("unchecked")
    private <T extends Condition> T serializationTransform(Condition condition) {
        return (T) JsonUtils.stringToObject(JsonUtils.objectToString(condition), Condition.class);
    }
}
