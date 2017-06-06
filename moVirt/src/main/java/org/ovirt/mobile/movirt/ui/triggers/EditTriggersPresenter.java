package org.ovirt.mobile.movirt.ui.triggers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.auth.account.data.AllAccounts;
import org.ovirt.mobile.movirt.auth.account.data.Selection;
import org.ovirt.mobile.movirt.model.Cluster;
import org.ovirt.mobile.movirt.model.Vm;
import org.ovirt.mobile.movirt.model.base.OVirtAccountNamedEntity;
import org.ovirt.mobile.movirt.model.trigger.Trigger;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.ObjectUtils;
import org.ovirt.mobile.movirt.util.preferences.CommonSharedPreferencesHelper;
import org.ovirt.mobile.movirt.util.resources.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@EBean
public class EditTriggersPresenter extends DisposablesPresenter<EditTriggersPresenter, EditTriggersContract.View>
        implements EditTriggersContract.Presenter {

    private final Subject<SelectedTrigger> selectedTrigger = BehaviorSubject.createDefault(new SelectedTrigger(null)).toSerialized();

    private Selection selection;

    private String entityId;

    @Bean
    CommonSharedPreferencesHelper commonSharedPreferencesHelper;

    @Bean
    ProviderFacade providerFacade;

    @Bean
    AccountRxStore rxStore;

    @Bean
    Resources resources;

    public EditTriggersPresenter setSelection(Selection selection) {
        this.selection = selection;
        return this;
    }

    public EditTriggersPresenter setEntityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    @Override
    public EditTriggersPresenter initialize() {
        ObjectUtils.requireNotNull(selection, "selection");
        super.initialize();
        getView().displayStatus(selection);

        ProviderFacade.QueryBuilder<Trigger> triggersQuery = providerFacade.query(Trigger.class);
        ProviderFacade.QueryBuilder<Cluster> clusterQuery = providerFacade.query(Cluster.class);
        ProviderFacade.QueryBuilder<Vm> vmQuery = providerFacade.query(Vm.class); // just vms for now

        if (selection.isOneAccount()) {
            triggersQuery.where(Trigger.ACCOUNT_ID, selection.getAccountId());
            clusterQuery.where(Cluster.ACCOUNT_ID, selection.getAccountId());
            vmQuery.where(Vm.ACCOUNT_ID, selection.getAccountId());
        }

        if (selection.isCluster()) {
            triggersQuery.where(Trigger.CLUSTER_ID, selection.getClusterId());
            clusterQuery.where(Cluster.ID, selection.getClusterId());
            vmQuery.where(Vm.CLUSTER_ID, selection.getClusterId());
        }

        if (entityId != null) {
            triggersQuery.where(Trigger.TARGET_ID, entityId);
            vmQuery.where(Vm.ID, entityId);
        }

        final Observable<List<Trigger>> triggersObservable = triggersQuery.asObservable();
        final Observable<List<Cluster>> clusterObservable = clusterQuery.asObservable();
        final Observable<List<Vm>> vmObservable = vmQuery.asObservable();

        getDisposables().add(Observable.combineLatest(rxStore.ALL_ACCOUNTS.startWith(AllAccounts.NO_ACCOUNTS), clusterObservable,
                triggersObservable, vmObservable, Wrapper::new)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> getView().showTriggers(wrapper.asViewTriggers(selection, entityId))));

        final Observable<TriggerAndVm> titleInfo;
        if (entityId != null) {
            titleInfo = Observable.combineLatest(
                    vmObservable.filter(vms -> !vms.isEmpty()).map(vms -> vms.iterator().next()),
                    selectedTrigger,
                    TriggerAndVm::new);
        } else {
            titleInfo = selectedTrigger.map(selected -> new TriggerAndVm(null, selected));
        }

        getDisposables().add(titleInfo
                .subscribeOn(Schedulers.computation())
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(wrapper -> {
                    if (wrapper.selectedTrigger.isSelected()) {
                        getView().displayTitle(wrapper.selectedTrigger.trigger.toString());
                    } else {
                        String show = resources.getTriggers();
                        if (wrapper.entity != null) {
                            show += " " + ((OVirtAccountNamedEntity.class.isAssignableFrom(wrapper.entity.getClass())) ?
                                    wrapper.entity.getName() : entityId);
                        }
                        getView().displayTitle(show);
                    }
                }));

        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        selectedTrigger.onComplete();
    }

    @Override
    public void addTrigger() {
        getView().startAddTriggerActivity(selection);
    }

    @Override
    public void triggerSelected(ViewTrigger trigger) {
        selectedTrigger.onNext(new SelectedTrigger(trigger == null ? null : trigger.trigger));
    }

    @Override
    public void triggerClicked(ViewTrigger trigger) {
        getView().startEditTriggerActivity(trigger);
    }

    @Override
    public void deleteTrigger(ViewTrigger trigger) {
        if (trigger != null) {
            providerFacade.delete(trigger.trigger);
        }
    }

    private class Wrapper {
        final AllAccounts accounts;
        final List<Cluster> clusters;
        final List<Trigger> triggers;
        final List<Vm> vms;

        Wrapper(AllAccounts accounts, List<Cluster> clusters, List<Trigger> triggers, List<Vm> vms) {
            this.accounts = accounts;
            this.clusters = clusters;
            this.triggers = triggers;
            this.vms = vms;
        }

        List<ViewTrigger> asViewTriggers(Selection selection, String entityId) {
            List<ViewTrigger> result = new ArrayList<>(triggers.size());
            Map<String, Cluster> clusterMap = new HashMap<>(clusters.size());
            Map<String, Vm> vmMap = new HashMap<>(vms.size());

            for (Cluster cluster : clusters) {
                clusterMap.put(cluster.getId(), cluster);
            }

            for (Vm vm : vms) {
                vmMap.put(vm.getId(), vm);
            }

            for (Trigger trigger : triggers) {
                Cluster cluster = clusterMap.get(trigger.getClusterId());
                Vm vm = vmMap.get(trigger.getTargetId());

                String vmName = vm == null ? null : vm.getName();

                boolean highlight = ObjectUtils.equals(trigger.getAccountId(), selection.getAccountId()) &&
                        ObjectUtils.equals(trigger.getClusterId(), selection.getClusterId()) &&
                        ObjectUtils.equals(trigger.getTargetId(), entityId);

                result.add(new ViewTrigger(trigger,
                        highlight,
                        accounts.getAccountById(trigger.getAccountId()),
                        cluster,
                        vmName));
            }

            Collections.sort(result);

            return result;
        }
    }
}
