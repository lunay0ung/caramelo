package com.example.luna.caramelo.Favorites.FavoriteSite;

import java.io.Serializable;

/**
 * Created by LUNA on 2017-12-11.
 */

@SuppressWarnings("serial")
public class Site implements Serializable{


    int image;
    String name, des, url, type;

    public Site(int image, String name, String des, String url, String kind) {
        this.image = image;
        this.name = name;
        this.des = des;
        this.url = url;
        this.type = kind;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
