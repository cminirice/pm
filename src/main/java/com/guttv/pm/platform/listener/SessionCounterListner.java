/**
 * 
 */
package com.guttv.pm.platform.listener;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.stereotype.Component;

import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
@Component
public class SessionCounterListner  implements HttpSessionListener {
	private static int activeSessions =0;
	
	private static Lock lock = new ReentrantLock();

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		lock.lock();
		try {
			activeSessions++;
			event.getSession().getServletContext().setAttribute(Constants.ACTIVE_SESSION_NUMBER, activeSessions);
		}finally {
			lock.unlock();
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		lock.lock();
		try {
			activeSessions--;
			if(activeSessions<0)activeSessions=0;
			event.getSession().getServletContext().setAttribute(Constants.ACTIVE_SESSION_NUMBER, activeSessions);
		}finally {
			lock.unlock();
		}
	}  
}
