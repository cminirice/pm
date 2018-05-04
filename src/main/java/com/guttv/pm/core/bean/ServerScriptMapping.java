package com.guttv.pm.core.bean;

/**
 * @author donghongchen
 * @create 2018-02-07 16:00
 **/
public class ServerScriptMapping extends BaseBean{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String code;

    private String serverCode;

    private String scriptCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public String getScriptCode() {
        return scriptCode;
    }

    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
    }

    @Override
    public String toString() {
        return "ServerScriptMapping{" +
                "code='" + code + '\'' +
                ", serverCode='" + serverCode + '\'' +
                ", scriptCode='" + scriptCode + '\'' +
                '}';
    }
}
