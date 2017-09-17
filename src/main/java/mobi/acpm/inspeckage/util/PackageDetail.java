package mobi.acpm.inspeckage.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PathPermission;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import mobi.acpm.inspeckage.Module;

public class PackageDetail {
    private ApplicationInfo mAppInfo;
    private Context mContext;
    private PackageInfo mPInfo;
    private SharedPreferences mPrefs;
    private PackageManager pm;

    public PackageDetail(Context context, String app) {
        this.mPrefs = context.getSharedPreferences(Module.PREFS, 1);
        this.mContext = context;
        this.pm = context.getPackageManager();
        for (PackageInfo pi : context.getPackageManager().getInstalledPackages(0)) {
            if ((!this.mPrefs.getBoolean(Config.SP_SWITCH_OUA, true) || (pi.applicationInfo.flags & 129) == 0) && pi.packageName.equals(app)) {
                try {
                    this.mPInfo = this.pm.getPackageInfo(app, 128);
                    this.mPInfo.gids = this.pm.getPackageInfo(app, 256).gids;
                    this.mPInfo.activities = this.pm.getPackageInfo(app, 1).activities;
                    this.mPInfo.providers = this.pm.getPackageInfo(app, 8).providers;
                    this.mPInfo.receivers = this.pm.getPackageInfo(app, 2).receivers;
                    this.mPInfo.services = this.pm.getPackageInfo(app, 4).services;
                    this.mPInfo.applicationInfo.sharedLibraryFiles = this.pm.getPackageInfo(app, 1024).applicationInfo.sharedLibraryFiles;
                    this.mPInfo.permissions = this.pm.getPackageInfo(app, 4096).permissions;
                    this.mPInfo.requestedPermissions = this.pm.getPackageInfo(app, 4096).requestedPermissions;
                    this.mAppInfo = pi.applicationInfo;
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Intent getLaunchIntent() {
        return this.pm.getLaunchIntentForPackage(getPackageName());
    }

    public String getPackageName() {
        String pkg_name = "";
        if (this.mPInfo != null) {
            return this.mPInfo.packageName;
        }
        return pkg_name;
    }

    public String getAppName() {
        return "" + this.mPInfo.applicationInfo.loadLabel(this.mContext.getPackageManager()).toString();
    }

    public String getVersion() {
        return this.mPInfo.versionName;
    }

    public String getRequestedPermissions() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.requestedPermissions != null) {
            for (String perm : this.mPInfo.requestedPermissions) {
                sb.append(perm + "\n");
            }
        } else {
            sb.append("-- Permissions\n");
        }
        return sb.toString();
    }

    public String getAppPermissions() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.permissions != null) {
            for (PermissionInfo perm : this.mPInfo.permissions) {
                sb.append(perm.name + "\n");
            }
        } else {
            sb.append("-- Permissions\n");
        }
        return sb.toString();
    }

    public String getExportedActivities() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.activities != null) {
            for (ActivityInfo ai : this.mPInfo.activities) {
                if (ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedActivities() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.activities != null) {
            for (ActivityInfo ai : this.mPInfo.activities) {
                if (!ai.exported) {
                    sb.append(ai.name + "\n");
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedServices() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.services != null) {
            for (ServiceInfo si : this.mPInfo.services) {
                if (si.exported) {
                    if (si.permission != null) {
                        sb.append(si.name + " PERM: " + si.permission + "\n");
                    } else {
                        sb.append(si.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedServices() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.services != null) {
            for (ServiceInfo si : this.mPInfo.services) {
                if (!si.exported) {
                    if (si.permission != null) {
                        sb.append(si.name + " PERM: " + si.permission + "\n");
                    } else {
                        sb.append(si.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedBroadcastReceivers() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.receivers != null) {
            for (ActivityInfo ai : this.mPInfo.receivers) {
                if (ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedBroadcastReceivers() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.receivers != null) {
            for (ActivityInfo ai : this.mPInfo.receivers) {
                if (!ai.exported) {
                    if (ai.permission != null) {
                        sb.append(ai.name + " PERM: " + ai.permission + "\n");
                    } else {
                        sb.append(ai.name + "\n");
                    }
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getExportedContentProvider() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.providers != null) {
            for (ProviderInfo pi : this.mPInfo.providers) {
                String piName = pi.name;
                if (pi.exported) {
                    piName = piName + " GRANT: " + String.valueOf(pi.grantUriPermissions) + "|";
                    if (pi.authority != null) {
                        piName = piName + " AUTHORITY: " + pi.authority + "|";
                    }
                    if (pi.readPermission != null) {
                        piName = piName + " READ: " + pi.readPermission + "|";
                    }
                    if (pi.writePermission != null) {
                        piName = piName + " WRITE: " + pi.writePermission + "|";
                    }
                    PathPermission[] pp = pi.pathPermissions;
                    if (pp != null) {
                        for (PathPermission pathPermission : pp) {
                            piName = ((piName + " PATH: " + pathPermission.getPath() + "|") + "  - READ: " + pathPermission.getReadPermission() + "|") + "  - WRITE: " + pathPermission.getWritePermission() + "|";
                        }
                    }
                    sb.append(piName + "\n");
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getNonExportedContentProvider() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.providers != null) {
            for (ProviderInfo pi : this.mPInfo.providers) {
                String piName = pi.name;
                if (!pi.exported) {
                    piName = piName + " GRANT: " + String.valueOf(pi.grantUriPermissions) + "|";
                    if (pi.authority != null) {
                        piName = piName + " AUTHORITY: " + pi.authority + "|";
                    }
                    if (pi.readPermission != null) {
                        piName = piName + " READ: " + pi.readPermission + "|";
                    }
                    if (pi.writePermission != null) {
                        piName = piName + " WRITE: " + pi.writePermission + "|";
                    }
                    PathPermission[] pp = pi.pathPermissions;
                    if (pp != null) {
                        for (PathPermission pathPermission : pp) {
                            piName = ((piName + " PATH: " + pathPermission.getPath() + "|") + "  - READ: " + pathPermission.getReadPermission() + "|") + "  - WRITE: " + pathPermission.getWritePermission() + "|";
                        }
                    }
                    sb.append(piName + "\n");
                }
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public String getUID() {
        String uid = "";
        if (this.mAppInfo != null) {
            return String.valueOf(this.mAppInfo.uid);
        }
        return "-- null";
    }

    public String getProcessName() {
        String pname = "";
        if (this.mAppInfo != null) {
            return this.mAppInfo.processName;
        }
        return "-- null";
    }

    public String getDataDir() {
        String dir = "";
        if (this.mAppInfo != null) {
            return this.mAppInfo.dataDir;
        }
        return dir;
    }

    public String getGIDs() {
        String gidList = "";
        if (this.mPInfo.gids == null || this.mPInfo.gids.length == 0) {
            gidList = "-- null";
        } else {
            for (int gid : this.mPInfo.gids) {
                gidList = gidList + "" + gid + "-";
            }
        }
        return gidList.substring(0, gidList.length() - 1);
    }

    public String isDebuggable() {
        Boolean isDebuggable = Boolean.valueOf(false);
        if ((this.mAppInfo.flags & 2) != 0) {
            isDebuggable = Boolean.valueOf(true);
        }
        return String.valueOf(isDebuggable);
    }

    public String allowBackup() {
        Boolean allow = Boolean.valueOf(false);
        if ((this.mAppInfo.flags & 32768) != 0) {
            allow = Boolean.valueOf(true);
        }
        return String.valueOf(allow);
    }

    public String getIconBase64() {
        String icon = "";
        if (this.mAppInfo != null) {
            return Util.imageToBase64(this.mAppInfo.loadIcon(this.pm));
        }
        return icon;
    }

    public String getSharedUserId() {
        String suserid = "";
        if (this.mPInfo.sharedUserId != null) {
            return this.mPInfo.sharedUserId;
        }
        return "-- null";
    }

    public String getApkDir() {
        String sourceDir = "";
        if (this.mPInfo.applicationInfo.publicSourceDir != null) {
            return this.mPInfo.applicationInfo.publicSourceDir;
        }
        return sourceDir;
    }

    public String getSharedLibraries() {
        StringBuilder sb = new StringBuilder();
        if (this.mPInfo.applicationInfo.sharedLibraryFiles != null) {
            for (String sl : this.mPInfo.applicationInfo.sharedLibraryFiles) {
                sb.append(sl + "\n");
            }
        } else {
            sb.append(" -- null");
        }
        return sb.toString();
    }

    public void extractInfoToFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Package: " + getPackageName() + "\n");
        sb.append("Process Name: " + getProcessName() + "\n");
        sb.append("APK Dir: " + getApkDir() + "\n");
        sb.append("UID: " + getUID() + "\n");
        sb.append("GIDs: " + getGIDs() + "\n");
        sb.append("Is Debuggable: " + isDebuggable() + "\n");
        sb.append("Allow Backup: " + allowBackup() + "\n");
        sb.append("Shared User ID: " + getSharedUserId() + "\n");
        sb.append(getRequestedPermissions());
        sb.append(getAppPermissions());
        sb.append(getExportedActivities());
        sb.append(getNonExportedActivities());
        sb.append(getExportedServices());
        sb.append(getNonExportedServices());
        sb.append(getExportedBroadcastReceivers());
        sb.append(getNonExportedBroadcastReceivers());
        sb.append(getExportedContentProvider());
        sb.append(getNonExportedContentProvider());
        sb.append(getSharedLibraries());
        FileUtil.writeToFile(this.mPrefs, sb.toString(), FileType.PACKAGE, "");
    }
}
