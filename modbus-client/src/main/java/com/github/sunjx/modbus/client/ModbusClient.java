package com.github.sunjx.modbus.client;

import com.github.sunjx.modbus.channel.ChannelManagerImpl;
import com.github.sunjx.modbus.channel.ChannelReconnectable;
import com.github.sunjx.modbus.handler.ModbusInboundHandler;
import com.github.sunjx.modbus.handler.ModbusRequestHandler;
import com.github.sunjx.modbus.handler.ModbusResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.TimeUnit;

public abstract class ModbusClient extends ChannelManagerImpl implements ChannelReconnectable {

    protected static final InternalLogger log = InternalLoggerFactory.getInstance(ModbusClient.class);

    public enum CONNECTION_STATES {
        /**
         *
         */
        connected, notConnected, pending;
    }


    public static final String PROP_CONNECTIONSTATE = "connectionState";
    private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final String host;
    private final int port;
    private Channel channel;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private CONNECTION_STATES connectionState = CONNECTION_STATES.notConnected;

    public ModbusClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    protected void setup(ModbusRequestHandler reqhandler) throws Exception {
        reqhandler.setChannelManager(this);
        ChannelInitializer<SocketChannel> handler = getChannelInitializer(reqhandler);
        setup(handler);
    }

    protected void setup(ModbusResponseHandler resphandler) throws Exception {
        resphandler.setChannelManager(this);
        ChannelInitializer<SocketChannel> handler = getChannelInitializer((ModbusInboundHandler) resphandler);
        setup(handler);
    }

    public void setup(ChannelInitializer<SocketChannel> handler) throws Exception {
        init(handler);
    }


    protected void init(ChannelInitializer<SocketChannel> handler) throws Exception {
        try {
            int threads = Math.max(2, Runtime.getRuntime().availableProcessors() * 2 - 1);
            this.workerGroup = new NioEventLoopGroup(threads, new DefaultThreadFactory("client", false));

            this.bootstrap = new Bootstrap();
            this.bootstrap.group(this.workerGroup);
            this.bootstrap.channel(NioSocketChannel.class);
            this.bootstrap.option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            this.bootstrap.handler(handler);
            doConnect(this.workerGroup);
        } catch (Exception ex) {
            setConnectionState(CONNECTION_STATES.notConnected);
            log.error("init", ex);
            throw new Exception("ConnectionException:" + ex.getLocalizedMessage());
        }
    }


    @Override
    public void reConnect() {
        try {
            log.info(String.format("reConnect:%s,%s", new Object[]{this.host, Integer.valueOf(this.port)}));
            doConnect(this.workerGroup);
        } catch (InterruptedException ex) {
            log.error("reConnect", ex);
        }
    }

    public void doConnect(final EventLoopGroup workerGroup) throws InterruptedException {
        if (this.channel == null || !this.channel.isActive()) {
            this.setConnectionState(ModbusClient.CONNECTION_STATES.pending);
            log.info(String.format("connect:%s,%s", this.host, this.port));
            ChannelFuture f = this.bootstrap.connect(this.host, this.port);
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture futureListener) throws Exception {
                    if (futureListener.isSuccess()) {
                        ModbusClient.this.channel = futureListener.channel();
                        ModbusClient.this.setConnectionState(ModbusClient.CONNECTION_STATES.connected);
                        ModbusClient.log.info(String.format("connect:%s,%s successfully", ModbusClient.this.host, ModbusClient.this.port));
                        ModbusClient.this.channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                ModbusClient.this.setConnectionState(ModbusClient.CONNECTION_STATES.notConnected);
                            }
                        });
                    } else {
                        ModbusClient.log.info(String.format("connect:%s,%s failed, try connect after 10s", ModbusClient.this.host, ModbusClient.this.port));
                        ModbusClient.this.bindSchedule4DoConnect(futureListener.channel(), workerGroup);
                    }

                }
            });
        }
    }

    private void bindSchedule4DoConnect(Channel channel, final EventLoopGroup workerGroup) {
        channel.eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    ModbusClient.log.info(String.format("doConnect"));
                    ModbusClient.this.doConnect(workerGroup);
                } catch (InterruptedException ex) {
                    ModbusClient.log.error("doConnect", ex);
                }
            }
        }, 10L, TimeUnit.SECONDS);
    }


    /* 163 */
    public Channel getChannel() {
        return this.channel;
    }


    @Override
    public void close() {
        if (this.channel != null) {
            this.channel.close().awaitUninterruptibly();
        }
    }


    public CONNECTION_STATES getConnectionState() {
        return this.connectionState;
    }


    public void setConnectionState(CONNECTION_STATES connectionState) {
        CONNECTION_STATES oldConnectionState = this.connectionState;
        this.connectionState = connectionState;
        this.propertyChangeSupport.firePropertyChange("connectionState", oldConnectionState, connectionState);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }


    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected abstract ChannelInitializer<SocketChannel> getChannelInitializer(ModbusInboundHandler paramModbusInboundHandler);
}

