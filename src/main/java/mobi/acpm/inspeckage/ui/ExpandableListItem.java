package mobi.acpm.inspeckage.ui;

import android.graphics.drawable.Drawable;

public class ExpandableListItem {
    private String mAppName = "";
    private Drawable mIcon;
    private boolean mIsSelected = false;
    private String mPackageName = "";

    public String getAppName() {
        return this.mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public String getPckName() {
        return this.mPackageName;
    }

    public void setPckName(String packName) {
        this.mPackageName = packName;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    public boolean isSelected() {
        return this.mIsSelected;
    }

    public void setSelected(boolean bypassed) {
        this.mIsSelected = bypassed;
    }
}
