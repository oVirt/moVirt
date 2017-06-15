package org.ovirt.mobile.movirt.util;

import org.ovirt.mobile.movirt.rest.dto.common.HasId;
import org.springframework.util.StringUtils;

public class IdHelper {
    public static final char SEPARATOR = '_';

    public static String combinedId(HasId accountId, HasId id) throws IllegalArgumentException {
        if (accountId == null || id == null) {
            throw new IllegalArgumentException("Both ids should not be empty");
        }

        return combinedId(accountId.getId(), id.getId());
    }

    public static String combinedId(String accountId, HasId id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("id should not be empty");
        }

        return combinedId(accountId, id.getId());
    }

    public static String combinedId(String accountId, String id) throws IllegalArgumentException {
        if (StringUtils.isEmpty(accountId) || StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Both ids should not be empty");
        }

        return accountId + SEPARATOR + id;
    }

    public static String combinedIdSafe(String accountId, HasId hasId) throws IllegalArgumentException {
        if (hasId != null && !StringUtils.isEmpty(hasId.getId())) {
            return combinedIdSafe(accountId, hasId.getId());
        }

        return null;
    }

    public static String combinedIdSafe(String accountId, String id) throws IllegalArgumentException {
        if (StringUtils.isEmpty(accountId)) {
            throw new IllegalArgumentException("Main id should not be empty");
        }

        if (id != null) {
            return accountId + SEPARATOR + id;
        }

        return null;
    }

    public static String getAccountIdPart(String combinedId) {
        if (combinedId == null) {
            return "";
        }

        return combinedId.substring(0, combinedId.indexOf(SEPARATOR));
    }

    public static String getIdPart(String combinedId) {
        if (combinedId == null) {
            return "";
        }

        return combinedId.substring(combinedId.indexOf(SEPARATOR) + 1);
    }
}
