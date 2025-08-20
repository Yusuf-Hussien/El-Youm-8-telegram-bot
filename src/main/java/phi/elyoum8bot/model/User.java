package phi.elyoum8bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class User {
    private BotState state;
    private long lastActiveTime;

    public User(BotState state) {
        this.state = state;
        this.lastActiveTime = System.currentTimeMillis();
    }
}
