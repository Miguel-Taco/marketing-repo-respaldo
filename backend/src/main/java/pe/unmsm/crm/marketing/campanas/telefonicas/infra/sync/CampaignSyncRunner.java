package pe.unmsm.crm.marketing.campanas.telefonicas.infra.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// DISABLED: CampaignSyncRunner commented out to prevent startup errors
// caused by foreign key constraint violations when syncing campaigns
// Re-enable when campaign data integrity issues are resolved

//@Component
@Profile("telemarketing-db")
@RequiredArgsConstructor
public class CampaignSyncRunner implements CommandLineRunner {

    private final CampaignSyncService syncService;

    @Override
    public void run(String... args) throws Exception {
        syncService.syncCampaigns();
    }
}
