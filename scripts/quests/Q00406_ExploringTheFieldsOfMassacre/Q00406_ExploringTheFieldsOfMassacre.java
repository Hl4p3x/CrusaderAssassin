/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00406_ExploringTheFieldsOfMassacre;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Serenitty
 */
public class Q00406_ExploringTheFieldsOfMassacre extends Quest
{
	// NPC
	private static final int GENIE_LAMP = 34369;
	// Monsters
	private static final int GUARD_BUTCHER = 22101;
	private static final int DOOM_ARCHER = 21008;
	private static final int DOOM_SCOUT = 21002;
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_GUARD = 21007;
	private static final int DOOM_TROOPER = 21009;
	private static final int DOOM_WARRIOR = 21010;
	private static final int ARCHER_OF_DESTRUCTION = 21001;
	private static final int DISMAL_OAK = 21004;
	private static final int GRAVEYANRD_PREDATOR = 21005;
	private static final int GRAVEYARD_LICH = 21003;
	private static final int GRAVEYARD_PREDATOR = 21005;
	// Item
	private static final ItemHolder HP_POTION = new ItemHolder(91857, 200);
	// Misc
	private static final int MIN_LEVEL = 52;
	private static final int MAX_LEVEL = 56;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q00406_ExploringTheFieldsOfMassacre()
	{
		super(406);
		addStartNpc(GENIE_LAMP);
		addTalkId(GENIE_LAMP);
		addKillId(GUARD_BUTCHER, DOOM_ARCHER, DOOM_SCOUT, DOOM_SERVANT);
		addKillId(DOOM_GUARD, DOOM_TROOPER, DOOM_WARRIOR, ARCHER_OF_DESTRUCTION);
		addKillId(DISMAL_OAK, GRAVEYANRD_PREDATOR, GRAVEYARD_LICH, GRAVEYARD_PREDATOR);
		addCondMinLevel(MIN_LEVEL, "no_lvl.html");
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_52_56_EXPLORING_THE_FIELDS_OF_MASSACRE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "34369.htm":
			case "34369-01.html":
			case "34369-02.htm":
			{
				htmltext = "34369-02.htm"; // no kill htm
				break;
			}
			case "StartMission":
			{
				qs.startQuest();
				qs.setCond(1, true);
				htmltext = "34369-02.htm"; // no kill htm
				break;
			}
			case "reward":
			{
				if (qs.isCond(2))
				{
					addExpAndSp(player, 25000000, 675000);
					giveItems(player, HP_POTION);
					htmltext = "34369-05.html";
					qs.exitQuest(false, true);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "34369.htm";
		}
		else if (qs.isStarted())
		{
			if (qs.isCond(1))
			{
				final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
				if ((killCount < 500))
				{
					htmltext = "34369-03.html"; // no kill html
				}
				else
				{
					htmltext = "34369.htm";
				}
			}
			else if (qs.isCond(2))
			{
				htmltext = "34369-04.html"; // reward
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == GENIE_LAMP)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 500)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
			}
			else
			{
				showOnScreenMsg(killer, NpcStringId.SUMMON_GENIE_AND_TALK_TO_HIM, ExShowScreenMessage.TOP_CENTER, 10000);
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.DEFEAT_MONSTERS_6.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}