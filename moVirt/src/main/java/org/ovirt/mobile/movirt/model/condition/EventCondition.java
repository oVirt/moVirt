package org.ovirt.mobile.movirt.model.condition;

import android.content.Context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents condition for event trigger
 * Created by Nika on 12.03.2015.
 */
public class EventCondition extends Condition<Event> {
    private final Pattern regexString;

    @JsonCreator
    public EventCondition(@JsonProperty("regexString") Pattern regexString) {
        this.regexString = regexString;
    }

    @Override
    public boolean evaluate(Event entity) {
        Matcher matcher =
                this.regexString.matcher(entity.getDescription());
        return matcher.find();
    }

    @Override
    public String getMessage(Context context, Event entity) {
        return context.getResources().getString(
                R.string.event_trigger_message, this.getRegexString(), entity.getDescription()
        );
    }

    @Override
    public String toString() {
        return "Event matches \"" + this.getRegexString() + "\" regex";
    }

    public String getRegexString() {
        return regexString.toString();
    }
}
