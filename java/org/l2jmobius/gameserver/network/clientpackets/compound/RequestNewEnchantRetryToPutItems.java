/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.clientpackets.compound;

import org.l2jmobius.commons.network.ReadablePacket;
import org.l2jmobius.gameserver.data.xml.CombinationItemsData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.request.CompoundRequest;
import org.l2jmobius.gameserver.model.item.combination.CombinationItem;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.ClientPacket;
import org.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantRetryToPutItemFail;
import org.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantRetryToPutItemOk;

/**
 * @author Sdw
 */
public class RequestNewEnchantRetryToPutItems implements ClientPacket
{
	private int _firstItemObjectId;
	private int _secondItemObjectId;
	
	@Override
	public void read(ReadablePacket packet)
	{
		_firstItemObjectId = packet.readInt();
		_secondItemObjectId = packet.readInt();
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isInStoreMode())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			return;
		}
		
		if (player.isProcessingTransaction() || player.isProcessingRequest())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			return;
		}
		
		final CompoundRequest request = new CompoundRequest(player);
		if (!player.addRequest(request))
		{
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			return;
		}
		
		// Make sure player owns first item.
		request.setItemOne(_firstItemObjectId);
		final Item itemOne = request.getItemOne();
		if (itemOne == null)
		{
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			player.removeRequest(request.getClass());
			return;
		}
		
		// Make sure player owns second item.
		request.setItemTwo(_secondItemObjectId);
		final Item itemTwo = request.getItemTwo();
		if (itemTwo == null)
		{
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			player.removeRequest(request.getClass());
			return;
		}
		
		// Not implemented or not able to merge!
		final CombinationItem combinationItem = CombinationItemsData.getInstance().getItemsBySlots(itemOne.getId(), itemOne.getEnchantLevel(), itemTwo.getId(), itemTwo.getEnchantLevel());
		if (combinationItem == null)
		{
			client.sendPacket(ExEnchantRetryToPutItemFail.STATIC_PACKET);
			player.removeRequest(request.getClass());
			return;
		}
		
		client.sendPacket(ExEnchantRetryToPutItemOk.STATIC_PACKET);
	}
}
