package phi.elyoum8bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import phi.elyoum8bot.model.User;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsersStatCleaner {

    private final Map<Long , User> userStates;

    @Scheduled(fixedRate = 5*60*1000)
    public void cleanUpInactiveUsers()
    {
        long fiveMinutesAgo = System.currentTimeMillis() - 5*60*1000;
        userStates.entrySet().removeIf((e)->e.getValue().getLastActiveTime() < fiveMinutesAgo);
        log.info("Cleaned up inactive users' stats");
    }
}
