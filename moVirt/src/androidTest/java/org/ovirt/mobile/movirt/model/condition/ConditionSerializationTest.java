package org.ovirt.mobile.movirt.model.condition;

import junit.framework.TestCase;

import org.ovirt.mobile.movirt.model.enums.VmStatus;
import org.ovirt.mobile.movirt.util.JsonUtils;

public class ConditionSerializationTest extends TestCase {

    public void testCpuCondition() {
        VmCpuThresholdCondition condition = new VmCpuThresholdCondition(42);
        VmCpuThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getPercentageLimit(), condition2.getPercentageLimit());
    }

    public void testMemoryCondition() {
        VmMemoryThresholdCondition condition = new VmMemoryThresholdCondition(42);
        VmMemoryThresholdCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getPercentageLimit(), condition2.getPercentageLimit());
    }

    public void testStatusCondition() {
        VmStatusCondition condition = new VmStatusCondition(VmStatus.DOWN);
        VmStatusCondition condition2 = serializationTransform(condition);

        assertEquals(condition.getStatus(), condition2.getStatus());
    }

    @SuppressWarnings("unchecked")
    private <T extends Condition> T serializationTransform(Condition condition) {
        return (T) JsonUtils.stringToObject(JsonUtils.objectToString(condition), Condition.class);
    }
}
