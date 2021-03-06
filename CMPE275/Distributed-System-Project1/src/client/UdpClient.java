package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import routing.MsgInterface.NetworkDiscoveryPacket;
import routing.MsgInterface.Route;
import server.UdpServer;
import server.UdpServerHandler;
import io.netty.util.internal.SocketUtils;
import app.MyConstants;


public final class UdpClient implements Runnable {

	public static void broadcast() throws Exception {
		Route.Builder rb = Route.newBuilder();
		rb.setPath(Route.Path.NETWORK_DISCOVERY);
		
		NetworkDiscoveryPacket.Builder builder = NetworkDiscoveryPacket.newBuilder();
		
		builder.setGroupTag(MyConstants.GROUP_NAME);
		builder.setSender(NetworkDiscoveryPacket.Sender.EXTERNAL_SERVER_NODE);
		builder.setMode(NetworkDiscoveryPacket.Mode.REQUEST);
		builder.setNodeId(MyConstants.NODE_NAME);
		builder.setNodeAddress(MyConstants.NODE_IP);
		builder.setNodePort(Integer.parseInt(MyConstants.NODE_PORT));
		builder.setSecret(MyConstants.SECRET);

		rb.setNetworkDiscoveryPacket(builder);
		rb.setId(1);
		Route request = rb.build();
		
		sendUDPMessage(request, MyConstants.UDP_IP_BROADCAST, MyConstants.UDP_PORT);
	}

	public static void sendUDPMessage(Route request, String IP, int port) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
					.handler(new ChannelInitializer<DatagramChannel>() {
						@Override
						public void initChannel(DatagramChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new UdpClientHandler());
						}
					});

			Channel ch = b.bind(0).sync().channel();
			System.out.println("******************");
			System.out.println(request);
			System.out.println("******************");
			ByteBuf buf = Unpooled.copiedBuffer(request.toByteArray());

			ch.writeAndFlush(new DatagramPacket(buf, SocketUtils.socketAddress(IP, port))).sync();
			
			
			

			// UdpClientHandler will close the DatagramChannel when a
			// response is received. If the channel is not closed within 5
			// seconds,
			// print an error message and quit.
			/*if (!ch.closeFuture().await(5000)) {
				System.err.println("request timed out.");
			}*/
		} finally {
			group.shutdownGracefully();
		}
		
	}
	
	public void run() {
    	System.out.println("response server from client running at port " +MyConstants.TEST_PORT);
    	EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<DatagramChannel>(){
                        @Override
                        public void initChannel(DatagramChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast("protobufDecoder", new DatagramPacketDecoder(new ProtobufDecoder(Route.getDefaultInstance())));
                             pipeline.addLast("protobufEncoder", new ProtobufEncoder());
                             pipeline.addLast("handler", new NewUdpClientHandler());

                        }});

            b.bind(MyConstants.TEST_PORT).sync().channel().closeFuture().await();
        } catch(Exception e){
        	e.printStackTrace();
        }
        finally {
            group.shutdownGracefully();
        }
    }
	public static void main(String []args) throws Exception{
		Thread responseThread = new Thread(new UdpClient());
		responseThread.start();
		UdpClient.broadcast();
	}
}
