import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 订阅模型：
 * 通配模式 （交换机类型：direct）
 * 每个消费者监听自己的队列，并且设置带统配符的routing key
 * 生产者将消息发给broker，由交换机根据routing key来转发消息到指定的队列
 * Routing key一般都是有一个或者多个单词组成，多个单词之间以“.”分割，例如：inform.sms
 * #：匹配一个或多个词
 * *：匹配不多不少恰好1个词
 */
public class App {

    private final static String TOPIC_EXCHANGE_NAME = "TEST_TOPIC_EXCHANGE";//topic交换机
    private final static String TOPIC_QUEUE_NAME_01 = "TOPIC_EXCHANGE_QUEUE_01";//topic队列1
    private final static String TOPIC_QUEUE_NAME_02 = "TOPIC_EXCHANGE_QUEUE_02";//topic队列2

    /**
     * 模拟生产者，指定routing key为quick.orange.rabbit
     * @throws IOException
     * @throws TimeoutException
     */
    private void send() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //消息内容
        String message = "这是一只行动迅速的橙色的兔子";
        //发送消息到交换机，指定routing key为：quick.orange.rabbit
        channel.basicPublish(TOPIC_EXCHANGE_NAME,"quick.orange.rabbit",null,message.getBytes());
        System.out.println(" [动物描述：] Sent '" + message + "'");

        channel.close();
        connection.close();
    }

    /**
     * 模拟消费者，订阅routing key为*.orange.*
     * @throws IOException
     * @throws TimeoutException
     */
    private void receive1() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //绑定队列到交换机,同时指定需要订阅的routing key。订阅所有的橙色动物
        channel.queueBind(TOPIC_QUEUE_NAME_01,TOPIC_EXCHANGE_NAME,"*.orange.*");
        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                System.out.println(" [消费者1] received : " + msg + "!");
            }
        };
        // 监听队列，自动ACK
        channel.basicConsume(TOPIC_QUEUE_NAME_01, true, consumer);
    }

    /**
     * 模拟消费者，订阅routing key为*.*.rabbit和lazy.#
     * @throws IOException
     * @throws TimeoutException
     */
    private void receive2() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //绑定队列到交换机,同时指定需要订阅的routing key。订阅关于兔子以及懒惰动物的消息
        channel.queueBind(TOPIC_QUEUE_NAME_02,TOPIC_EXCHANGE_NAME,"*.*.rabbit");
        channel.queueBind(TOPIC_QUEUE_NAME_02,TOPIC_EXCHANGE_NAME,"lazy.#");
        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                System.out.println(" [消费者2] received : " + msg + "!");
            }
        };
        // 监听队列，自动ACK
        channel.basicConsume(TOPIC_QUEUE_NAME_02, true, consumer);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        App app = new App();
        app.receive1();
        app.receive2();

        app.send();
    }
}
