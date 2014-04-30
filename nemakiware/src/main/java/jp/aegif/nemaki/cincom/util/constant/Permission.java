package jp.aegif.nemaki.cincom.util.constant;

public class Permission {
	public static final byte NONE = 0;
    public static final byte READ = 1;
    public static final byte WRITE = 2;
    public static final byte REMOVE = 4;
    public static final byte DELETE = REMOVE;
    public static final byte CREATE = 8;
    public static final byte ADMINISTRATOR = 16;
    public static final byte WORKFLOW_READ = 32;
    public static final byte WORKFLOW_WRITE = 64;

    public static final String PERMISSION_NONE = "cmis:none";
    public static final String PERMISSION_READ = "cmis:read";
    public static final String PERMISSION_WRITE = "cmis:write";
    public static final String PERMISSION_DELETE = "cmis:delete";
    public static final String PERMISSION_CREATE = "cmis:create";
    public static final String PERMISSION_ADMINISTRATOR = "cmis:all";
    public static final String PERMISSION_WORKFLOW_READ = "";
    public static final String PERMISSION_WORKFLOW_WRITE = "";
}
