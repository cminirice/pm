/**
 * 
 */
package com.guttv.pm.core.flow;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.cache.ComClassLoaderCache;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.flow.cb.LoadComponentFromAnnotation;
import com.guttv.pm.core.flow.cb.LoadComponentFromConfigFile;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.support.ann.Writeable;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Decompression;
import com.guttv.pm.utils.Enums.ComponentRunType;
import com.guttv.pm.utils.Enums.ComponentStatus;
import com.guttv.pm.utils.FtpUtil;
import com.guttv.pm.utils.IOUtil;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.MD5Util;

/**
 * @author Peter
 *
 */
public class ComPackRegisteManager {

	private static Logger logger = LoggerFactory.getLogger(ComPackRegisteManager.class);

	public static void main(String[] a) throws Exception {
		ComponentPackageBean comPack = new ComponentPackageBean();
		comPack.setComPackageFilePath("D:\\data\\task\\task-word.zip");
		ComPackRegisteManager.getInstance().registComPack(comPack);
		// com.guttv.pm.frame.flow.Flow
		// com.guttv.pm.frame.task.GuttvWordCount
		Object obj = ComClassLoaderCache.getInstance().getInstance("word-v1.0",
				"com.guttv.pm.frame.task.GuttvWordCount");
		System.out.println("###################" + obj);

		Object obj1 = obj;

		ComPackRegisteManager.getInstance().unRegistComPack("word-v1.0");
		try {
			obj = null;
			obj = ComClassLoaderCache.getInstance().getInstance("word-v1.0", "com.guttv.pm.frame.task.GuttvWordCount");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("###################" + obj);
		// Class clz = Class.forName("com.guttv.pm.frame.task.WordCreator");
		// System.out.println("2222######################" + clz.newInstance());

		comPack = new ComponentPackageBean();
		comPack.setComPackageFilePath("D:\\data\\task\\task-word.jar");
		ComPackRegisteManager.getInstance().registComPack(comPack);
		obj = ComClassLoaderCache.getInstance().getInstance("word-v1.0", "com.guttv.pm.frame.task.GuttvWordCount");
		System.out.println("2222222222222222222  " + obj);

		Object obj2 = obj;

		System.out.println("+++++++++++++++++++++  " + (obj1 == obj2));

		ComPackRegisteManager.getInstance().unRegistComPack("word-v1.0");
		try {
			obj = null;
			obj = ComClassLoaderCache.getInstance().getInstance("word-v1.0", "com.guttv.pm.frame.task.GuttvWordCount");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("333333333333333333" + obj);
	}

	/**
	 * 把组件包上传到FTP服务器，顺便计算MD5值
	 * 
	 * @param localPath
	 * @return
	 * @throws Exception
	 */
	public ComponentPackageBean uploadAndCreate(ComponentPackageBean comPack, File localPath) throws Exception {

		// 校验是否存在
		if (!localPath.exists()) {
			throw new Exception("组件包文件[" + localPath.getAbsolutePath() + "]不存在");
		}

		// 不能是目录
		if (localPath.isDirectory()) {
			throw new Exception("目前只支持jar或者zip两种格式的组件包，不支持文件夹");
		}

		// 校验类型
		String name = localPath.getName().toLowerCase();
		if (!(name.endsWith(".jar") || name.endsWith(".zip"))) {
			throw new Exception("目前只支持jar或者zip两种格式的组件包，不支持文件：" + localPath.getName());
		}

		// 获取FTP上传地址
		String ftpServer = ConfigCache.getInstance().getProperty(Constants.COM_FTP_SERVER, null);
		if (StringUtils.isBlank(ftpServer)) {
			throw new Exception("请配置组件包上传的FTP服务器");
		}

		// 上传FTP
		String fileName = localPath.getName();
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		String md5 = MD5Util.getFileMD5(localPath);
		String serverPath = ftpServer + "/" + md5 + suffix;
		try {
			logger.info("上传组件包[" + localPath.getAbsolutePath() + "]至：" + serverPath);
			if (!IOUtil.uploadToFTPForRetry(localPath.getAbsolutePath(), serverPath)) {
				throw new Exception("组件包[" + localPath.getAbsolutePath() + "]上传服务器[" + serverPath + "]失败");
			}
		} catch (Exception e) {
			String error = "组件包[" + localPath.getAbsolutePath() + "]上传服务器[" + serverPath + "]失败:" + e.getMessage();
			logger.error(error, e);
			throw new Exception(error);
		}

		//
		if (comPack == null) {
			comPack = new ComponentPackageBean();
		}

		// 设置MD5
		comPack.setMd5(md5);
		comPack.setSrcFileName(fileName);
		comPack.setComPackageFilePath(serverPath);

		return comPack;
	}

	/**
	 * 
	 * @param zipFile
	 * @throws Exception
	 */
	private void registComPackFromZip(ComponentPackageBean comPack, File zipFile) throws Exception {

		File toFile = new File(Constants.TMP_DIR + RandomStringUtils.randomAlphanumeric(5));

		logger.info("开始注册文件：" + zipFile.getAbsolutePath());
		logger.debug("解压至：" + toFile.getAbsolutePath());

		// 把注册包解压
		List<File> files = Decompression.unzip(zipFile, toFile);

		List<URL> jarFile = new ArrayList<URL>();
		// 加载JAR包
		for (File file : files) {
			// 加载jar包
			if (file.getName().toLowerCase().endsWith(".jar")) {
				jarFile.add(file.toURI().toURL());
			}
		}

		// 缓存类加载器
		ComponentClassLoader urlClassLoader = null;
		if (jarFile.size() > 0) {
			logger.info("缓存组件类加载器...");
			urlClassLoader = new ComponentClassLoader(jarFile.toArray(new URL[jarFile.size()]),
					ClassLoader.getSystemClassLoader());
		}

		int xmlCount = 0;
		for (File file : files) {
			if (file.getName().toLowerCase().endsWith("com.xml")) {
				xmlCount++;
			}
		}

		// 如果没有配置文件，试着用注解找
		if (xmlCount == 0) {
			LoadComponentFromAnnotation.loadFromAnn(jarFile.toArray(new URL[jarFile.size()]), comPack, urlClassLoader);
		}

		// 有切只能有一个配置文件
		if (xmlCount > 1) {
			String error = "配置zip文件中必须且仅只能有一个组件配置文件，[" + zipFile + "]文件中有[" + xmlCount + "]个组件配置文件";
			logger.error(error);
			throw new Exception(error);
		}

		try {
			// 加载组件配置
			for (File file : files) {
				if (file.getName().toLowerCase().endsWith("com.xml")) {
					logger.info("加载组件配置信息....");
					LoadComponentFromConfigFile.loadFrom(file, comPack);
				}
			}

			// 校验版本
			String comID = comPack.getComID();
			logger.info("校验组件版本信息....");
			ComponentPackageBean old = ComponentPackageCache.getInstance().getComPack(comID);
			if (old != null) {
				throw new ComPackExistException("标识为[" + comID + "]的组件已经存在，请先卸载！");
			}

			// 校验组件配置
			logger.info("校验组件的有效性...");
			checkComPackConfig(comPack, urlClassLoader);

			if (urlClassLoader != null) {
				ComClassLoaderCache.getInstance().cacheComponentClassLoader(comPack.getComID(), urlClassLoader);
			}

			// 缓存组件包
			logger.info("缓存组件包信息...");
			old = ComponentPackageCache.getInstance().cacheComPack(comPack);
			if (old != null) {
				logger.info("注册包[comID=" + old.getComID() + "]被替换：" + JsonUtil.toJson(old));
			}

			// 缓存组件
			logger.info("缓存组件信息...");
			ComponentCache.getInstance().cacheComponent(comPack.getComponents());

		} finally {
			// 使用完清理掉
			FileUtils.deleteQuietly(toFile);
		}
	}

	private void registComPackFromJar(ComponentPackageBean comPack, File jarFile) throws Exception {

		logger.info("启动类加载器....");
		ComponentClassLoader urlClassLoader = new ComponentClassLoader(new URL[] { jarFile.toURI().toURL() },
				ClassLoader.getSystemClassLoader());

		List<String> comXmlPaths = Decompression.getResource(jarFile, Constants.COMPONENT_CONFIG_PATTERN);

		// 有切只能有一个配置文件
		if (comXmlPaths == null || comXmlPaths.size() != 1) {
			LoadComponentFromAnnotation.loadFromAnn(new URL[] { jarFile.toURI().toURL() }, comPack, urlClassLoader);
		} else {
			// 加载组件配置
			logger.info("加载组件配置信息....");
			LoadComponentFromConfigFile.loadFrom(urlClassLoader.getResourceAsStream(comXmlPaths.get(0)), comPack);
		}

		// 校验版本
		logger.info("校验组件版本信息....");
		String comID = comPack.getComID();
		ComponentPackageBean old = ComponentPackageCache.getInstance().getComPack(comID);
		if (old != null) {
			IOUtils.closeQuietly(urlClassLoader);
			throw new ComPackExistException("标识为[" + comID + "]的组件已经存在，请先卸载！");
		}

		// 校验组件配置
		logger.info("校验组件的有效性...");
		checkComPackConfig(comPack, urlClassLoader);

		// 缓存类加载器
		logger.info("缓存组件类加载器...");
		ComClassLoaderCache.getInstance().cacheComponentClassLoader(comPack.getComID(), urlClassLoader);

		// 缓存组件包
		logger.info("缓存组件包信息...");
		old = ComponentPackageCache.getInstance().cacheComPack(comPack);
		if (old != null) {
			logger.info("注册包[comID=" + old.getComID() + "]被替换：" + JsonUtil.toJson(old));
		}

		// 缓存组件
		logger.info("缓存组件信息...");
		ComponentCache.getInstance().cacheComponent(comPack.getComponents());

	}

	/**
	 * 注册，默认缓存组件的classloader
	 * 
	 * @param comPack
	 * @throws Exception
	 */
	public synchronized void registComPack(ComponentPackageBean comPack) throws Exception {
		registComPack(comPack, true);
	}

	/**
	 * 每个ZIP文件中只能有一个xxxcom.xml 组件配置文件
	 * 
	 * @param zipFile
	 * @param cacheClassLoader
	 *            是否缓存classLoader
	 * @throws Exception
	 */
	public synchronized void registComPack(ComponentPackageBean comPack, boolean cacheClassLoader) throws Exception {

		String srcPath = comPack.getComPackageFilePath();

		if (StringUtils.isBlank(srcPath)) {
			throw new Exception("组件文件不能为空");
		}

		// 下载到本地

		File localFile = new File(srcPath);
		boolean flag = false; // 记录一下是否经过下载
		try {
			//
			if (!localFile.exists()) {
				String destPath = Constants.TMP_DIR + new File(comPack.getComPackageFilePath()).getName();
				logger.info("下载组件包：" + comPack.getComPackageFilePath());
				IOUtil.downLoadForRetry(new URL(comPack.getComPackageFilePath()), destPath);
				flag = true;
				localFile = new File(destPath);

				String md5 = MD5Util.getFileMD5(localFile);
				if (StringUtils.isNotBlank(comPack.getMd5()) && !comPack.getMd5().equalsIgnoreCase(md5)) {
					throw new Exception("组件包[" + comPack.getComPackageFilePath() + "]原始MD5值[" + comPack.getMd5()
							+ "]，与本地MD5值[" + md5 + "]不同");
				}
			}

			if (comPack.getId() == null || comPack.getId() < 1) {
				comPack.setId(DistributedSequence.getInstance().getNext());
			}

			String name = localFile.getName().toLowerCase();
			if (name.endsWith(".zip")) { // 按zip注册
				registComPackFromZip(comPack, localFile);
			} else if (name.endsWith(".jar")) { // 按jar 包注册
				registComPackFromJar(comPack, localFile);
			} else {
				throw new Exception("不支持的文件类型：" + comPack.getComPackageFilePath());
			}

			// 如果不是在本机运行，可以在此处把classLoader卸掉，避免占用过多内存
			if (!cacheClassLoader) {
				logger.debug("卸载[" + comPack.getComID() + "]的ClassLoader");
				ComClassLoaderCache.getInstance().uncacheComponentCassLoader(comPack.getComID());
			}else {
				URLClassLoader urlClassLoader = ComClassLoaderCache.getInstance().getComponentClassLoader(comPack.getComID());
				if(urlClassLoader != null) {
					((ComponentClassLoader) urlClassLoader).tryInitApplicationContext(comPack.getComID());
				}
			}
		} catch (ComPackExistException e) {
			// 如果是组件包已经存在的异常，不能卸载。
			throw e;
		} catch (Exception e) {
			try {
				// 出异常时卸载
				unRegistComPack(comPack.getComID());
			} catch (Exception e1) {
				logger.error("卸载组件包[" + comPack.getComPackageFilePath() + "][" + comPack.getComID() + "]时出现异常："
						+ e.getMessage(), e);
			}
			throw e;
		} finally {
			// 如果是下载下来的，用完删除
			if (flag) {
				FileUtils.deleteQuietly(localFile);
			}
		}
	}

	/**
	 * 卸载组件包
	 * 
	 * @param comID
	 * @param deleteJar
	 *            是否删除服务器上的组件包
	 */
	public void unRegistComPack(String comID, boolean deleteJar) {

		if (StringUtils.isBlank(comID)) {
			return;
		}

		// 清除类加载器
		logger.info("卸载组件[" + comID + "]类加载器....");
		ComClassLoaderCache.getInstance().uncacheComponentCassLoader(comID);

		// 卸组件包缓存
		logger.info("卸载组件包[" + comID + "]信息....");
		ComponentPackageBean comPack = ComponentPackageCache.getInstance().uncacheComPack(comID);

		// 卸载组件缓存
		logger.info("卸载组件[" + comID + "]组件信息....");
		ComponentCache.getInstance().uncacheComponent(comID);

		if (deleteJar && comPack != null && StringUtils.isNotBlank(comPack.getComPackageFilePath())) {
			FtpUtil ftpUtil = null;
			try {
				// 将要删除路径
				logger.info("将要删除组件文件：" + comPack.getComPackageFilePath());
				ftpUtil = FtpUtil.getFtpUtil(comPack.getComPackageFilePath());
				ftpUtil.login();
				if (ftpUtil.deleteFile(new URL(comPack.getComPackageFilePath()).getPath())) {
					logger.info("删除组件成功：" + comPack.getComPackageFilePath());
				} else {
					logger.warn("删除组件失败：" + comPack.getComPackageFilePath());
				}
			} catch (Exception ex) {
				logger.error("删除组件包异常：" + comPack.getComPackageFilePath(), ex);
			} finally {
				if (ftpUtil != null) {
					try {
						ftpUtil.logout();
					} catch (Exception e1) {
					}
					ftpUtil = null;
				}
			}
		}

	}

	/**
	 * 卸载一个组件
	 * 
	 * @param comID
	 */
	public void unRegistComPack(String comID) {
		unRegistComPack(comID, true);
	}

	/**
	 * 校验TASK的有效性
	 * 
	 * @param componentPackage
	 * @throws Exception
	 */
	private void checkComPackConfig(ComponentPackageBean componentPackage, ComponentClassLoader urlClassLoader)
			throws Exception {

		// 校验组件
		List<ComponentBean> components = componentPackage.getComponents();
		if (components == null || components.size() == 0) {
			throw new Exception("组件包[" + componentPackage.getComPackageFilePath() + "]中没有解析出任何组件");
		}

		// 校验组件是否存在 只有启动状态的才会验证，不是启动用状态的组件可以用来先暂时注入组件
		for (ComponentBean com : components) {

			// 校验该组件包中的类有没有和其它组件包的类一样的
			ComponentBean old = ComponentCache.getInstance().getComponent(com.getClz());
			if (old != null) {
				if (!com.getComID().equals(old.getComID())) {
					throw new Exception("本组件包与标识为[" + old.getComID() + "]有组件包中有同名的class:" + com.getClz());
				}
			}

			// 验证实例化
			if (com.getStatus() != null && com.getStatus().equals(ComponentStatus.NORMAL.getValue())) {

				Object instance = urlClassLoader.newInstance(com.getClz());

				// 如果不是系统定义的类，需要判断其执行方法是否存在
				if (!(instance instanceof AbstractTask) && !ComponentRunType.Rest.equals(com.getRunType())) {
					Method method = null;
					if (com.isNeedRead()) {
						method = instance.getClass().getMethod(com.getMethod(), Object.class);
					} else {
						method = instance.getClass().getMethod(com.getMethod());
					}

					if (com.isNeedWrite()) {
						Class<?> returnType = method.getReturnType();
						if ("void".equalsIgnoreCase(returnType.getName())) {
							throw new Exception("组件[comID=" + com.getComID() + "][" + com.getClz() + "]的方法["
									+ com.getMethod() + "]配置需要有写操作[needWrite=" + com.isNeedWrite() + "]，方法需要有返回值");
						}
					}
				}

				// 校验初始化方法
				if (StringUtils.isNotBlank(com.getInitMethod())) {
					instance.getClass().getMethod(com.getInitMethod());
				}

				// 校验关闭方法
				if (StringUtils.isNotBlank(com.getCloseMethod())) {
					instance.getClass().getMethod(com.getCloseMethod());
				}

				// rest类型的接口校验
				if (ComponentRunType.Rest.equals(com.getRunType())) {
					Class<?> clz = instance.getClass();
					RestController restAnn = clz.getAnnotation(RestController.class);
					if (restAnn == null) {
						throw new Exception("组件[comID=" + com.getComID() + "][" + com.getClz()
								+ "]发布为Rest接口类型，但没有找到RestController注解");
					}
					// 校验方法

					Method[] methods = clz.getMethods();
					boolean flag = false; // 标记有rest方法
					for (Method method : methods) {
						RequestMapping rm = method.getAnnotation(RequestMapping.class);
						if (rm != null) {
							flag = true;
							String[] values = rm.value();
							if (values != null && values.length > 0) {
								for (String value : values) {
									if (StringUtils.isBlank(restAnn.value() + value)) {
										throw new Exception(
												"组件类[" + com.getClz() + "]方法[" + method.getName() + "]的访问地址为空");
									}
									flag = true;
								}
							}
						}

						// 有一个可写的方法，标记该接口类可写
						Writeable writeable = method.getAnnotation(Writeable.class);
						if (writeable != null) {
							com.setNeedWrite(true);
						}
					}
					if (!flag) {
						throw new Exception("组件类[" + com.getClz() + "]发布为Rest类型接口，但没有发现有效的方法");
					}
				}
			}
		}

		// 校验组件的其它有效性

	}

	/**
	 * 此方法在JAR包重新注册时，不能把以前的类卸载 加载组件JAR包
	 * 
	 * @param jarFile
	 * @throws Exception
	 */
	@Deprecated
	public void loadJar(File jarFile) throws Exception {

		URLClassLoader urlLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysClass = URLClassLoader.class;
		boolean accessible = false;
		Method method = null;
		try {
			// 改变方法的可见性（即通过反映访问本来不可以访问的方法）
			method = sysClass.getDeclaredMethod("addURL", new Class[] { URL.class });
			accessible = method.isAccessible();
			method.setAccessible(true);
			method.invoke(urlLoader, jarFile.toURI().toURL());
		} finally {
			if (method != null) {
				try {
					method.setAccessible(accessible);
				} catch (Exception e) {
				}
			}
		}

	}

	// 下面是单例配置
	private ComPackRegisteManager() {
	}

	private static ComPackRegisteManager instance = new ComPackRegisteManager();

	public static synchronized ComPackRegisteManager getInstance() {
		return instance;
	}
}
