package org.ovirt.mobile.movirt.ui.triggers;

import org.androidannotations.annotations.EActivity;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;

@EActivity(R.layout.activity_base_trigger)
public class AddTriggerActivity extends BaseTriggerActivity {

    @Override
    public void onDone() {
        Trigger trigger = new Trigger();
        final Condition condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setAccountId(getSelection().getAccountId());
        trigger.setClusterId(getSelection().getClusterId());
        trigger.setTargetId(getTargetEntityId());
        trigger.setEntityType(getEntityType(condition));
        trigger.setCondition(condition);
        trigger.setNotificationType(getNotificationType());

        provider.insert(trigger);
        finish();
    }
}
