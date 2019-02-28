package ch.supsi.ist.camre.paths.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by milan antonovic on 22/08/14.
 */
public class Author {

    private String nickname;
    private String uid;
    private String timestamp;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public static Author getTestAuthor(){
        Author author = new Author();
        author.setNickname("ist");
        author.setUid("0000000000000000001");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        author.setTimestamp(df.format(new Date()));
        return author;
    }

}
