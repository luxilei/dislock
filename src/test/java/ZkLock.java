/**
 * Description
 *
 * @author : lxl
 * @version : 1.0
 * @date : 2019/4/10
 */
public class ZkLock{

    static ZkSynchronizer zkSynchronizer;

    public static ZkLock getLock(LockRegistryAndGet lockRegistry, String lockDir){
        zkSynchronizer = new ZkSynchronizer(lockRegistry,lockDir);
        return new ZkLock(lockRegistry,lockDir);
    }

    public ZkLock(LockRegistryAndGet lockRegistry, String lockDir){
        zkSynchronizer = new ZkSynchronizer(lockRegistry,lockDir);
    }

    public void lock() {
        zkSynchronizer.acquire();
    }

    public boolean tryLock() {
        return zkSynchronizer.tryAcquire();
    }

    public void unlock() {
        zkSynchronizer.release();
    }

    //Test1
    public static void main(String[] args){
        LockRegistryAndGet lockRegistryAndGet = new LockRegistryAndGet();
        ZkLock lock = new ZkLock(lockRegistryAndGet,"/test");

        lock.lock();
        System.out.println("=========== outer lock");
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("=========== outer lock1");

        lock.unlock();
        System.out.println("=========== outer unlock");
    }
}
