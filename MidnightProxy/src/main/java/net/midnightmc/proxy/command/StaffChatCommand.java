package net.midnightmc.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.midnightmc.proxy.StaffChat;
import net.midnightmc.proxy.Util;

public class StaffChatCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player p) {
            if (StaffChat.getInstance().isStaffChatMode(p)) {
                StaffChat.getInstance().setStaffChatMode(p, false);
                Util.sendMessage(p, "Staff Chat Off");
            } else {
                StaffChat.getInstance().setStaffChatMode(p, true);
                Util.sendMessage(p, "Staff Chat On");
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("midnight.staffchat");
    }

}
