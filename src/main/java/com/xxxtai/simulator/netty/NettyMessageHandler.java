package com.xxxtai.simulator.netty;

import com.xxxtai.express.constant.Constant;
import com.xxxtai.express.constant.State;
import com.xxxtai.express.model.Car;
import com.xxxtai.express.model.Graph;
import com.xxxtai.simulator.model.AGVCar;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j(topic = "develop")
public class NettyMessageHandler extends SimpleChannelInboundHandler<String> {
    @Resource
    private Graph graph;
    private Car car;
    
    public NettyMessageHandler(Car car){
        this.car = car;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info(this.car.getAGVNum() + "AGV inactive!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.car.setSocketChannel(null);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    log.info(this.car.getAGVNum() + "AGV send ping to server----------");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        log.info(msg);

        if (msg != null && msg.length() > 0) {
            this.car.setLastCommunicationTime(System.currentTimeMillis());
            if (msg.startsWith(Constant.ROUTE_PREFIX)) {
                log.info(this.car.getAGVNum() + "AGV route:" + msg);
                ((AGVCar) car).setCardCommandMap(Constant.getContent(msg));
            }
            if (msg.endsWith(Constant.SUFFIX) && msg.startsWith(Constant.COMMAND_PREFIX)) {
                String content = Constant.getContent(msg);
                String[] c = content.split(Constant.SPLIT);
                if (Integer.valueOf(c[0], 16) == 1) {
                    this.car.setState(State.FORWARD);
                    log.info("让" + this.car.getAGVNum() + "AGV前进");
                } else if (Integer.valueOf(c[0], 16) == 2) {
                    this.car.setState(State.STOP);
                    log.info("让" + this.car.getAGVNum() + "AGV停止");
                }
            }
        }
    }
}
