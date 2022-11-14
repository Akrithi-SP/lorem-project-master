package com.robosoft.lorem.controller;

import com.robosoft.lorem.entity.Address;
import com.robosoft.lorem.entity.OpeningInfo;
import com.robosoft.lorem.entity.Restaurant;
import com.robosoft.lorem.model.*;
import com.robosoft.lorem.response.BrandList;
import com.robosoft.lorem.routeResponse.Location;
import com.robosoft.lorem.service.UserService;
import com.robosoft.lorem.utility.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController
{
    @Autowired
    UserService userservice;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JWTUtility jwtUtility;

    @PostMapping("/register")
    public ResponseEntity<?> Register(@RequestBody User user)
    {
        boolean reg_user =userservice.createAccount(user);
        if(reg_user)
        {
            return new ResponseEntity<>("Hi "+user.getFirstName()+" Welcome to lorem", HttpStatus.OK);
        }
        return new ResponseEntity<>("Please verify your email", HttpStatus.FORBIDDEN);
    }

    @PutMapping("/editProfile")
    public ResponseEntity<String> editProfile(@ModelAttribute userEditFields user)
    {
        boolean reg_user =userservice.editProfile(user);
        if(reg_user)
        {
            return new ResponseEntity<String >("updated successfully", HttpStatus.OK);
        }
        //e.printStackTrace();
        return new ResponseEntity<>("Could not update", HttpStatus.FORBIDDEN);
    }

    @GetMapping("/refer")
    public ResponseEntity<Integer> referAFriend(int userId)
    {
        try
        {
            int reg_user =userservice.referAFriend(userId);
            return new ResponseEntity<Integer>(reg_user, HttpStatus.OK);
        } catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // if required
    @GetMapping("/share/referAFriend")
    public ResponseEntity<Map<String,String>> shareReference(String userId)
    {
        try
        {
            Map<String,String> reg_user =userservice.onClickShareApp(userId);
            return new ResponseEntity<Map<String,String> >(reg_user, HttpStatus.OK);
        } catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/view/gallery")
    public ResponseEntity<?> displayGallery(@RequestParam("restaurantId") int restaurantId, @RequestParam("page") int page)
    {
         Map<Integer,List<menu>> gallery =userservice.Gallery(restaurantId,page);

         if(gallery==null)
         {
             return new ResponseEntity<>("No photos here", HttpStatus.FORBIDDEN);
         }
         return new ResponseEntity<>(gallery, HttpStatus.OK);
    }

    //search with filter
    @GetMapping("/Search")
    public RestaurantSearchResult getRest(@RequestBody SearchFilter searchFilter) {
        return userservice.searchRestaurant(searchFilter);
    }



    //get nearby brands
    @GetMapping("/Brands/{address}/{limit}")
    public NearByBrandsSearchResult getNearByBrands(@PathVariable String address, @PathVariable int limit){
        return userservice.getNearbyBrands(address,limit);
    }


    //create and update cart
    @PostMapping("/Cart")
    public CartModel createOrUpdateCart(@RequestBody CartModel cartModel){
        return userservice.saveOrUpdateCart(cartModel);
    }


    //like and unlike review
    @PostMapping("/Review/Like/{reviewId}/{userId}")
    public ResponseEntity<?> likeAReview(@PathVariable int reviewId, @PathVariable int userId){
        if(userservice.likeAreview(userId,reviewId))
            return new ResponseEntity<>("Liked Review Successfully...",HttpStatus.OK);

        return new ResponseEntity<>("UnLiked Review Successfully...",HttpStatus.EXPECTATION_FAILED);
    }


    //get My Profile
    @GetMapping("/Profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable int userId){
        UserProfile userProfile = userservice.getUserProfile(userId);

        if(userProfile!=null)
            return new ResponseEntity<>(userProfile,HttpStatus.OK);

        return new ResponseEntity<>(userProfile,HttpStatus.NO_CONTENT);
    }


    //get my orders based on status
    @GetMapping("/Orders/{orderStatus}/{pageNumber}/{userId}")
    public OrderResponseModel getMyOrdersByStatus(@PathVariable String orderStatus,@PathVariable int pageNumber,@PathVariable int userId){
        return userservice.getMyOrdersByStatus(orderStatus,userId,pageNumber);
    }


    //SEARCH RESTAURANT BY USING DISH TYPE
    @GetMapping("/Search/{restaurantId}/{dishType}")
    public ResponseEntity<List<MenuDetails>> search(@PathVariable int restaurantId, @PathVariable String dishType) {
        List<MenuDetails> menuDetails = userservice.Search(restaurantId, dishType, "");
        if (menuDetails.size() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(menuDetails));
    }

    //    DISPLAY MENU ITEMS
    @GetMapping("/displayMenu/{restaurantId}")
    public ResponseEntity<List<MenuItem>> displayMenu(@PathVariable int restaurantId) {
        List<MenuItem> menuItems = userservice.DisplayMenu(restaurantId);
        if (menuItems.size() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(menuItems));
    }

    //    SEARCH ITEM BY USING DISH NAME
    @GetMapping("/searchItem/{restaurantId}/{dishName}")
    public ResponseEntity<List<MenuItem>> searchItem(@PathVariable int restaurantId, @PathVariable String dishName) {
        List<MenuItem> menuItems = userservice.searchItem(restaurantId, dishName);
        if (menuItems.size() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(menuItems));
    }

    //    VIEW RESTAURANT
    @GetMapping("/viewRestaurant/{restaurantId}")
    public ResponseEntity<RestaurantDetails> viewRestaurant(@PathVariable int restaurantId, @RequestBody Location start) {
        RestaurantDetails restaurantDetails = userservice.viewRestaurant(restaurantId, start);
        if (restaurantDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(restaurantDetails));

    }

    //    OVERVIEW OF RESTAURANT
    @GetMapping("/overView/{restaurantId}")
    public ResponseEntity<OverviewDetails> overView(@PathVariable int restaurantId) {
        OverviewDetails overviewDeatails = userservice.overview(restaurantId);
        if (overviewDeatails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(overviewDeatails));
    }

    //   ADDING OPENING INFORMATION
    @PostMapping("/addOpeningInfo/{restaurantId}/{userId}")
    public ResponseEntity<String> addOpeningInfo(@ModelAttribute OpeningInfo openingInfo, @PathVariable int restaurantId, @PathVariable int userId) throws Exception {
        if (userservice.addOpeningInfo(openingInfo, restaurantId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body("successful");

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }

    //       OPENING INFORMATION FOR CURRENT DATE
    @GetMapping("/opening/{restaurantId}")
    public ResponseEntity<OpeningDetails> opening(@PathVariable int restaurantId) {
        OpeningDetails openingDetails = userservice.opening(restaurantId);
        if (openingDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(openingDetails));
    }

    //    OPENING INFORMATION FOR NEXT 7 DAYS FROM CURRENT DATE
    @GetMapping("/openingsFor7Days/{restaurantId}")
    public ResponseEntity<List<OpeningDetails>> openingsFor7Days(@PathVariable int restaurantId) {
        List<OpeningDetails> openingDetails = userservice.openingsFor7Days(restaurantId);
        if (openingDetails == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(openingDetails));
    }

    //adding new address
    @PostMapping("/addAddress")
    public ResponseEntity<String> addAddress(@RequestBody Address address) throws Exception {
        if (userservice.addAddress(address)) {
            return ResponseEntity.status(HttpStatus.OK).body("Address added successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }

    //editing address by user
    @PostMapping("/editAddress/{addressId}/{userId}")
    public ResponseEntity<String> editAddress(@RequestBody Address address, @PathVariable int addressId, @PathVariable int userId) throws Exception {
        if (userservice.editAddress(address, addressId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Address updated successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }

    //    deleting the address
    @DeleteMapping("/deleteAddress/{addressId}/{userId}")
    public ResponseEntity<String> deleteAddress(@PathVariable int addressId, @PathVariable int userId) throws Exception {
        if (userservice.deleteAddress(addressId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Address deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("address deletion failed");

    }

    // setting primary address by user
    @PutMapping("/setPrimaryAddress/{addressId}/{userId}")
    public ResponseEntity<String> setPrimaryAddress(@PathVariable int addressId, @PathVariable int userId) throws Exception {
        if (userservice.setPrimaryAddress(addressId, userId)) {
            return ResponseEntity.status(HttpStatus.OK).body("primary address set successfully");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("failed");

    }

    @GetMapping("/displayAddress/{userId}")
    public ResponseEntity<List<AddressDetails>> displayAddress(@PathVariable int userId) {
        List<AddressDetails> addressList = userservice.displayAddress(userId);
        if (addressList.size() <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(addressList));
    }

    @GetMapping("/displayAddresses/{userId}")
    public ResponseEntity<AddressDesc> displayAddresses(@PathVariable int userId)
    {
        AddressDesc addressDesc = userservice.displayAddresses(userId);
        if(addressDesc==null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.of(Optional.of(addressDesc));
    }





    @PostMapping("/authenticate")
    public JWTResponse authenticate(@RequestBody JWTRequest jwtRequest) throws Exception
    {

        try
        {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            jwtRequest.getEmailId(),
                            jwtRequest.getPassword()
                    )
            );
        }
        catch (BadCredentialsException e)
        {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = userservice.loadUserByUsername(jwtRequest.getEmailId());

        final String token = jwtUtility.generateToken(userDetails);

        return  new JWTResponse(token);
    }

    @PutMapping("/likeBrand")
    public String addToFav(@RequestBody FavTable favTable)
    {
        return userservice.addToFavourite(favTable);
    }

    @GetMapping("/viewPopularBrands")
    public ResponseEntity<?>listPopularBrands()
    {
        try
        {
            Map<Integer, List<BrandList>> brandLists=userservice.viewPopularBrands();
            if(brandLists.size()==0)
            {
                return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(brandLists,HttpStatus.OK);
        }
        catch (Exception exception)
        {
            return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/viewAllBrands")
    public ResponseEntity<?> viewAllPopularBrands()
    {
        try
        {
            Map<Integer, List<BrandList>> brandLists=userservice.viewAllBrands();
            if(brandLists.size()==0)
            {
                return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(brandLists,HttpStatus.OK);
        }
        catch (Exception exception)
        {
            return new ResponseEntity<>("No Popular brands to show",HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/review")
    public String addReview(@ModelAttribute ReviewInfo reviewInfo)
    {
        return userservice.addReview(reviewInfo);
    }

    @GetMapping("/getReviews")
    public ResponseEntity<?> getReviews(@RequestBody Restaurant restaurant)
    {
        try
        {
            Map<Integer,Object> reviewPageResponseList= userservice.viewReviews(restaurant);
            if(reviewPageResponseList.size()==0)
            {
                return new ResponseEntity<>("No Reviews to this restaurant",HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(reviewPageResponseList, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("No Reviews To this restaurant",HttpStatus.FORBIDDEN);
        }
    }




}
