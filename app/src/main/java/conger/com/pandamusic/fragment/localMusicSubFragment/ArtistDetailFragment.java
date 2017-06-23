package conger.com.pandamusic.fragment.localMusicSubFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.constants.Constants;
import conger.com.pandamusic.fragment.BaseFragment;
import conger.com.pandamusic.model.LocalArtistInfo;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.utils.MusicUtils;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.DividerItemDecoration;



/**
 * Created by selfimpr on 2017/5/23.
 */

public class ArtistDetailFragment extends BaseFragment {


    @Bind(R.id.recyclerview)
    private RecyclerView recyclerView;


    private int currentlyPlayingPosition = 0;
    private long artistID = -1;
    private LinearLayoutManager layoutManager;
    private ActionBar ab;
    private List<Music> mMusicInfoList;

    ArtDetailAdapter artDetailAdapter;

    public static ArtistDetailFragment newInstance(long id) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong("artist_id", id);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong("artist_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common, container, false);
    }

    @Override
    protected void init() {

        mMusicInfoList = MusicUtils.queryMusic1(getContext(),artistID + "", Constants.START_FROM_ARTIST);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        artDetailAdapter = new ArtDetailAdapter();
        recyclerView.setAdapter(artDetailAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();

        LocalArtistInfo artistInfo = MusicUtils.getArtistinfo(getContext(), artistID);


    }


    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void setListener() {

    }

    class ArtDetailAdapter extends RecyclerView.Adapter<ArtDetailAdapter.ListItemViewHolder>{


        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_musci_common_item, parent, false));

        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {

            Music musicInfo = mMusicInfoList.get(position);

            holder.mainTitle.setText(musicInfo.getTitle());
            holder.title.setText(musicInfo.getArtist());



        }

        @Override
        public int getItemCount() {
            return mMusicInfoList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView moreOverflow;
            TextView mainTitle, title;
           // ImageView playState;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
               // this.playState = (ImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                view.setOnClickListener(this);
                //设置弹出菜单
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //更多菜单的点击事件
                        final Music music = mMusicInfoList.get(getAdapterPosition());
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                        dialog.setTitle(music.getTitle());
                        int position = getAdapterPosition();
                        int itemsId = (position == getPlayService().getPlayingPosition()) ? R.array.local_music_dialog_without_delete : R.array.local_music_dialog;
                        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:// 分享
                                        shareMusic(music);
                                        break;
                                    case 1:// 设为铃声
                                        requestSetRingtone(music);
                                        break;
                                    case 2:// 查看歌曲信息
                                        musicInfo(music);
                                        break;
                                    case 3:// 删除
                                        deleteMusic(music);
                                        break;
                                }
                            }
                        });
                        dialog.show();
                    }
                });

            }

            //播放歌曲
            @Override
            public void onClick(View v) {

                getPlayService().play(mMusicInfoList.get(getAdapterPosition()));
            }

        }

    }


}
