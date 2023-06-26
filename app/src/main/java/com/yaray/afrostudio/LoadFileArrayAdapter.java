package com.yaray.afrostudio;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.core.content.ContextCompat;

public class LoadFileArrayAdapter extends ArrayAdapter<String> {

    private static class ViewHolder {
        private TextView textview;
        private TextView textview_author;

        ViewHolder() { // nothing to do here
        }
    }

    private final LayoutInflater inflater;
    private List<String> strings;

    public LoadFileArrayAdapter(final Context context, final int resource, final int textViewResourceId, final List<String> stringList) {
        super(context, resource, textViewResourceId, stringList);

        this.strings = stringList;
        this.inflater = LayoutInflater.from(context);

    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        View itemView = convertView;
        ViewHolder holder = null;
        final String thisString = getItem(position);

        if (null == itemView) {
            itemView = this.inflater.inflate(R.layout.dialog_load_file_name, parent, false);
            holder = new ViewHolder();
            holder.textview = (TextView) itemView.findViewById(R.id.file_textview);
            holder.textview_author = (TextView) itemView.findViewById(R.id.file_textview_author);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        //Log.e("LoadFileArrayAdapter" , thisString);

        if (thisString.equals("Local") || thisString.equals("Server")){
            holder.textview.setText(thisString);
            holder.textview.setTextSize(14);
            holder.textview.setGravity(Gravity.CENTER);
            holder.textview.setTextColor(ContextCompat.getColor(holder.textview.getContext(), R.color.green_afrostudio));
            holder.textview_author.setTextSize(0);
            holder.textview_author.setText("");
            holder.textview_author.setTextColor(ContextCompat.getColor(holder.textview_author.getContext(), R.color.green_afrostudio));
        } else if(thisString.contains(".afr")){ // Local
            String name = thisString.substring(0, thisString.indexOf("_by_"));
            holder.textview.setTextSize(18);
            holder.textview.setGravity(Gravity.LEFT);
            holder.textview.setText(name);
            holder.textview.setTextColor(ContextCompat.getColor(holder.textview.getContext(), R.color.gray));
            String author = thisString.substring(thisString.indexOf("_by_")+4, thisString.indexOf("_u_"));
            String user = thisString.substring(thisString.indexOf("_u_")+3, thisString.indexOf(".afr"));
            holder.textview_author.setTextSize(14);
            holder.textview_author.setText(author + " (" + user + ")");
            holder.textview_author.setTextColor(ContextCompat.getColor(holder.textview_author.getContext(), R.color.gray));
        } else { // Remote
            String name = thisString.substring(0, thisString.indexOf("_by_"));
            holder.textview.setTextSize(18);
            holder.textview.setGravity(Gravity.LEFT);
            holder.textview.setText(name);
            holder.textview.setTextColor(ContextCompat.getColor(holder.textview.getContext(), R.color.gray));
            String author = thisString.substring(thisString.indexOf("_by_")+4, thisString.indexOf("_u_"));
            String user = thisString.substring(thisString.indexOf("_u_")+3, thisString.length());
            holder.textview_author.setTextSize(14);
            holder.textview_author.setText(author + " (" + user + ")");
            holder.textview_author.setTextColor(ContextCompat.getColor(holder.textview_author.getContext(), R.color.gray));
        }


        return itemView;
    }

}
