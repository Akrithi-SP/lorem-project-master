package com.robosoft.lorem.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.robosoft.lorem.entity.*;
import com.robosoft.lorem.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.robosoft.lorem.model.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class AdminService
{
    private static final String ADD_BRANDED_OFFERS="insert into offer(offerId,discount,maxCashBack,validUpto,validPerMonth,photo,description,brandId,codEnabled,superCashPercent,maxSuperCash) values(?,?,?,?,?,?,?,?,?,?,?)";
    private static final String ADD_OFFERS="insert into offer(offerId,discount,maxCashBack,validUpto,validPerMonth,photo,description,codEnabled,superCashPercent,maxSuperCash) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String GET_USER_BY_EMAIL_ID="SELECT * FROM offer WHERE offerId=?";
    private static final String GET_OFFERS="select offerId,discount,description,photo from offer order by discount desc limit ?,?";
    private static final String ALL_OFFERS="select offerId,discount,description,photo from offer order by discount desc limit ?,?";
    private static final String VIEW_DETAILS_OF_AN_OFFER="select * from offer where offerId=?";
    private static final String GET_BRAND_OFFERS ="select offerId,discount,description,photo from offer where brandId=? limit ?,?";

    int offset=0;
    int limit=3;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    String query;

    public boolean addOffers(offer offer)
    {

        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < 5; i++)
        {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String offerCode = sb.toString();

        if(offer.getBrandId()!=0)
        {
            jdbcTemplate.update(ADD_BRANDED_OFFERS,offerCode,offer.getDiscount(),offer.getMaxCashBack(),offer.getValidUpto(),offer.getValidPerMonth(),offer.getPhoto(),offer.getDescription(),offer.getBrandId(),offer.isCodEnabled(),offer.getSuperCashPercent(),offer.getMaxSuperCash());
        }
        else
        {
            jdbcTemplate.update(ADD_OFFERS,offerCode,offer.getDiscount(),offer.getMaxCashBack(),offer.getValidUpto(),offer.getValidPerMonth(),offer.getPhoto(),offer.getDescription(),offer.isCodEnabled(),offer.getSuperCashPercent(),offer.getMaxSuperCash());
        }

        return true;
    }


    public Map<Integer,List<offerSelectiveFields>> viewBestOffers(int page )
    {
        //offset = limit*(pg-1) --> 3*(1-1)
        offset=limit*(page-1);
        Map map = new HashMap<String,List>();

        List <offerSelectiveFields> users= jdbcTemplate.query(GET_OFFERS, new BeanPropertyRowMapper<>(offerSelectiveFields.class),offset,limit);

        if(users.size()!=0)
        {
            map.put(users.size(), users);
            return map;
        }
        return null;
    }

    public Map<Integer,List<offerSelectiveFields>> viewAllOffers(int page)
    {
        Map map = new HashMap<String,List>();
        offset=limit*(page-1);
        List <offerSelectiveFields> allOffers= jdbcTemplate.query(ALL_OFFERS,new BeanPropertyRowMapper<>(offerSelectiveFields.class),offset,limit);

        if(allOffers.size()!=0)
        {
            map.put(allOffers.size(),allOffers);
            return map;
        }
        return null;
    }

    public offerAllFields viewOfferDetails(String offerId)
    {
        try
        {
            offerAllFields offer_obj=jdbcTemplate.queryForObject(VIEW_DETAILS_OF_AN_OFFER,new BeanPropertyRowMapper<>(offerAllFields.class),offerId);
            return  offer_obj;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public Map<Integer,List<offerSelectiveFields>> viewBrandOffer(int brandId, int page)
    {
        Map map = new HashMap<String,List>();
        offset=limit*(page-1);
        List<offerSelectiveFields> offerList=jdbcTemplate.query(GET_BRAND_OFFERS,new BeanPropertyRowMapper<>(offerSelectiveFields.class),brandId,offset,limit);

        if(offerList.size()!=0)
        {
            map.put(offerList.size(),offerList);
            return map;
        }
        return null;

    }

    public boolean changeRole(int userId, String role) {

        String userRole = getRole(role.toUpperCase());

        if(userRole.equalsIgnoreCase("none"))
            return false;

        query = "update user set role='"+userRole+"' where userId="+userId;

        if(jdbcTemplate.update(query)>0)
            return true;

        return false;

    }


    public String getRole(String role){
        switch (role){
            case "ADMIN"-> {return Role.ROLE_ADMIN.toString();}

            case "USER"->{return Role.ROLE_USER.toString();}

            case "MERCHANT"->{return Role.ROLE_MERCHANT.toString();}

            default -> {return "None";}
        }
    }



    public boolean addRestaurant(Restaurant restaurant) throws IOException {

        try {
            String insertQueryForBrand = "insert into restaurant(restaurantName,userId,addressId,profilePic,workingHours,cardAccepted,Description,restaurantType,brandId) values(?,?,?,?,?,?,?,?,?)";
            String insertQueryForNonBrand = "insert into restaurant(restaurantName,userId,addressId,profilePic,workingHours,cardAccepted,Description,restaurantType) values(?,?,?,?,?,?,?,?)";

            query = insertQueryForBrand;

            if (restaurant.getBrandId() == null) {
                query = insertQueryForNonBrand;
            }
            jdbcTemplate.update(query, (preparedStatement) -> {
                preparedStatement.setString(1, restaurant.getRestaurantName());
                preparedStatement.setInt(2, restaurant.getUserId());
                preparedStatement.setInt(3, restaurant.getAddressId());
                preparedStatement.setString(4, null);
                if (restaurant.getProfilePic() != null) {
                    preparedStatement.setString(4, restaurant.getProfilePic());
                }
                preparedStatement.setString(5, restaurant.getWorkingHours());
                preparedStatement.setBoolean(6, restaurant.isCardAccepted());
                preparedStatement.setString(7, restaurant.getDescription());
                preparedStatement.setString(8, restaurant.getRestaurantType());
                if (restaurant.getBrandId() != null) {
                    preparedStatement.setInt(9, restaurant.getBrandId());
                }
            });
            return true;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addDish(Dish dish) throws IOException {
        query = "insert into dish(dishName,description,dishType,veg) values(?,?,?,?)";
        try {
            jdbcTemplate.update(query, dish.getDishName(), dish.getDescription(), dish.getDishType(), dish.isVeg());
            return true;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double minPrice(int restaurantId) {
        String minQuery = "select min(price) from menu where restaurantId=" + restaurantId;
        return jdbcTemplate.queryForObject(minQuery, double.class);
    }

    public void updateMinCost(int restaurantId, double minCost) {
        query = "update restaurant set minimumCost=" + minCost + " where restaurantId=" + restaurantId;
        jdbcTemplate.update(query);
    }

    public double averageCost(int restaurantId) {
        query = "select round(avg(price)) from menu where restaurantId=" + restaurantId;
        return jdbcTemplate.queryForObject(query, double.class);

    }

    public void updateAverageCost(int restaurantId, double averageCost) {
        query = "update restaurant set averageCost=" + averageCost + " where restaurantId=" + restaurantId;
        jdbcTemplate.update(query);
    }

    public boolean addMenu(Menu menu, AddonMapping addonMapping) throws IOException {
        query = "insert into menu(restaurantId,dishId,price,customizable,dishPhoto,foodType) values(?,?,?,?,?,?)";
        String addonQuery="insert into addonmapping(addonId,dishId,restaurantId) values(?,?,?)";
        try {
            jdbcTemplate.update(query, (preparedStatement) -> {
                preparedStatement.setInt(1, menu.getRestaurantId());
                preparedStatement.setInt(2, menu.getDishId());
                preparedStatement.setFloat(3, menu.getPrice());
                preparedStatement.setBoolean(4, menu.isCustomizable());
                preparedStatement.setString(5, null);
                if (menu.getDishPhoto() != null) {
                    preparedStatement.setString(5, menu.getDishPhoto());
                }
                preparedStatement.setString(6, menu.getFoodType());

                Double minCost = minPrice(menu.getRestaurantId());
                if (menu.getPrice() <= minCost) {
                    updateMinCost(menu.getRestaurantId(), minCost);
                }
            });
            jdbcTemplate.update(addonQuery, (preparedStatement) -> {
                if(addonMapping.getAddOnId()!=0) {
                    preparedStatement.setInt(1, addonMapping.getAddOnId());
                    preparedStatement.setInt(2, menu.getDishId());
                    preparedStatement.setInt(3, menu.getRestaurantId());
                }
            });
            Double avgCost = averageCost(menu.getRestaurantId());
            updateAverageCost(menu.getRestaurantId(), avgCost);
            return true;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addon(Addon addon) throws IOException {
        query = "insert into addon(addon,price) values(?,?)";
        try {
            jdbcTemplate.update(query, (preparedStatement) -> {
                preparedStatement.setString(1, addon.getAddon());
                preparedStatement.setFloat(2, addon.getPrice());
            });

            return true;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }


    public String addBrand(Brand brand)
    {
        try
        {
            String fileName = brand.getLogo().getOriginalFilename();
            String fileName1= brand.getProfilePic().getOriginalFilename();
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));  // to generated random string values for file name.
            fileName1 = UUID.randomUUID().toString().concat(this.getExtension(fileName1));
            File file = this.convertToFile(brand.getLogo(), fileName);
            File file1=this.convertToFile(brand.getProfilePic(), fileName1);// to convert multipartFile to File
            String TEMP_URL = this.uploadFile(file, fileName);
            String URl=this.uploadFile(file1,fileName1);// to get uploaded file link
            file.delete();
            file1.delete();// to delete the copy of uploaded file stored in the project folder
            jdbcTemplate.update("insert into brand (brandName, description, logo, profilePic) values(?,?,?,?)",brand.getBrandName(),brand.getDescription(),TEMP_URL,URl);
            return "Successfully Added";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "Failed to Upload";
        }
    }

    public String uploadFile(File file, String fileName) throws IOException
    {
        BlobId blobId = BlobId.of("image-3edad.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("C:\\Users\\Abhishek N\\Downloads\\image.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        return String.format("https://firebasestorage.googleapis.com/v0/b/image-3edad.appspot.com/o/%s?alt=media&token=", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    public File convertToFile(MultipartFile multipartFile, String fileName) throws IOException
    {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile))
        {
            fos.write(multipartFile.getBytes());
            fos.close();
        }
        return tempFile;
    }

    public String getExtension(String fileName)
    {
        return fileName.substring(fileName.lastIndexOf("."));
    }



}
