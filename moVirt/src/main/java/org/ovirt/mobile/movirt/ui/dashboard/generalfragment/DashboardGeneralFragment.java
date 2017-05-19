package org.ovirt.mobile.movirt.ui.dashboard.generalfragment;

import android.content.Intent;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;

import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.ui.PresenterFragment;
import org.ovirt.mobile.movirt.ui.dashboard.generalfragment.resources.UtilizationResource;
import org.ovirt.mobile.movirt.ui.mainactivity.MainActivity;
import org.ovirt.mobile.movirt.ui.mainactivity.MainActivity_;
import org.ovirt.mobile.movirt.util.usage.MemorySize;
import org.ovirt.mobile.movirt.util.usage.UsageResource;

public abstract class DashboardGeneralFragment extends PresenterFragment {

    protected void renderCpuPercentageCircle(PercentageCircleView cpuPercentageCircle, TextView summaryCpuPercentageCircle,
                                             UtilizationResource resource, final StartActivityAction action) {
        renderPercentageCircleView(cpuPercentageCircle, resource);
        renderSummary(summaryCpuPercentageCircle, resource);

        cpuPercentageCircle.setOnTouchListener((v, event) -> {
            boolean nullAction = action == null;
            if (!nullAction && MotionEvent.ACTION_UP == event.getAction() && cpuPercentageCircle.isActivated()) {
                startMainActivity(action);
            }
            return nullAction;
        });
    }

    protected void renderMemoryPercentageCircle(PercentageCircleView memoryPercentageCircle, TextView summaryMemoryPercentageCircle,
                                                UtilizationResource resource, final StartActivityAction action) {
        renderPercentageCircleView(memoryPercentageCircle, resource);
        renderSummary(summaryMemoryPercentageCircle, resource);

        memoryPercentageCircle.setOnTouchListener((v, event) -> {
            boolean nullAction = action == null;
            if (!nullAction && MotionEvent.ACTION_UP == event.getAction() && memoryPercentageCircle.isActivated()) {
                startMainActivity(action);
            }
            return nullAction;
        });
    }

    protected void renderStoragePercentageCircle(PercentageCircleView storagePercentageCircle, TextView summaryStoragePercentageCircle,
                                                 UtilizationResource resource, final StartActivityAction action) {
        renderPercentageCircleView(storagePercentageCircle, resource);
        renderSummary(summaryStoragePercentageCircle, resource);

        storagePercentageCircle.setOnTouchListener((v, event) -> {
            boolean nullAction = action == null;
            if (!nullAction && MotionEvent.ACTION_UP == event.getAction() && storagePercentageCircle.isActivated()) {
                startMainActivity(action);
            }
            return nullAction;
        });
    }

    private void renderPercentageCircleView(PercentageCircleView circleView, UtilizationResource resource) {
        circleView.setMaxResource(resource.getTotal());
        circleView.setUsedResource(resource.getUsed());

        String resourceDescription = resource.getTotal() instanceof MemorySize ?
                getString(R.string.unit_used, resource.getTotal().getReadableUnitAsString()) : getString(R.string.used);
        circleView.setUsedResourceDescription(resourceDescription);
    }

    private void renderSummary(TextView textView, UtilizationResource resource) {
        UsageResource totalResource = resource.getTotal();
        String totalText = totalResource.getReadableValueAsString();
        String totalUnit = totalResource.getReadableUnitAsString();
        String availableText;
        String summary;

        if (totalResource instanceof MemorySize) {
            MemorySize totalMemoryResource = (MemorySize) resource.getTotal();
            MemorySize availableMemoryResource = (MemorySize) resource.getAvailable();

            availableText = availableMemoryResource.getReadableValueAsString(totalMemoryResource.getReadableUnit());
            summary = getString(R.string.summary_mem_available_of, availableText, totalText, totalUnit);
        } else {
            availableText = resource.getAvailable().toString();
            summary = getString(R.string.summary_cpu_available_of, availableText, totalText, totalUnit);
        }

        textView.setText(summary);

        // compute size of the text based on the string length,
        int stringLength = availableText.length() + totalText.length() + totalUnit.length();
        int textLength;

        // auto-resizing hack
        switch (stringLength) {
            // 15 is maximum stringLength
            case 15:
            case 14:
            case 13:
                textLength = 12;
                break;
            case 12:
            case 11:
                textLength = 13;
                break;
            // other lengths can be displayed with default size 14sp
            default:
                textLength = 14;
                break;
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textLength);
    }

    private void startMainActivity(StartActivityAction action) {
        Intent intent = new Intent(getActivity(), MainActivity_.class);
        intent.setAction(action.getFragment().name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.Extras.FRAGMENT.name(), action.getFragment());
        intent.putExtra(MainActivity.Extras.ORDER_BY.name(), action.getOrderBy());
        intent.putExtra(MainActivity.Extras.ORDER.name(), action.getOrder());
        startActivity(intent);
    }
}
