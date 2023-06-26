package Main;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;


public class botStartup {

	public static void main(String[] args) throws LoginException{
		JDABuilder jda = JDABuilder.createDefault("OTg4ODY3NDk3NDkzMTUxNzY3.Gi8eh0.UDVXcip4f4lYUS0v67LRp1EdFdOPm8n2HxHQcM");
		jda.setActivity(Activity.listening("fchelp"));
		jda.setStatus(OnlineStatus.ONLINE);
		jda.setChunkingFilter(ChunkingFilter.ALL);
		jda.setMemberCachePolicy(MemberCachePolicy.ALL);
		jda.enableIntents(GatewayIntent.GUILD_MEMBERS);
		jda.enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		jda.addEventListeners(new Commands());
		jda.addEventListeners(new Queue());
		jda.addEventListeners(new Match());
		jda.build();

	}

}
