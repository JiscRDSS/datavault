package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 *  By default this class is enabled in the Spring XML config, to disable it just comment it out.
 */

@Component
public class InitialiseDatabase {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseDatabase.class);

    private ArchiveStoreService archiveStoreService;

    private List<ArchiveStore> archiveStoresList;

    public void setArchiveStoreService(ArchiveStoreService archiveStoreService) {
        this.archiveStoreService = archiveStoreService;
    }

    public void setArchiveStoresList(List<ArchiveStore> archiveStoresList) {
        this.archiveStoresList = archiveStoresList;
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("unused") ContextRefreshedEvent event) {
        initialiseArchiveStores();
    }

    protected void initialiseArchiveStores() {
        logger.info("Initialising ArchiveStores");

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();

        if (archiveStores.isEmpty()) {
            for (ArchiveStore store : archiveStoresList) {
                logger.info("Processing store `" + store.getStorageClass() + "`");

                if (store.isEnabledOnDbInit()) {
                    archiveStoreService.addArchiveStore(store);
                }
            }
        }
    }
}
