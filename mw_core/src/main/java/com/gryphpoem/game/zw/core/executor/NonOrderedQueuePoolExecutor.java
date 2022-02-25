package com.gryphpoem.game.zw.core.executor;

import java.util.concurrent.*;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.Handler;
import com.gryphpoem.game.zw.core.util.LogUtil;

public class NonOrderedQueuePoolExecutor extends ThreadPoolExecutor {
	public NonOrderedQueuePoolExecutor(int corePoolSize) {
		super(corePoolSize, corePoolSize, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
	}

	public NonOrderedQueuePoolExecutor(int corePoolSize, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, corePoolSize, 30, TimeUnit.SECONDS, workQueue);
	}

	public void execute(Handler command) {
		Work work = new Work(command);
		execute(work);
	}

	private static class Work implements Runnable {

		private Handler command;

		public Work(Handler command) {
			this.command = command;
		}

		public void run() {
			long start = System.currentTimeMillis();
			try {
				command.action();
			} catch (MwException e) {
				LogUtil.error(e.getMessage(), e);

				if (command instanceof AbsClientHandler) { // 返回错误消息
					AbsClientHandler handler = (AbsClientHandler) command;
					handler.sendErrorMsgToPlayer(e.getCode());
				}
			} catch (Exception e) {
				LogUtil.error("command.getClass().getSimpleName() Exception", e);
			} finally {
				long end = System.currentTimeMillis();
				if (end - start > 50) {
					LogUtil.error("NonOrderedQueuePoolExecutor-->" + command.getClass().getSimpleName() + " run:"
							+ (end - start));
				}
			}

		}
	}
}
