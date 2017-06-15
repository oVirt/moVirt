package org.ovirt.mobile.movirt.auth.account.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AllAccounts {
    public static final AllAccounts NO_ACCOUNTS = new AllAccounts(null);

    private final Map<String, MovirtAccount> accounts;

    public static AllAccounts newInstance(Collection<MovirtAccount> accounts) {
        return accounts == null ? NO_ACCOUNTS : new AllAccounts(accounts);
    }

    private AllAccounts(Collection<MovirtAccount> accounts) {
        if (accounts == null) {
            this.accounts = Collections.emptyMap();
        } else {
            final Map<String, MovirtAccount> temp = new HashMap<>();
            for (MovirtAccount account : accounts) {
                temp.put(account.getId(), account);
            }

            this.accounts = Collections.unmodifiableMap(temp);
        }
    }

    public Collection<MovirtAccount> getAccounts() {
        return accounts.values();
    }

    public MovirtAccount getAccountById(String id) {
        return accounts.get(id);
    }

    public int getCount() {
        return accounts.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllAccounts)) return false;

        AllAccounts that = (AllAccounts) o;

        return accounts.equals(that.accounts);
    }

    @Override
    public int hashCode() {
        return accounts.hashCode();
    }

    @Override
    public String toString() {
        return Arrays.toString(getAccounts().toArray());
    }
}
