/**
 * 
 */
package com.guttv.pm.core.flow.cb;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.support.ann.CloseMethod;
import com.guttv.pm.support.ann.ComponentAnn;
import com.guttv.pm.support.ann.ExecuteMethod;
import com.guttv.pm.support.ann.ExecuteMethod.ExecuteType;
import com.guttv.pm.support.ann.InitMethod;
import com.guttv.pm.support.ann.ProAnn;
import com.guttv.pm.support.ann.ProAnn.ProType;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums;
import com.guttv.pm.utils.Enums.ComponentProType;
import com.guttv.pm.utils.Enums.ComponentRunType;
import com.guttv.pm.utils.ReflectUtil;
import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
public class LoadComponentFromAnnotation {
	private static Logger logger = LoggerFactory.getLogger(LoadComponentFromAnnotation.class);

	/**
	 * 从JAR包里加载组件信息，如果这些JAR包里有多个组件，要求comID必须显示定义统一的一个，一个组件包里不允许有多个comID，可以有多个组
	 * 
	 * @param jars
	 * @param componentPackage
	 * @param loader
	 * @throws Exception
	 */
	public static void loadFromAnn(URL[] jars, final ComponentPackageBean componentPackage, ClassLoader loader)
			throws Exception {
		List<Class<?>> componentClzs = new ArrayList<Class<?>>();

		// 先从JAR包中找到所有的组件类
		JarFile jarFile = null;
		Enumeration<JarEntry> entries = null;
		JarEntry jarEntry = null;
		String entry = null;
		Class<?> clz = null;
		for (URL jar : jars) {
			jarFile = new JarFile(jar.getFile());
			entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				jarEntry = entries.nextElement();
				if (jarEntry.isDirectory()) {
					continue;
				}
				entry = jarEntry.getName();
				if (!entry.endsWith(".class")) {
					continue;
				}
				try {
					clz = loader.loadClass(Utils.resourceAsClassName(entry));
					if (clz.getAnnotation(ComponentAnn.class) != null) {
						componentClzs.add(clz);
					}
				} catch (Throwable e) {
					logger.error(e.getMessage());
				}
			}
		}

		if (componentClzs.size() == 0) {
			throw new Exception("没有发现一个注解的组件类");
		}

		List<ComponentBean> coms = new ArrayList<ComponentBean>();
		ComponentAnn comAnn = null;
		ComponentBean component = null;
		String comID = null; // 统一的comID,一个组件包中只能有一个comID
		for (Class<?> c : componentClzs) {

			try {
				comAnn = c.getAnnotation(ComponentAnn.class);
				String id = comAnn.comID();

				// 如果comID为空时，计算出来一个
				if (StringUtils.isBlank(id)) {
					id = className2ComID(c.getSimpleName());
				}

				// 这里记录统一的comID
				if (StringUtils.isBlank(comID)) {
					comID = id.toLowerCase();
				} else {
					// comID必须都一样，否则卸载的时候会有问题
					if (!comID.equalsIgnoreCase(id)) {
						logger.error("发现有不一样的comID[" + comID + "][" + id + "]");
						throw new Exception("一个组件包中有多个组件时，comID必须显式统一");
					}
				}

				component = new ComponentBean();
				component.setComID(comID);
				component.setId(DistributedSequence.getInstance().getNext());

				// 组
				String value = comAnn.group();
				if (StringUtils.isBlank(value)) {
					value = Constants.DEFAULT_COMPONENT_GROUP;
				}
				component.setGroup(value);

				// 组件类
				component.setClz(c.getName());

				// 组件名称
				value = comAnn.name();
				if (StringUtils.isBlank(value)) {
					value = c.getSimpleName();
				}
				component.setName(value);

				// 组件中文名
				value = comAnn.cn();
				if (StringUtils.isBlank(value)) {
					value = component.getName();
				}
				component.setCn(value);

				component.setReceive(comAnn.receive());

				component.setDescription(comAnn.description());

				// 保存的组件的属性
				List<ComponentProBean> componentPros = new ArrayList<ComponentProBean>();
				component.setComponentPros(componentPros);

				// 处理属性
				Field[] fields = ReflectUtil.getDeclaredFields(c);
				if (fields != null && fields.length > 0) {
					ComponentProBean componentPro = null;
					ProAnn proAnn = null;
					Object instance = null; // 实例化该对象是为了获取对象中的默认值
					for (Field field : fields) {

						proAnn = field.getAnnotation(ProAnn.class);
						// 有属性注解的字段，才认为是需要读取的
						if (proAnn == null) {
							continue;
						}

						componentPro = new ComponentProBean();
						componentPro.setComponentClz(c.getName());
						componentPro.setId(DistributedSequence.getInstance().getNext());

						String cn = proAnn.cn();
						if (StringUtils.isNotBlank(cn)) {
							componentPro.setCn(cn);
						} else {
							componentPro.setCn(field.getName());
						}

						componentPro.setName(field.getName());

						if (ProType.DEV.equals(proAnn.type())) {
							componentPro.setType(ComponentProType.DEV);
						} else {
							componentPro.setType(ComponentProType.NOR);
						}

						// 获取默认值
						boolean access = field.isAccessible();
						field.setAccessible(true);
						if (instance == null) {
							// 实例化是为了获取默认值
							instance = c.newInstance();
						}
						Object def = field.get(instance);
						if (def != null) {
							componentPro.setValue(String.valueOf(def));
						}
						field.setAccessible(access);

						componentPros.add(componentPro);
					}
				}

				RestController restAnn = c.getAnnotation(RestController.class);
				if (restAnn != null) {
					// 这里是Rest类型的
					component.setNeedRead(false);
					component.setNeedWrite(false);
					component.setRunType(ComponentRunType.Rest);
				} else {
					// 开始处理方法，每个方法只能有一个方法注解，下面处理时，发现一个后就不再往后处理
					Method[] methods = c.getMethods();
					for (Method m : methods) {

						ExecuteMethod em = m.getAnnotation(ExecuteMethod.class);
						if (em != null) {
							component.setMethod(m.getName());

							// 判断是不是要消费数据
							Class<?>[] paramTypes = m.getParameterTypes();
							if (paramTypes == null || paramTypes.length == 0) {
								component.setNeedRead(false);
							} else if (paramTypes.length != 1) {
								throw new Exception(
										"组件类[" + c.getName() + "]的方法[" + m.getName() + "]配置为执行方法，要求必须只能有一个参数");
							} else {
								component.setNeedRead(true);
							}

							// 是否生产数据
							Class<?> returnType = m.getReturnType();
							if (returnType == null || "void".equalsIgnoreCase(returnType.getName())) {
								component.setNeedWrite(false);
							} else {
								component.setNeedWrite(true);
							}

							// 执行方式
							ExecuteType type = em.type();
							if (type == null) {
								type = ExecuteType.Once;
							}

							ComponentRunType runType = Enums.getEnum(ComponentRunType.class, type.getValue());
							if (runType == null) {
								throw new Exception("不支持的执行方式[" + type.getValue() + "]");
							}
							component.setRunType(runType);

							// 如果是调度任务，检查是不是有crontab字段
							checkCrontab(component);

							continue; // 防止有多个方法注解
						}

						// 关闭方法
						CloseMethod cm = m.getAnnotation(CloseMethod.class);
						if (cm != null) {
							if (m.getParameterTypes().length != 0) {
								throw new Exception("关闭方法不能有参数");
							}
							component.setCloseMethod(m.getName());
							continue;
						}

						// 初始化方法
						InitMethod im = m.getAnnotation(InitMethod.class);
						if (im != null) {
							if (m.getParameterTypes().length != 0) {
								throw new Exception("初始化方法不能有参数");
							}
							component.setInitMethod(m.getName());
							continue;
						}
					}

					if (StringUtils.isBlank(component.getMethod())) {
						throw new Exception("组件中没有发现执行方法,必须有一个ExecuteMethod注解的方法");
					}
				}

				coms.add(component);
			} catch (Throwable e) {
				logger.error("处理class[" + c.getName() + "]异常：" + e.getMessage(), e);
				// 一个包中有一个组件解析失败，则认为是失败
				throw new Exception("处理class[" + c.getName() + "]异常：" + e.getMessage());
			}

			// 都解析成功了再设置值
			componentPackage.setComponents(coms);
			componentPackage.setComID(comID);
		}

	}

	/**
	 * 
	 * @param com
	 * @throws Exception
	 */
	private static void checkCrontab(ComponentBean com) throws Exception {
		if (com.getRunType() == null || !ComponentRunType.Scheduler.equals(com.getRunType())) {
			return;
		}

		if (com.isNeedRead()) {
			throw new Exception("任务类型[" + ComponentRunType.Scheduler + "]不支持读数据，支持方法不能有参数");
		}

		List<ComponentProBean> componentPros = com.getComponentPros();
		// 标记有没有crontab字段
		boolean flag = false;
		for (ComponentProBean cpb : componentPros) {
			if ("crontab".equals(cpb.getName()) && ComponentProType.DEV.equals(cpb.getType())) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			ComponentProBean cpb = new ComponentProBean();
			cpb.setComponentClz(com.getClz());
			cpb.setCn("调度crontab串");
			cpb.setName("crontab");
			cpb.setId(DistributedSequence.getInstance().getNext());
			cpb.setValue(null);
			cpb.setType(ComponentProType.DEV);
			componentPros.add(cpb);
		}
	}

	/**
	 * 把类的全称变成comID
	 * 
	 * @param clzName
	 * @return
	 */
	private static String className2ComID(String clzName) {
		StringBuilder sb = new StringBuilder();
		sb.append(clzName.charAt(0));
		for (int i = 1; i < clzName.length(); i++) {
			if (Character.isUpperCase(clzName.charAt(i))) {
				sb.append("_").append(clzName.charAt(i));
			} else {
				sb.append(clzName.charAt(i));
			}
		}
		sb.append("-v1.0");
		return sb.toString().toLowerCase();
	}
}
