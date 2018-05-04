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
public class CheckTargetFileUndeleteListener implements FileAlterationListener {
	protected static final Logger logger = LoggerFactory.getLogger(CheckTargetFileUndeleteListener.class);
	private String targetFile = null;
	private String srcFile = null;

	public CheckTargetFileUndeleteListener(File target, File src) {
		targetFile = target.getAbsolutePath();
		srcFile = src.getAbsolutePath();
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

		if (!path.startsWith(targetFile)) {
			// 不是监控的同一个目录，也就是删除的文件不是目标文件夹下的
			logger.warn("变化的文件[" + path + "]，不在目标文件夹[" + targetFile + "]里面");
			return;
		}

		String relativePath = path.substring(targetFile.length());

		File src = Paths.get(srcFile, relativePath).toFile();
		if (!src.exists()) {
			logger.debug("源文件[" + src.getAbsolutePath() + "]不存在");
			// 源不存在
			return;
		}

		try {
			FileUtils.copyFile(src, file);
			logger.debug("复制文件[" + src.getAbsolutePath() + "]到[" + file.getAbsolutePath() + "]");
		} catch (IOException e) {
			logger.error("复制文件[" + src.getAbsolutePath() + "]到[" + file.getAbsolutePath() + "]异常:" + e.getMessage(), e);
		}
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

	}
}
