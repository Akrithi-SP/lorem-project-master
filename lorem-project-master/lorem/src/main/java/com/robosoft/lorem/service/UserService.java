package com.robosoft.lorem.service;

import com.robosoft.lorem.entity.Address;
import com.robosoft.lorem.entity.OpeningInfo;
import com.robosoft.lorem.entity.Payment;
import com.robosoft.lorem.entity.Restaurant;
import com.robosoft.lorem.model.*;
import com.robosoft.lorem.response.BrandList;
import com.robosoft.lorem.response.ReviewPageResponse;
import com.robosoft.lorem.routeResponse.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class UserService implements UserDetailsService
{
    private static final String REGISTER_WITH_MOBILE="INSERT INTO USER(firstName,lastName,emailId,mobileNo,password) VALUES(?,?,?,?,?)";
    private static final String REGISTER="INSERT INTO USER(firstName,lastName,emailId,password) VALUES(?,?,?,?)";
    private static final String REGISTRATION_CHECK="SELECT otpVerified from newUser where emailId=?";
    private static final String VIEW_DETAILS_OF_AN_OFFER="select * from offer where offerId=?";

    private static final String INSERT_MOBILE_NUMBER="INSERT INTO mobileOtp(mobileNo) Values(?)";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    LocationService locationService;

    @Autowired
    AdminService adminService;


    @Value("${page.data.count}")
    private int perPageDataCount;

    String query;

    int offset=0;
    int limit=24;
    public boolean createAccount(User user)
    {
        boolean otpVerified=jdbcTemplate.queryForObject(REGISTRATION_CHECK,new Object[]{user.getEmailId()},Boolean.class);

        if(otpVerified)
        {

            //check old mobile number
            //String check_mobile=jdbcTemplate.queryForObject("select mobileNo from mobileOtp where mobileNo=?",new Object[]{user.getMobileNo()},String.class);

            // has new number & no old mobile number
            // && check_mobile==null

            if(user.getMobileNo()!=null)
            {
                jdbcTemplate.update(INSERT_MOBILE_NUMBER,user.getMobileNo());

                //String  hashcode= passwordEncoder.encode(user.getPassword());
                jdbcTemplate.update(REGISTER_WITH_MOBILE,user.getFirstName(),user.getLastName(),user.getEmailId(),user.getMobileNo(),user.getPassword());
            }

            else
            {
                jdbcTemplate.update(REGISTER,user.getFirstName(),user.getLastName(),user.getEmailId(),user.getPassword());
            }
            // checking for referred registration
            if(user.getReferralCode()!=0)
            {
                int score=jdbcTemplate.queryForObject("select creditScore from user where userid=?",new Object[]{user.getReferralCode()},Integer.class);
                score=score+1;
                jdbcTemplate.update("update user set creditScore=? where userId=?",score,user.getReferralCode());
            }

            //User userReturn= jdbcTemplate.queryForObject(GET_USER_BY_EMAIL_ID, new BeanPropertyRowMapper<>(User.class),user.getEmailId());
            return true;
        }
        return false;
    }

    public boolean editProfile(userEditFields userEditFields)
    {
        if(userEditFields.getMobileNo()!=null)
        {
            //check old number
            String phone = jdbcTemplate.queryForObject("select mobileNo from user where userId=?",new Object[]{userEditFields.getUserId()},String.class);

            if(phone!=null && phone.equals(userEditFields.getMobileNo()))
            {
                jdbcTemplate.update("update user set firstName=?, lastName=?, profilePic=? where userId=?",userEditFields.getFirstName(),userEditFields.getLastName(),userEditFields.getProfilePic().getOriginalFilename(),userEditFields.getUserId());
                return true;
            }

            //insert mobile number to mobileOtp table
            jdbcTemplate.update("insert into mobileOtp(mobileNo) values(?)",userEditFields.getMobileNo());

            // update user table
            jdbcTemplate.update("update user set firstName=?, lastName=?, mobileNo=?, profilePic=? where userId=?",userEditFields.getFirstName(),userEditFields.getLastName(),userEditFields.getMobileNo(),userEditFields.getProfilePic().getOriginalFilename(),userEditFields.getUserId());

            if(phone!=null)
            {
                //delete old number from mobileOtp
                jdbcTemplate.update("delete from mobileOtp where mobileNo=?",phone);
            }
            return true;
        }
        else
        {
            jdbcTemplate.update("update user set firstName=?, lastName=?, profilePic=? where userId=?",userEditFields.getFirstName(),userEditFields.getLastName(),userEditFields.getProfilePic().getOriginalFilename(),userEditFields.getUserId());
            return true;
        }
    }

    public int referAFriend(int userId)
    {
        return userId;
    }

    // if required
    public Map<String,String> onClickShareApp(String userId)
    {
        String code=userId;
        String refer_link="localhost:8080/users/emails2fa";
        Map map = new HashMap<String,String>();
        map.put(code,refer_link);
        return map;
    }

    public Map<Integer,List<menu>> Gallery(int restaurantId, int page)
    {
        Map map = new HashMap<String,List>();
        offset=limit*(page-1);
        List<menu> photos=jdbcTemplate.query("select dishPhoto from menu where restaurantId=? limit ?,?",(rs, rowNum) -> {
            return new menu(rs.getString("dishPhoto"));
        },restaurantId,offset,limit);

        if(photos.size()!=0)
        {
            map.put(photos.size(),photos);
            return map;
        }

        return null;

    }







    public RestaurantSearchResult searchRestaurant(SearchFilter searchFilter) {


        RestaurantSearchResult restaurantSearchResult = new RestaurantSearchResult();

        long offset = this.getOffset(searchFilter.getPageNumber());


        if(searchFilter.getRestaurantOrFoodType()==null)
            searchFilter.setRestaurantOrFoodType("");


        String selectFields = "SELECT distinct " +
                "r.restaurantId," +
                "r.restaurantName," +
                "r.overAllRating," +
                "r.minimumCost," +
                "r.addressId," +
                "r.profilePic," +
                "r.workingHours," +
                "r.cardAccepted," +
                "r.Description," +
                "r.restaurantType," +
                "r.brandId," +
                "r.userId," +
                "a.longitude," +
                "a.lattitude," +
                "o.openingTime," +
                "o.closingTime," +
                "opened," +
                "r.averageCost,"+
                "a.addressDesc,"+
                "r.averageDeliveryTime ";


        query = "FROM restaurant r " +
                "inner join menu m " +
                "on r.restaurantId=m.restaurantId " +
                "inner join address a " +
                "on r.addressId=a.addressId " +
                "inner join openinginfo o " +
                "on r.restaurantId=o.restaurantId " +
                "where (r.restaurantName like '%"+searchFilter.getRestaurantOrFoodType()+"%' " +
                "or " +
                "m.foodType like '%"+searchFilter.getRestaurantOrFoodType()+"%') " +
                "and " +
                "o.dateOf='"+searchFilter.getDate()+"' " +
                "and a.addressDesc like '%"+searchFilter.getAddress()+"%' ";






        if(searchFilter.isOpenNow())
            query = query+"and o.opened=true ";

        if(searchFilter.getMaxAvgMealCost()>0)
            query=query+"and r.averageCost<="+searchFilter.getMaxAvgMealCost()+" ";

        if(searchFilter.getMaxMinOrderCost()>0)
            query = query+"and r.minimumCost<="+searchFilter.getMaxMinOrderCost()+" ";

        if(searchFilter.getCuisineType()!=null)
            query= query+" and r.restaurantType like '%"+searchFilter.getCuisineType()+"%' ";

        if(searchFilter.getDeliveryTime()!=0)
            query=query+" and r.averageDeliveryTime<="+searchFilter.getDeliveryTime()+" ";

        if(!searchFilter.isDescRating())
            query=query+" order by r.overAllRating asc ";
        else
            query=query+" order by r.overAllRating desc ";


        if(searchFilter.getPageNumber()==1){
            String countQuery = "SELECT count(distinct r.restaurantId) ";
            long count = jdbcTemplate.queryForObject(countQuery+query, Long.class);
            restaurantSearchResult.setTotalRocordsCount(count);

            if (count==0)
                return restaurantSearchResult;
        }




        query = selectFields+query+"limit "+offset+","+perPageDataCount;

        List<RestaurantSearchModel> restaurants =  jdbcTemplate.query(query,(rs,noOfROws)->{

            RestaurantSearchModel restaurantSearchModel = new RestaurantSearchModel();
            restaurantSearchModel.setRestaurantId(rs.getInt(1));
            restaurantSearchModel.setRestaurantName(rs.getString(2));
            restaurantSearchModel.setOverAllRating(rs.getDouble(3));
            restaurantSearchModel.setMinimumCost(rs.getDouble(4));
            restaurantSearchModel.setAddressId(rs.getInt(5));
            restaurantSearchModel.setProfilePic(rs.getString(6));
            restaurantSearchModel.setWorkingHours(rs.getString(7));
            restaurantSearchModel.setCardAccepted(rs.getBoolean(8));
            restaurantSearchModel.setDescription(rs.getString(9));
            restaurantSearchModel.setRestaurantType(rs.getString(10));
            restaurantSearchModel.setBrandId(rs.getInt(11));
            restaurantSearchModel.setUserId(rs.getInt(12));

            Location restaurantLocation = new Location(rs.getDouble(13), rs.getDouble(14));

            restaurantSearchModel.setLocation(restaurantLocation);
            restaurantSearchModel.setOpeningTime(rs.getString(15));
            restaurantSearchModel.setClosingTime(rs.getString(16));
            restaurantSearchModel.setOpened(rs.getBoolean(17));
            restaurantSearchModel.setAvgMealCost(rs.getDouble(18));
            restaurantSearchModel.setDeliveryTime(locationService.getDuration(searchFilter.getLocation(),restaurantLocation));
            restaurantSearchModel.setAddressDesc(rs.getString(19));
            restaurantSearchModel.setAverageDeliveryTime(rs.getDouble(20));

            return restaurantSearchModel;
        });

        restaurantSearchResult.setPerPageRecordsCount(restaurants.size());

        restaurantSearchResult.setPageResults(restaurants);

        return restaurantSearchResult;
    }


    public double getAverageMealCostForRestaurant(int restaurantId){
        return jdbcTemplate.queryForObject("select avg(price) from menu where restaurantId="+restaurantId, Double.class);
    }

    public long getOffset(int pageNumber){
        return (long)perPageDataCount*(pageNumber-1);
    }



    public NearByBrandsSearchResult getNearbyBrands(String address, int limit){
        query="select distinct b.brandId,b.brandName,b.description,b.logo,b.profilePic,b.brandOrigin from brand b inner join restaurant r on b.brandId=r.brandId inner join address a on r.addressId=a.addressId where addressDesc like '%"+address+"%' limit "+limit;


        NearByBrandsSearchResult nearByBrandsSearchResult = new NearByBrandsSearchResult();


        List<BrandSearchModel> nearByBrands =  jdbcTemplate.query(query,(rs, noOfRows)->{
            BrandSearchModel brandSearchModel = new BrandSearchModel();
            brandSearchModel.setBrandId(rs.getInt(1));
            brandSearchModel.setBrandName(rs.getString(2));
            brandSearchModel.setDescription(rs.getString(3));
            brandSearchModel.setLogo(rs.getString(4));
            brandSearchModel.setProfilePic(rs.getString(5));
            brandSearchModel.setBrandOrigin(rs.getString(6));

            return brandSearchModel;
        });

        nearByBrandsSearchResult.setResultsCount(nearByBrands.size());
        nearByBrandsSearchResult.setNearByBrands(nearByBrands);

        return nearByBrandsSearchResult;
    }

    public CartModel saveOrUpdateCart(CartModel cartModel) {
        //check if cart is an existing cart then delete its items

        int cartId;


        //if update operation
        if(cartModel.getCartId()!=null){
            this.deleteCartItems(cartModel.getCartId());
            cartId = this.updateCart(cartModel);
        }
        //if it's a new cart then create it in the database and get the id
        else {
            cartId = this.createCart(cartModel);
        }
        //add items of cart into item table
        query = "insert into item(dishId,cartId,addOnCount,count,customizable) values(?,?,?,?,?)";
        for(ItemModel item:cartModel.getItemsIncart()){
            this.addItemIntoCart(item,cartId,query);
        }

        cartModel.setCartId(cartId);

        return cartModel;

    }

    //add item into item table
    public boolean addItemIntoCart(ItemModel itemModel,int cartId,String query){
        jdbcTemplate.update(query,(preparedStatement)->{
            preparedStatement.setInt(1,itemModel.getDishId());
            preparedStatement.setInt(2,cartId);
            preparedStatement.setInt(3,itemModel.getAddOnCount());
            preparedStatement.setInt(4,itemModel.getItemCount());
            preparedStatement.setString(5,itemModel.getCustomizationInfo());
        });

        return true;
    }



    //delete all items from cart
    public boolean deleteCartItems(int cartId){
        query = "delete from item where cartId="+cartId;

        jdbcTemplate.update(query);

        return true;

    }

    //create a cart in the db using userId and fetch cart id
    public int createCart(CartModel cartModel){
        query = "insert into cart(userId,cookingInstructions,scheduledDate,scheduledTime,totalAmount,restaurantId) values(?,?,?,?,?,?)";

        jdbcTemplate.update(query,(preparedStatement)->{
            preparedStatement.setInt(1,cartModel.getUserId());
            preparedStatement.setString(2, cartModel.getCookingInstruction());
            preparedStatement.setDate(3,cartModel.getScheduleDate());
            preparedStatement.setTime(4,cartModel.getScheduleTime());
            preparedStatement.setDouble(5,cartModel.getToPay());
            preparedStatement.setInt(6,cartModel.getRestaurantId());
        });
        query = "select max(cartId) from cart where userId="+cartModel.getUserId();

        return jdbcTemplate.queryForObject(query,Integer.class);
    }


    //update a cart in the db
    public int updateCart(CartModel cartModel){
        query = "update cart set cookingInstructions='"+cartModel.getCookingInstruction()+"',totalAmount="+cartModel.getToPay()+" where cartId="+cartModel.getCartId();

        jdbcTemplate.update(query);

        return cartModel.getCartId();
    }


    //like or unlike a review

    public boolean likeAreview(int userId, int reviewId) {
        query = "insert into likes values("+userId+","+reviewId+")";

        try{
            jdbcTemplate.update(query);
        }catch(DuplicateKeyException exception){
            query = "delete from likes where userID="+userId+" and reviewId="+reviewId;

            jdbcTemplate.update(query);

            query = "update review set likeCount=likeCount-1 where reviewId="+reviewId;

            return false;
        }

        query = "update review set likeCount=likeCount+1 where reviewId="+reviewId;

        jdbcTemplate.update(query);

        return true;
    }


    //get user profile using userId

    public UserProfile getUserProfile(int userId) {
        query = "select userId,firstName,lastName,emailId,mobileNo,profilePic,creditScore from user where userId="+userId;

        UserProfile userProfile = jdbcTemplate.queryForObject(query,(rs,noOfRows)->{
            UserProfile returningUserProfile = new UserProfile();


            returningUserProfile.setUserId(rs.getInt(1));
            returningUserProfile.setFirstName(rs.getString(2));
            returningUserProfile.setLastName(rs.getString(3));
            returningUserProfile.setEmail(rs.getString(4));
            returningUserProfile.setMobileNumber(rs.getString(5));
            returningUserProfile.setProfilePicURL(rs.getString(6));
            returningUserProfile.setCreditScore(rs.getInt(7));


            return returningUserProfile;
        });


        if(userProfile.getMobileNumber()!=null){
            query = "select otpVerified from mobileotp where mobileNo="+userProfile.getMobileNumber();

            userProfile.setMobileVerified(jdbcTemplate.queryForObject(query, Boolean.class));
        }

        return userProfile;

    }



    //get orders  of a user using userId and order status

    public OrderResponseModel getMyOrdersByStatus(String orderStatus, int userId,int pageNumber) {

        OrderResponseModel orderResponseModel = new OrderResponseModel();


        int orderIndex = this.getStatusIndex(orderStatus);

        long offset = this.getOffset(pageNumber);

        //for any other status
        if(orderIndex==9)
            return null;

        String startingQuery = "select orderId,orderStatus,cartId,restaurantId";
        query = " from orders where ";
        //for active
        if(orderIndex==6)
            query = query+"orderStatus<="+orderIndex+" and userId="+userId;

            //for cancelled or past
        else
            query = query+"orderStatus="+orderIndex+" and userId="+userId;

        if(pageNumber==1){
            int count = jdbcTemplate.queryForObject("select count(orderId)"+query, Integer.class);
            orderResponseModel.setTotalRecordsCount(count);

            if(count==0)
                return orderResponseModel;
        }

        query=startingQuery+query+" limit "+offset+","+perPageDataCount;

        List<OrderModel> orders = this.getOrdersUsingQuery(query);

        orderResponseModel.setOrders(orders);
        orderResponseModel.setTotalRecordsInPage(orders.size());

        return orderResponseModel;
    }


    //get order status index using order status
    public int getStatusIndex(String status){
        switch (status.toUpperCase()){
            case "ACTIVE"->{return 6;}
            case "PAST"->{return 7;}
            case "CANCELLED"->{return 8;}
            default -> {return 9;}
        }
    }

    //fetch list of orders using a query
    public List<OrderModel> getOrdersUsingQuery(String query){
        return jdbcTemplate.query(query,(resultSet,noOfRows)->{
            OrderModel orderModel = new OrderModel();

            //get order id and status
            orderModel.setOrderId(resultSet.getInt(1));
            orderModel.setOrderStatus(resultSet.getString(2));
            orderModel.setItemsCount(jdbcTemplate.queryForObject("select count(cartId) from item where cartId="+resultSet.getInt(3), Integer.class));

            //get restaurant name and restaurant address
            RestaurantSearchModel restaurantSearchModel = jdbcTemplate.queryForObject("select r.restaurantName,a.addressDesc from restaurant r inner join address a on r.addressId=a.addressId where r.restaurantId="+resultSet.getInt(4),(rs, no)->{
                RestaurantSearchModel returningRestaurantSearchModel = new RestaurantSearchModel();

                returningRestaurantSearchModel.setRestaurantName(rs.getString(1));
                returningRestaurantSearchModel.setAddressDesc(rs.getString(2));
                return returningRestaurantSearchModel;
            });

            orderModel.setRestaurantName(restaurantSearchModel.getRestaurantName());
            orderModel.setRestaurantAddress(restaurantSearchModel.getAddressDesc());

            //get amount and card id using order id
            Payment payment = jdbcTemplate.queryForObject("select amount,cardNo from payment where orderId="+ orderModel.getOrderId(),(rs, no)->{
                Payment returnedPayment = new Payment();
                returnedPayment.setAmount(rs.getDouble(1));
                returnedPayment.setCardNumber(rs.getString(2));

                return returnedPayment;
            });


            System.out.println(orderModel.getOrderId());
            //if card id null then set amount as amount or else set amount as grandTotal
            if(payment.getCardNumber()==null)
                orderModel.setAmount(payment.getAmount());
            else{

                Double amount = jdbcTemplate.queryForObject("select grandTotal from paymentdetails where orderId="+ orderModel.getOrderId(), Double.class);
                orderModel.setAmount(amount);
            }

            return orderModel;
        });
    }


    public List<Integer> deliveryRatings(int restaurantId)
    {
        query="select serviceRating from review where restaurantId="+restaurantId+" limit 5";
        return jdbcTemplate.queryForList(query,int.class);

    }

    public RestaurantDetails viewRestaurant(int restaurantId, Location start) {
        query="select restaurantName,restaurantType,overAllRating,minimumCost,workingHours,longitude,lattitude from restaurant rs inner join address a on rs.addressId=a.addressId where rs.restaurantId="+restaurantId;
        return jdbcTemplate.queryForObject(query, (resultSet, no) ->
        {
            RestaurantDetails restaurantDetails = new RestaurantDetails();

            restaurantDetails.setRestaurantName(resultSet.getString(1));
            restaurantDetails.setRestaurantType(resultSet.getString(2));
            restaurantDetails.setOverAllRating(resultSet.getInt(3));
            restaurantDetails.setMinimumCost(resultSet.getDouble(4));
            restaurantDetails.setWorkingHours(resultSet.getString(5));
            restaurantDetails.setDeliveryRating(deliveryRatings(restaurantId));
            restaurantDetails.setDuration(locationService.getDuration(start,new Location(resultSet.getDouble(6),resultSet.getDouble(7))));
            return restaurantDetails;
        });

    }



    public List<MenuDetails> Search(int restaurantId, String dishType, String dishName) {
        query="select dishName,price,customizable,description,dishPhoto,veg from menu inner join dish on menu.dishId = dish.dishId where restaurantId="+restaurantId+" and dishType like '%"+dishType+"%' and dishName like '%"+dishName+"%'";
        List<MenuDetails> menuDetails = new ArrayList<MenuDetails>();
        jdbcTemplate.query(query, (resultSet, no) ->
        {
            MenuDetails menu = new MenuDetails();
            menu.setDishName(resultSet.getString(1));
            menu.setPrice(resultSet.getFloat(2));
            menu.setCustomizable(resultSet.getBoolean(3));
            menu.setDescription(resultSet.getString(4));
            menu.setDishPhoto(resultSet.getString(5));
            menu.setVeg(resultSet.getBoolean(6));
            menuDetails.add(menu);
            return menu;
        });

        System.out.println(menuDetails);
        return menuDetails;
    }

    public List<String> dishTypes(int restaurantId)
    {
        String selectQuery="select distinct dishType from dish inner join menu on dish.dishId=menu.dishId where restaurantId="+restaurantId;
        List<String> dishTypes = jdbcTemplate.queryForList(selectQuery,String.class);
        return dishTypes;
    }

    public List<MenuItem> DisplayMenu(int restaurantId) {
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        List<String> dishTypes = dishTypes(restaurantId);
        for(String dishType:dishTypes)
        {
            MenuItem menuItem = new MenuItem(dishType);
            List<MenuDetails> menuDetails = Search(restaurantId,dishType,"");
            menuItem.setMenuDetailsList(menuDetails);
            menuItem.setCount(menuDetails.size());
            menuItems.add(menuItem);
        }
        return menuItems;
    }


    public List<MenuItem> searchItem(int restaurantId, String dishName) {
        List<MenuItem> menuItems = new ArrayList<MenuItem>();
        List<String> dishTypes = dishTypes(restaurantId);
        for(String dishType:dishTypes)
        {
            MenuItem menuItem = new MenuItem(dishType);
            List<MenuDetails> menuDetails = Search(restaurantId,dishType,dishName);
            menuItem.setMenuDetailsList(menuDetails);
            menuItem.setCount(menuDetails.size());
            menuItems.add(menuItem);
        }
        return menuItems;
    }



    public OverviewDetails overview(int restaurantId) {
        query=" select restaurantId,Description,restaurantType,averageCost,cardAccepted,mobileNo,addressDesc from restaurant inner join address on address.addressId=restaurant.addressId inner join user on restaurant.userId=user.userId where restaurantId="+restaurantId;
        String openQuery="select opened,dateOf,openingTime,closingTime,reason from openingInfo where restaurantId="+restaurantId+" and dateOf='"+ LocalDate.now()+"'";
        OverviewDetails overviewDetails=jdbcTemplate.queryForObject(query,new BeanPropertyRowMapper<>(OverviewDetails.class));

        List<OpeningDetails> openingDetails= jdbcTemplate.query(openQuery,new BeanPropertyRowMapper<>(OpeningDetails.class));
        if(overviewDetails!=null)
        {
            overviewDetails.setOpeningDetails(openingDetails);
        }

        return overviewDetails;
    }


    public boolean isOwner(int restaurantId,int userId)
    {
        query="select userId from restaurant where restaurantId="+restaurantId;
        int returnedUserId = jdbcTemplate.queryForObject(query,Integer.class);
        return userId==returnedUserId;
    }

    public boolean addOpeningInfo(OpeningInfo openingInfo, int restaurantId, int userId) throws IOException {
        try {
            if (isOwner(restaurantId,userId)) {
                query = "insert into openingInfo(restaurantId,opened,dateOf,openingTime,closingTime,reason) values(?,?,?,?,?,?)";
                jdbcTemplate.update(query,
                        (preparedStatement->{
                            preparedStatement.setInt(1,restaurantId);
                            preparedStatement.setBoolean(2,openingInfo.isOpened());
                            preparedStatement.setDate(3,openingInfo.getDateOf());
                            preparedStatement.setString(4,null);
                            preparedStatement.setString(5,null);
                            if(openingInfo.isOpened()==true) {
                                preparedStatement.setString(4, openingInfo.getOpeningTime());
                                preparedStatement.setString(5, openingInfo.getClosingTime());
                            }
                            if(openingInfo.getReason()!=null) {
                                preparedStatement.setString(6, openingInfo.getReason());
                            }
                        }));
                return true;
            }
            return false;
        } catch(DataAccessException e){
            e.printStackTrace();
            return false;
        }

    }

    public OpeningDetails opening(int restaurantId) {
        String openQuery = "select opened,dateOf,openingTime,closingTime,reason from openingInfo where restaurantId=" + restaurantId + " and dateOf='" + LocalDate.now() + "'";
        return jdbcTemplate.queryForObject(openQuery, (resultSet, no) ->
        {
            OpeningDetails openingDetails = new OpeningDetails();

            openingDetails.setOpened(resultSet.getBoolean(1));
            openingDetails.setDateOf(resultSet.getDate(2));
            openingDetails.setOpeningTime(resultSet.getString(3));
            openingDetails.setClosingTime(resultSet.getString(4));
            openingDetails.setReason(resultSet.getString(5));


            return openingDetails;
        });
    }

    public List<OpeningDetails> openingsFor7Days(int restaurantId) {
        String opensQuery = "select opened,dateOf,openingTime,closingTime,reason from openingInfo where restaurantId=" + restaurantId + " and (dateOf>='" + LocalDate.now()+"' and dateOf<'"+LocalDate.now().plusDays(7)+"')";
        return jdbcTemplate.query(opensQuery, (resultSet, no) ->
        {
            OpeningDetails openingDetails = new OpeningDetails();


            openingDetails.setOpened(resultSet.getBoolean(1));
            openingDetails.setDateOf(resultSet.getDate(2));
            openingDetails.setOpeningTime(resultSet.getString(3));
            openingDetails.setClosingTime(resultSet.getString(4));
            openingDetails.setReason(resultSet.getString(5));


            return openingDetails;
        });

    }

    public boolean addAddress(Address address) throws IOException {
        try {
//            userId have to take when user logged in
            query = "insert into address(userId,primaryAddress,addressType,city,area,addressDesc,lattitude,longitude) values(?,?,?,?,?,?,?,?)";
            jdbcTemplate.update(query, address.getUserId(), address.isPrimaryAddress(), address.getAddressType(), address.getCity(), address.getArea(), address.getAddressDesc(), address.getLattitude(), address.getLongitude());
            return true;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean editAddress(Address address, int addressId, int userId) {
        try
        {
            if(isUser(addressId,userId))
            {
                if(address.getAddressType()!=null)
                {
                    jdbcTemplate.update("update address set addressType='"+address.getAddressType()+"' where addressId="+addressId);
                    return true;
                }
                if(address.getCity()!=null)
                {
                    jdbcTemplate.update("update address set city='"+address.getCity()+"' where addressId="+addressId);
                    return true;
                }
                if(address.getArea()!=null)
                {
                    jdbcTemplate.update("update address set area='"+address.getArea()+"' where addressId="+addressId);
                    return true;
                }
                if(address.getAddressDesc()!=null)
                {
                    jdbcTemplate.update("update address set addressDesc='"+address.getAddressDesc()+"' where addressId="+addressId);
                    return true;
                }
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean deleteAddress(int addressId, int userId) {
        if(isUser(addressId,userId))
        {
            query="delete from address where addressId="+addressId;
            jdbcTemplate.update(query);
            return true;
        }
        return false;
    }

    public boolean isUser(int addressId,int userId)
    {
        query="select userId from address where addressId="+addressId;
        int returnedUserId = jdbcTemplate.queryForObject(query,Integer.class);
        return userId==returnedUserId;

    }

    public boolean setPrimaryAddress(int addressId,int userId)
    {
        if(isUser(addressId,userId))
        {
            jdbcTemplate.update("update address set primaryAddress ="+false+" where primaryAddress="+true+" and userId="+userId);
            jdbcTemplate.update("update address set primaryAddress="+true+" where addressId="+addressId);
            return true;
        }
        return false;
    }

    public List<AddressDetails> displayAddress(int userId) {
        query="select addressId,addressType,primaryAddress,addressDesc from address where userId="+userId;
        try {
            List<AddressDetails> addressDetails = new ArrayList<AddressDetails>();
            jdbcTemplate.query(query, (resultSet, no) ->
            {
                AddressDetails address = new AddressDetails();
                address.setAddressId(resultSet.getInt(1));
                address.setAddressType(resultSet.getString(2));
                address.setPrimaryAddress(resultSet.getBoolean(3));
                address.setAddressDesc(resultSet.getString(4));
                addressDetails.add(address);
                return address;
            });
            return addressDetails ;

        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AddressDesc displayAddresses(int userId)
    {
        AddressDesc addressDesc = new AddressDesc();
        List<AddressDetails> details=displayAddress(userId);
        addressDesc.setAddressDetailsList(details);
        addressDesc.setCount(details.size());
        if(details.size()<=0)
        {
            addressDesc.setCount(0);
            addressDesc.setAddressDetailsList(null);

        }
        return addressDesc;
    }

    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException
    {
        String email=jdbcTemplate.queryForObject("select emailId from user where emailId=?", String.class, new Object[]{emailId});
        String password=jdbcTemplate.queryForObject("select password from user where emailId=?",String.class, new Object[]{emailId});
        return new org.springframework.security.core.userdetails.User(email, password, new ArrayList<>());
    }

    public String getUserNameFromToken()
    {
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails)
        {
            username = ((UserDetails) principal).getUsername();
        }
        else
        {
            username = principal.toString();
        }
        return username;
    }

    public String addToFavourite(FavTable favTable)
    {
        try
        {
            String email = getUserNameFromToken();
            int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
            jdbcTemplate.update("insert into favTable values(?,?)", id, favTable.getBrandId());
            return "added to favourites";
        }
        catch (Exception e)
        {
            return "Something Went Wrong";
        }
    }

    public Map<Integer, List<BrandList>> viewPopularBrands()
    {
        int lowerLimit = 0;
        int upperLimit = 1;
        Map<Integer, List<BrandList>> popular = new HashMap<>();
        try
        {
            int brandNo = jdbcTemplate.queryForObject("select brandId from favTable group by brandId limit ?,?", Integer.class, new Object[]{lowerLimit, upperLimit});
            List<BrandList> brands = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand where brandId=?", new BeanPropertyRowMapper<>(BrandList.class), brandNo);
            lowerLimit = lowerLimit + 1;
            popular.put(brands.size(), brands);
            return popular;
        }
        catch (EmptyResultDataAccessException e)
        {
            lowerLimit = lowerLimit - 1;
            int brandNo = jdbcTemplate.queryForObject("select brandId from favTable group by brandId limit ?,?", Integer.class, new Object[]{lowerLimit, upperLimit});
            List<BrandList> brands = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand where brandId=?", new BeanPropertyRowMapper<>(BrandList.class), brandNo);
            popular.put(brands.size(), brands);
            return popular;
        }
    }

    public Map<Integer, List<BrandList>> viewAllBrands()
    {
        Map<Integer, List<BrandList>> theThings = new HashMap<>();
        List<BrandList> brandLists = jdbcTemplate.query("select brandName, description, logo, profilePic, brandOrigin from brand", new BeanPropertyRowMapper<>(BrandList.class));
        theThings.put(brandLists.size(), brandLists);
        return theThings;
    }

    public String addReview(ReviewInfo reviewInfo)
    {
        try
        {
            String email = getUserNameFromToken();
            int id = jdbcTemplate.queryForObject("select userId from user where emailId=?", Integer.class, new Object[]{email});
            int userId = jdbcTemplate.queryForObject("select userId from orders where userId=? group by userId", Integer.class, new Object[]{id});
            int restaurantId = jdbcTemplate.queryForObject("select restaurantId from orders where userId=? group by restaurantId", Integer.class, new Object[]{userId});
            if (restaurantId == reviewInfo.getRestaurantId())
            {
                String query = "insert into review (userId, restaurantId, description, localDate, foodRating, serviceRating) values(?,?,?,?,?,?)";
                jdbcTemplate.update(query, reviewInfo.getUserId(), reviewInfo.getRestaurantId(), reviewInfo.getDescription(), LocalDate.now(), reviewInfo.getFoodRating(), reviewInfo.getServiceRating());
                int reviewId = jdbcTemplate.queryForObject("select max(reviewId) from review where userId=?", Integer.class, new Object[]{reviewInfo.getUserId()});
                ReviewInfo reviewInfo1 = jdbcTemplate.queryForObject("select foodRating, serviceRating from review where reviewId=?", new BeanPropertyRowMapper<>(ReviewInfo.class), reviewId);
                jdbcTemplate.update("update review set averageRating=? where reviewId=?", (reviewInfo1.getFoodRating() + reviewInfo1.getServiceRating()) / 2, reviewId);
                try
                {
                    if (reviewInfo.getMultipartFileList() == null)
                    {
                        for (int i = 0; i < reviewInfo.getMultipartFileList().size(); i++)
                        {
                            String fileName = reviewInfo.getMultipartFileList().get(i).getOriginalFilename();
                            fileName = UUID.randomUUID().toString().concat(adminService.getExtension(fileName));
                            File file = adminService.convertToFile(reviewInfo.getMultipartFileList().get(i), fileName);
                            String TEMP_URL = adminService.uploadFile(file, fileName);
                            file.delete();
                            jdbcTemplate.update("insert into photo (photoPic, reviewId) values(?,?)", TEMP_URL, reviewId);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return "Review Added Without photo";
                }
            }
            else
            {
                return "You Cant give Review to this Restaurant";
            }
            return "Review Added With Photo";
        }
        catch (Exception e)
        {
            return "Failed to add review";
        }

    }

    public  Map<Integer,Object> viewReviews(Restaurant restaurant)
    {
        Map<Integer, Object> reviews = new HashMap<>();
        try
        {
            String query = "select user.userId, user.firstName, user.lastName, user.profilePic, review.reviewId, review.description, review.averageRating, review.likeCount, review.localDate from user inner join review on user.userId=review.userId where review.restaurantId=?";
            List<ReviewPageResponse> reviewPageResponses =new ArrayList<ReviewPageResponse>();
            jdbcTemplate.query(query, (rs, rowNum) ->
            {
                ReviewPageResponse reviewPageResponse = new ReviewPageResponse();
                reviewPageResponse.setUserId(rs.getInt("user.userId"));
                reviewPageResponse.setFirstName(rs.getString("user.firstName"));
                reviewPageResponse.setLastName(rs.getString("user.lastName"));
                reviewPageResponse.setProfilePic(rs.getString("user.profilePic"));
                reviewPageResponse.setReviewId(rs.getInt("review.reviewId"));
                reviewPageResponse.setDescription(rs.getString("review.description"));
                reviewPageResponse.setAverageRating(rs.getInt("review.averageRating"));
                reviewPageResponse.setLikeCount(rs.getInt("review.likeCount"));
                reviewPageResponse.setDate(rs.getDate("review.localDate"));
                reviewPageResponse.setPhoto(getReviewPhotos(rs.getInt("review.reviewId")));
                reviewPageResponse.setReviewCount(giveReviewCount(rs.getInt("user.userId")));
                reviewPageResponse.setRatingCount(giveRatingCount(rs.getInt("user.userId")));
                reviewPageResponses.add(reviewPageResponse);
                reviews.put(reviewPageResponses.size(),reviewPageResponse);
                return reviewPageResponse;
            },restaurant.getRestaurantId());
            return reviews;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public List<String> getReviewPhotos(int reviewId)
    {
        return jdbcTemplate.queryForList("select photoPic from photo where reviewId="+reviewId,String.class);
    }

    public int giveReviewCount(int userId)
    {
        return jdbcTemplate.queryForObject("select count(userId) from review where userId=?", Integer.class, new Object[]{userId});
    }

    public int giveRatingCount(int userId)
    {
        int totalFoodRating=jdbcTemplate.queryForObject("select sum(foodRating) from review where userId=?", Integer.class, new Object[]{userId});
        int totalServiceRating=jdbcTemplate.queryForObject("select sum(serviceRating) from review where userId=?", Integer.class, new Object[]{userId});
        int totalRating=totalFoodRating+totalServiceRating;
        return totalRating;
    }

    // ************* view offer details

    public offerAllFields viewOfferDetails(String offerId)
    {
        try
        {
            offerAllFields offerAllFields= new offerAllFields();

            // offer details
            offer offer_obj=jdbcTemplate.queryForObject(VIEW_DETAILS_OF_AN_OFFER,new BeanPropertyRowMapper<>(offer.class),offerId);
            offerAllFields.setOffer(offer_obj);

            //int user_id=getUserIdFromEmail();
            int user_id=21;

            // user address
            String delivery_to= jdbcTemplate.queryForObject("select addressDesc from address where userId=? and primaryAddress=true",String.class, new Object[]{user_id});
            offerAllFields.setAddressDesc(delivery_to);


            try
            {
                // to check if offer id branded or not and search correspondingly
                int brandId=jdbcTemplate.queryForObject("select brandId from offer where offerId=?",Integer.class, new Object[]{offerId});

                if(brandId!=0)
                {
                    // to get nearby outlets
                    List<RestaurantAddress> offer_applicable_outlets=jdbcTemplate.query("select restaurant.restaurantName, address.addressDesc from restaurant inner join address on restaurant.addressId=address.addressId where brandId=? and address.addressDesc like '%"+delivery_to+"%' ",new BeanPropertyRowMapper<>(RestaurantAddress.class),brandId);
                    offerAllFields.setRestaurantAddress(offer_applicable_outlets);

                    return  offerAllFields;
                }
            }
            catch (Exception e)
            {
                List<RestaurantAddress> offer_applicable_outlets=jdbcTemplate.query("select restaurant.restaurantName, address.addressDesc from restaurant inner join address on restaurant.addressId=address.addressId where brandId is null && address.addressName like '%"+delivery_to+"%'",new BeanPropertyRowMapper<>(RestaurantAddress.class),delivery_to);
                offerAllFields.setRestaurantAddress(offer_applicable_outlets);

                return  offerAllFields;
                //return "offer details\n "+offer_obj+" deliver food to\n "+delivery_to+" nearby outlets\n"+offer_applicable_outlets;
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }




















}

