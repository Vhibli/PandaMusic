package conger.com.pandamusic.fragment.localMusicSubFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.fragment.BaseFragment;
import conger.com.pandamusic.model.LocalArtistInfo;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.DividerItemDecoration;



/**
 * Created by selfimpr on 2017/5/22.
 */

public class ArtistFragment extends BaseFragment {


    @Bind(R.id.recyclerview)
    private RecyclerView recyclerView;

    private List<LocalArtistInfo> mArtistInfoList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private ArtistAdapter mAdapter;

    private RecyclerView.ItemDecoration itemDecoration;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_albun,container,false);
        return view;
    }

    @Override
    protected void init() {


        mArtistInfoList = AppCache.getLocalArtistInfo();


        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        //fastScroller = (FastScroller) view.findViewById(R.id.fastscroller);
        //new loadArtists().execute("");
        mAdapter = new ArtistAdapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void setListener() {

    }

    public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ListItemViewHolder>{


        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recyclerview_common_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {

            LocalArtistInfo mArtist = mArtistInfoList.get(position);
            holder.mainTitle.setText(mArtist.artist_name);
            holder.title.setText(mArtist.number_of_tracks+"首");
           //Glide.with(getContext()).load(mArtist.a).into(holder.draweeView);
        }

        @Override
        public int getItemCount() {
            return mArtistInfoList.size();
        }

        //ViewHolder
        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView moreOverflow;
            ImageView draweeView;
            TextView mainTitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (ImageView) view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
//                moreOverflow.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////                            MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).album_id + "", IConstants.ALBUMOVERFLOW);
////                            morefragment.show(getFragmentManager(), "album");
//                    }
//                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                ArtistDetailFragment fragment = ArtistDetailFragment.newInstance(mArtistInfoList.get(getAdapterPosition()).artist_id);
                transaction.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.container));
                transaction.add(R.id.container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }

    }

}
