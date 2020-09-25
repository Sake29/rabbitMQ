import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 模拟RPC
 * 方法1：执行App类下的main方法
 * 方法2：先执行RPCService下的main方法，再执行RPCClient下的main方法
 */
public class App extends Thread {

    @Override
    public void run() {
        RPCClient fibonacciRpc = new RPCClient();
        for (int i = 0; i < 32; i++) {
            String i_str = Integer.toString(i);
            try {
                //模拟远程调用
                fibonacciRpc.call(i_str);
            } catch (IOException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException, TimeoutException, IOException {

        //运行客户端，远程调用服务器上的计算斐波那契数列的方法
        App app = new App();
        app.start();

        //运行服务器，提供计算斐波那契数列的服务
        RPCService.execute();

    }
}
