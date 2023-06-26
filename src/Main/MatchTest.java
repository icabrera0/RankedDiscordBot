package Main;


import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MatchTest extends ListenerAdapter{
	public final String PREFIX = "fc";
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String[] args = event.getMessage().getContentRaw().split(" ");
		
		if (args[0].equalsIgnoreCase(PREFIX + "accept")) {
			
		}
	}
	
}
