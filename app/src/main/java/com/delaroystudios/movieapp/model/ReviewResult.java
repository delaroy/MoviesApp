package com.delaroystudios.movieapp.model;

/**
 * Created by delaroy on 8/25/18.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewResult implements Parcelable
{

    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("url")
    @Expose
    private String url;
    public final static Creator<ReviewResult> CREATOR = new Creator<ReviewResult>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ReviewResult createFromParcel(Parcel in) {
            return new ReviewResult(in);
        }

        public ReviewResult[] newArray(int size) {
            return (new ReviewResult[size]);
        }

    }
            ;

    protected ReviewResult(Parcel in) {
        this.author = ((String) in.readValue((String.class.getClassLoader())));
        this.content = ((String) in.readValue((String.class.getClassLoader())));
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.url = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public ReviewResult() {
    }

    /**
     *
     * @param id
     * @param content
     * @param author
     * @param url
     */
    public ReviewResult(String author, String content, String id, String url) {
        super();
        this.author = author;
        this.content = content;
        this.id = id;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(author);
        dest.writeValue(content);
        dest.writeValue(id);
        dest.writeValue(url);
    }

    public int describeContents() {
        return 0;
    }

}