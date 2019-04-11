import com.lxl.zk.base.Constant;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 连接ZK注册中心，创建服务注册目录
 */
public class LockRegistryAndGet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LockRegistryAndGet.class);

    private final String LOCK_NODE_NAME = "/lockNode";
    private final CountDownLatch latch = new CountDownLatch(1);
    private final CountDownLatch lockLatch = new CountDownLatch(1);

    private ZooKeeper zk;

    public LockRegistryAndGet() {
        zk = connectServer();
    }

//    public void register(String data) {
//        if (data != null) {
//            zk = connectServer();
//            if (zk != null) {
//                createNode(Constant.ZK_DATA_PATH, data);
//            }
//        }
//    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
           zk = new ZooKeeper(Constant.ZK_CONNECT, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    // 判断是否已连接ZK,连接后计数器递减.
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });

            // 若计数器不为0,则等待.直到链接
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    public String createLockNode(String lockDir) {
        String curChildPath = null;
        try {
            Stat exists = zk.exists(lockDir, null);
            if(null == exists){
                zk.create(lockDir,"".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            String nodePath = lockDir + LOCK_NODE_NAME;
            curChildPath = zk.create(nodePath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create zookeeper node ({})", curChildPath);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
            e.printStackTrace();
        }
        return curChildPath;
    }

    public List<String> getLockChildrens(String lockDir) {
        List<String> childrens = null;
        try {
            childrens = zk.getChildren(lockDir, null);
            LOGGER.info("getChildrens zookeeper node ({} => {})", lockDir);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
            e.printStackTrace();
        }
        return childrens;
    }

    public boolean tryCompare(String lockDir,String currentChild) {
        List<String> childrens = this.getLockChildrens(lockDir);
        Collections.sort(childrens);
        String mimChildren = childrens.get(0);
        return mimChildren.compareTo(currentChild) >= 0;
    }

    public boolean compareAndWatch(String lockDir,String currentChild) {
        List<String> childrens = this.getLockChildrens(lockDir);
        Collections.sort(childrens);
        String mimChildren = childrens.get(0);
        try {
            if(mimChildren.compareTo(currentChild) >= 0){
                return true;
            }else {
                zk.exists(lockDir + "/" + mimChildren, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getState() == Event.KeeperState.SyncConnected && event.getType() == Event.EventType.NodeDeleted) {
                            lockLatch.countDown();
                        }
                    }
                });
            }
            lockLatch.await();
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteLockNode(String lockDir,String currentChild) {
        try {

            zk.delete(lockDir +"/"+ currentChild,0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return true;
    }
}