package com.example.raelee.moodyj;

import java.util.List;

public class MusicDescriptorData {

    // DTO Generator로 생성한 클래스

    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("tonal")
    private Tonal tonal;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("rhythm")
    private Rhythm rhythm;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("metadata")
    private Metadata metadata;
    @com.google.gson.annotations.Expose
    @com.google.gson.annotations.SerializedName("lowlevel")
    private Lowlevel lowlevel;

    public Tonal getTonal() {
        return tonal;
    }

    public void setTonal(Tonal tonal) {
        this.tonal = tonal;
    }


    public Rhythm getRhythm() {
        return rhythm;
    }

    public void setRhythm(Rhythm rhythm) {
        this.rhythm = rhythm;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Lowlevel getLowlevel() {
        return lowlevel;
    }

    public void setLowlevel(Lowlevel lowlevel) {
        this.lowlevel = lowlevel;
    }

    public static class Tonal {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("chords_scale")
        private String chords_scale;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("chords_key")
        private String chords_key;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("tuning_diatonic_strength")
        private double tuning_diatonic_strength;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("chords_number_rate")
        private double chords_number_rate;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("chords_changes_rate")
        private double chords_changes_rate;

        public String getChords_scale() {
            return chords_scale;
        }

        public void setChords_scale(String chords_scale) {
            this.chords_scale = chords_scale;
        }

        public String getChords_key() {
            return chords_key;
        }

        public void setChords_key(String chords_key) {
            this.chords_key = chords_key;
        }

        public double getTuning_diatonic_strength() {
            return tuning_diatonic_strength;
        }

        public void setTuning_diatonic_strength(double tuning_diatonic_strength) {
            this.tuning_diatonic_strength = tuning_diatonic_strength;
        }

        public double getChords_number_rate() {
            return chords_number_rate;
        }

        public void setChords_number_rate(double chords_number_rate) {
            this.chords_number_rate = chords_number_rate;
        }

        public double getChords_changes_rate() {
            return chords_changes_rate;
        }

        public void setChords_changes_rate(double chords_changes_rate) {
            this.chords_changes_rate = chords_changes_rate;
        }
    }

    public static class Rhythm {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("onset_rate")
        private double onset_rate;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("danceability")
        private double danceability;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("bpm")
        private double bpm;

        public double getOnset_rate() {
            return onset_rate;
        }

        public void setOnset_rate(double onset_rate) {
            this.onset_rate = onset_rate;
        }

        public double getDanceability() {
            return danceability;
        }

        public void setDanceability(double danceability) {
            this.danceability = danceability;
        }

        public double getBpm() {
            return bpm;
        }

        public void setBpm(double bpm) {
            this.bpm = bpm;
        }
    }

    public static class Metadata {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("tags")
        private Tags tags;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("audio_properties")
        private Audio_properties audio_properties;

        public Tags getTags() {
            return tags;
        }

        public void setTags(Tags tags) {
            this.tags = tags;
        }

        public Audio_properties getAudio_properties() {
            return audio_properties;
        }

        public void setAudio_properties(Audio_properties audio_properties) {
            this.audio_properties = audio_properties;
        }
    }

    public static class Tags {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("tracknumber")
        private List<String> tracknumber;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("title")
        private List<String> title;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("discnumber")
        private List<String> discnumber;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("date")
        private List<String> date;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("artist")
        private List<String> artist;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("albumartist")
        private List<String> albumartist;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("album")
        private List<String> album;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("file_name")
        private String file_name;

        public List<String> getTracknumber() {
            return tracknumber;
        }

        public void setTracknumber(List<String> tracknumber) {
            this.tracknumber = tracknumber;
        }

        public List<String> getTitle() {
            return title;
        }

        public void setTitle(List<String> title) {
            this.title = title;
        }

        public List<String> getDiscnumber() {
            return discnumber;
        }

        public void setDiscnumber(List<String> discnumber) {
            this.discnumber = discnumber;
        }

        public List<String> getDate() {
            return date;
        }

        public void setDate(List<String> date) {
            this.date = date;
        }

        public List<String> getArtist() {
            return artist;
        }

        public void setArtist(List<String> artist) {
            this.artist = artist;
        }

        public List<String> getAlbumartist() {
            return albumartist;
        }

        public void setAlbumartist(List<String> albumartist) {
            this.albumartist = albumartist;
        }

        public List<String> getAlbum() {
            return album;
        }

        public void setAlbum(List<String> album) {
            this.album = album;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }
    }

    public static class Audio_properties {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("md5_encoded")
        private String md5_encoded;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("codec")
        private String codec;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("sample_rate")
        private int sample_rate;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("replay_gain")
        private double replay_gain;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("number_channels")
        private int number_channels;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("lossless")
        private int lossless;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("length")
        private double length;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("bit_rate")
        private int bit_rate;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("analysis")
        private Analysis analysis;

        public String getMd5_encoded() {
            return md5_encoded;
        }

        public void setMd5_encoded(String md5_encoded) {
            this.md5_encoded = md5_encoded;
        }

        public String getCodec() {
            return codec;
        }

        public void setCodec(String codec) {
            this.codec = codec;
        }

        public int getSample_rate() {
            return sample_rate;
        }

        public void setSample_rate(int sample_rate) {
            this.sample_rate = sample_rate;
        }

        public double getReplay_gain() {
            return replay_gain;
        }

        public void setReplay_gain(double replay_gain) {
            this.replay_gain = replay_gain;
        }

        public int getNumber_channels() {
            return number_channels;
        }

        public void setNumber_channels(int number_channels) {
            this.number_channels = number_channels;
        }

        public int getLossless() {
            return lossless;
        }

        public void setLossless(int lossless) {
            this.lossless = lossless;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public int getBit_rate() {
            return bit_rate;
        }

        public void setBit_rate(int bit_rate) {
            this.bit_rate = bit_rate;
        }

        public Analysis getAnalysis() {
            return analysis;
        }

        public void setAnalysis(Analysis analysis) {
            this.analysis = analysis;
        }
    }

    public static class Analysis {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("downmix")
        private String downmix;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("start_time")
        private int start_time;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("sample_rate")
        private int sample_rate;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("length")
        private double length;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("equal_loudness")
        private int equal_loudness;

        public String getDownmix() {
            return downmix;
        }

        public void setDownmix(String downmix) {
            this.downmix = downmix;
        }

        public int getStart_time() {
            return start_time;
        }

        public void setStart_time(int start_time) {
            this.start_time = start_time;
        }

        public int getSample_rate() {
            return sample_rate;
        }

        public void setSample_rate(int sample_rate) {
            this.sample_rate = sample_rate;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public int getEqual_loudness() {
            return equal_loudness;
        }

        public void setEqual_loudness(int equal_loudness) {
            this.equal_loudness = equal_loudness;
        }
    }

    public static class Lowlevel {
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("dynamic_complexity")
        private double dynamic_complexity;
        @com.google.gson.annotations.Expose
        @com.google.gson.annotations.SerializedName("average_loudness")
        private double average_loudness;

        public double getDynamic_complexity() {
            return dynamic_complexity;
        }

        public void setDynamic_complexity(double dynamic_complexity) {
            this.dynamic_complexity = dynamic_complexity;
        }

        public double getAverage_loudness() {
            return average_loudness;
        }

        public void setAverage_loudness(double average_loudness) {
            this.average_loudness = average_loudness;
        }
    }
}
