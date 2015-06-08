package nz.co.nonameden.spotifystreamer.infrastructure.adapters;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import nz.co.nonameden.spotifystreamer.R;
import nz.co.nonameden.spotifystreamer.databinding.ItemArtistBinding;
import nz.co.nonameden.spotifystreamer.infrastructure.models.ArtistViewModel;

/**
 * Created by nonameden on 5/06/15.
 */
public class ArtistListAdapter extends BaseAdapter {

    private ArrayList<ArtistViewModel> mItems = new ArrayList<>();

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public ArtistViewModel getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemArtistBinding dataBinding = DataBindingUtil.inflate(
                    inflater, R.layout.item_artist, parent, false);
            convertView = dataBinding.getRoot();
            convertView.setTag(dataBinding);
        }
        ItemArtistBinding dataBinding = (ItemArtistBinding) convertView.getTag();
        dataBinding.setArtist(mItems.get(position));
        return convertView;
    }

    public void setItems(List<ArtistViewModel> items) {
        mItems.clear();
        if(items!=null && items.size()>0) {
            mItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    public ArrayList<ArtistViewModel> getItems() {
        return mItems;
    }
}
