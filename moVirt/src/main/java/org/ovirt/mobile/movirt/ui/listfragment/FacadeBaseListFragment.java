package org.ovirt.mobile.movirt.ui.listfragment;

import android.content.Intent;
import android.database.Cursor;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.EntityFacade;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.ovirt.mobile.movirt.ui.ProgressBarResponse;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.ovirt.mobile.movirt.provider.OVirtContract.NamedEntity.NAME;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class FacadeBaseListFragment<E extends OVirtEntity> extends BaseListFragment<E> {

    protected EntityFacade<E> entityFacade;

    protected FacadeBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @AfterViews
    protected void init2() {
        entityFacade = entityFacadeLocator.getFacade(entityClass);
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        String searchNameString = searchText.getText().toString();
        if (!StringUtils.isEmpty(searchNameString)) {
            query.whereLike(NAME, "%" + searchNameString + "%");
        }
    }

    @ItemClick(android.R.id.list)
    protected void itemClicked(Cursor cursor) {
        if (entityFacade != null) {
            E entity = entityFacade.mapFromCursor(cursor);
            Intent intent = entityFacade.getDetailIntent(entity, getActivity());
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    @Background
    public void onRefresh() {
        if (entityFacade != null) {
            entityFacade.syncAll(new ProgressBarResponse<List<E>>(this));
        }
    }
}

