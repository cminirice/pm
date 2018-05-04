/**
 * 
 */
package com.guttv.pm.utils.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Peter
 *
 */
public class CheckSrcModify2TargetListener implements FileAlterationListener {

	protected static final Logger logger = LoggerFactory.getLogger(CheckSrcModify2TargetListener.class);
	private String targetFile = null;
	private String srcFile = null;
	

	public CheckSrcModify2TargetListener(File target, File src) throws Exception{
		targetFile = target.getAbsolutePath();
		srcFile = src.getAbsolutePath();
		if(target.isFile()) {
			throw new Exception("目标文件只能是文件夹：" + targetFile);
		}
		if(src.isFile()) {
			throw new Exception("目标文件只能是文件夹：" + srcFile);
		}
		
		if(targetFile.startsWith(srcFile)) {
			throw new Exception("源文件夹["+srcFile+"]是目标文件夹["+targetFile+"]的子目录，会导致死循环。");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onStart(org.apache.
	 * commons.io.monitor.FileAlterationObserver)
	 */
	@Override
	public void onStart(FileAlterationObserver observer) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onDirectoryCreate(
	 * java.io.File)
	 */
	@Override
	public void onDirectoryCreate(File directory) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onDirectoryChange(
	 * java.io.File)
	 */
	@Override
	public void onDirectoryChange(File directory) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onDirectoryDelete(
	 * java.io.File)
	 */
	@Override
	public void onDirectoryDelete(File directory) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onFileCreate(java.io
	 * .File)
	 */
	@Override
	public void onFileCreate(File file) {
		String path = file.getAbsolutePath();

		if (!path.startsWith(srcFile)) {
			// 不是监控的同一个目录，也就是删除的文件不是源文件夹下的
			logger.warn("修改文件[" + path + "]，不在源文件夹[" + srcFile + "]里面");
			return;
		}

		String relativePath = path.substring(srcFile.length());

		File target = Paths.get(targetFile, relativePath).toFile();

		try {
			FileUtils.copyFile(file, target);
			logger.debug("复制文件[" + file.getAbsolutePath() + "]到[" + target.getAbsolutePath() + "]");
		} catch (IOException e) {
			logger.error("复制文件[" + file.getAbsolutePath() + "]到[" + target.getAbsolutePath() + "]异常:" + e.getMessage(),
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onFileChange(java.io
	 * .File)
	 */
	@Override
	public void onFileChange(File file) {
		onFileCreate(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onFileDelete(java.io
	 * .File)
	 */
	@Override
	public void onFileDelete(File file) {
		String path = file.getAbsolutePath();

		if (!path.startsWith(srcFile)) {
			// 不是监控的同一个目录，也就是删除的文件不是源文件夹下的
			logger.warn("修改文件[" + path + "]，不在源文件夹[" + srcFile + "]里面");
			return;
		}

		String relativePath = path.substring(srcFile.length());

		File target = Paths.get(targetFile, relativePath).toFile();

		FileUtils.deleteQuietly(target);
		logger.debug("删除文件[" + target.getAbsolutePath() + "]");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.io.monitor.FileAlterationListener#onStop(org.apache.
	 * commons.io.monitor.FileAlterationObserver)
	 */
	@Override
	public void onStop(FileAlterationObserver observer) {
		// TODO Auto-generated method stub

	}

}
