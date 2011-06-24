package com.ChaseHQ.Statistician.EventDataHandlers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.ChaseHQ.Statistician.StatisticianPlugin;
import com.ChaseHQ.Statistician.Database.StatDBSynchDataGetSet;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBDataStores;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBDataValues_Config;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBDataValues_Players;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBStaticValue_Creatures;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBStaticValue_KillTypes;
import com.ChaseHQ.Statistician.Database.DataValues.StatDBStaticValue_Projectiles;
import com.ChaseHQ.Statistician.Stats.KillTag;
import com.ChaseHQ.Statistician.Utils.StringHandler;

public class EDHPlayer {
	public void PlayerJoin(final Player player) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
				if (StatDBSynchDataGetSet.isPlayerInDB(player.getUniqueId().toString())) {
					// Player Exists and has been here before
					StatDBSynchDataGetSet.playerLogin(player.getUniqueId().toString());
					if (StatDBDataValues_Config.SHOW_LASTJOIN_WELCOME.getConfigValueBoolean()){
						// Show Last Joined Message
						String lastJoinMessage = StringHandler.formatForChat(StatDBDataValues_Config.LASTJOIN_WELCOME_MSG.getConfigValueString(), player);
						String timeStamp = StatDBSynchDataGetSet.getValue(StatDBDataStores.PLAYER, StatDBDataValues_Players.LAST_LOGOUT, StatDBDataValues_Players.UUID, player.getUniqueId().toString());
						String lastJoin = new SimpleDateFormat(StatDBDataValues_Config.DATE_FORMAT.getConfigValueString()).format(new Date(Long.parseLong(timeStamp) * 1000));
						lastJoinMessage = lastJoinMessage.replaceAll("\\{lastJoin}", lastJoin);
						player.sendMessage(lastJoinMessage);
					}
				} else {
					// First Time Player Logged in with Statistician Running
					StatDBSynchDataGetSet.playerCreate(player.getUniqueId().toString(),player.getName());
					if (StatDBDataValues_Config.SHOW_FIRSTJOIN_WELCOME.getConfigValueBoolean()) {
						// If they want to show first join message
						player.sendMessage(StringHandler.formatForChat(StatDBDataValues_Config.FIRSTJOIN_WELCOME_MSG.getConfigValueString(), player));
					}
				}
				} catch (NullPointerException e) {
					
				} finally {
					// Watch them
					StatisticianPlugin.getEnabledPlugin().getPlayerData().addPlayerToWatch(player.getUniqueId().toString(), player.getLocation());
				}
			}
		});
	}
	
	public void PlayerQuit(final Player player) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatDBSynchDataGetSet.playerLogout(player.getUniqueId().toString());
				// Unwatch Them
				StatisticianPlugin.getEnabledPlugin().getPlayerData().removePlayerToWatch(player.getUniqueId().toString());
			}
		});

	}
	
	public void PlayerBlockBreak(final Player player, final Integer blockID) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addBlockBreak(player.getUniqueId().toString(), blockID);
			}
		});
	}
	
	public void PlayerBlockPlace(final Player player, final Integer blockID) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addBlockPlaced(player.getUniqueId().toString(), blockID);
			}
		});
	}
	
	public void PlayerMove(final Player player, final boolean isInMinecart, final boolean isOnPig, final boolean isInBoat) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatisticianPlugin.getEnabledPlugin().getPlayerData().incrementStepsTaken(player.getUniqueId().toString(), player.getLocation(), isInMinecart, isOnPig, isInBoat);	
			}
		});
	}
	
	public void PlayerKilledByPlayer(final Player killer, final Player victim, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KilledBy = StatDBStaticValue_Creatures.PLAYER;
				kt.KilledBy_UUID = killer.getUniqueId().toString();
				kt.KilledUsing = killer.getItemInHand().getTypeId();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				
				if (kt.KilledUsing == 0) {
					// Map it to Hand instead of air
					kt.KilledUsing = 9001;
				}
				
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(killer.getUniqueId().toString(), kt);
			}
		});
		
	}
	
	public void PlayerKilledByPlayerProjectile(final Player killer, final Player victim, final Entity projectile, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KilledBy = StatDBStaticValue_Creatures.PLAYER;
				kt.KilledBy_UUID = killer.getUniqueId().toString();
				kt.KilledUsing = killer.getItemInHand().getTypeId();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				kt.KillProjectile = StatDBStaticValue_Projectiles.mapProjectile(projectile);
				
				if (kt.KilledUsing == 0) {
					// Map it to Hand instead of air
					kt.KilledUsing = 9001;
				}
				
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(killer.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledByCreature(final Player victim, final Creature creature, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KilledBy = StatDBStaticValue_Creatures.mapCreature(creature);
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(victim.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledByCreatureProjectile (final Player victim, final Creature creature, final Entity projectile, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KilledBy = StatDBStaticValue_Creatures.mapCreature(creature);
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				kt.KillProjectile = StatDBStaticValue_Projectiles.mapProjectile(projectile);
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(victim.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledCreature (final Player killer, final Creature creature, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.mapCreature(creature);
				kt.KilledBy = StatDBStaticValue_Creatures.PLAYER;
				kt.KilledBy_UUID = killer.getUniqueId().toString();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				kt.KilledUsing = killer.getItemInHand().getTypeId();
				
				if (kt.KilledUsing == 0) {
					// Map it to Hand instead of air
					kt.KilledUsing = 9001;
				}
				
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(killer.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledCreatureProjectile(final Player killer, final Creature creature, final Entity projectile, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.mapCreature(creature);
				kt.KilledBy = StatDBStaticValue_Creatures.PLAYER;
				kt.KilledBy_UUID = killer.getUniqueId().toString();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				kt.KillProjectile = StatDBStaticValue_Projectiles.mapProjectile(projectile);
				kt.KilledUsing = killer.getItemInHand().getTypeId();
				
				if (kt.KilledUsing == 0) {
					// Map it to Hand instead of air
					kt.KilledUsing = 9001;
				}
				
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(killer.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledByBlock (final Player victim, final Block block, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.KilledBy = StatDBStaticValue_Creatures.BLOCK;
				if (block != null) {
					kt.KilledUsing = block.getTypeId();
				}
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(victim.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerKilledByOtherCause(final Player victim, final DamageCause cause) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				KillTag kt = new KillTag();
				kt.Killed = StatDBStaticValue_Creatures.PLAYER;
				kt.Killed_UUID = victim.getUniqueId().toString();
				kt.KillType = StatDBStaticValue_KillTypes.mapDamageCause(cause);
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addKillTag(victim.getUniqueId().toString(), kt);
			}
		});
	}
	
	public void PlayerPickedUpItem(final Player player, final Integer itemID, final Integer numberInStack) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addItemPickup(player.getUniqueId().toString(), itemID, numberInStack);
			}
		});
	}
	
	public void PlayerDroppedItem(final Player player, final Integer itemID, final Integer numberInStack) {
		StatisticianPlugin.getEnabledPlugin().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				StatisticianPlugin.getEnabledPlugin().getPlayerData().addItemDropped(player.getUniqueId().toString(), itemID, numberInStack);
			}
		});
	}
}
