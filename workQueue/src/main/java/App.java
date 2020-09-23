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

    private final static String QUEUE_NAME = "TEST_WORK_QUEUE";
    private boolean flag = false;//用于标记消息是否发送完，用于观测
    private int consumer1Count = 0;//消费者1接受消息条数
    private int consumer2Count = 0;//消费者2接受消息条数
    private int consumer3Count = 0;//消费者3接受消息条数

    /**
     * 模拟生产者循环发送50条消息
     */
    private void send() throws IOException, TimeoutException, InterruptedException {
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
        flag = true;
        channel.close();
        connection.close();
    }

    /**
     * 模拟消费者消费消息并处理
     * 自动ACK，即消息一旦被接收，消费者自动发送消息确认（默认）
     * @param consumerName 消费者名字，此处绑定消费者名字为消费者1，消费者2，消费者3
     * @param costTime 每次处理消息所需要的时间
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    private void receive(final String consumerName, final int costTime) throws IOException, TimeoutException, InterruptedException {
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
                if (flag==false){
                    //为了观察消费者的消费顺序，等消息全部发完后，再开始监听，实际上是实时监听的
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (consumerName == "消费者1"){
                    consumer1Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer1Count);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }
                if (consumerName == "消费者2"){
                    consumer2Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer2Count);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }
                if (consumerName == "消费者3"){
                    consumer3Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer3Count);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }

            }
        };
        // 监听队列，第二个参数：是否自动进行消息确认。
        channel.basicConsume(QUEUE_NAME, true, consumer);

    }

    /**
     * 模拟消费者消费消息并处理
     * 手动ACK，即消息被接收后，消费者需要手动发送消息确认
     * 能者多劳模式，在接收到该Consumer的ack前，rabbitMQ不会将新的Message分发给它
     * @param consumerName
     * @param costTime
     * @throws IOException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    private void receiveWitnACK(final String consumerName, final int costTime) throws IOException, TimeoutException, InterruptedException {
        // 获取到连接
        Connection connection = ConnectionUtil.getConnection();
        //创建会话通道,生产者和mq服务所有通信都在channel通道中完成
        final Channel channel = connection.createChannel();
        // 声明队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //设置每个消费者同时只能处理1条消息，在手动ack下生效
        channel.basicQos(1);
        //实现消费方法
        DefaultConsumer consumer = new DefaultConsumer(channel){
            // 获取消息，并且处理，这个方法类似事件监听，如果有消息的时候，会被自动调用
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (flag==false){
                    //为了观察消费者的消费顺序，等消息全部发完后，再开始监听，实际上是实时监听的
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (consumerName == "消费者1"){
                    consumer1Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer1Count);
                    // 手动进行ACK
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }
                if (consumerName == "消费者2"){
                    consumer2Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer2Count);
                    // 手动进行ACK
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }
                if (consumerName == "消费者3"){
                    consumer3Count++;
                    // body 即消息体
                    String msg = new String(body,"utf-8");
                    System.out.println(" ["+consumerName+"] received : " + msg + "!"+"processed："+consumer3Count);
                    // 手动进行ACK
                    channel.basicAck(envelope.getDeliveryTag(), false);
                    //模拟任务耗时1s
                    try { TimeUnit.SECONDS.sleep(costTime); } catch (Exception e) { e.printStackTrace(); }
                }

            }
        };
        // 监听队列，第二个参数：是否自动进行消息确认，
        // false：手动进行ACK
        channel.basicConsume(QUEUE_NAME, false, consumer);
    }

    /**
     * 平均分配任务
     */
    @Test
    public void test1() throws InterruptedException, TimeoutException, IOException {
        App app = new App();
        app.receive("消费者1",1);
        app.receive("消费者2",3);
        app.receive("消费者3",5);

        app.send();
        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }
    }

    /**
     * 能者多劳模式
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws IOException
     */
    @Test
    public void test2() throws InterruptedException, TimeoutException, IOException {
        App app = new App();
        app.receiveWitnACK("消费者1",1);
        app.receiveWitnACK("消费者2",3);
        app.receiveWitnACK("消费者3",5);

        app.send();
        synchronized (this){
            // 因为以上接收消息的方法是异步的（非阻塞），当采用单元测试方式执行该方法时，程序会在打印消息前结束，因此使用wait来防止程序提前终止。若使用main方法执行，则不需要担心该问题。
            wait();
        }
    }

}
