import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

public class App {

    private final static String QUEUE_NAME="SAKE_FIRST_QUEUE";

    private void send() throws IOException, TimeoutException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //从连接中创建通道，使用通道完成消息相关操作
        Channel channel = connection.createChannel();
        //消息内容
        String message = "hello ,this is Sake's first Message";
        //向指定队列中发送消息
        /**
         * 1、exchange，交换机，如果不指定将使用mq的默认交换机（设置为""）
         * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认交换机，routingKey设置为队列的名称
         * 3、props，消息的属性
         * 4、body，消息内容
         */
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        System.out.println("[x] Send '"+message+"'");
        channel.close();
        connection.close();
    }

    private void receive() throws Exception {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //创建会话通道，生产者和mq服务所有的通信都在通道里完成
        Channel channel = connection.createChannel();
        //创建队列
        /**
         * 参数含义：
         * 1、queue 队列名称
         * 2、durable 是否持久化，如果持久化，mq重启后队列还在
         * 3、exclusive 是否独占连接，队列只允许在该连接中访问，如果connection连接关闭队列则自动删除,如果将此参数设置true可用于临时队列的创建
         * 4、autoDelete 自动删除，队列不再使用时是否自动删除此队列，如果将此参数和exclusive参数设置为true就可以实现临时队列（队列不用了就自动删除）
         * 5、arguments 参数，可以设置一个队列的扩展参数，比如：可设置存活时间
         */
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //实现消费方法
        DefaultConsumer consumer = new DefaultConsumer(channel){
            /**
             * 获取消息并处理，这个方法类似事件监听，如果有消息的时候会自动调用
             * 当接收到消息时此方法被调用
             * @param consumerTag 消费者标签
             * @param envelope 通过信封
             * @param properties 消息属性
             * @param body 消息内容
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws UnsupportedEncodingException {
                //交换机
                String exchange = envelope.getExchange();
                //消息id，mq在channel中用来标识消息的id，可用于确认消息已接收
                long deliveryTag = envelope.getDeliveryTag();
                // body 即消息体
                String msg = new String(body,"utf-8");
                System.out.println(" [x] received : " + msg + "!");
            }
        };
        // 监听队列，第二个参数：是否自动进行消息确认。
        //参数：String queue, boolean autoAck, Consumer callback
        /**
         * 参数明细：
         * 1、queue 队列名称
         * 2、autoAck 自动回复，当消费者接收到消息后要告诉mq消息已接收，如果将此参数设置为tru表示会自动回复mq，如果设置为false要通过编程实现回复
         * 3、callback，消费方法，当消费者接收到消息要执行的方法
         */
        channel.basicConsume(QUEUE_NAME, true, consumer);
/*        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }*/
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.receive();
        app.send();
    }
}