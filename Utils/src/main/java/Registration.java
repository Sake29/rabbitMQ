import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 首次运行，请先注册队列和交换机
 */
public class Registration {

    private final static String DIRECT_EXCHANGE_NAME = "TEST_DIRECT_EXCHANGE";//direct交换机
    private final static String TOPIC_EXCHANGE_NAME = "TEST_TOPIC_EXCHANGE";//topic交换机
    private final static String FANOUT_EXCHANGE_NAME = "TEST_FANOUT_EXCHANGE";//fanout交换机
    private final static String SMS_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_SMS";//短信队列
    private final static String EMAIL_QUEUE_NAME = "FANOUT_EXCHANGE_QUEUE_EMAIL";//邮件队列
    private final static String WORK_QUEUE_NAME = "TEST_WORK_QUEUE";//workqueue队列
    private final static String HELLO_QUEUE_NAME="SAKE_FIRST_QUEUE";//helloworld队列
    private final static String TOPIC_QUEUE_NAME_01 = "TOPIC_EXCHANGE_QUEUE_01";//topic队列1
    private final static String TOPIC_QUEUE_NAME_02 = "TOPIC_EXCHANGE_QUEUE_02";//topic队列2


    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();
        //声明exchange,指定类型为fanout
        /**
         * 参数含义：
         * 1、s 交换机名称
         * 2、s1 交换机类型
         */
        channel.exchangeDeclare(FANOUT_EXCHANGE_NAME,BuiltinExchangeType.FANOUT);
        channel.exchangeDeclare(DIRECT_EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(TOPIC_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        //声明队列
        /**
         * 参数含义：
         * 1、queue 队列名称
         * 2、durable 是否持久化，如果持久化，mq重启后队列还在
         * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
         * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
         * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
         */
        channel.queueDeclare(HELLO_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(WORK_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(EMAIL_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(SMS_QUEUE_NAME,false,false,false,null);
        channel.queueDeclare(TOPIC_QUEUE_NAME_01,false,false,false,null);
        channel.queueDeclare(TOPIC_QUEUE_NAME_02,false,false,false,null);


        System.out.println("所有队列及交换机注册完成！");
        channel.close();
        connection.close();
    }

}
