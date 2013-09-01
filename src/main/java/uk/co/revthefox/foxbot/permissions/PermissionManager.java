package uk.co.revthefox.foxbot.permissions;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.pircbotx.User;
import uk.co.revthefox.foxbot.FoxBot;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager
{
    private FoxBot foxbot;
    private List<User> authedUsers = new ArrayList<>();

    public PermissionManager(FoxBot foxbot)
    {
        this.foxbot = foxbot;
    }

    // Exactly the same as userHasPermission(), except this gives no output.
    public Boolean userHasQuietPermission(User user, String permission)
    {
        String authType = foxbot.getConfig().getMatchUsersByHostmask() ? "\"" + user.getHostmask() + "\"" : user.getNick();
        Config permissions = foxbot.getPermissionsFile();

        if (!authedUsers.contains(user) && user.isVerified())
        {
            authedUsers.add(user);
        }

        return !(foxbot.getConfig().getUsersMustBeVerified()
                && !authedUsers.contains(user))
                && !(!permissions.hasPath("permissions." + authType)
                && !permissions.getStringList("permissions.default").contains(permission))
                && !permissions.getStringList("permissions." + authType).contains("-" + permission)
                && (permissions.getStringList("permissions.default").contains(permission)
                || permissions.getStringList("permissions.default").contains("permissions.*")
                || permissions.getStringList("permissions." + authType).contains(permission)
                || permissions.getStringList("permissions." + authType).contains("permissions.*"));

    }

    public Boolean userHasPermission(User user, String permission)
    {
        String authType = foxbot.getConfig().getMatchUsersByHostmask() ? "\"" + user.getHostmask() + "\"" : user.getNick();
        Config permissions = foxbot.getPermissionsFile();

        if (!authedUsers.contains(user) && user.isVerified())
        {
            authedUsers.add(user);
        }

        if (foxbot.getConfig().getUsersMustBeVerified() && !authedUsers.contains(user))
        {
            foxbot.getBot().sendNotice(user, "You must be logged into nickserv to use bot commands.");
            return false;
        }

        return !(!foxbot.getPermissionsFile().hasPath("permissions." + authType)
                && !permissions.getStringList("permissions.default").contains(permission))
                && !permissions.getStringList("permissions." + authType).contains("-" + permission)
                && (permissions.getStringList("permissions.default").contains(permission)
                || permissions.getStringList("permissions.default").contains("permissions.*")
                || permissions.getStringList("permissions." + authType).contains(permission)
                || permissions.getStringList("permissions." + authType).contains("permissions.*"));
    }

    public Boolean isNickProtected(String nick)
    {
        return foxbot.getNickProtectionFile().hasPath("protection." + nick) && !foxbot.getBot().getUser(nick).getHostmask().equals(foxbot.getNickProtectionFile().getString("protection." + nick + ".hostmask"));
    }

    public void removeAuthedUser(User user)
    {
        if (authedUsers.contains(user))
        {
            authedUsers.remove(user);
        }
    }
}
