package com.ruse.net.login;

import com.ruse.net.packet.Packet;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * Handles login encoding requests
 * @author Gabriel Hannason
 */
public class LoginEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext context, Channel channel, Object message) throws Exception {
		return ((Packet)message).getBuffer();
	}
}
