package org.ovirt.mobile.movirt.ui.listfragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.ovirt.mobile.movirt.auth.account.AccountRxStore;
import org.ovirt.mobile.movirt.provider.SortOrder;
import org.ovirt.mobile.movirt.ui.listfragment.spinner.SortEntry;
import org.ovirt.mobile.movirt.ui.mvp.DisposablesPresenter;
import org.ovirt.mobile.movirt.util.Disposables;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@EBean
public class BaseListFragmentPresenter extends DisposablesPresenter<BaseListFragmentPresenter, BaseListFragmentContract.View>
        implements BaseListFragmentContract.Presenter {

    private Subject<SortEntry> sortEntry = BehaviorSubject.<SortEntry>create().toSerialized();
    private Subject<SortOrder> sortOrder = BehaviorSubject.<SortOrder>create().toSerialized();

    private SortEntry[] allEntries;

    private SortEntry storedSortEntry;
    private SortOrder storedSortOrder;

    @Bean
    AccountRxStore rxStore;

    @Override
    public BaseListFragmentPresenter initAllSortEntries(SortEntry[] sortEntries) {
        allEntries = sortEntries == null ? new SortEntry[]{} : sortEntries;
        return this;
    }

    @Override
    public BaseListFragmentPresenter initSelection(SortEntry sortEntry, SortOrder sortOrder) {
        storedSortEntry = sortEntry;
        storedSortOrder = sortOrder;
        return this;
    }

    @Override
    public BaseListFragmentPresenter initialize() {
        super.initialize();

        if (allEntries.length > 0) { // custom sort otherwise
            if (storedSortEntry == null) {
                storedSortEntry = allEntries[0];
            }

            if (storedSortOrder == null) {
                storedSortOrder = allEntries[0].getDefaultSortOrder();
            }
            this.sortEntry.onNext(storedSortEntry);
            this.sortOrder.onNext(storedSortOrder);
            // set before we get call ui
            getView().displaySelection(storedSortEntry, storedSortOrder);

            getDisposables().add(Observable.combineLatest(sortEntry, sortOrder, SortBundle::new)
                    .distinctUntilChanged()
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sortBundle -> {
                        getView().displaySelection(sortBundle.sortEntry, sortBundle.sortOrder);
                        getView().refreshDisplay();
                    }));
        }

        getDisposables().add(rxStore.ACTIVE_SELECTION
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selection -> {
                    getView().refreshDisplay();
                }));
        return this;
    }

    @Override
    public void destroy() {
        super.destroy();
        sortEntry.onComplete();
        sortOrder.onComplete();
    }

    @Override
    public Disposables getDisposables() {
        return super.getDisposables();
    }

    @Override
    public void setSelection(String sortBy, SortOrder sortOrder) {
        for (SortEntry sortEntry : allEntries) {
            if (sortEntry.getItemName().getColumnName().equals(sortBy)) {
                this.sortEntry.onNext(sortEntry);
                this.sortOrder.onNext(sortOrder);
                // simulate click - because we are already receiving callbacks from initialized ui
                getView().displaySelection(sortEntry, sortOrder);
                break;
            }
        }
    }

    @Override
    public void onOrderBySelected(SortEntry sortEntry) {
        if (sortEntry != null && !this.sortEntry.blockingFirst(SortEntry.EMPTY).equals(sortEntry)) {
            this.sortEntry.onNext(sortEntry);
            this.sortOrder.onNext(sortEntry.getDefaultSortOrder());
        }
    }

    @Override
    public void onOrderSelected(SortOrder sortOrder) {
        if (sortOrder != null && this.sortOrder.blockingFirst(null) != sortOrder) {
            this.sortOrder.onNext(sortOrder);
        }
    }

    private class SortBundle {
        SortEntry sortEntry;
        SortOrder sortOrder;

        public SortBundle(SortEntry sortEntry, SortOrder sortOrder) {
            this.sortEntry = sortEntry;
            this.sortOrder = sortOrder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SortBundle)) return false;

            SortBundle that = (SortBundle) o;

            if (sortEntry != null ? !sortEntry.equals(that.sortEntry) : that.sortEntry != null)
                return false;
            return sortOrder == that.sortOrder;
        }

        @Override
        public int hashCode() {
            int result = sortEntry != null ? sortEntry.hashCode() : 0;
            result = 31 * result + (sortOrder != null ? sortOrder.hashCode() : 0);
            return result;
        }
    }
}
