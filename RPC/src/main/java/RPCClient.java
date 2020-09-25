import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 模拟RPC客户端，向服务器请求计算斐波那契数列
 */
public class RPCClient {

    private static final String RPC_QUEUE_NAME = "RPC_QUEUE";

    /**
     * 模拟客户端发送请求后，创建回调队列，每个客户端只创建一个
     * @param message 发送请求
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void call(String message) throws IOException, InterruptedException, TimeoutException {
        //创建连接
        Connection connection = ConnectionUtil.getConnection();
        //创建管道
        Channel channel = connection.createChannel();
        //关联id，用于关联请求和响应消息
        final String corrId = UUID.randomUUID().toString();
        //获取回调队列
        String replyQueueName = channel.queueDeclare().getQueue();
        //设置replyTo和correlationID属性值
        //构建生成器
        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties.Builder();
        //设置属性
        propertiesBuilder.correlationId(corrId);
        propertiesBuilder.replyTo(replyQueueName);
        //构建属性
        AMQP.BasicProperties properties = propertiesBuilder.build();
        //发送消息到队列
        channel.basicPublish("",RPC_QUEUE_NAME,properties,message.getBytes());
        System.out.println(" [RpcClient] Requesting fib("+message+")");
        //使用阻塞队列来存储回调结果
        //当队列容器已满，生产者线程会被阻塞，直到队列未满；当队列容器为空时，消费者线程会被阻塞，直至队列非空时为止
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        //定义消息的回退方法
        channel.basicConsume(replyQueueName,true,new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));
                }
            }
        });
        String res = response.take();
        System.out.println(" [RpcClient] Got " + res );
        channel.close();
        connection.close();
    }


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        RPCClient fibonacciRpc = new RPCClient();
        for (int i = 0; i < 32; i++) {
            String i_str = Integer.toString(i);
            fibonacciRpc.call(i_str);
        }
    }
}
