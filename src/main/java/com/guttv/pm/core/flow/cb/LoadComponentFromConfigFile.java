/**
 * 
 */
package com.guttv.pm.core.flow.cb;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums;
import com.guttv.pm.utils.Enums.ComponentProType;
import com.guttv.pm.utils.Enums.ComponentRunType;
import com.guttv.pm.utils.Enums.ComponentStatus;

/**
 * @author Peter
 *
 */
public class LoadComponentFromConfigFile {

	private static Logger logger = LoggerFactory.getLogger(LoadComponentFromConfigFile.class);

	/**
	 * 
	 * @param comFilePath
	 * @param componentPackage
	 * @return
	 * @throws Exception
	 */
	public static void loadFrom(File comFilePath, final ComponentPackageBean componentPackage) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(comFilePath);
		LoadComponentFromConfigFile.loadFrom(doc, componentPackage);
	}

	/**
	 * 
	 * @param is
	 * @param componentPackage
	 * @return
	 * @throws Exception
	 */
	public static void loadFrom(InputStream is, final ComponentPackageBean componentPackage) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(is);
		LoadComponentFromConfigFile.loadFrom(doc, componentPackage);
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static void loadFrom(URL url, final ComponentPackageBean componentPackage) throws Exception {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(url);
		LoadComponentFromConfigFile.loadFrom(doc, componentPackage);
	}

	/**
	 * 
	 * @param path
	 * @param componentPackage
	 * @return
	 * @throws Exception
	 */
	public static void loadFrom(String path, final ComponentPackageBean componentPackage) throws Exception {
		loadFrom(new File(path), componentPackage);
	}

	/**
	 * 
	 * <code>
	<?xml version="1.0" encoding="UTF-8"?>
	<!-- comID属性是必须字段，唯一标记一组任务 -->
	<regist comID="domain-load-program-v1.0" group="default">
	<!-- 建议一个注册文件中只有一个task节点，不是必须的。这里的一组任务共同安装、卸载 -->
		<com>
			<!--必填值。 组件类 -->
			<class>com.guttv.component.domain.program.LoadProgramComponent</class>
			<!-- 系统自动调用的方法，如果是继承自 AbstractTask 类，不用填此方法，needRead为true时，该方法必须有一个Object参数，
			needWrite为true时，该方法可以有一个返回值 -->
			<method>loadProgram</method>
			<!-- 初始化方法 方法不能有参数 -->
			<initMethod>initDBPool</initMethod>
			<!-- 关闭的方法 ，方法不能有参数，如果组件实现了 Closeable 接口，可以不用配置此方法，系统自动调用 close方法 -->
			<closeMethod>closeDBPool</closeMethod>
			<!-- 必填值。方法执行类型，目前支持 cycle、once、scheduler、rest 区分大小写-->
			<runType>cycle</runType>
			<!-- 组件的英文名称，如果不填的话，默认为类的名称简写 -->
			<name>LoadProgramComponent</name>
			<!-- 组件的中文名称，原则上需要填，尽量控制在六字以内，默认为name值 -->
			<cn>加载节目信息</cn>
			<!-- 组件状态，如果为false,注册时将不被实例化验证 -->
			<run>true</run>
			<!-- 默认为false，保存流程时，验证如果需要读的，就得有连线进入；如果需要写，就需要有连线出。 -->
			<needRead>true</needRead>
			<needWrite>true</needWrite>
			<!-- 定义读队列的类型 native://：本地队列，zk:// ：zookeeper:// ,rabbit:// :rabbit -->
			<queueType></queueType>
			<!-- 最多只能有一个receive节点。如果为空，系统默认任务名称为队列名称（queueType+name确定队列名称）。系统会自动转小写，如果该值配置，queueType值无效 ；needRead如果为false,本值无效-->
			<receive></receive>
			<!-- 可以有多个dispatch节点，配置流程时不用该节点。发送的队列不需要定义，页面画流程时自动填补，如果needWrite为false，不填补该值 -->
			<dispatch></dispatch>
			<!-- 功能的描述信息 方便画图时查看 -->
			<description><![CDATA[
				查询节目信息：
				1：只支持消费Map类型的数据
				2：如果Map中有program关键字，直接返回
				3：如果Map中有programCode关键字，按此值查询
				4：如果Map中有contentCode关键字，并且contentType关键字对应的值为2，按contentCode查询
				5：否则抛出异常，报不支持该数据
				6：poolName属性的配置必须与数据库连接池中名称一致
				7：把查询出来的节目信息以program为关键字写回
			]]></description>
			<!-- 开发配置，不能在界面修改 如果有时间类型的属性，格式为 yyyyMMddHHmmss-->
			<!-- 该部分属性会赋值给任务对象，如果组件类是AbstractTask的子类，此部分属性就会赋值给组件类 -->
			<dpros>
				<!-- 分布式锁路径 用zookeeper实现 -->
				<pro name="singleLockPath" cn="分布式锁路径"></pro>
				<!-- 消息队列的最大深度，需要写操作的组件用到，默认50000 -->
				<pro name="maxDepth" cn="队列最大深度">5000</pro>
				<!-- 需要读操作的组件可以配置该属性 从队列里读数据的超时时间 默认5000，具体情况根据队列的实际情况而定，如果是阻塞MQ，该值无效 -->
				<!-- 该值不易太大，超时时会等待下次循环 -->
				<pro name="readTimeout" cn="读超时时间">10000</pro>
				<!-- cycle类型的组件需要此属性，任务的执行周期 默认0 -->
				<pro name="period" cn="循环周期">5000</pro>
				<!-- cycle类型的组件，配置 singleLockPath 属性时用到此属性，没有获取分布式锁时的休眠时间 -->
				<pro name="lockRetryTime" cn="重获锁周期">300000</pro>
				<!-- scheduler类型的任务需要此属性 -->
				<pro name="crontab" cn="调度crontab串"></pro>
				<pro name="errorQueue" cn="处理异常时反馈通道">rabbit://domain_error</pro>
				<pro name="alarmQueue" cn="运行异常告警通道">rabbit://domain_aram</pro>
			</dpros>
			<!-- 可以在界面修改的属性 ，格式为 yyyyMMddHHmmss-->
			<!-- 该部分属性会赋值给组件对象，如果组件类是AbstractTask的子类，此部分属性就会赋值给任务对象 -->
			<pros>
				<!-- 属性name不能为空，如果cn为空，默认设值为name的值 -->
				<pro name="driver" cn="数据库驱动">com.mysql.jdbc.Driver</pro>
				<pro name="url" cn="数据库连接串"></pro>
				<pro name="user" cn="数据库用户名"></pro>
				<pro name="pwd" cn="数据库密码"></pro>
				<pro name="poolName" cn="连接池名称">c</pro>
				<pro name="maxConnections" cn="连接池最大连接数">3</pro>
				<pro name="timeout" cn="获取连接超时时间">30000</pro>
			</pros>
		</com>
	</regist>
	</code>
	 **/

	/**
	 * 
	 * @param doc
	 * @param comPackage
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void loadFrom(Document doc, final ComponentPackageBean comPackage) throws Exception {

		List<ComponentBean> coms = new ArrayList<ComponentBean>();

		Element root = doc.getRootElement();

		// 获取并校验组件标识
		String comID = root.attributeValue("comID");
		if (StringUtils.isBlank(comID)) {
			throw new Exception("根结点[" + root.getName() + "]必须要有[comID]属性，且不能为空");
		}

		if (StringUtils.isNotBlank(comPackage.getComID()) && !comPackage.getComID().equalsIgnoreCase(comID)) {
			throw new Exception("组件包对象中已经有comID值，请确认执行流程。");
		}

		String group = root.attributeValue("group");
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_COMPONENT_GROUP;
		}

		// 为了保证ZK上的路径统一，都用小写字母
		comID = comID.toLowerCase();

		// 设置组件标识
		comPackage.setComID(comID);

		// 解析文件中的组件
		List<Element> ts = root.elements("com");
		if (ts != null && ts.size() > 0) {
			ComponentBean component = null;
			String value = null;
			for (Element e : ts) {

				component = new ComponentBean();
				component.setId(DistributedSequence.getInstance().getNext());

				String clz = e.elementTextTrim("class");
				if (StringUtils.isBlank(clz)) {
					throw new Exception("组件[" + comID + "]中有class属性为空的组件");
				}

				// 方法执行类型
				value = e.elementTextTrim("runType");
				if (StringUtils.isBlank(value)) {
					throw new Exception("组件[" + comID + "][" + clz + "]中的runType属性为空");
				}
				ComponentRunType runType = Enums.getEnum(ComponentRunType.class, value);
				if (runType == null) {
					throw new Exception("组件[" + comID + "][" + clz + "]中的runType属性有不支持的类型[" + value + "]");
				}
				component.setRunType(runType);

				// 类全路径，必须字段，启动时会被实例化
				component.setClz(clz);

				// 设置版本
				component.setComID(comID);

				// 设置组
				component.setGroup(group);

				// 设置执行的方法
				component.setMethod(e.elementTextTrim("method"));

				// 初始化方法
				value = e.elementTextTrim("initMethod");
				if (StringUtils.isNotBlank(value)) {
					component.setInitMethod(value);
				}

				// 关闭化方法
				value = e.elementTextTrim("closeMethod");
				if (StringUtils.isNotBlank(value)) {
					component.setCloseMethod(value);
				}

				// 组件的名称
				value = e.elementTextTrim("name");
				if (StringUtils.isBlank(value)) {
					value = clz.substring(clz.lastIndexOf(".") + 1);
				}
				component.setName(value);

				// 组件的中文名称
				value = e.elementTextTrim("cn");
				if (StringUtils.isBlank(value)) {
					value = component.getName();
				}
				component.setCn(value);

				// 启动时的线程数，默认为1
				value = e.elementTextTrim("threadNum");
				if (!StringUtils.isBlank(value)) {
					try {
						int threadNum = Integer.parseInt(value);
						if (threadNum <= 0)
							threadNum = 1;
						component.setThreadNum(threadNum);
					} catch (Exception e1) {
						logger.warn("组件配置[" + component.getName() + "]的线程数应该是数字：" + value);
						component.setThreadNum(1);
					}
				} else {
					component.setThreadNum(1);
				}

				// 默认值为true
				value = e.elementTextTrim("run");
				if (!StringUtils.isBlank(value)) {
					if (!Boolean.parseBoolean(value)) {
						component.setStatus(ComponentStatus.FORBIDDEN.getValue());
					} else {
						component.setStatus(ComponentStatus.NORMAL.getValue());
					}
				} else {
					component.setStatus(ComponentStatus.NORMAL.getValue());
				}

				// 是否需要读
				value = e.elementTextTrim("needRead");
				component.setNeedRead(Boolean.parseBoolean(value));

				// 是否需要写
				value = e.elementTextTrim("needWrite");
				component.setNeedWrite(Boolean.parseBoolean(value));

				// 队列的类型
				component.setQueueType(e.elementTextTrim("queueType"));

				// 组件接收数据的通道 全转为小写
				value = e.elementTextTrim("receive");
				if (!StringUtils.isBlank(value)) {
					component.setReceive(value.toLowerCase());
				}

				// 组件处理完后，发送的队列 加载分发信息
				List<ComponentDispatchBean> dispatchs = new ArrayList<ComponentDispatchBean>();
				// 全转为小写
				List<Element> ls = e.elements("dispatch");
				if (ls != null && ls.size() > 0) {
					ComponentDispatchBean dis = null;
					for (Element el : ls) {
						value = el.getTextTrim();
						if (!StringUtils.isBlank(value)) {
							dis = new ComponentDispatchBean();
							dis.setId(DistributedSequence.getInstance().getNext());

							// 队列名称转小写
							dis.setQueue(value.toLowerCase());
							dis.setToComponent(clz);

							// 接收规则
							String rule = el.attributeValue("rule");
							dis.setRule(rule);

							dispatchs.add(dis);
						}
					}
				}
				component.setDispatchs(dispatchs);

				// 描述信息
				component.setDescription(e.elementTextTrim("description"));

				// 保存的组件的属性
				List<ComponentProBean> componentPros = new ArrayList<ComponentProBean>();
				component.setComponentPros(componentPros);

				// 加载可以编辑属性信息
				ComponentProBean componentPro = null;
				Element pros = e.element("pros");
				if (pros != null) {
					List<Element> prosElements = pros.elements();
					if (prosElements != null && prosElements.size() > 0) {
						for (Element pro : prosElements) {
							componentPro = new ComponentProBean();
							componentPro.setId(DistributedSequence.getInstance().getNext());

							componentPro.setType(ComponentProType.NOR);

							componentPro.setComponentClz(clz);

							// 属性的名称，不能为空
							value = pro.attributeValue("name");
							if (StringUtils.isBlank(value)) {
								// 没有名字的属性，认为是无用的，丢弃
								continue;
							}
							componentPro.setName(value);

							// 如果没有中文名字，就用英文名字代替
							value = pro.attributeValue("cn");
							if (StringUtils.isBlank(value)) {
								value = componentPro.getName();
							}
							componentPro.setCn(value);

							// 值
							componentPro.setValue(pro.getTextTrim());

							componentPros.add(componentPro);
						}
					}
				}

				// 加载程序运行属性信息
				Element dpros = e.element("dpros");
				if (dpros != null) {
					List<Element> prosElements = dpros.elements();
					if (prosElements != null && prosElements.size() > 0) {
						for (Element pro : prosElements) {
							componentPro = new ComponentProBean();
							componentPro.setId(DistributedSequence.getInstance().getNext());

							componentPro.setType(ComponentProType.DEV);

							componentPro.setComponentClz(clz);

							value = pro.attributeValue("name");
							if (StringUtils.isBlank(value)) {
								// 没有名字的属性，认为是无用的，丢弃
								continue;
							}
							componentPro.setName(value);

							// 中文名字，如果为空，设置为name
							value = pro.attributeValue("cn");
							if (StringUtils.isBlank(value)) {
								value = componentPro.getName();
							}
							componentPro.setCn(value);

							// 值
							componentPro.setValue(pro.getTextTrim());

							componentPros.add(componentPro);
						}
					}
				}

				coms.add(component);
			}
		}

		// 设置组件
		comPackage.setComponents(coms);

	}
}
