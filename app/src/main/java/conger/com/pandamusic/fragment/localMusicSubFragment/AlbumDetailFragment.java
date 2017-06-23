package conger.com.pandamusic.fragment.localMusicSubFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import conger.com.pandamusic.constants.Constants;
import conger.com.pandamusic.fragment.BaseFragment;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.utils.MusicUtils;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.DividerItemDecoration;


/**
 * Created by selfimpr on 2017/5/24.
 */

public class AlbumDetailFragment extends BaseFragment {



    @Bind(R.id.recyclerview)
    private RecyclerView recyclerView;


    private AlbumDetailAdapter mAdapter;
    private List<Music> musicInfos1 = new ArrayList<>();
    private long albumID = -1;
    private LinearLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;

    public static AlbumDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putLong("album_id", id);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumID = getArguments().getLong("album_id");
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_common, container, false);
    }

    @Override
    protected void init() {


        musicInfos1 = MusicUtils.queryMusic1(getContext(), albumID + "", Constants.START_FROM_ALBUM);


        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AlbumDetailAdapter();
        recyclerView.setAdapter(mAdapter);
        itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);


    }

    @Override
    protected void setListener() {

    }

    class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.ListItemViewHolder> {


        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {


            Music music = musicInfos1.get(position);
            holder.mainTitle.setText(music.getTitle());
            holder.title.setText(music.getArtist());

        }

        @Override
        public int getItemCount() {
            return musicInfos1.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView moreOverflow;
            TextView mainTitle, title;


            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);

                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                view.setOnClickListener(this);
                //设置弹出菜单
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //更多菜单的点击事件
                        final Music music = musicInfos1.get(getAdapterPosition());
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

            @Override
            public void onClick(View v) {

                getPlayService().play(musicInfos1.get(getAdapterPosition()));

            }
        }

    }

}
