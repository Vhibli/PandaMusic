package conger.com.pandamusic.fragment.localMusicSubFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import conger.com.pandamusic.R;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.fragment.BaseFragment;
import conger.com.pandamusic.model.AlbumInfo;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.DividerItemDecoration;


import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;


import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by selfimpr on 2017/5/22.
 */

public class AlbumFragment extends BaseFragment {


    @Bind(R.id.recyclerview)
    RecyclerView recyclerView;


    private LinearLayoutManager layoutManager;
    private List<AlbumInfo> mAlbumList = new ArrayList<>();
    private AlbumAdapter mAdapter;

    private RecyclerView.ItemDecoration itemDecoration;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sub_albun,container,false);
        return view;
    }

    @Override
    protected void init() {

        mAlbumList = AppCache.getAlbumList();

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new AlbumAdapter();
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void setListener() {

    }


    //设置分割线
    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ListItemViewHolder>{


        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recyclerview_common_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {

            AlbumInfo model = mAlbumList.get(position);

            holder.title.setText(model.album_name.toString());
            holder.title2.setText(model.number_of_songs + "首" + model.album_artist);
            ImageLoader.getInstance().displayImage(model.album_art,holder.draweeView);
        }



        @Override
        public int getItemCount() {
            return mAlbumList.size();
        }

        //ViewHolder
        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView moreOverflow;
            ImageView draweeView;
            TextView title, title2;

            ListItemViewHolder(View view) {
                super(view);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title2 = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (ImageView) view.findViewById(R.id.viewpager_list_img);
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                AlbumDetailFragment fragment = AlbumDetailFragment.newInstance(mAlbumList.get(getAdapterPosition()).album_id, false, null);
                transaction.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.container));
                transaction.add(R.id.container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }


    }
}
