package com.robosoft.lorem.controller;

import com.robosoft.lorem.entity.*;
import com.robosoft.lorem.model.Brand;
import com.robosoft.lorem.model.offer;
import com.robosoft.lorem.model.offerAllFields;
import com.robosoft.lorem.model.offerSelectiveFields;
import com.robosoft.lorem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController
{
    @Autowired
    AdminService adminService;


    //assign role to a user
    @PatchMapping("/Role/{userId}/{role}")
    public ResponseEntity<?> assignRole(@PathVariable int userId,@PathVariable String role){
        if(adminService.changeRole(userId, role))
            return new ResponseEntity<>("Role Changed Successfully..",HttpStatus.OK);

        return new ResponseEntity<>("Some Error Occurred While Changing Role..",HttpStatus.EXPECTATION_FAILED);
    }

    @PostMapping("/addOffers")
    public ResponseEntity<?> addOffers(@ModelAttribute offer offer)
    {
        boolean offer_obj=adminService.addOffers(offer);
        if(offer_obj)
        {
            return new ResponseEntity<>("Offer added successfully", HttpStatus.OK);
        }
        return  new ResponseEntity<>("Cannot add offers",HttpStatus.FORBIDDEN);
    }


    @GetMapping("/viewBestOffers")
    public ResponseEntity<?> viewBestOffers(@RequestParam int page)
    {
        Map<Integer,List<offerSelectiveFields>> offerList=adminService.viewBestOffers(page);
        if(offerList==null)
        {
            return  new ResponseEntity<>("No offers ahead",HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(offerList, HttpStatus.OK);
    }

    @GetMapping("/viewAllOffers")
    public ResponseEntity<?> viewAllOffers(@RequestParam int page)
    {

        Map<Integer,List<offerSelectiveFields>> offerList=adminService.viewAllOffers(page);
        if(offerList==null)
        {
            return  new ResponseEntity<>("No offers ahead",HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(offerList, HttpStatus.OK);
    }

    @GetMapping("/viewDetails")
    public ResponseEntity<?> viewDetails(@RequestParam String offerId)
    {
        try
        {
            offerAllFields offerObj=adminService.viewOfferDetails(offerId);
            return new ResponseEntity<>(offerObj, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/viewBrandOffers")
    public ResponseEntity<?> viewBrandOffers(@RequestParam int brandID, @RequestParam int page)
    {
        Map<Integer,List<offerSelectiveFields>> offerList=adminService.viewBrandOffer(brandID,page);
        if(offerList==null)
        {
            return  new ResponseEntity<>("No offers ahead for this brand",HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(offerList, HttpStatus.OK);
    }

    @PostMapping("/addRestaurant")
    public ResponseEntity<String> addRestaurant(@ModelAttribute Restaurant newrestaurant) throws Exception {
        if(adminService.addRestaurant(newrestaurant))
        {
            return ResponseEntity.status(HttpStatus.OK).body("Restaurant added");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }


    @PostMapping("/addDish")
    public ResponseEntity<String> addDish(@ModelAttribute Dish dish) throws Exception {
        if(adminService.addDish(dish))
        {
            return ResponseEntity.status(HttpStatus.OK).body("Dish added");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }

    @PostMapping("/addMenu")
    public ResponseEntity<String> addMenu(@ModelAttribute Menu menu, @ModelAttribute AddonMapping addonMapping) throws Exception {
        if(adminService.addMenu(menu,addonMapping))
        {
            return ResponseEntity.status(HttpStatus.OK).body("Menu added");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }
    @PostMapping("/addon")
    public ResponseEntity<String> addon(@RequestBody Addon addon) throws Exception {
        if(adminService.addon(addon))
        {
            return ResponseEntity.status(HttpStatus.OK).body("Addon added");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Task failed");

    }

    @PostMapping("/addBrand")
    public String addBrand(@ModelAttribute Brand brand)
    {
        return adminService.addBrand(brand);
    }
}
