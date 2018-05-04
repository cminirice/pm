/**
 * 
 */
package com.guttv.pm.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.code.ann.TableMeta;
import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.bean.FlowBean;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * @author Peter
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CodeGenerator {
	private Configuration cfg = new Configuration(Configuration.getVersion());
	private String outDir = "D:\\code";
	/**
	 * @param args
	 */
	
	public static void main(String[] args) throws Exception{
		CodeGenerator codeGenerator = new CodeGenerator();
		List<Class> list = new ArrayList<Class>();
		
		list.add(ComponentBean.class);
		list.add(ComponentPackageBean.class);
		list.add(FlowBean.class);
		list.add(ExecuteContainer.class);
		codeGenerator.initTemplate();
		codeGenerator.process(list);
	}
	
	//处理类
	private void process(List<Class> list) {
		//先把这些类转换成Table对象
		List<CodeEntity> tables = new ArrayList<CodeEntity>();
		CodeEntity codeEntity = null;
		String entityName = null;
		for(Class clz : list) {
			codeEntity = new CodeEntity();
			codeEntity.setClz(clz.getName());
			entityName = clz.getSimpleName();
			if(entityName.endsWith("Bean")) {
				entityName = entityName.substring(0,entityName.length()-4);
			}
			codeEntity.setName(entityName);
			codeEntity.setFsName(StringUtils.uncapitalize(entityName));
			codeEntity.setUnderscores(this.addUnderscores(entityName));
			
			//处理类注解
			codeEntity.setTableMeta(new CodeTable((TableMeta)clz.getAnnotation(TableMeta.class)));
			codeEntity.setCn(codeEntity.getTableMeta().getCn());
			
			//处理属性注解
			Field[] fields = clz.getDeclaredFields();
			for(Field field : fields) {
				FieldMeta fieldMeta = field.getAnnotation(FieldMeta.class);
				if(fieldMeta == null) {
					continue;
				}
				codeEntity.addField(new CodeField(fieldMeta,field));
			}
			System.out.println(codeEntity);
			tables.add(codeEntity);
		}
		
		//遍历表写
		for(CodeEntity t : tables) {
			try {
				tableCode(t);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("处理对象["+t.getClz()+"]时出现异常：" + e.getMessage());
			}
		}
	}
	
	private void tableCode(CodeEntity codeEntity) throws Exception{
		//放参数
		Map<String,Object> root = new HashMap<String,Object>();
		//下面的“codeEntity” 不能随便更改，改过后，得改所有模板里引用的对象
		root.put("codeEntity", codeEntity);
		
		//下面生成Dao
		// 通过freeMarker解释模板，首先需要获得template
		Template template = cfg.getTemplate("dao.ftl");
		String destPath = outDir + File.separator + "dao" + File.separator + codeEntity.getName() + "Dao.java";
		enablePath(destPath);
		// 定义解释完模板后的输出
		PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		// 解释模板
		template.process(root, printWriter);
		
		//下面生成DaoImpl
		template = cfg.getTemplate("daoImpl.ftl");
		destPath = outDir + File.separator + "dao" + File.separator + "impl" + File.separator + codeEntity.getName() + "DaoImpl.java";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//下面生成Service
		template = cfg.getTemplate("service.ftl");
		destPath = outDir + File.separator + "service" + File.separator + codeEntity.getName() + "Service.java";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//下面生成ServiceImpl
		template = cfg.getTemplate("serviceImpl.ftl");
		destPath = outDir + File.separator + "service" + File.separator + "impl" + File.separator + codeEntity.getName() + "ServiceImpl.java";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//下面生成action
		template = cfg.getTemplate("action.ftl");
		destPath = outDir + File.separator + "action" + File.separator + codeEntity.getTableMeta().getPkg() + File.separator + codeEntity.getName() + "Action.java";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//下面生成list页面
		template = cfg.getTemplate("jsp_list.ftl");
		destPath = outDir + File.separator + "jsp" + File.separator + codeEntity.getTableMeta().getPkg() + File.separator + "list.jsp";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//下面生成编辑页面
		template = cfg.getTemplate("jsp_input.ftl");
		destPath = outDir + File.separator + "jsp" + File.separator + codeEntity.getTableMeta().getPkg() + File.separator + "input.jsp";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
		
		//生成详细页面
		template = cfg.getTemplate("jsp_view.ftl");
		destPath = outDir + File.separator + "jsp" + File.separator + codeEntity.getTableMeta().getPkg() + File.separator + "view.jsp";
		enablePath(destPath);
		printWriter = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));
		template.process(root, printWriter);
	}
	
	private void enablePath(String path) {
		File file = new File(path);
		File p = file.getParentFile();
		if(p.exists()) {
			return;
		}
		p.mkdirs();
	}
	
	//初始化模板
	private void initTemplate() throws Exception{
		
			String templatePath = CodeGenerator.class.getClassLoader().getResource("com/guttv/pm/code/template").getPath();
			
			// 定义从哪里加载模板文件
			cfg.setDirectoryForTemplateLoading(new File(templatePath));
			
			// 定义对象包装器
			cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.getVersion()));
			
			// 定义异常处理器
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
	}
	
	  private String addUnderscores(String param)
	  {
	    if (param == null)
	      return null;
	    StringBuffer sb = new StringBuffer(param.replace('.', '_'));
	    for (int i = 1; i < sb.length() - 1; ++i)
	    {
	      if ((!(Character.isLowerCase(sb.charAt(i - 1)))) || (!(Character.isUpperCase(sb.charAt(i)))) || (!(Character.isLowerCase(sb.charAt(i + 1)))))
	        continue;
	      sb.insert(i++, '_');
	    }
	    return sb.toString().toLowerCase();
	  }

}
