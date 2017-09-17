package mobi.acpm.inspeckage.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import mobi.acpm.inspeckage.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private HashMap<String, List<ExpandableListItem>> mListDataChild;
    private List<String> mListDataHeader;

    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<ExpandableListItem>> listChildData) {
        this.mContext = context;
        this.mListDataHeader = listDataHeader;
        this.mListDataChild = listChildData;
    }

    public Object getChild(int groupPosition, int childPosititon) {
        return ((List) this.mListDataChild.get(this.mListDataHeader.get(groupPosition))).get(childPosititon);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long) childPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ExpandableListItem childText = (ExpandableListItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.list_item, null);
        }
        ImageView iconImage = (ImageView) convertView.findViewById(R.id.imageViewIcon);
        iconImage.setImageDrawable(childText.getIcon());
        iconImage.setLayoutParams(new LayoutParams(90, 90));
        ((TextView) convertView.findViewById(R.id.txtListItem)).setText(childText.getAppName());
        ((TextView) convertView.findViewById(R.id.txtListPkg)).setText(childText.getPckName());
        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        return ((List) this.mListDataChild.get(this.mListDataHeader.get(groupPosition))).size();
    }

    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    public long getGroupId(int groupPosition) {
        return (long) groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.list_group, null);
        }
        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, 1);
        lblListHeader.setText(headerTitle);
        return convertView;
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
