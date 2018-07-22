package jason.theplacesearchapp.helper;

public class ReviewCard {
    private String photo;
    private String name;
    private int rating;
    private int time;
    private String text;
    private String url;

    public ReviewCard(String photo, String name, int rating, int time, String text, String url) {
        this.photo = photo;
        this.name = name;
        this.rating = rating;
        this.time = time;
        this.text = text;
        this.url = url;
    }

    public String getPhoto() {
        return photo;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public int getTime() {
        return time;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }
}
