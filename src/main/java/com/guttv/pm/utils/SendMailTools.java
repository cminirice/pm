/**
 * 
 */
package com.guttv.pm.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Peter
 *
 */
public class SendMailTools {
	public static void main(String[] a) throws Exception {
		sendMail("test", "test","shideming@guttv.cn");
	}

	static class MyAuthenricator extends Authenticator {
		String user = null;
		String pass = "";

		public MyAuthenricator(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, pass);
		}

	}

	public static void sendMail(String subject, String msg, String toUser) throws Exception {
		FileInputStream fis = null;
		Properties pro = null;
		try {
			fis = new FileInputStream(SendMailTools.class.getClassLoader().getResource("mail.properties").getPath());
			pro = new Properties();
			pro.load(new BufferedReader(new InputStreamReader(fis, "UTF-8")));

			String fromUser = pro.getProperty("from.user");
			String fromPwd = pro.getProperty("from.password");
			String fromEmail = pro.getProperty("from.address");

			SendMailTools.sendEMail(pro, fromUser, fromPwd, fromEmail, toUser, subject, msg, null);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	public static void sendMail(String subject, String msg, String toUser, List<String> files) throws Exception {
		FileInputStream fis = null;
		Properties pro = null;
		try {
			fis = new FileInputStream(SendMailTools.class.getClassLoader().getResource("mail.properties").getPath());
			pro = new Properties();
			pro.load(new BufferedReader(new InputStreamReader(fis, "UTF-8")));

			String fromUser = pro.getProperty("from.user");
			String fromPwd = pro.getProperty("from.password");
			String fromEmail = pro.getProperty("from.address");

			SendMailTools.sendEMail(pro, fromUser, fromPwd, fromEmail, toUser, subject, msg, files);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	/**
	 * 
	 * @param pro
	 *            邮箱服务器信息
	 * @param fromUser
	 *            发送邮箱用户名
	 * @param fromPwd
	 *            发送邮箱用户密码
	 * @param fromEmail
	 *            发送邮箱
	 * @param toEmail
	 *            多个目标地址时，用英文逗号隔开
	 * @param subject
	 *            邮件主题
	 * @param msg
	 *            邮件内容
	 * @param files
	 *            附件
	 * @throws Exception
	 */
	public static void sendEMail(Properties pro, String fromUser, String fromPwd, String fromEmail, String toEmail,
			String subject, String msg, List<String> files) throws Exception {
		// 从配置文件中读取配置信息

		// 根据邮件的回话属性构造一个发送邮件的Session
		MyAuthenricator authenticator = new MyAuthenricator(fromUser, fromPwd);
		Session session = Session.getInstance(pro, authenticator);
		// 监控邮件命令

		// 根据Session 构建邮件信息
		Message message = new MimeMessage(session);
		// 创建邮件发送者地址
		Address from = new InternetAddress(fromEmail);
		// 设置邮件消息的发送者
		message.setFrom(from);
		if (StringUtils.isNotBlank(toEmail)) {
			// 创建邮件的接收者地址
			Address[] to = InternetAddress.parse(toEmail);
			// 设置邮件接收人地址
			message.setRecipients(Message.RecipientType.TO, to);
			message.setSubject(subject);
			// 邮件容器
			MimeMultipart mimeMultiPart = new MimeMultipart();
			// 设置HTML
			BodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent(msg, "text/html;charset=utf-8");
			mimeMultiPart.addBodyPart(bodyPart);
			// 添加附件

			if (files != null) {
				BodyPart attchPart = null;
				for (int i = 0; i < files.size(); i++) {
					if (StringUtils.isNotBlank(files.get(i))) {
						attchPart = new MimeBodyPart();
						// 附件数据源
						DataSource source = new FileDataSource(files.get(i));
						// 将附件数据源添加到邮件体
						attchPart.setDataHandler(new DataHandler(source));
						// 设置附件名称为原文件名
						attchPart.setFileName(MimeUtility.encodeText(source.getName()));
						mimeMultiPart.addBodyPart(attchPart);
					}
				}
			}
			message.setContent(mimeMultiPart);
			message.setSentDate(new Date());
			// 保存邮件
			message.saveChanges();
			// 发送邮件
			Transport.send(message);
		}

	}
}
