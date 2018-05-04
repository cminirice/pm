/**
 * 
 */
package com.guttv.pm.platform.action.container.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.bean.ExecuteContainer;

/**
 * @author Peter
 *
 */
public class ExecuteContainerComparator implements Comparator<ExecuteContainer> {
	private String field = null;
	private String type = null; // desc asc

	public ExecuteContainerComparator(String field, String type) {
		this.field = field;
		this.type = type;
		if (StringUtils.isBlank(field)) {
			this.field = "createTime";
		}
		if (StringUtils.isBlank(type)) {
			this.type = "desc";
		}
	}

	@Override
	public int compare(ExecuteContainer o1, ExecuteContainer o2) {
		if (field.equalsIgnoreCase("heartbeatTime")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getHeartbeat() == null || o1.getHeartbeat().getHeartbeatTime() == null) {
					return -1;
				} else {
					if (o2 == null || o2.getHeartbeat() == null || o2.getHeartbeat().getHeartbeatTime() == null) {
						return 1;
					} else {
						return o1.getHeartbeat().getHeartbeatTime().compareTo(o2.getHeartbeat().getHeartbeatTime());
					}
				}
			} else { // 降序
				if (o1 == null || o1.getHeartbeat() == null || o1.getHeartbeat().getHeartbeatTime() == null) {
					return 1;
				} else {
					if (o2 == null || o2.getHeartbeat() == null || o2.getHeartbeat().getHeartbeatTime() == null) {
						return -1;
					} else {
						return -o1.getHeartbeat().getHeartbeatTime().compareTo(o2.getHeartbeat().getHeartbeatTime());
					}
				}
			}
		} else if (field.equalsIgnoreCase("createTime")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getCreateTime() == null) {
					return -1;
				} else {
					return o1.getCreateTime().compareTo(o2.getCreateTime());
				}
			} else { // 降序
				if (o1 == null || o1.getCreateTime() == null) {
					return 1;
				} else {
					return -o1.getCreateTime().compareTo(o2.getCreateTime());
				}
			}
		} else if (field.equalsIgnoreCase("alias")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getAlias() == null) {
					return -1;
				} else {
					return o1.getAlias().compareTo(o2.getAlias());
				}
			} else { // 降序
				if (o1 == null || o1.getAlias() == null) {
					return 1;
				} else {
					return -o1.getAlias().compareTo(o2.getAlias());
				}
			}
		} else if (field.equalsIgnoreCase("ip")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getIp() == null) {
					return -1;
				} else {
					return o1.getIp().compareTo(o2.getIp());
				}
			} else { // 降序
				if (o1 == null || o1.getIp() == null) {
					return 1;
				} else {
					return -o1.getIp().compareTo(o2.getIp());
				}
			}
		} else if (field.equalsIgnoreCase("hostname")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getHostname() == null) {
					return -1;
				} else {
					return o1.getHostname().compareTo(o2.getHostname());
				}
			} else { // 降序
				if (o1 == null || o1.getHostname() == null) {
					return 1;
				} else {
					return -o1.getHostname().compareTo(o2.getHostname());
				}
			}
		} else if (field.equalsIgnoreCase("latestStartTime")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getLatestStartTime() == null) {
					return -1;
				} else {
					return o1.getLatestStartTime().compareTo(o2.getLatestStartTime());
				}
			} else { // 降序
				if (o1 == null || o1.getLatestStartTime() == null) {
					return 1;
				} else {
					return -o1.getLatestStartTime().compareTo(o2.getLatestStartTime());
				}
			}
		} else if (field.equalsIgnoreCase("status")) {
			// 升序
			if ("asc".equalsIgnoreCase(type)) {
				if (o1 == null || o1.getStatus() == null) {
					return -1;
				} else {
					return o1.getStatus().compareTo(o2.getStatus());
				}
			} else { // 降序
				if (o1 == null || o1.getStatus() == null) {
					return 1;
				} else {
					return -o1.getStatus().compareTo(o2.getStatus());
				}
			}
		}

		return 0;
	}
}
