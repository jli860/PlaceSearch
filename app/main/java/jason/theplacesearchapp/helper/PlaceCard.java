package jason.theplacesearchapp.helper;

public class PlaceCard {
    private String icon;
    private String name;
    private String vicinity;
    private String placeId;
    private Boolean favored;

    public PlaceCard(String icon, String name, String vicinity, String placeId, Boolean favored) {
        this.icon = icon;
        this.name = name;
        this.vicinity = vicinity;
        this.placeId = placeId;
        this.favored = favored;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public String getPlaceId() {
        return placeId;
    }

    public Boolean isFavored() {
        return favored;
    }

    public void setFavored() {
        favored = !favored;
    }
}
