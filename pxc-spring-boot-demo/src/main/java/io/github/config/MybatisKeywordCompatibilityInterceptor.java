// package io.github.config;
//
// import com.baomidou.mybatisplus.annotation.DbType;
// import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
// import io.github.panxiaochao.boot3.utils.SpringContextUtil;
// import org.apache.ibatis.executor.statement.StatementHandler;
// import org.apache.ibatis.mapping.BoundSql;
// import org.apache.ibatis.plugin.Interceptor;
// import org.apache.ibatis.plugin.Intercepts;
// import org.apache.ibatis.plugin.Invocation;
// import org.apache.ibatis.plugin.Signature;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
// import org.springframework.stereotype.Component;
//
// import java.sql.Connection;
//
// /**
//  * <p>
//  * SQL执行之前进行拦截处理
//  * </p>
//  *
//  * @author Lypxc
//  * @version 1.0
//  * @since 2024-08-05
//  */
// @Component
// @Intercepts({
// 		@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
// public class MybatisKeywordCompatibilityInterceptor implements Interceptor {
//
// 	public DataSourceProperties getDataSourceProperties() {
// 		return SpringContextUtil.getBean(DataSourceProperties.class);
// 	}
//
// 	@Override
// 	public Object intercept(Invocation invocation) throws Throwable {
// 		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
// 		BoundSql boundSql = statementHandler.getBoundSql();
// 		System.out.println("*****" + getDbType(getDataSourceProperties().getUrl()));
// 		String newSql = boundSql.getSql().replaceAll("good_name", "good_name as goodName");
// 		PluginUtils.mpBoundSql(boundSql).sql(newSql);
// 		return invocation.proceed();
// 	}
//
// 	/**
// 	 * 获取数据库类型
// 	 * @param jdbcUrl jdbcUrl
// 	 * @return 类型枚举值，如果没找到，则返回 null
// 	 */
// 	public static DbType getDbType(String jdbcUrl) {
// 		// 统一变小写
// 		jdbcUrl = jdbcUrl.toLowerCase();
// 		if (jdbcUrl.contains(":mysql:") || jdbcUrl.contains(":cobar:")) {
// 			return DbType.MYSQL;
// 		}
// 		else if (jdbcUrl.contains(":oracle:")) {
// 			return DbType.ORACLE;
// 		}
// 		else if (jdbcUrl.contains(":postgresql:")) {
// 			return DbType.POSTGRE_SQL;
// 		}
// 		else if (jdbcUrl.contains(":sqlserver:")) {
// 			return DbType.SQL_SERVER;
// 		}
// 		else if (jdbcUrl.contains(":db2:")) {
// 			return DbType.DB2;
// 		}
// 		else if (jdbcUrl.contains(":mariadb:")) {
// 			return DbType.MARIADB;
// 		}
// 		else if (jdbcUrl.contains(":sqlite:")) {
// 			return DbType.SQLITE;
// 		}
// 		else if (jdbcUrl.contains(":h2:")) {
// 			return DbType.H2;
// 		}
// 		else if (jdbcUrl.contains(":lealone:")) {
// 			return DbType.LEALONE;
// 		}
// 		else if (jdbcUrl.contains(":kingbase:") || jdbcUrl.contains(":kingbase8:")) {
// 			return DbType.KINGBASE_ES;
// 		}
// 		else if (jdbcUrl.contains(":dm:")) {
// 			return DbType.DM;
// 		}
// 		else if (jdbcUrl.contains(":zenith:")) {
// 			return DbType.GAUSS;
// 		}
// 		else if (jdbcUrl.contains(":oscar:")) {
// 			return DbType.OSCAR;
// 		}
// 		else if (jdbcUrl.contains(":firebird:")) {
// 			return DbType.FIREBIRD;
// 		}
// 		else if (jdbcUrl.contains(":xugu:")) {
// 			return DbType.XU_GU;
// 		}
// 		else if (jdbcUrl.contains(":clickhouse:")) {
// 			return DbType.CLICK_HOUSE;
// 		}
// 		else if (jdbcUrl.contains(":sybase:")) {
// 			return DbType.SYBASE;
// 		}
// 		else {
// 			return DbType.OTHER;
// 		}
// 	}
//
// }
