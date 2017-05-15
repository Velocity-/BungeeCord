package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf> {

	@Setter private Protocol protocol;
	private final boolean server;
	@Setter private int protocolVersion;

	@Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		protocolVersion = 47;
		Protocol.DirectionData prot = server ? protocol.TO_SERVER : protocol.TO_CLIENT;
		ByteBuf copy = in.copy(); // Can't slice this one due to EntityMap :(

		try {
			int packetId = DefinedPacket.readVarInt(in);

			if (server) {
				System.out.println("ServerPkt: " + packetId + ", 0x" + String.format("%02X", packetId) + ", " + prot + ", " + protocolVersion);
			} else {
				System.out.println("ClientPkt: " + packetId + ", 0x" + String.format("%02X", packetId) + ", " + prot + ", " + protocolVersion);
			}

			DefinedPacket packet = prot.createPacket(packetId, protocolVersion);
			if (packet != null) {
				packet.read(in, prot.getDirection(), protocolVersion);

				if (in.isReadable()) {
					throw new BadPacketException("Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot);
				}
			} else {
				in.skipBytes(in.readableBytes());
			}

			out.add(new PacketWrapper(packet, copy));
			copy = null;
		} finally {
			if (copy != null) {
				copy.release();
			}
		}
	}

}
