import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCService {

    private static final String RPC_QUEUE_NAME = "RPC_QUEUE";

    /**
     * 递归求斐波那契数列
     * @param n
     * @return
     */
    private static int fib(int n){
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    /**
     * 模拟RPC服务器被客户端远程调用
     */
    public static void execute() throws IOException, TimeoutException, InterruptedException {
        //建立连接
        Connection connection = ConnectionUtil.getConnection();
        //建立通道
        Channel channel = connection.createChannel();
        // 设置同时最多只能获取一个消息
        channel.basicQos(1);
        System.out.println(" [RPCServer] is wating RPC requests");
        //定义消息的回调处理类
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //生产返回的结果，设置corrID
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties()
                        .builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                //生成返回
                String response = generateResponse(body);
                //回复消息，通知已经收到请求了
                channel.basicPublish("",properties.getReplyTo(),replyProps,response.getBytes());
                //对消息进行回答
                channel.basicAck(envelope.getDeliveryTag(),false);
                //唤醒所有线程
                synchronized (this){
                    this.notify();
                }
            }
        };
        // 消费消息
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        // 在收到消息前，本线程进入等待状态
        while (true) {
            synchronized(consumer) {
                consumer.wait();
            }
        }
    }

    private static String generateResponse(byte[] body) {
        String response = "";
        System.out.println(" [RpcServer] receive requests: fib[" + new String(body)+"]");
        int n = Integer.parseInt(new  String(body));
        response += fib(n);
        return " response:" + response;
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        execute();
    }
}
