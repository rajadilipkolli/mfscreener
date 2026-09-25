package com.learning.mfscreener.repository.util;

import com.learning.mfscreener.entities.UserCASDetailsEntity;
import com.learning.mfscreener.entities.UserFolioDetailsEntity;
import com.learning.mfscreener.entities.UserSchemeDetailsEntity;
import com.learning.mfscreener.entities.UserTransactionDetailsEntity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reconnects separately fetched, detached CAS entities before a repeat import is merged. */
public final class UserCasEntityGraphBuilder {

    private UserCasEntityGraphBuilder() {}

    public static UserCASDetailsEntity rebuild(
            UserCASDetailsEntity cas,
            List<UserSchemeDetailsEntity> schemes,
            List<UserTransactionDetailsEntity> transactions) {
        Map<Long, UserFolioDetailsEntity> foliosById = new LinkedHashMap<>();
        for (UserFolioDetailsEntity folio : cas.getFolioEntities()) {
            foliosById.putIfAbsent(folio.getId(), folio);
        }
        cas.setFolioEntities(new ArrayList<>(foliosById.values()));
        foliosById.values().forEach(folio -> {
            folio.setUserCasDetailsEntity(cas);
            folio.setSchemeEntities(new ArrayList<>());
        });

        Map<Long, UserSchemeDetailsEntity> schemesById = new LinkedHashMap<>();
        for (UserSchemeDetailsEntity scheme : schemes) {
            schemesById.putIfAbsent(scheme.getId(), scheme);
        }
        schemesById.values().forEach(scheme -> {
            UserFolioDetailsEntity parent = scheme.getUserFolioDetailsEntity();
            UserFolioDetailsEntity folio = foliosById.get(parent.getId());
            if (folio == null) {
                throw new IllegalStateException("Scheme " + scheme.getId() + " has no folio in CAS " + cas.getId());
            }
            folio.addSchemeEntity(scheme);
            scheme.setTransactionEntities(new ArrayList<>());
        });

        Map<Long, UserTransactionDetailsEntity> transactionsById = new LinkedHashMap<>();
        for (UserTransactionDetailsEntity transaction : transactions) {
            transactionsById.putIfAbsent(transaction.getId(), transaction);
        }
        transactionsById.values().forEach(transaction -> {
            UserSchemeDetailsEntity parent = transaction.getUserSchemeDetailsEntity();
            UserSchemeDetailsEntity scheme = schemesById.get(parent.getId());
            if (scheme == null) {
                throw new IllegalStateException(
                        "Transaction " + transaction.getId() + " has no scheme in CAS " + cas.getId());
            }
            scheme.addTransactionEntity(transaction);
        });
        return cas;
    }
}
