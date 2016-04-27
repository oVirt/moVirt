package org.ovirt.mobile.movirt.ui.triggers;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.condition.Condition;
import org.ovirt.mobile.movirt.model.trigger.Trigger;

@EActivity(R.layout.activity_base_trigger)
@OptionsMenu(R.menu.add_trigger)
public class AddTriggerActivity extends BaseTriggerActivity {

    @OptionsItem(R.id.action_add_trigger)
    public void addTrigger() {
        Trigger trigger = new Trigger<>();
        final Condition condition = getCondition();
        if (condition == null) {
            return;
        }
        trigger.setTargetId(getTargetEntityId());
        trigger.setEntityType(getEntityType(condition));
        trigger.setCondition(condition);
        trigger.setScope(getTriggerScope());
        trigger.setNotificationType(getNotificationType());

        provider.insert(trigger);
        finish();
    }
}
