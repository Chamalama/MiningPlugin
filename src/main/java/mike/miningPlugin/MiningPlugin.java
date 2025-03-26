package mike.miningPlugin;

import co.aikar.commands.PaperCommandManager;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import mike.miningPlugin.command.MiningCMD;
import mike.miningPlugin.listener.MiningListener;
import mike.miningPlugin.service.Services;
import mike.miningPlugin.service.task.MiningBlockReplaceTask;
import mike.miningPlugin.service.task.MiningClearTask;
import mike.miningPlugin.service.task.MiningTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.*;

@Getter
public final class MiningPlugin extends JavaPlugin {

    public static MiningPlugin INSTANCE;

    public Services services;

    public MiningListener miningListener;

    public PaperCommandManager paperCommandManager;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService blockExecutor = Executors.newScheduledThreadPool(2);
    private final ThreadFactory threadFactory = new DefaultThreadFactory("MINING");
    private final ExecutorService cachedThread = Executors.newCachedThreadPool(threadFactory);

    @Override
    public void onEnable() {
        INSTANCE = this;
        services = new Services(this);
        miningListener = new MiningListener(this);
        paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new MiningCMD(services));

        schedule(new MiningTask(services), 35, 10);
        executor.scheduleAtFixedRate(new MiningClearTask(services), 0, 2, TimeUnit.MINUTES);
        blockExecutor.scheduleAtFixedRate(new MiningBlockReplaceTask(services), 0, 1, TimeUnit.SECONDS);

    }

    @Override
    public void onDisable() {
        executor.shutdown();
        blockExecutor.shutdown();
    }

    public void schedule(Runnable runnable, long delay, long period) {
        cachedThread.submit(() -> {
            while(true) {
                try{
                    Thread.sleep(delay);
                    runnable.run();
                    if(period <= 0) {
                        return;
                    }
                }catch (InterruptedException ignored) {

                }
            }
        });
    }

}
