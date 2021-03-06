package com.cf.gp;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cf.gp.model.StockPriceHistory;
import com.cf.gp.service.StockPriceHistoryService;

/**
 * 
-- 118W的数据量 只有5个字段的小表  MYSQL5.7
第一次 ： code为BTREE索引方法，Normal索引类型
1.
SELECT * from t_stock_price_history t where t.S_CODE = '000002'  -- code有索引第一次执行用时0.065 之后同样运行 0.001
SELECT * from t_stock_price_history t where t.S_NAME = '万 科Ａ' -- name没索引第一次执行用时1.257 之后同样运行 1.329

第二次：code_name_date的复合索引，BTREE索引方法，Normal索引类型
1.
SELECT * from t_stock_price_history t where t.S_CODE = '000002'  -- 第一次执行用时0.119 之后同样运行 0.001，第三次再执行完name条件查询后再一次执行code的查询用时：0.127
SELECT * from t_stock_price_history t where t.S_NAME = '万 科Ａ' -- 第一次执行用时1.297 之后同样运行 1.302  （说明没有用到符合索引）

2.
SELECT * from t_stock_price_history t where t.S_CODE = '000002' and t.S_NAME = '万 科Ａ' 第一次执行0.083，之后同样运行 0.001
SELECT * from t_stock_price_history t where t.S_NAME = '万 科Ａ' and t.S_CODE = '000002' 第一次执行0.107，之后同样运行 0.001
SELECT * from t_stock_price_history t where t.S_CODE = '000002' and t.S_NAME = '万 科Ａ' and S_DATE = '2018-01-10' 第一次执行0.047
 *
 */
public class StockPriceHistoryTest {

	private ClassPathXmlApplicationContext app;
	
	@Before
	public void init() {
		app = new ClassPathXmlApplicationContext(new String[]{"spring/spring.xml","mybatis/spring-mybatis.xml"});
	}
	
	/**
	 * 区间数据
	 */
//	@Test
	public void queryInfoWithInsertDB() {
		//第一次 exec time = 43536 - 59466条记录
		//第二次 exec time = 373330 - 527369条记录 （20170501 - 20180109）
		//第三次 exec time = 514094 - 650525条记录 （20160501 - 20170430）
		//第四次 exec time = 90622 - 196027条记录 （20160101 - 20160430）
		
		//改表结构后
		//第一次 exec time = 87141 - 196027条记录 （20160101 - 20160430）
		//第二次 exec time = 232306 - 650525条记录 （20160501 - 20170430）
		//第三次 exec time = 154648 - 527369条记录 （20170501 - 20180117）
		long start = System.currentTimeMillis();
		StockPriceHistoryService stockPriceHistoryService = (StockPriceHistoryService)app.getBean("stockPriceHistoryService");
		boolean result = stockPriceHistoryService.queryHistoryDataWithInsertDB("20170501", "20180117");
		long end = System.currentTimeMillis();
		System.out.println(result);
		System.out.println("exec time = " + String.valueOf(end - start));
	}
	
	/**
	 * 实时数据 - 新浪的请求多了会封IP
	 */
	@Test
	public void queryDataWithInsertDB() {
		//第一次 exec time = 31160 - 3411条记录 （30多次http）
		//第二次 exec time = 40016 - 3410条记录 （30多次http） 36页  38次请求 - 2018-01-11
		
		//改表结构后
		//第一次 exec time = 31160 - 3411条记录 （30多次http）
		long start = System.currentTimeMillis();
		StockPriceHistoryService stockPriceHistoryService = (StockPriceHistoryService)app.getBean("stockPriceHistoryService");
		boolean result = stockPriceHistoryService.queryDataWithInsertDB();
		long end = System.currentTimeMillis();
		System.out.println(result);
		System.out.println("exec time = " + String.valueOf(end - start));
	}
	
//	@Test
	public void queryDataWithInsertDBq() {
		StockPriceHistoryService stockPriceHistoryService = (StockPriceHistoryService)app.getBean("stockPriceHistoryService");
		List<StockPriceHistory> queryOneDayDataByDate = stockPriceHistoryService.queryOneDayDataByDate("2018-01-12");
		System.out.println(queryOneDayDataByDate.size());
	}
	
}
