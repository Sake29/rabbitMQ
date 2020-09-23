import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 订阅模式：
 * 路由模型 （交换机类型：direct）
 * 生产者向Exchange发送消息时，会指定一个routing key。
 * Exchange（交换机）接收生产者的消息后，把消息递交给与routing key完全匹配的队列
 */
public class App {

    private final static String EXCHANGE_NAME = "TEST_DIRECT_EXCHANGE";//交换机
    private final static String SMS_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_SMS";//短信队列
    private final static String EMAIL_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_EMAIL";//邮件队列

    /**
     * 模拟生产者，指定route key
     * @throws IOException
     * @throws TimeoutException
     */
    private void send() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //消息内容
        String messageSms = "注册成功！请短信回复[T]退订";
        String messageEmail = "注册成功！该邮件请勿回复";
        //发送消息，指定route key
        channel.basicPublish(EXCHANGE_NAME,"sms",null,messageSms.getBytes());
        channel.basicPublish(EXCHANGE_NAME,"email",null,messageEmail.getBytes());
        System.out.println(" [x] Sent '" + messageSms + "'");
        System.out.println(" [x] Sent '" + messageEmail + "'");

        channel.close();
        connection.close();
    }

    /**
     * 模拟消费者，短信服务，订阅route key为"sms"
     * @throws IOException
     * @throws TimeoutException
     */
    private void receiveForSms() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //绑定队列到交换机，同时指定route key
        channel.queueBind(SMS_QUEUE_NAME,EXCHANGE_NAME,"sms");

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
        // 监听队列，自动ACK
        channel.basicConsume(SMS_QUEUE_NAME, true, consumer);
    }

    /**
     * 模拟消费者，邮件服务，订阅route key为"email"
     * @throws IOException
     * @throws TimeoutException
     */
    private void receiveForEmail() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //绑定队列到交换机，同时指定route key
        channel.queueBind(EMAIL_QUEUE_NAME,EXCHANGE_NAME,"email");

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
        // 监听队列，自动ACK
        channel.basicConsume(EMAIL_QUEUE_NAME, true, consumer);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        App app = new App();
        app.receiveForEmail();
        app.receiveForSms();

        app.send();
    }

}
