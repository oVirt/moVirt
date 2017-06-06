package org.ovirt.mobile.movirt.ui.events;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.androidannotations.annotations.EFragment;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.model.Event;
import org.ovirt.mobile.movirt.provider.OVirtContract;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.listfragment.MultipleFacadeBaseListFragment;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.ItemName;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortOrderType;

import static org.ovirt.mobile.movirt.provider.OVirtContract.Event.DESCRIPTION;

@EFragment(R.layout.fragment_base_entity_list)
public class EventsFragment extends MultipleFacadeBaseListFragment<Event> {

    private TextView lastSelectedTextView = null;

    public EventsFragment() {
        super(Event.class);
    }

    protected CursorAdapter createCursorAdapter() {
        return new EventsCursorAdapter(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lastSelectedTextView = null;
    }

    @Override
    protected SortEntry[] getSortEntries() {
        return new SortEntry[]{
                new SortEntry(new ItemName(OVirtContract.Event.TIME), SortOrderType.OLDEST_TO_LATEST, SortOrder.DESCENDING),
                new SortEntry(new ItemName(OVirtContract.Event.SEVERITY), SortOrderType.A_TO_Z),
                new SortEntry(new ItemName(OVirtContract.Event.DESCRIPTION), SortOrderType.A_TO_Z)
        };
    }

    @Override
    protected String searchBy() {
        return DESCRIPTION;
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return (parent, view, position, id) -> {
            if (view != lastSelectedTextView) { // selects different part of the same view
                deselectLastTextView();
            }
            if (view instanceof TextView) {
                lastSelectedTextView = (TextView) view;
            }
            return false;
        };
    }

    @Override
    protected void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            deselectLastTextView();
        }
    }

    // discard selected (for copy-paste) event to prevent visual bugs when scrolling
    private void deselectLastTextView() {
        if (lastSelectedTextView != null && lastSelectedTextView.hasSelection()) {
            lastSelectedTextView.setTextIsSelectable(false);
            lastSelectedTextView.setTextIsSelectable(true);
        }
    }
}
