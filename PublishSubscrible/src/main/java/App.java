import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 订阅模式：
 * 一个生产者多个消费者，每个消费者都有一个自己的队列
 * 生产者没有将消息直接发送给队列，而是发送给exchange(交换机、转发器)
 * 每个队列都需要绑定到交换机上
 * 生产者发送的消息，经过交换机到达队列，实现一个消息被多个消费者消费
 *
 * Publish/subscribe模型（交换机类型：Fanout，也称为广播 ）
 */
public class App {

    private final static String EXCHANGE_NAME = "TEST_FANOUT_EXCHANGE";//交换机
    private final static String SMS_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_SMS";//短信队列
    private final static String EMAIL_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_EMAIL";//邮件队列


    /**
     * 模拟生产者发送消息
     */
    private void send() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();

        //消息内容
        String message = "注册成功!";
        //发布消息到交换机
        channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes());
        System.out.println(" [生产者] send '" + message + "'");
        channel.close();
        connection.close();
    }

    /**
     * 注册成功发给短信服务
     * @throws IOException
     * @throws TimeoutException
     */
    private void receiveForSms() throws IOException, TimeoutException {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(SMS_QUEUE_NAME, false, false, false, null);

        // 绑定队列到交换机
        channel.queueBind(SMS_QUEUE_NAME, EXCHANGE_NAME, "");

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                System.out.println(" [短信服务] received : " + msg + "!");
            }
        };
        // 监听队列，自动返回完成
        channel.basicConsume(SMS_QUEUE_NAME, true, consumer);
    }


    private void receiveForEmail() throws IOException, TimeoutException {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        // 获取通道
        Channel channel = connection.createChannel();
        // 绑定队列到交换机
        channel.queueBind(EMAIL_QUEUE_NAME, EXCHANGE_NAME, "");

        // 定义队列的消费者
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body);
                System.out.println(" [邮件服务] received : " + msg + "!");
            }
        };
        // 监听队列，自动返回完成
        channel.basicConsume(EMAIL_QUEUE_NAME, true, consumer);
    }


    public static void main(String[] args) throws IOException, TimeoutException {
        App app = new App();
        app.receiveForSms();
        app.receiveForEmail();

        app.send();


    }

}
