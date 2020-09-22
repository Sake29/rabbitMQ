import com.rabbitmq.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Work Queues模型
 * 该模型描述的是一个生产者（Boss）向队列发消息（任务）
 * 多个消费者（worker）从队列接受消息（任务）
 */
public class App {

    private final static String QUEUE_NAME = "test_work_queue";

    /**
     * 模拟生产者循环发送50条消息
     */
    @Test
    public void send() throws IOException, TimeoutException, InterruptedException {
        //获取连接
        Connection connection = ConnectionUtil.getConnection();
        //获取通道
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //发布消息
        for (int i = 0; i < 50; i++) {
            //消息内容
            String message = "task.."+i;
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
            System.out.println("[x] send '"+message+"'");
            Thread.sleep(i*2);
        }
        channel.close();
        connection.close();
    }

    /**
     * 模拟消费者1，每次业务处理需要1秒
     * @throws IOException
     * @throws TimeoutException
     */
    public void receive1() throws IOException, TimeoutException, InterruptedException {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //实现消费方法
        DefaultConsumer consumer = new DefaultConsumer(channel){
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body,"utf-8");
                System.out.println(" [消费者1] received : " + msg + "!");
                //模拟任务耗时1s
                try { TimeUnit.SECONDS.sleep(1); } catch (Exception e) { e.printStackTrace(); }
            }
        };
        // 监听队列，第二个参数：是否自动进行消息确认。
        channel.basicConsume(QUEUE_NAME, true, consumer);
        channel.close();
        connection.close();
        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }
    }

    /**
     * 模拟消费者2，每次业务处理需要3秒
     * @throws IOException
     * @throws TimeoutException
     */
    public void receive2() throws IOException, TimeoutException, InterruptedException {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
        Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //实现消费方法
        DefaultConsumer consumer = new DefaultConsumer(channel){
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body,"utf-8");
                System.out.println(" [消费者2] received : " + msg + "!");
                //模拟任务耗时1s
                try { TimeUnit.SECONDS.sleep(3); } catch (Exception e) { e.printStackTrace(); }
            }
        };
        // 监听队列，第二个参数：是否自动进行消息确认。
        channel.basicConsume(QUEUE_NAME, true, consumer);
        channel.close();
        connection.close();
        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }
    }

    @Test
    public void receive() throws IOException, TimeoutException, InterruptedException {
       /* //开启一个线程调用消费者1
        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    receive1();
                    System.out.println("rec1");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        //调用消费者2
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    receive2();
                    System.out.println("rec2");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        thread2.start();*/
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
        final Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //实现消费方法
        final DefaultConsumer consumer1 = new DefaultConsumer(channel){
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body,"utf-8");
                System.out.println(" [消费者1] received : " + msg + "!");
                //模拟任务耗时1s
                try { TimeUnit.SECONDS.sleep(1); } catch (Exception e) { e.printStackTrace(); }
            }
        };
        final DefaultConsumer consumer2 = new DefaultConsumer(channel){
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // body 即消息体
                String msg = new String(body,"utf-8");
                System.out.println(" [消费者2] received : " + msg + "!");
                //模拟任务耗时1s
                try { TimeUnit.SECONDS.sleep(3); } catch (Exception e) { e.printStackTrace(); }
            }
        };

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                // 监听队列，第二个参数：是否自动进行消息确认。
                try {
                    channel.basicConsume(QUEUE_NAME, true, consumer1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                // 监听队列，第二个参数：是否自动进行消息确认。
                try {
                    channel.basicConsume(QUEUE_NAME, true, consumer2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        thread2.start();

/*        channel.close();
        connection.close();*/
        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }
    }
}
