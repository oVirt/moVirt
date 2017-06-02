package org.ovirt.mobile.movirt.ui.listfragment;

import android.database.Cursor;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.ovirt.mobile.movirt.R;
import org.ovirt.mobile.movirt.facade.intent.EntityIntentResolver;
import org.ovirt.mobile.movirt.facade.intent.IntentResolvers;
import org.ovirt.mobile.movirt.model.base.OVirtEntity;
import org.ovirt.mobile.movirt.model.mapping.EntityMapper;
import org.ovirt.mobile.movirt.provider.ProviderFacade;
import org.springframework.util.StringUtils;

import static org.ovirt.mobile.movirt.provider.OVirtContract.AccountNamedEntity.NAME;

@EFragment(R.layout.fragment_base_entity_list)
public abstract class SearchBaseListFragment<E extends OVirtEntity> extends BaseListFragment<E> {

    @Bean
    protected IntentResolvers intentResolvers;

    protected SearchBaseListFragment(Class<E> clazz) {
        super(clazz);
    }

    @Override
    protected void appendQuery(ProviderFacade.QueryBuilder<E> query) {
        super.appendQuery(query);

        String searchNameString = searchText.getText().toString();
        if (!StringUtils.isEmpty(searchNameString)) {
            query.whereLike(searchBy(), "%" + searchNameString + "%");
        }
    }

    @ItemClick(android.R.id.list)
    protected void itemClicked(Cursor cursor) {
        final EntityIntentResolver<OVirtEntity> resolver = intentResolvers.getResolver(entityClass);
        if (resolver != null) {
            E entity = EntityMapper.forEntity(entityClass).fromCursor(cursor);
            if (resolver.hasIntent(entity)) {
                startActivity(resolver.getDetailIntent(entity, getActivity()));
            }
        }
    }

    protected String searchBy(){
        return NAME;
    }
}

