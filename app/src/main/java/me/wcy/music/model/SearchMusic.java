package me.wcy.music.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * JavaBean
 * Created by hzwangchenyan on 2016/1/13.
 */

// 搜索到的歌曲列表
public class SearchMusic {
    @SerializedName("song")
    private List<Song> song;

    public List<Song> getSong() {
        return song;
    }

    public void setSong(List<Song> song) {
        this.song = song;
    }

    // 歌曲（歌曲名，作者，歌曲编号）
    public static class Song {
        @SerializedName("songname")
        private String songname;
        @SerializedName("artistname")
        private String artistname;
        @SerializedName("songid")
        private String songid;

        public String getSongname() {
            return songname;
        }

        public void setSongname(String songname) {
            this.songname = songname;
        }

        public String getArtistname() {
            return artistname;
        }

        public void setArtistname(String artistname) {
            this.artistname = artistname;
        }

        public String getSongid() {
            return songid;
        }

        public void setSongid(String songid) {
            this.songid = songid;
        }
    }
}
