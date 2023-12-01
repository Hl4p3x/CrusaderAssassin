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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.ReadablePacket;
import org.l2jmobius.gameserver.model.ShortCuts;
import org.l2jmobius.gameserver.model.Shortcut;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.taskmanager.AutoUseTaskManager;

/**
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestShortCutDel implements ClientPacket
{
	private int _slot;
	private int _page;
	
	@Override
	public void read(ReadablePacket packet)
	{
		final int position = packet.readInt();
		_slot = position % ShortCuts.MAX_SHORTCUTS_PER_BAR;
		_page = position / ShortCuts.MAX_SHORTCUTS_PER_BAR;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((_page > 23) || (_page < 0))
		{
			return;
		}
		
		// Delete the shortcut.
		final Shortcut oldShortcut = player.getShortCut(_slot, _page);
		player.deleteShortCut(_slot, _page);
		
		if (oldShortcut != null)
		{
			boolean removed = true;
			
			// Keep other similar shortcuts activated.
			if (oldShortcut.isAutoUse())
			{
				player.removeAutoShortcut(_slot, _page);
				for (Shortcut shortcut : player.getAllShortCuts())
				{
					if ((oldShortcut.getId() == shortcut.getId()) && (oldShortcut.getType() == shortcut.getType()))
					{
						player.addAutoShortcut(shortcut.getSlot(), shortcut.getPage());
						removed = false;
					}
				}
			}
			
			// Remove auto used ids.
			if (removed)
			{
				switch (oldShortcut.getType())
				{
					case SKILL:
					{
						AutoUseTaskManager.getInstance().removeAutoBuff(player, oldShortcut.getId());
						AutoUseTaskManager.getInstance().removeAutoSkill(player, oldShortcut.getId());
						break;
					}
					case ITEM:
					{
						AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, oldShortcut.getId());
						break;
					}
					case ACTION:
					{
						AutoUseTaskManager.getInstance().removeAutoAction(player, oldShortcut.getId());
						break;
					}
				}
			}
		}
		
		player.restoreAutoShortcutVisual();
	}
}
