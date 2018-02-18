package com.example.luna.caramelo;

/**
 * Created by LUNA on 2017-12-15.
 */

public class Favorite {

    private String email, username, category, name, des, url, type;
   // private String image; //임시로 스틑링화
    private int  image;

   public Favorite(String email, String username, String category, int image, String name, String des, String url, String type) {
       this.email = email;
       this.username = username;
       this.category = category;
       this.image = image;
       this.name =  name;
       this.des =des;
       this.url = url;
       this.type = type;
   }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDes() {
        return des;
    }

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public int getImage() {
        return image;
    }
}//
