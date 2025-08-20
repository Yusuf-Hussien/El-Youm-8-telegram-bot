package phi.elyoum8bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import phi.elyoum8bot.model.BotState;
import phi.elyoum8bot.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class UsersStateConfig {

    @Bean
    public Map<Long , User> userStates()
    {
        return new ConcurrentHashMap<>();
    }

}
