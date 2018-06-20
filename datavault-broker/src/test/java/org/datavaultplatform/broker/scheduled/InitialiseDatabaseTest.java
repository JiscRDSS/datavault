package org.datavaultplatform.broker.scheduled;

import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InitialiseDatabaseTest {

    private final InitialiseDatabase initialiseDatabase = new InitialiseDatabase();

    @Mock
    private ArchiveStoreService mockArchiveStoreService;
    private final List<ArchiveStore> archiveStoresList = new ArrayList<>();
    private ArchiveStore archiveStore;

    // ArchiveStore parameters
    private final String storageClass = "org.datavaultplatform.common.storage.impl.S3Cloud";
    private final String label = "Cloud archive store";
    private HashMap<String, String> storeProperties;
    private final boolean retrieveEnabled = true;
    private final boolean enabledOnDbInit = true;

    @Before
    public void setUp() {
        storeProperties = new HashMap<>();
        storeProperties.put("testPropertyKey", "testPropertyValue");
        archiveStore = new ArchiveStore(storageClass, storeProperties, label, retrieveEnabled, enabledOnDbInit);
        archiveStoresList.add(archiveStore);

        initialiseDatabase.setArchiveStoreService(mockArchiveStoreService);
    }

    @Test
    public void addArchiveStoreCalledCorrectlyWithEnabledArchiveStore() {
        System.out.println("Running addArchiveStoreCalledCorrectlyWithEnabledArchiveStore()");

        try {
            // `ArchiveStore` is enabled via `setUp` so set `archiveStoreList` and call `initialiseArchiveStores`
            initialiseDatabase.setArchiveStoresList(archiveStoresList);
            initialiseDatabase.initialiseArchiveStores();

            // verify that `addArchiveStore` is called and the correct parameters are used
            ArgumentCaptor<ArchiveStore> argument = ArgumentCaptor.forClass(ArchiveStore.class);
            verify(mockArchiveStoreService).addArchiveStore(argument.capture());
            assertEquals(storageClass, argument.getValue().getStorageClass());
            assertEquals(storeProperties, argument.getValue().getProperties());
            assertEquals(label, argument.getValue().getLabel());
            assertEquals(retrieveEnabled, argument.getValue().isRetrieveEnabled());
            assertEquals(enabledOnDbInit, argument.getValue().isEnabledOnDbInit());
        } catch (Exception e) {
            fail("initialiseArchiveStores not behaving as expected.");
        }
    }

    @Test
    public void addArchiveStoreNotCalledWithDisabledArchiveStore() {
        System.out.println("Running addArchiveStoreNotCalledWithDisabledArchiveStore()");

        try {
            // disable `ArchiveStore`, set `archiveStoreList` and call `initialiseArchiveStores`
            archiveStore.setEnabledOnDbInit(false);
            initialiseDatabase.setArchiveStoresList(archiveStoresList);
            initialiseDatabase.initialiseArchiveStores();

            // verify that `addArchiveStore` is not called
            verify(mockArchiveStoreService, times(0)).addArchiveStore(any());
        } catch (Exception e) {
            fail("initialiseArchiveStores not behaving as expected.");
        }
    }

    @Test
    public void addArchiveStoreNotCalledWhenArchiveStoresNotEmpty() {
        System.out.println("Running addArchiveStoreNotCalledWhenArchiveStoresNotEmpty()");

        try {
            // make sure the call to `getArchiveStores()` isn't empty
            List<ArchiveStore> archiveStores = new ArrayList<>();
            archiveStores.add(archiveStore);
            when(this.mockArchiveStoreService.getArchiveStores()).thenReturn(archiveStores);

            // call `initialiseArchiveStores`
            this.initialiseDatabase.initialiseArchiveStores();

            // verify that `addArchiveStore` is not called
            verify(mockArchiveStoreService, times(0)).addArchiveStore(any());
        } catch (Exception e) {
            fail("initialiseArchiveStores not behaving as expected.");
        }
    }


}