package io.github.test.handle;

import io.github.panxiaochao.boot3.operate.log.core.domain.OperateLogDomain;
import io.github.panxiaochao.boot3.operate.log.core.handler.AbstractOperateLogHandler;
import io.github.test.service.HandlerService;
import jakarta.annotation.Resource;

/**
 * @author Lypxc
 * @since 2023-07-03
 */
// @Component
public class OperateLogHandler extends AbstractOperateLogHandler {

	@Resource
	private HandlerService handlerService;

	@Override
	public void saveOperateLog(OperateLogDomain operateLogDomain) {
		logger.info("OperateLog Db");
		handlerService.hello();
	}

}
