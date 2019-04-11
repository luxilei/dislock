/**
 * Description
 *
 * @author : lxl
 * @version : 1.0
 * @date : 2019/4/10
 */
public class ZkSynchronizer {

    ThreadLocal threadLocal = new ThreadLocal();
    private LockRegistryAndGet lockRegistryAndGet;
    private String lockDir;

    public ZkSynchronizer (LockRegistryAndGet lockRegistryAndGet, String lockDir){
        this.lockRegistryAndGet = lockRegistryAndGet;
        this.lockDir = lockDir;
    }

    //获取锁并等待
    public final void acquire() {
        if(!tryAcquire()){
            chekAndWatch();
        }
    }

    private boolean chekAndWatch() {
        for (;;){
            boolean b = lockRegistryAndGet.compareAndWatch(lockDir, getCurNode());
            if(b){
                return b;
            }
        }
    }

    //标识是否获取锁
    public boolean tryAcquire(){
        //创建节点 判断当前节点是否是最小序号节点 是否最小节点
        String lockNode = lockRegistryAndGet.createLockNode(lockDir);
        lockNode = lockNode.split("/")[2];
        setCurNode(lockNode);
        return lockRegistryAndGet.tryCompare(lockDir,lockNode);
    }


    public final boolean release() {
        lockRegistryAndGet.deleteLockNode(lockDir,getCurNode());
        return true;
    }


    public void setCurNode(String lockNode){
        threadLocal.set(lockNode);
    }

    public String getCurNode(){
        return (String) threadLocal.get();
    }


    //Test1
    public static void main(String[] args){
        LockRegistryAndGet lockRegistryAndGet = new LockRegistryAndGet();
        ZkLock lock = new ZkLock(lockRegistryAndGet,"/test");

        lock.lock();

        System.out.println("=========== inner lock");
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("=========== inner lock1");

        lock.unlock();
        System.out.println("=========== inner unlock");
    }
}
