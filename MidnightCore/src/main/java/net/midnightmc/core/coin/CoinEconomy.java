package net.midnightmc.core.coin;

import net.midnightmc.core.MidnightCorePlugin;
import net.midnightmc.core.playerdata.PlayerData;
import net.midnightmc.core.utils.UUIDUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CoinEconomy implements Economy {

    private static final CoinEconomy instance;

    static {
        instance = new CoinEconomy();
    }

    public CoinEconomy() {
        Bukkit.getScheduler().runTaskTimer(MidnightCorePlugin.getPlugin(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                Bukkit.getScheduler().runTaskAsynchronously(MidnightCorePlugin.getPlugin(), () -> CoinManager.getInstance().check(uuid));
            }
        }, 20L * 5, 20L * 5);
    }

    public static CoinEconomy getInstance() {
        return instance;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Midnight";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return String.valueOf(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "Coins";
    }

    @Override
    public String currencyNameSingular() {
        return "Coin";
    }

    @Override
    public boolean hasAccount(String playerName) {
        return UUIDUtils.getUUID(playerName) != null;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return PlayerData.get(player.getUniqueId()) != null;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        UUID uuid = UUIDUtils.getUUID(playerName);
        if (uuid == null) {
            return 0;
        }
        return getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        PlayerData data = PlayerData.get(player.getUniqueId());
        if (data == null) {
            return 0;
        }
        return data.getCoins();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) > amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        UUID uuid = UUIDUtils.getUUID(playerName);
        if (uuid == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
        return withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (player == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "");
        }
        if (player.isOnline()) {
            if (CoinManager.getInstance().apply(player.getUniqueId(), "add", (int) amount)) {
                return new EconomyResponse(amount, PlayerData.get(player.getUniqueId()).getCoins(), EconomyResponse.ResponseType.SUCCESS, "");
            }
        } else {
            if (CoinManager.getInstance().query(player.getUniqueId(), "add", (int) amount)) {
                return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.SUCCESS, "");
            }
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "database error");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        UUID uuid = UUIDUtils.getUUID(playerName);
        if (uuid == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "cannot find player");
        }
        return depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (player.isOnline()) {
            if (CoinManager.getInstance().apply(player.getUniqueId(), "subtract", (int) amount)) {
                return new EconomyResponse(amount, PlayerData.get(player.getUniqueId()).getCoins(), EconomyResponse.ResponseType.SUCCESS, "");
            }
        } else {
            if (CoinManager.getInstance().query(player.getUniqueId(), "subtract", (int) amount)) {
                return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.SUCCESS, "");
            }
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "database error");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        UUID uuid = UUIDUtils.getUUID(playerName);
        if (uuid == null) {
            return false;
        }
        return createPlayerAccount(Bukkit.getOfflinePlayer(uuid));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return player.isOnline();
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

}
