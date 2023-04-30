package com.example.mfscreener.repository;

import com.example.mfscreener.entities.UserCASDetailsEntity;
import com.example.mfscreener.entities.UserFolioDetailsEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.hibernate.jpa.AvailableHints;
import org.springframework.transaction.annotation.Transactional;

public class CustomCASDetailsEntityRepositoryImpl implements CustomCASDetailsEntityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public UserCASDetailsEntity findByInvestorInfoEntity_EmailAndInvestorInfoEntity_Name(String email, String name) {

        UserCASDetailsEntity userCASDetailsEntity = this.entityManager
                .createQuery(
                        """
                    select u from UserCASDetailsEntity u join fetch u.folioEntities
                    where u.investorInfoEntity.email = :email and u.investorInfoEntity.name = :name
                """,
                        UserCASDetailsEntity.class)
                .setParameter("email", email)
                .setParameter("name", name)
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getSingleResult();

        List<UserFolioDetailsEntity> userFolioDetailsEntityList = this.entityManager
                .createQuery(
                        """
                    select u from UserFolioDetailsEntity u left join fetch u.userCasDetailsEntity fe
                    join fetch u.schemeEntities sc join fetch sc.transactionEntities where fe in :folioEntries
                """,
                        UserFolioDetailsEntity.class)
                .setParameter("folioEntries", userCASDetailsEntity.getFolioEntities())
                .setHint(AvailableHints.HINT_READ_ONLY, true)
                .getResultList();

        //        userFolioDetailsEntityList.forEach(userFolioDetailsEntity -> {
        //            List<UserSchemeDetailsEntity> userSchemeDetailsEntityList = this.entityManager
        //                    .createQuery(
        //                            """
        //                        select u from UserSchemeDetailsEntity u left join u.userFolioDetailsEntity join fetch
        // u.transactionEntities where u.userFolioDetailsEntity.schemeEntities
        //                        in :schemeEntities
        //                    """,
        //                            UserSchemeDetailsEntity.class)
        //                    .setParameter("schemeEntities", userFolioDetailsEntity.getSchemeEntities())
        //                    .setHint(AvailableHints.HINT_READ_ONLY, true)
        //                    .getResultList();
        //            userFolioDetailsEntity.setSchemeEntities(userSchemeDetailsEntityList);
        //        });

        userCASDetailsEntity.setFolioEntities(userFolioDetailsEntityList);
        return userCASDetailsEntity;
    }
}
