package esardini.com.infotainment2.bean;

public class VideoShareBean {

    private String url;
    private String description;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String desc = description;

        if(description.contains("\"")){
            desc = description.replace("\"", "\\\"");
        }

        this.description = desc;
    }
}
